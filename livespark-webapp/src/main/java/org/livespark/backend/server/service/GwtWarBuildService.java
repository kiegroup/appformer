/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.backend.server.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.guvnor.common.services.backend.file.DotFileFilter;
import org.guvnor.common.services.project.builder.model.BuildMessage;
import org.guvnor.common.services.project.builder.model.BuildMessage.Level;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.model.IncrementalBuildResults;
import org.guvnor.common.services.project.builder.service.BuildService;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.kie.workbench.common.services.backend.builder.BuildServiceImpl;
import org.livespark.client.AppReady;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.DirectoryStream;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Paths;
import org.uberfire.java.nio.file.StandardDeleteOption;
import org.uberfire.workbench.events.ResourceChange;

// TODO make this service support multiple users
@ApplicationScoped
@Service
@Alternative
@Priority(value = 100)
public class GwtWarBuildService implements BuildService {

    private boolean isCodeServerLaunched = false;

    @Inject
    private ServerMessageBus bus;

    @Inject
    private Event<AppReady> appReadyEvent;

    public abstract class BaseBuildCallable implements Callable<List<BuildMessage>> {

        protected final Project project;
        protected final File pomXml;
        protected final String sessionId;
        protected final ServletRequest sreq;

        public BaseBuildCallable( Project project,
                                  File pomXml,
                                  String sessionId,
                                  ServletRequest sreq ) {
            this.project = project;
            this.pomXml = pomXml;
            this.sessionId = sessionId;
            this.sreq = sreq;
        }

        protected abstract List<BuildMessage> postBuildTasks( InvocationResult res ) throws Exception;

        @Override
        public List<BuildMessage> call() throws Exception {
            final List<BuildMessage> retVal = new ArrayList<BuildMessage>();

            try {
                cleanClientConsole();
                final InvocationResult res = executeRequest();
                retVal.addAll( postBuildTasks( res ) );
            } catch ( Throwable t ) {
                logBuildException( project, t );
            }

            return retVal;
        }

        protected List<BuildMessage> processBuildResults( InvocationResult res ) {
            final List<BuildMessage> messages = new ArrayList<BuildMessage>();

            if ( res.getExitCode() == 0 ) {
                messages.add( createSuccessMessage() );
            } else {
                messages.add( createFailureMessage() );
            }

            return messages;
        }

        protected InvocationResult executeRequest() throws Throwable {
            final DefaultInvocationRequest packageRequest = createDevModePackageRequest( pomXml );

            packageRequest.setOutputHandler( new InvocationOutputHandler() {
                @Override
                public void consumeLine( String line ) {
                    sendOutputToClient(line, sessionId);
                }
            } );

            return new DefaultInvoker().execute( packageRequest );
        }

        private void cleanClientConsole() {
            MessageBuilder.createMessage()
                          .toSubject( "MavenBuilderOutput" )
                          .signalling()
                          .with( MessageParts.SessionID, sessionId )
                          .with( "clean", Boolean.TRUE )
                          .noErrorHandling().sendNowWith( bus );
        }

        protected String getWildflyHome() throws MalformedURLException, URISyntaxException {
            String wildflyHome = System.getProperty( "errai.jboss.home" );

            if ( wildflyHome == null ) {
                wildflyHome = findWildflyHome();
            }

            return wildflyHome;
        }

        private String findWildflyHome() throws MalformedURLException, URISyntaxException {
            final ServletContext context = sreq.getServletContext();
            final String webXmlPath = context.getRealPath( "/WEB-INF/web.xml" );
            File cur = new File( webXmlPath );

            do {
                cur = cur.getParentFile();
            } while ( cur != null && !cur.getName().contains( "wildfly" ) );

            if ( cur == null ) {
                throw new RuntimeException( "Could not locate Wildfly/JBoss root directory. Please set the errai.jboss.home system property." );
            }

            return cur.getAbsolutePath();
        }

        protected void sendOutputToClient(String output, String sessionId) {
            MessageBuilder.createMessage()
                .toSubject( "MavenBuilderOutput" )
                .signalling()
                .with( MessageParts.SessionID, sessionId )
                .with( "output", output + "\n" )
                .noErrorHandling().sendNowWith( bus );
        }
    }

    public class BuildAndDeployCallable extends BaseBuildCallable {

        public BuildAndDeployCallable( Project project,
                                        File pomXml,
                                        String sessionId,
                                        ServletRequest sreq) {
            super( project, pomXml, sessionId, sreq );
        }

        @Override
        protected List<BuildMessage> postBuildTasks( InvocationResult res ) throws Exception {
            final List<BuildMessage> messages = new ArrayList<BuildMessage>();
            messages.addAll( processBuildResults( res ) );
            messages.addAll( deployIfSuccessful( res ) );

            return messages;
        }

