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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import org.guvnor.ala.pipeline.ConfigExecutor;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.events.AfterPipelineExecutionEvent;
import org.guvnor.ala.pipeline.execution.PipelineExecutor;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.ala.registry.RuntimeRegistry;
import org.guvnor.ala.registry.SourceRegistry;
import org.guvnor.ala.runtime.Runtime;

import org.guvnor.common.services.backend.file.DotFileFilter;
import org.guvnor.common.services.project.builder.model.BuildMessage;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.model.IncrementalBuildResults;
import org.guvnor.common.services.project.builder.service.PostBuildHandler;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.common.services.project.service.ProjectRepositoriesService;
import org.guvnor.common.services.project.service.ProjectRepositoryResolver;
import org.guvnor.common.services.shared.message.Level;
import org.guvnor.m2repo.backend.server.ExtendedM2RepoService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.kie.workbench.common.services.backend.builder.BuildServiceImpl;
import org.kie.workbench.common.services.backend.builder.LRUBuilderCache;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.backend.server.service.build.BuildCallable;
import org.livespark.backend.server.service.build.BuildCallableFactory;
import org.livespark.backend.server.service.dir.TmpDirFactory;
import org.livespark.client.shared.AppReady;
import org.livespark.client.shared.GwtWarBuildService;
import org.livespark.project.ProjectUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Paths;
import org.uberfire.java.nio.file.StandardDeleteOption;
import org.uberfire.workbench.events.ResourceChange;

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

    private RepositoryService repositoryService;

    private Event<AppReady> appReadyEvent;

    private Instance<ConfigExecutor> configExecutors;

    private RuntimeRegistry runtimeRegistry;
    
    private SourceRegistry sourceRegistry;

    private PipelineRegistry pipelineRegistry;

    private CDIPipelineEventListener pipelineEventListener;

    private PipelineExecutor executor;
    
    // For proxying
    public GwtWarBuildServiceImpl() {
    }

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
            final @Named( "ioStrategy" ) IOService ioService, final Instance<ConfigExecutor> configExecutors,
            final RepositoryService repositoryService, final Event<AppReady> appReadyEvent,
            final SourceRegistry sourceRegistry,
            final RuntimeRegistry runtimeRegistry, final PipelineRegistry pipelineRegistry,
            final CDIPipelineEventListener pipelineEventListener ) {
        super( pomService, m2RepoService, projectService, repositoryResolver, projectRepositoriesService, cache, handlers );
        this.callableFactory = callableFactory;
        this.tmpDirFactory = tmpDirFactory;
        this.ioService = ioService;
        this.configExecutors = configExecutors;
        this.repositoryService = repositoryService;
        this.appReadyEvent = appReadyEvent;
        this.runtimeRegistry = runtimeRegistry;
        this.pipelineRegistry = pipelineRegistry;
        this.sourceRegistry = sourceRegistry;
        this.pipelineEventListener = pipelineEventListener;
    }

    @Resource
    private ManagedExecutorService execService;

    @PostConstruct
    private void setup() {
        unpacker = new ProjectUnpacker( ioService, new DotFileFilter() );
        Iterator<ConfigExecutor> iterator = configExecutors.iterator();
        Collection<ConfigExecutor> configs = new ArrayList<>();
        while ( iterator.hasNext() ) {
            ConfigExecutor configExecutor = iterator.next();
            configs.add( configExecutor );
        }
        executor = new PipelineExecutor( configs );
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
        final String[] changeableSubDirs = new String[]{ "src/main/java",
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
        if ( !file.exists() ) {
            throw new RuntimeException( "The following required file did not exist: " + file.getAbsolutePath() );
        }

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
        return buildAndDeployWithPipeline( project );
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
        return buildAndDeploySDMWithPipeline(project);
    }

    public BuildResults buildAndDeployWithPipeline( Project project ) {
        final BuildResults results = new BuildResults( project.getPom().getGav() );
        Path rootPath = project.getRootPath();
        Path repoPath = PathFactory.newPath( "repo", rootPath.toURI().substring( 0, rootPath.toURI().indexOf( rootPath.getFileName() ) ) );
        Repository repository = repositoryService.getRepository( repoPath );
        
        Pipeline pipe = pipelineRegistry.getPipelineByName( "wildfly pipeline" );

        Input wildflyInput = new Input() {
            {
                put( "repo-name", repository.getAlias() );
                put( "branch", repository.getDefaultBranch() );
                put( "project-dir", project.getProjectName() );
                put( "wildfly-user", "testadmin" );
                put( "wildfly-password", "testadmin" );
                put( "bindAddress", "localhost" );
                put( "host", "localhost" );
                put( "port", "8888" );
                put( "management-port", "9990" );

            }
        };
        executor.execute( wildflyInput, pipe, System.out::println, pipelineEventListener );

        return results;
    }
    
    public BuildResults buildAndDeploySDMWithPipeline( Project project ) {
        final BuildResults results = new BuildResults( project.getPom().getGav() );
        Path rootPath = project.getRootPath();
        Path repoPath = PathFactory.newPath( "repo", rootPath.toURI().substring( 0, rootPath.toURI().indexOf( rootPath.getFileName() ) ) );
        Repository repository = repositoryService.getRepository( repoPath );
        
  
        Pipeline pipe = pipelineRegistry.getPipelineByName( "wildfly sdm pipeline" );
        
        Input wildflyInput = new Input() {
            {
                
                put( "repo-name", repository.getAlias() );
                put( "branch", repository.getDefaultBranch() );
                put( "project-dir", project.getProjectName() );
                put( "wildfly-user", "testadmin" );
                put( "wildfly-password", "testadmin" );
                put( "bindAddress", "localhost" );
                put( "host", "localhost" );
                put( "port", "8888" );
                put( "management-port", "9990" );

            }
        };
        org.guvnor.ala.build.Project projectByName = sourceRegistry.getProjectByName(project.getProjectName());

        if (projectByName != null) {
            wildflyInput.put("project-temp-dir", projectByName.getTempDir());
        }
       
        
        executor.execute( wildflyInput, pipe, System.out::println, pipelineEventListener );

        return results;
    }

    @Override
    public IncrementalBuildResults addPackageResource( final Path resource ) {
        if ( isNotLiveSparkGeneratedJavaSource( resource ) ) {
            return super.addPackageResource( resource );
        } else {
            return new IncrementalBuildResults();
        }
    }

    @Override
    public IncrementalBuildResults updatePackageResource( final Path resource ) {
        if ( isNotLiveSparkGeneratedJavaSource( resource ) ) {
            return super.updatePackageResource( resource );
        } else {
            return new IncrementalBuildResults();
        }
    }

    @Override
    public IncrementalBuildResults applyBatchResourceChanges( final Project project,
            final Map<Path, Collection<ResourceChange>> changes ) {
        final Map<Path, Collection<ResourceChange>> nonGeneratedFileChanges
                = changes.entrySet()
                .stream()
                .filter( e -> isNotLiveSparkGeneratedJavaSource( e.getKey() ) )
                .collect( Collectors.toMap( e -> e.getKey(), e -> e.getValue() ) );

        if ( nonGeneratedFileChanges.isEmpty() ) {
            return new IncrementalBuildResults();
        } else {
            return super.applyBatchResourceChanges( project, nonGeneratedFileChanges );
        }
    }

    @Override
    public IncrementalBuildResults deletePackageResource( final Path resource ) {
        if ( isNotLiveSparkGeneratedJavaSource( resource ) ) {
            return super.deletePackageResource( resource );
        } else {
            return new IncrementalBuildResults();
        }
    }

    private boolean isNotLiveSparkGeneratedJavaSource( final Path resource ) {
        final String fileName = resource.getFileName();
        return !( fileName.endsWith( "FormModel.java" )
                || fileName.endsWith( "FormView.java" )
                || fileName.endsWith( "ListView.java" )
                || fileName.endsWith( "RestService.java" )
                || fileName.endsWith( "EntityService.java" )
                || fileName.endsWith( "RestServiceImpl.java" ) );
    }

    public void buildFinished( @Observes AfterPipelineExecutionEvent e ) {
        List<org.guvnor.ala.runtime.Runtime> allRuntimes = runtimeRegistry.getRuntimes( 0, 10, "", true );
        Runtime get = allRuntimes.get( 0 );
        String url = "http://" + get.getEndpoint().getHost() + ":" + get.getEndpoint().getPort() + "/" + get.getEndpoint().getContext();
        appReadyEvent.fire( new AppReady( url ) );
    }

}