package org.livespark.backend.server.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;

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


@ApplicationScoped
@Service
@Alternative
@Priority( value = 100 )
public class GwtWarBuildService implements BuildService {

	@Inject
    private ServerMessageBus bus;
	
	@Inject
	private Event<AppReady> appReadyEvent;
	
    private abstract class BaseBuildCallable implements Callable<List<BuildMessage>> {

        private final Project project;
        private final InvocationRequest req;
        private final String sessionId;

        private BaseBuildCallable( Project project, InvocationRequest req, String sessionId ) {
            this.project = project;
            this.req = req;
            this.sessionId = sessionId;
        }

        protected abstract List<BuildMessage> postBuildTasks( InvocationResult res );

        @Override
        public List<BuildMessage> call() throws Exception {
            final List<BuildMessage> retVal = new ArrayList<BuildMessage>();

            try {
            	MessageBuilder.createMessage()
            		.toSubject( "MavenBuilderOutput" )
            		.signalling()
            		.with( MessageParts.SessionID, sessionId )
            		.with( "clean", Boolean.TRUE )
            		.noErrorHandling().sendNowWith( bus );
            	
                req.setOutputHandler(new InvocationOutputHandler() {
					@Override
					public void consumeLine(String line) {
						MessageBuilder.createMessage()
                        	.toSubject( "MavenBuilderOutput" )
                        	.signalling()
                        	.with( MessageParts.SessionID, sessionId )
                        	.with( "output", line + "\n")
                        	.noErrorHandling().sendNowWith( bus );
					}
				});
				final InvocationResult res = new DefaultInvoker().execute( req );
                retVal.addAll( postBuildTasks( res ) );
                
				final File targetDir = new File(req.getPomFile().getParent(), "/target");
				final Collection<File> wars = FileUtils.listFiles(targetDir, new String[] { "war" }, false);
				// TODO handle production mode
				for (final File war : wars) {
					String home = System.getProperty("errai.jboss.home");
					File deployDir = new File(home, "/standalone/deployments");
					FileUtils.deleteQuietly(new File(deployDir, war.getName()));
					FileUtils.copyFileToDirectory(war, deployDir);

					FileAlterationMonitor monitor = new FileAlterationMonitor(500);
					IOFileFilter filter = FileFilterUtils.nameFileFilter(war.getName() + ".deployed");
					FileAlterationObserver observer = new FileAlterationObserver(deployDir, filter);
					observer.addListener(new FileAlterationListenerAdaptor() {
						
						@Override
						public void onFileCreate(final File file) {
							// TODO port conf.
							appReadyEvent.fire(new AppReady("http://localhost:" + 8888 + "/" + war.getName().replace(".war", "")));
						}
					});
					monitor.addObserver(observer);
					monitor.start();
				}
			} catch (Throwable t) {
				logBuildException(project, t);
			}

            return retVal;
        }
    }

    private class OnlyBuildCallable extends BaseBuildCallable {
        private OnlyBuildCallable( Project project, InvocationRequest req, String sessionId ) {
            super( project, req, sessionId );
        }

        @Override
        protected List<BuildMessage> postBuildTasks( InvocationResult res ) {
            final List<BuildMessage> messages = new ArrayList<BuildMessage>();
            messages.addAll( processBuildResults( res ) );

            return messages;
        }
    }

    private class BuildAndDeployCallable extends BaseBuildCallable {
        private BuildAndDeployCallable( Project project, InvocationRequest req, String sessionId ) {
            super( project, req, sessionId );
        }

        @Override
        protected List<BuildMessage> postBuildTasks( InvocationResult res ) {
            final List<BuildMessage> messages = new ArrayList<BuildMessage>();
            messages.addAll( processBuildResults( res ) );
            messages.addAll( deployIfSuccessful( res ) );

            return messages;
        }
    }

    private interface CallableProducer {
        Callable<List<BuildMessage>> get( Project project, InvocationRequest req );
    }

    private static final Logger logger = LoggerFactory.getLogger( BuildServiceImpl.class );

    private final DirectoryStream.Filter<org.uberfire.java.nio.file.Path> dotFileFilter = new DotFileFilter();

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Resource
    private ManagedExecutorService execService;

    private Map<Project, File> tmpDirs = new ConcurrentHashMap<Project, File>();

    private void writeSourceFileSystemToDisk( Project project, org.uberfire.java.nio.file.Path tmpRoot ) {
        Path root = project.getRootPath();
        DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream = Files.newDirectoryStream( org.uberfire.backend.server.util.Paths.convert( root ) );

        visitPaths( directoryStream, root.toURI(), tmpRoot );
    }

