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

import org.guvnor.ala.pipeline.ConfigExecutor;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.events.AfterPipelineExecutionEvent;
import org.guvnor.ala.pipeline.execution.PipelineExecutor;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.ala.registry.RuntimeRegistry;
import org.guvnor.ala.registry.SourceRegistry;
import org.guvnor.ala.runtime.Runtime;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.model.IncrementalBuildResults;
import org.guvnor.common.services.project.builder.service.PostBuildHandler;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.common.services.project.service.ProjectRepositoriesService;
import org.guvnor.common.services.project.service.ProjectRepositoryResolver;
import org.guvnor.m2repo.backend.server.ExtendedM2RepoService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.services.backend.builder.BuildServiceImpl;
import org.kie.workbench.common.services.backend.builder.LRUBuilderCache;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.client.shared.AppReady;
import org.livespark.client.shared.GwtWarBuildService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.workbench.events.ResourceChange;

@Specializes
@ApplicationScoped
@Service
public class GwtWarBuildServiceImpl extends BuildServiceImpl implements GwtWarBuildService {

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
            final Instance<ConfigExecutor> configExecutors,
            final RepositoryService repositoryService, final Event<AppReady> appReadyEvent,
            final SourceRegistry sourceRegistry,
            final RuntimeRegistry runtimeRegistry, final PipelineRegistry pipelineRegistry,
            final CDIPipelineEventListener pipelineEventListener ) {
        super( pomService, m2RepoService, projectService, repositoryResolver, projectRepositoriesService, cache, handlers );
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
        final Iterator<ConfigExecutor> iterator = configExecutors.iterator();
        final Collection<ConfigExecutor> configs = new ArrayList<>();
        while ( iterator.hasNext() ) {
            final ConfigExecutor configExecutor = iterator.next();
            configs.add( configExecutor );
        }
        executor = new PipelineExecutor( configs );
    }

    @Override
    public BuildResults buildAndDeploy( final Project project ) {
        return buildAndDeploy( project, false, DeploymentMode.VALIDATED );
    }

    @Override
    public BuildResults buildAndDeploy( final Project project, final DeploymentMode mode ) {
        return buildAndDeploy( project, false, mode );
    }

    @Override
    public BuildResults buildAndDeploy( final Project project, final boolean suppressHandlers ) {
        return buildAndDeploy( project, suppressHandlers, DeploymentMode.VALIDATED );
    }

    @Override
    public BuildResults buildAndDeploy( final Project project, final boolean suppressHandlers, final DeploymentMode mode ) {
        return buildAndDeployWithPipeline( project );
    }

    @Override
    public BuildResults buildAndDeployDevMode( final Project project ) {
        return buildAndDeploySDMWithPipeline(project);
    }

    public BuildResults buildAndDeployWithPipeline( final Project project ) {
        final BuildResults results = new BuildResults( project.getPom().getGav() );
        final Path rootPath = project.getRootPath();
        final Path repoPath = PathFactory.newPath( "repo", rootPath.toURI().substring( 0, rootPath.toURI().indexOf( rootPath.getFileName() ) ) );
        final Repository repository = repositoryService.getRepository( repoPath );

        final Pipeline pipe = pipelineRegistry.getPipelineByName( "wildfly pipeline" );

        final Input wildflyInput = new Input() {
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

    public BuildResults buildAndDeploySDMWithPipeline( final Project project ) {
        final BuildResults results = new BuildResults( project.getPom().getGav() );
        final Path rootPath = project.getRootPath();
        final Path repoPath = PathFactory.newPath( "repo", rootPath.toURI().substring( 0, rootPath.toURI().indexOf( rootPath.getFileName() ) ) );
        final Repository repository = repositoryService.getRepository( repoPath );


        final Pipeline pipe = pipelineRegistry.getPipelineByName( "wildfly sdm pipeline" );

        final Input wildflyInput = new Input() {
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
        final org.guvnor.ala.build.Project projectByName = sourceRegistry.getProjectByName(project.getProjectName());

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

    public void buildFinished( @Observes final AfterPipelineExecutionEvent e ) {
        final List<org.guvnor.ala.runtime.Runtime> allRuntimes = runtimeRegistry.getRuntimes( 0, 10, "", true );
        final Runtime get = allRuntimes.get( 0 );
        final String url = "http://" + get.getEndpoint().getHost() + ":" + get.getEndpoint().getPort() + "/" + get.getEndpoint().getContext();
        appReadyEvent.fire( new AppReady( url ) );
    }

}