        protected List<BuildMessage> deployIfSuccessful( InvocationResult res ) throws MalformedURLException, URISyntaxException, IOException, Exception {
            if ( res.getExitCode() == 0 )
                deploy();

            return Collections.emptyList();
        }

        protected void deploy() throws MalformedURLException, URISyntaxException, IOException, Exception {
            final File targetDir = new File( pomXml.getParent(), "/target" );
            final Collection<File> wars = FileUtils.listFiles( targetDir, new String[]{"war"}, false );
            for ( final File war : wars ) {
                sendOutputToClient("Deploying " + war.getName() + "...", sessionId);
                String home = getWildflyHome();
                File deployDir = new File( home, "/standalone/deployments" );
                FileUtils.deleteQuietly( new File( deployDir, war.getName() ) );
                FileUtils.copyFileToDirectory( war, deployDir );

                FileAlterationMonitor monitor = new FileAlterationMonitor( 500 );
                IOFileFilter filter = FileFilterUtils.nameFileFilter( war.getName() + ".deployed" );
                FileAlterationObserver observer = new FileAlterationObserver( deployDir, filter );
                observer.addListener( new FileAlterationListenerAdaptor() {

                    @Override
                    public void onFileCreate( final File file ) {
                       fireAppReadyEvent(war, sreq);
                    }

                    @Override
                    public void onFileChange(final File file) {
                        fireAppReadyEvent(war, sreq);
                    }
                } );
                monitor.addObserver( observer );
                monitor.start();
            }
        }

        private void fireAppReadyEvent(File war, ServletRequest sreq) {
            final String url = "http://" +
                sreq.getServerName() + ":" +
                sreq.getServerPort() + "/" +
                war.getName().replace( ".war", "" );

            appReadyEvent.fire( new AppReady( url ) );
        }
    }

    private class BuildAndDeployWithCodeServerCallable extends BuildAndDeployCallable {

        private volatile boolean isCodeServerReady = false;
        private volatile Throwable error = null;

        private BuildAndDeployWithCodeServerCallable( Project project,
                                                      File pomXml,
                                                      String sessionId,
                                                      ServletRequest sreq ) {
            super( project, pomXml, sessionId, sreq );
        }

        @Override
        protected InvocationResult executeRequest() throws Throwable {
            final InvocationRequest codeServerRequest = createCodeServerRequest( pomXml );
            final DefaultInvocationRequest packageRequest = createDevModePackageRequest( pomXml );

            setCodeServerOutputHandler( codeServerRequest );
            setPackageOutputHandler( packageRequest );

            maybeLaunchCodeServer( codeServerRequest );
            blockUntilCodeServerIsReadyOrError();
            if ( error != null ) {
                throw error;
            }

            return new DefaultInvoker().execute( createDevModePackageRequest( pomXml ) );
        }

