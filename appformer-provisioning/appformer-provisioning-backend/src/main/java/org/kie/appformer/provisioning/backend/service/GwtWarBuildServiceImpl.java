/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.provisioning.backend.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.kie.appformer.ala.wildfly.executor.AppFormerProvisioningHelper;
import org.kie.appformer.provisioning.service.TestConnectionResult;
import org.kie.appformer.provisioning.shared.AppReady;
import org.kie.appformer.provisioning.service.GwtWarBuildService;
import org.kie.workbench.common.services.backend.builder.BuildServiceImpl;
import org.kie.workbench.common.services.backend.builder.LRUBuilderCache;
import org.kie.workbench.common.services.shared.project.KieProjectService;
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

    private AppFormerProvisioningHelper provisioningHelper;

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
                                   final Instance< PostBuildHandler > handlers,
                                   final Instance< ConfigExecutor > configExecutors,
                                   final RepositoryService repositoryService, final Event< AppReady > appReadyEvent,
                                   final SourceRegistry sourceRegistry,
                                   final RuntimeRegistry runtimeRegistry, final PipelineRegistry pipelineRegistry,
                                   final CDIPipelineEventListener pipelineEventListener,
                                   final AppFormerProvisioningHelper provisioningHelper ) {
        super( pomService, m2RepoService, projectService, repositoryResolver, projectRepositoriesService, cache, handlers );
        this.configExecutors = configExecutors;
        this.repositoryService = repositoryService;
        this.appReadyEvent = appReadyEvent;
        this.runtimeRegistry = runtimeRegistry;
        this.pipelineRegistry = pipelineRegistry;
        this.sourceRegistry = sourceRegistry;
        this.pipelineEventListener = pipelineEventListener;
        this.provisioningHelper = provisioningHelper;
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

    @Override
    public BuildResults buildAndDeployProvisioningMode( Project project, Map<String, String> params ) {
        return buildAndDeployProvisioningWithPipeline( project, params );
    }

    @Override
    public TestConnectionResult testConnection( String host,
                                                int port,
                                                int managementPort,
                                                String managementUser,
                                                String managementPassword ) {
        TestConnectionResult result = new TestConnectionResult(  );
        try {
            String message = provisioningHelper.testManagementConnection( host,
                    managementPort, managementUser, managementPassword, "ManagementRealm" );
            result.setManagementConnectionError( false );
            result.setManagementConnectionMessage( message );
        } catch ( Exception e ) {
            result.setManagementConnectionError( true );
            result.setManagementConnectionMessage( e.getMessage() );
        }
        return result;
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
                put( "provider-name", "local" );
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
                put( "provider-name", "local" );
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

    public BuildResults buildAndDeployProvisioningWithPipeline( final Project project, Map<String, String> params ) {
        final BuildResults results = new BuildResults( project.getPom().getGav() );
        final Path rootPath = project.getRootPath();
        final Path repoPath = PathFactory.newPath( "repo", rootPath.toURI().substring( 0, rootPath.toURI().indexOf( rootPath.getFileName() ) ) );
        final Repository repository = repositoryService.getRepository( repoPath );

        if ( params.get( "provider-name" ) == null ) {
            params.put( "provider-name", "wildfly-" + UUID.randomUUID().toString() );
        } else if ( "local".equals( params.get( "provider-name" ) ) ) {
            WildflyServerOptions localServer = getLocalServerOptions();
            params.put( "wildfly-user", localServer.getUser() );
            params.put( "wildfly-password", localServer.getPassword() );
            params.put( "host", localServer.getHost() );
            params.put( "port", String.valueOf( localServer.getPort() ) );
            params.put( "management-port", String.valueOf( localServer.getManagementPort() ) );
        }

        final Pipeline pipe = pipelineRegistry.getPipelineByName( "wildfly pipeline" );

        final Input wildflyInput = new Input() {
            {
                put( "repo-name", repository.getAlias() );
                put( "branch", repository.getDefaultBranch() );
                put( "project-dir", project.getProjectName() );
                put( "provider-name", params.get( "provider-name" ) );
                put( "wildfly-user", params.get( "wildfly-user" ) );
                put( "wildfly-password", params.get( "wildfly-password" ) );
                put( "host", params.get( "host" ) );
                put( "port", params.get( "port" ) );
                put( "management-port", params.get( "management-port" ) );
                put( "kie-data-source", params.get( "kie-data-source" ) );
            }
        };
        executor.execute( wildflyInput, pipe, System.out::println, pipelineEventListener );

        return results;
    }

    @Override
    public IncrementalBuildResults addPackageResource( final Path resource ) {
        if ( isNotAppFormerGeneratedJavaSource( resource ) ) {
            return super.addPackageResource( resource );
        } else {
            return new IncrementalBuildResults();
        }
    }

    @Override
    public IncrementalBuildResults updatePackageResource( final Path resource ) {
        if ( isNotAppFormerGeneratedJavaSource( resource ) ) {
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
                .filter( e -> isNotAppFormerGeneratedJavaSource( e.getKey() ) )
                .collect( Collectors.toMap( e -> e.getKey(), e -> e.getValue() ) );

        if ( nonGeneratedFileChanges.isEmpty() ) {
            return new IncrementalBuildResults();
        } else {
            return super.applyBatchResourceChanges( project, nonGeneratedFileChanges );
        }
    }

    @Override
    public IncrementalBuildResults deletePackageResource( final Path resource ) {
        if ( isNotAppFormerGeneratedJavaSource( resource ) ) {
            return super.deletePackageResource( resource );
        } else {
            return new IncrementalBuildResults();
        }
    }

    private boolean isNotAppFormerGeneratedJavaSource( final Path resource ) {
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


    private WildflyServerOptions getLocalServerOptions() {
        WildflyServerOptions options = new WildflyServerOptions( "localhost",
                8080, 9990, "testadmin", "testadmin", "ManagementRealm" );

        String value;

        if ( ( value = System.getProperties().getProperty( "jboss.http.port" ) ) != null ) {
            try {
                options.setPort( Integer.parseInt( value ) );
            } catch ( Exception e ) {
                //default port has already been set, ignore this uncommon error.
            }
        }

        if ( ( value = System.getProperties().getProperty( "jboss.management.http.port" ) ) != null ) {
            try {
                options.setManagementPort( Integer.parseInt( value ) );
            } catch ( Exception e ) {
                //default port has already been set, ignore this uncommon error.
            }
        }
        return options;
    }

    private class WildflyServerOptions {

        private String host;

        private int port;

        private int managementPort;

        private String user;

        private String password;

        private String realm;

        public WildflyServerOptions( String host,
                                     int port,
                                     int managementPort,
                                     String user,
                                     String password,
                                     String realm ) {
            this.host = host;
            this.port = port;
            this.managementPort = managementPort;
            this.user = user;
            this.password = password;
            this.realm = realm;
        }

        public String getHost( ) {
            return host;
        }

        public void setHost( String host ) {
            this.host = host;
        }

        public int getPort( ) {
            return port;
        }

        public void setPort( int port ) {
            this.port = port;
        }

        public int getManagementPort( ) {
            return managementPort;
        }

        public void setManagementPort( int managementPort ) {
            this.managementPort = managementPort;
        }

        public String getUser( ) {
            return user;
        }

        public void setUser( String user ) {
            this.user = user;
        }

        public String getPassword( ) {
            return password;
        }

        public void setPassword( String password ) {
            this.password = password;
        }

        public String getRealm( ) {
            return realm;
        }

        public void setRealm( String realm ) {
            this.realm = realm;
        }
    }

}