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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletRequest;

import org.guvnor.common.services.backend.file.DotFileFilter;
import org.guvnor.common.services.project.builder.model.BuildMessage;
import org.guvnor.common.services.project.builder.model.BuildMessage.Level;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.model.IncrementalBuildResults;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.kie.workbench.common.services.backend.builder.BuildServiceImpl;
import org.livespark.backend.server.service.build.BuildCallable;
import org.livespark.backend.server.service.build.BuildCallableFactory;
import org.livespark.client.shared.GwtWarBuildService;
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
@Priority(value = 100)
public class GwtWarBuildServiceImpl implements GwtWarBuildService {

    private interface CallableProducer {
        BuildCallable get( Project project, File pomXml );
    }

    @Inject
    private BuildCallableFactory callableFactory;

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
        final ServletRequest sreq = RpcContext.getServletRequest();
        return buildHelper( project,
                            new CallableProducer() {

                                @Override
                                public BuildCallable get( Project project, File pomXml ) {
                                    return callableFactory.createProductionDeploymentCallable( project, pomXml, sessionId, sreq );
                                }
                            } );
    }

    @Override
    public BuildResults buildAndDeployDevMode( Project project ) {
        final String sessionId = RpcContext.getQueueSession().getSessionId();
        final ServletRequest sreq = RpcContext.getServletRequest();
        return buildHelper( project,
                            new CallableProducer() {

                                @Override
                                public BuildCallable get( Project project, File pomXml ) {
                                    return callableFactory.createDevModeDeploymentCallable( project, pomXml, sessionId, sreq );
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