        private void maybeLaunchCodeServer(final InvocationRequest codeServerRequest ) {
            if ( isCodeServerLaunched ) {
                isCodeServerReady = true;
            } else {
                execService.submit( new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new DefaultInvoker().execute( codeServerRequest );
                        } catch ( MavenInvocationException e ) {
                            error = e;
                        }
                    }
                } );
            }
        }

        private void blockUntilCodeServerIsReadyOrError() throws InterruptedException {
            while ( !( isCodeServerReady || error != null ) ) {
                Thread.sleep( 500 );
            }
        }

        private void setCodeServerOutputHandler( final InvocationRequest codeServerRequest ) {
            codeServerRequest.setOutputHandler( new InvocationOutputHandler() {

                @Override
                public void consumeLine( String line ) {
                    if ( !isCodeServerReady && line.contains( "The code server is ready at" ) ) {
                        isCodeServerReady = true;
                        isCodeServerLaunched = true;
                    }
                    sendOutputToClient(line, sessionId);
                }
            } );
        }

        private void setPackageOutputHandler( final InvocationRequest packageRequest ) {
            packageRequest.setOutputHandler( new InvocationOutputHandler() {
                @Override
                public void consumeLine( String line ) {
                    sendOutputToClient(line, sessionId);
                }
            } );
        }
    }

    private interface CallableProducer {

        Callable<List<BuildMessage>> get( Project project, File pomXml );
    }

    private static final Logger logger = LoggerFactory.getLogger( BuildServiceImpl.class );

    private final DirectoryStream.Filter<org.uberfire.java.nio.file.Path> dotFileFilter = new DotFileFilter();

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Resource
    private ManagedExecutorService execService;

    private Map<Project, File> tmpDirs = new ConcurrentHashMap<Project, File>();

    private void writeSourceFileSystemToDisk( Project project,
                                              org.uberfire.java.nio.file.Path tmpRoot ) {
        Path root = project.getRootPath();
        DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream = Files.newDirectoryStream( org.uberfire.backend.server.util.Paths.convert( root ) );

        visitPaths( directoryStream,
                    root.toURI(),
                    tmpRoot );
    }

    private org.uberfire.java.nio.file.Path visitPaths( DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream,
                                                        String projectPrefix,
                                                        org.uberfire.java.nio.file.Path tmp ) {

        for ( final org.uberfire.java.nio.file.Path path : directoryStream ) {
            final String destinationPath = filterPrefix( projectPrefix,
                                                         path );
            if ( Files.isDirectory( path ) ) {
                new File( tmp.toFile(), destinationPath ).mkdir();
                visitPaths( Files.newDirectoryStream( path ),
                            projectPrefix,
                            tmp );
            } else {
                //Don't process dotFiles
                if ( !dotFileFilter.accept( path ) ) {
                    //Add new resource
                    final InputStream is = ioService.newInputStream( path );
                    final BufferedInputStream bis = new BufferedInputStream( is );
                    writePath( destinationPath,
                               bis,
                               tmp.toUri().toString() );
                }
            }
        }

        return tmp;
    }

    private String filterPrefix( String pathPrefix,
                                 org.uberfire.java.nio.file.Path path ) {
        return path.toUri().toString().substring( pathPrefix.length() + 1 );
    }

    private void writePath( String destinationPath,
                            BufferedInputStream bis,
                            String outputRoot ) {
        org.uberfire.java.nio.file.Path fullPath = Paths.get( outputRoot + "/" + destinationPath );
        ioService.copy( bis,
                        fullPath );
    }

    @Override
    public BuildResults build( final Project project ) {
        return new BuildResults();
    }

    private BuildResults buildHelper( final Project project,
                                      final CallableProducer producer ) {
        final BuildResults buildResults = new BuildResults();
        final File tmpRoot;
        try {
            tmpRoot = copyProjectSourceToTmpDir( project );
        } catch ( IOException e ) {
            final BuildMessage errorMsg = generateErrorBuildMessage( e );
            buildResults.addBuildMessage( errorMsg );
            logger.error( errorMsg.getText(),
                          e );

            return buildResults;
        }

        final File pomXml = assertExists( new File( tmpRoot,
                                                    "pom.xml" ) );
        runMaven( project, producer, buildResults, pomXml );

        return buildResults;
    }

    private void runMaven( final Project project,
                           final CallableProducer producer,
                           final BuildResults buildResults,
                           final File pomXml ) {
        final InvocationRequest packageRequest = createDevModePackageRequest( pomXml );
        final InvocationRequest codeServerRequest = createCodeServerRequest( pomXml );

        startBuild( project, producer, pomXml );

        try {

            final BuildMessage message = new BuildMessage();
            message.setLevel( Level.INFO );
            message.setText( "Build started..." );
            buildResults.addBuildMessage( message );

        } catch ( Exception e ) {
            logBuildException( project,
                               e );
            final BuildMessage errorMsg = generateErrorBuildMessage( e );
            buildResults.addBuildMessage( errorMsg );
            logger.error( errorMsg.getText(),
                          e );
        }
    }

    private void startBuild( final Project project,
                             final CallableProducer producer,
                             final File pomXml ) {
        execService.submit( producer.get( project, pomXml ) );
    }

    private InvocationRequest createCodeServerRequest( final File pomXml ) {
        final DefaultInvocationRequest codeServerRequest = new DefaultInvocationRequest();
        final Properties codeServerProperties = new Properties();
        final File webappFolder = new File( pomXml.getParentFile(), "src/main/webapp" );

        codeServerProperties.setProperty( "gwt.codeServer.launcherDir", webappFolder.getAbsolutePath() );

        codeServerRequest.setPomFile( pomXml );
        codeServerRequest.setGoals( Collections.singletonList( "gwt:run-codeserver" ) );
        codeServerRequest.setProperties( codeServerProperties );

        return codeServerRequest;
    }

    private DefaultInvocationRequest createDevModePackageRequest( final File pomXml ) {
        final DefaultInvocationRequest packageRequest = new DefaultInvocationRequest();
        final Properties props = new Properties();

        props.setProperty( "gwt.compiler.skip", "true" );

        packageRequest.setPomFile( pomXml );
        packageRequest.setGoals( Collections.singletonList( "package" ) );
        packageRequest.setProperties( props );

        return packageRequest;
    }

    private File copyProjectSourceToTmpDir( Project project ) throws IOException {
        final File tmpDir = getOrCreateTmpProjectDir( project );

        deleteChangeableContents( tmpDir );
        writeSourceFileSystemToDisk( project,
                                     Paths.get( tmpDir.toURI().toString() ) );

        return tmpDir;
    }

    private void deleteChangeableContents( File tmpDir ) {
        /*
         * This is here so we don't delete files generated by the codeserver from a previous use of "BuildAndDeploy".
         */
        final String[] changeableSubDirs = new String[] { "src/main/java", "src/main/resources", "src/test", "target" };

        if ( tmpDir.exists() && !tmpDir.isDirectory() ) {
            Files.deleteIfExists( Paths.get( tmpDir.toURI().toString() ),
                                  StandardDeleteOption.NON_EMPTY_DIRECTORIES );
            tmpDir.mkdir();
        }

        for ( final String subDir : changeableSubDirs ) {
            final File dirFile = new File( tmpDir, subDir );
            Files.deleteIfExists( Paths.get( dirFile.toURI().toString() ),
                                  StandardDeleteOption.NON_EMPTY_DIRECTORIES );
        }

    }

    private BuildMessage generateErrorBuildMessage( Exception e ) {
        final BuildMessage msg = new BuildMessage();
        msg.setLevel( Level.ERROR );
        msg.setText( "Unexpected error occurred while building project: " + e.getMessage() );

        return msg;
    }

    private void logBuildException( final Project project,
                                    Throwable t ) {
        // TODO add error messages to build results
        logger.error( "Unable to build LiveSpark project, " + project.getProjectName(),
                      t );
    }

    private BuildMessage createFailureMessage() {
        final BuildMessage message = new BuildMessage();
        message.setLevel( Level.INFO );
        message.setText( "Build was successful" );

        return message;
    }

    private BuildMessage createSuccessMessage() {
        final BuildMessage message = new BuildMessage();
        message.setLevel( Level.INFO );
        message.setText( "Build successful" );

        return message;
    }

    private File assertExists( File file ) {
        if ( !file.exists() )
            throw new RuntimeException( "The following required file did not exist: " + file.getAbsolutePath() );

        return file;
    }

    private File getOrCreateTmpProjectDir( Project project ) throws IOException {
        File tmp = tmpDirs.get( project );
        if ( tmp == null || !tmp.exists() ) {
            tmp = createTmpProjectDir( project );
            tmpDirs.put( project,
                         tmp );
        }

        return tmp;
    }

    private File createTmpProjectDir( Project project ) throws IOException {
        final File tmpDir = File.createTempFile( padName( project.getProjectName() ),
                                                 "" );
        tmpDir.mkdirs();

        return tmpDir;
    }

    private String padName( String projectName ) {
        if ( projectName.length() >= 3 ) {
            return projectName;
        } else {
            final StringBuilder builder = new StringBuilder( projectName );
            do {
                builder.append( '0' );
            } while ( builder.length() < 3 );

            return builder.toString();
        }
    }

    @Override
    public BuildResults buildAndDeploy( Project project ) {
        return buildAndDeploy( project,
                               false );
    }

    @Override
    public BuildResults buildAndDeploy( Project project,
                                        boolean suppressHandlers ) {
        final String sessionId = RpcContext.getQueueSession().getSessionId();
        return buildHelper( project,
                            new CallableProducer() {

                                @Override
                                public Callable<List<BuildMessage>> get( Project project, File pomXml ) {
                                    return new BuildAndDeployWithCodeServerCallable( project,
                                                                                     pomXml,
                                                                                     sessionId,
                                                                                     RpcContext.getServletRequest() );
                                }
                            } );
    }

    @Override
    public boolean isBuilt( Project project ) {
        /*
         * In BuildServiceImpl this returns true after the first initial build is performed so that incremental builds can be done subsequently.
         * Since we don't currently have incremental builds, we always return true.
         */
        return true;
    }

    @Override
    public IncrementalBuildResults addPackageResource( Path resource ) {
        // Currently no incremental build support
        return new IncrementalBuildResults();
    }

    @Override
    public IncrementalBuildResults deletePackageResource( Path resource ) {
        // Currently no incremental build support
        return new IncrementalBuildResults();
    }

    @Override
    public IncrementalBuildResults updatePackageResource( Path resource ) {
        // Currently no incremental build support
        return new IncrementalBuildResults();
    }

    @Override
    public IncrementalBuildResults applyBatchResourceChanges( Project project,
                                                              Map<Path, Collection<ResourceChange>> changes ) {
        // Currently no incremental build support
        return new IncrementalBuildResults();
    }

}
