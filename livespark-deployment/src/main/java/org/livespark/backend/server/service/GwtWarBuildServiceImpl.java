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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.guvnor.common.services.backend.file.DotFileFilter;
import org.guvnor.common.services.project.builder.model.BuildMessage;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.service.PostBuildHandler;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.common.services.project.service.ProjectRepositoriesService;
import org.guvnor.common.services.project.service.ProjectRepositoryResolver;
import org.guvnor.common.services.shared.message.Level;
import org.guvnor.m2repo.backend.server.ExtendedM2RepoService;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.kie.workbench.common.services.backend.builder.BuildServiceImpl;
import org.kie.workbench.common.services.backend.builder.LRUBuilderCache;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.backend.server.service.build.BuildCallable;
import org.livespark.backend.server.service.build.BuildCallableFactory;
import org.livespark.backend.server.service.dir.TmpDirFactory;
import org.livespark.client.shared.GwtWarBuildService;
import org.livespark.project.ProjectUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Paths;
import org.uberfire.java.nio.file.StandardDeleteOption;

@Specializes
@ApplicationScoped
@Service
public class GwtWarBuildServiceImpl extends BuildServiceImpl implements GwtWarBuildService {

    private interface CallableProducer {
        BuildCallable get( Project project, File pomXml );
    }

    private static final Logger logger = LoggerFactory.getLogger( BuildServiceImpl.class );

    private BuildCallableFactory callableFactory;

    private TmpDirFactory tmpDirFactory;

    private ProjectUnpacker unpacker;

    private IOService ioService;

    // For proxying
    public GwtWarBuildServiceImpl() {}

    @Inject
    public GwtWarBuildServiceImpl( final POMService pomService,
                                   final ExtendedM2RepoService m2RepoService,
                                   final KieProjectService projectService,
                                   final ProjectRepositoryResolver repositoryResolver,
                                   final ProjectRepositoriesService projectRepositoriesService,
                                   final LRUBuilderCache cache,
                                   final Instance<PostBuildHandler> handlers,
                                   final BuildCallableFactory callableFactory,
                                   final TmpDirFactory tmpDirFactory,
                                   final @Named("ioStrategy") IOService ioService ) {
        super( pomService, m2RepoService, projectService, repositoryResolver, projectRepositoriesService, cache, handlers );
        this.callableFactory = callableFactory;
        this.tmpDirFactory = tmpDirFactory;
        this.ioService = ioService;
    }


    @Resource
    private ManagedExecutorService execService;

    @PostConstruct
    private void setup() {
        unpacker = new ProjectUnpacker( ioService, new DotFileFilter() );
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
        return buildAndDeploy( project, false, DeploymentMode.VALIDATED );
    }

    @Override
    public BuildResults buildAndDeploy( Project project, DeploymentMode mode ) {
        return buildAndDeploy( project, false, mode );
    }

    @Override
    public BuildResults buildAndDeploy( Project project, boolean suppressHandlers ) {
        return buildAndDeploy( project, suppressHandlers, DeploymentMode.VALIDATED );
    }

    @Override
    public BuildResults buildAndDeploy( Project project, boolean suppressHandlers, DeploymentMode mode ) {
        BuildResults results = super.buildAndDeploy( project, suppressHandlers, mode );
        deployWar( project );
        return results;
    }

    private void deployWar( final Project project ) {
        new Runnable() {
            @Override
            public void run() {
                final String queueSessionId = RpcContext.getQueueSession().getSessionId();
                final HttpSession session = RpcContext.getHttpSession();
                final ServletRequest sreq = RpcContext.getServletRequest();
                buildHelper( project,
                        session,
                        new CallableProducer() {

                            @Override
                            public BuildCallable get( Project project, File pomXml ) {
                                return callableFactory.createProductionDeploymentCallable( project, pomXml, session, queueSessionId, sreq );
                            }
                        } );
            }
        }.run();
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

}