    private org.uberfire.java.nio.file.Path visitPaths( DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream,
                                                        String projectPrefix,
                                                        org.uberfire.java.nio.file.Path tmp ) {
        for ( final org.uberfire.java.nio.file.Path path : directoryStream ) {
            final String destinationPath = filterPrefix( projectPrefix, path );
            if ( Files.isDirectory( path ) ) {
                new File( tmp.toFile(), destinationPath ).mkdir();
                visitPaths( Files.newDirectoryStream( path ), projectPrefix, tmp );
            } else {
                //Don't process dotFiles
                if ( !dotFileFilter.accept( path ) ) {
                    //Add new resource
                    final InputStream is = ioService.newInputStream( path );
                    final BufferedInputStream bis = new BufferedInputStream( is );
                    writePath( destinationPath, bis, tmp.toUri().toString() );
                }
            }
        }

        return tmp;
    }

    private String filterPrefix( String pathPrefix, org.uberfire.java.nio.file.Path path ) {
        return path.toUri().toString().substring( pathPrefix.length() + 1 );
    }

    private void writePath( String destinationPath, BufferedInputStream bis, String outputRoot ) {
        org.uberfire.java.nio.file.Path fullPath = Paths.get( outputRoot + "/" + destinationPath );
        ioService.copy( bis, fullPath );
    }

    @Override
    public BuildResults build( final Project project ) {
//    	final String sessionId = RpcContext.getQueueSession().getSessionId();
//        return buildHelper( project, new CallableProducer() {
//            @Override
//            public Callable<List<BuildMessage>> get( Project project,  InvocationRequest req ) {
//                return new OnlyBuildCallable( project, req, sessionId );
//            }
//        });
    	return new BuildResults();
    }

    private BuildResults buildHelper( final Project project, CallableProducer producer ) {
        final BuildResults buildResults = new BuildResults();
        final File tmpRoot;
        try {
            tmpRoot = copyProjectSourceToTmpDir( project );
        } catch ( IOException e ) {
            final BuildMessage errorMsg = generateErrorBuildMessage( e );
            buildResults.addBuildMessage( errorMsg );
            logger.error( errorMsg.getText(), e );

            return buildResults;
        }

        final File pomXml = assertExists( new File( tmpRoot, "pom.xml" ) );
        final InvocationRequest req = new DefaultInvocationRequest();

        req.setPomFile( pomXml );
        req.setGoals( Collections.singletonList( "package" ) );

        final Future<List<BuildMessage>> buildFuture = execService.submit( producer.get( project, req ) );

        try {
        	
        	final BuildMessage message = new BuildMessage();
            message.setLevel( Level.INFO );
            message.setText( "Build started..." );
            buildResults.addBuildMessage(message);
            
        } catch (Exception e) {
            logBuildException( project, e );
            final BuildMessage errorMsg = generateErrorBuildMessage( e );
            buildResults.addBuildMessage( errorMsg );
            logger.error( errorMsg.getText(), e );
        }

        return buildResults;
    }

    private File copyProjectSourceToTmpDir( Project project ) throws IOException {
        final File tmpDir = getOrCreateTmpProjectDir( project );

        deleteContents( tmpDir );
        writeSourceFileSystemToDisk( project, Paths.get( tmpDir.toURI().toString() ) );

        return tmpDir;
    }

    private void deleteContents( File tmpDir ) {
        Files.deleteIfExists( Paths.get( tmpDir.toURI().toString() ), StandardDeleteOption.NON_EMPTY_DIRECTORIES );
        tmpDir.mkdir();
    }

    private BuildMessage generateErrorBuildMessage( Exception e ) {
        final BuildMessage msg = new BuildMessage();
        msg.setLevel( Level.ERROR );
        msg.setText( "Unexpected error occurred while building project: " + e.getMessage() );

        return msg;
    }

    private void logBuildException( final Project project, Throwable t ) {
        // TODO add error messages to build results
        logger.error( "Unable to build LiveSpark project, " + project.getProjectName(), t );
    }

    private List<BuildMessage> deployIfSuccessful( InvocationResult res ) {
        // TODO implement
        return Collections.emptyList();
    }

    private List<BuildMessage> processBuildResults( InvocationResult res ) {
        final List<BuildMessage> messages = new ArrayList<BuildMessage>();

        if ( res.getExitCode() == 0 ) {
            messages.add( createSuccessMessage() );
        } else {
            messages.add( createFailureMessage() );
        }

        return messages;
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
            tmpDirs.put( project, tmp );
        }

        return tmp;
    }

    private File createTmpProjectDir( Project project ) throws IOException {
        final File tmpDir = File.createTempFile( padName( project.getProjectName() ), "" );
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
        return buildAndDeploy( project, false );
    }

    @Override
    public BuildResults buildAndDeploy( Project project, boolean suppressHandlers ) {
    	final String sessionId = RpcContext.getQueueSession().getSessionId();
        return buildHelper( project, new CallableProducer() {
            @Override
            public Callable<List<BuildMessage>> get( Project project, InvocationRequest req ) {
                return new BuildAndDeployCallable( project, req, sessionId );
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
    public IncrementalBuildResults applyBatchResourceChanges( Project project, Map<Path, Collection<ResourceChange>> changes ) {
        // Currently no incremental build support
        return new IncrementalBuildResults();
    }

}
