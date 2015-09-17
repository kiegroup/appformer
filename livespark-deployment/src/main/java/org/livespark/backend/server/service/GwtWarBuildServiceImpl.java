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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.guvnor.common.services.backend.file.DotFileFilter;
import org.guvnor.common.services.project.builder.model.BuildMessage;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.model.IncrementalBuildResults;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.shared.message.Level;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.kie.workbench.common.services.backend.builder.BuildServiceImpl;
import org.livespark.backend.server.service.build.BuildCallable;
import org.livespark.backend.server.service.build.BuildCallableFactory;
import org.livespark.backend.server.service.dir.TmpDirFactory;
import org.livespark.client.shared.GwtWarBuildService;
import org.livespark.project.ProjectUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
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

    private static final Logger logger = LoggerFactory.getLogger( BuildServiceImpl.class );

    @Inject
    private BuildCallableFactory callableFactory;

    @Inject
    private TmpDirFactory tmpDirFactory;

    private ProjectUnpacker unpacker;

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Resource
    private ManagedExecutorService execService;

    @PostConstruct
    private void setup() {
        unpacker = new ProjectUnpacker( ioService, new DotFileFilter() );
    }

    @Override
    public BuildResults build( final Project project ) {
        return new BuildResults();
    }

    private BuildResults buildHelper( final Project project,
                                      final HttpSession session,
                                      final CallableProducer producer ) {
        final BuildResults buildResults = new BuildResults();
        final File tmpRoot;
        try {
            tmpRoot = tmpDirFactory.getTmpDir( project, session );
            copyProjectSourceToTmpDir( project, tmpRoot );
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

    private File copyProjectSourceToTmpDir( final Project project, final File tmpDir ) throws IOException {
        deleteChangeableContents( tmpDir );
        unpacker.writeSourceFileSystemToDisk( project, Paths.get( tmpDir.toURI().toString() ) );

        return tmpDir;
    }

    private void deleteChangeableContents( File tmpDir ) {
        /*
         * This is here so we don't delete files generated by the codeserver from a previous use of "BuildAndDeploy".
         */
        final String[] changeableSubDirs = new String[]{"src/main/java",
                                                        "src/main/resources",
                                                        "src/test"
                                                        };

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

    @Override
    public BuildResults buildAndDeploy( Project project ) {
        return buildAndDeploy( project,
                               false );
    }

    @Override
    public BuildResults buildAndDeploy( Project project,
                                        boolean suppressHandlers ) {
        final String queueSessionId = RpcContext.getQueueSession().getSessionId();
        final HttpSession session = RpcContext.getHttpSession();
        final ServletRequest sreq = RpcContext.getServletRequest();
        return buildHelper( project,
                            session,
                            new CallableProducer() {

                                @Override
                                public BuildCallable get( Project project, File pomXml ) {
                                    return callableFactory.createProductionDeploymentCallable( project, pomXml, session, queueSessionId, sreq );
                                }
                            } );
    }

    @Override
    public BuildResults buildAndDeployDevMode( Project project ) {
        final String queueSessionId = RpcContext.getQueueSession().getSessionId();
        final HttpSession session = RpcContext.getHttpSession();
        final ServletRequest sreq = RpcContext.getServletRequest();
        return buildHelper( project,
                            session,
                            new CallableProducer() {

                                @Override
                                public BuildCallable get( Project project, File pomXml ) {
                                    return callableFactory.createDevModeDeploymentCallable( project, pomXml, session, queueSessionId, sreq );
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
