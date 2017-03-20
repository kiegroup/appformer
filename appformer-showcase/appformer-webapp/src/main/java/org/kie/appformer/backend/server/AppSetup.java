/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.backend.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import org.guvnor.ala.build.maven.config.MavenBuildConfig;
import org.guvnor.ala.build.maven.config.MavenBuildExecConfig;
import org.guvnor.ala.build.maven.config.MavenProjectConfig;
import org.guvnor.ala.build.maven.config.gwt.GWTCodeServerMavenExecConfig;
import org.guvnor.ala.config.BinaryConfig;
import org.guvnor.ala.config.BuildConfig;
import org.guvnor.ala.config.ProjectConfig;
import org.guvnor.ala.config.ProviderConfig;
import org.guvnor.ala.config.RuntimeConfig;
import org.guvnor.ala.config.SourceConfig;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.PipelineFactory;
import org.guvnor.ala.pipeline.Stage;
import static org.guvnor.ala.pipeline.StageUtil.config;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.ala.registry.RuntimeRegistry;
import org.guvnor.ala.source.git.config.GitConfig;
import org.guvnor.ala.wildfly.config.WildflyProviderConfig;
import org.guvnor.ala.wildfly.config.impl.ContextAwareWildflyRuntimeExecConfig;
import org.kie.appformer.ala.wildfly.config.AppFormerWildflyRuntimeExecConfig;

import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.server.config.ConfigGroup;
import org.guvnor.structure.server.config.ConfigType;
import org.guvnor.structure.server.config.ConfigurationFactory;
import org.guvnor.structure.server.config.ConfigurationService;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.kie.workbench.screens.workbench.backend.BaseAppSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.services.cdi.ApplicationStarted;
import org.uberfire.commons.services.cdi.Startup;
import org.uberfire.commons.services.cdi.StartupType;
import org.uberfire.io.IOService;

//This is a temporary solution when running in PROD-MODE as /webapp/.niogit/system.git folder
//is not deployed to the Application Servers /bin folder. This will be remedied when an
//installer is written to create the system.git repository in the correct location.
@Startup(StartupType.BOOTSTRAP)
@ApplicationScoped
public class AppSetup extends BaseAppSetup {

    private static final Logger logger = LoggerFactory.getLogger( AppSetup.class );

    // default repository section - start
    private static final String OU_NAME = "demo";
    private static final String OU_OWNER = "demo@demo.org";
    // default repository section - end

    private Event<ApplicationStarted> applicationStartedEvent;

    private RuntimeRegistry runtimeRegistry;

    private PipelineRegistry pipelineRegistry;


    protected AppSetup() {
    }

    @Inject
    public AppSetup( @Named("ioStrategy") final IOService ioService,
                     final RepositoryService repositoryService,
                     final OrganizationalUnitService organizationalUnitService,
                     final KieProjectService projectService,
                     final ConfigurationService configurationService,
                     final ConfigurationFactory configurationFactory,
                     final Event<ApplicationStarted> applicationStartedEvent,
                     final RuntimeRegistry runtimeRegistry,
                     final PipelineRegistry pipelineRegistry ) {
        super( ioService, repositoryService, organizationalUnitService, projectService, configurationService, configurationFactory );
        this.applicationStartedEvent = applicationStartedEvent;
        this.runtimeRegistry = runtimeRegistry;
        this.pipelineRegistry = pipelineRegistry;

    }



    @PostConstruct
    public void init() {
        try {
            configurationService.startBatch();
            final String exampleRepositoriesRoot = System.getProperty( "org.kie.example.repositories" );
            if ( !( exampleRepositoriesRoot == null || "".equalsIgnoreCase( exampleRepositoriesRoot ) ) ) {
                loadExampleRepositories( exampleRepositoriesRoot,
                                         OU_NAME,
                                         OU_OWNER,
                                         GIT_SCHEME );

            } else if ( "true".equalsIgnoreCase( System.getProperty( "org.kie.example" ) ) ) {

                final Repository exampleRepo = createRepository( "repository1",
                                                           "git",
                                                           null,
                                                           "",
                                                           "" );
                createOU( exampleRepo,
                          "example",
                          "" );
                createProject( exampleRepo,
                               "org.kie.example",
                               "project1",
                               "1.0.0-SNAPSHOT" );
            }

            //Define mandatory properties
            setupConfigurationGroup( ConfigType.GLOBAL,
                                     GLOBAL_SETTINGS,
                                     getGlobalConfiguration() );

            // notify components that bootstrap is completed to start post setups
            applicationStartedEvent.fire( new ApplicationStarted() );
        } catch ( final Exception e ) {
            logger.error( "Error during update config", e );
            throw new RuntimeException( e );
        } finally {
            configurationService.endBatch();
        }
        initPipelines();
    }

    public void initPipelines(){
        // Create Wildfly Pipeline Configuration
        final Stage<Input, SourceConfig> sourceConfig = config( "Git Source", (Function<Input , SourceConfig>) (s) -> {

            logger.info("Executing function STAGE: Git Source con input: " + s );
            return new GitConfig(){};
        } );
        final Stage<SourceConfig, ProjectConfig> projectConfig = config( "Maven Project", (Function<SourceConfig, ProjectConfig>) (s) -> {
            logger.info("Executing function STAGE: Maven Project con input: " + s );
            return new MavenProjectConfig() {};
        } );

        final Stage<ProjectConfig, BuildConfig> buildConfig = config( "Maven Build Config", (Function<ProjectConfig, BuildConfig>) (s) -> {
            logger.info("Executing function STAGE: Maven Build Config con input: " + s );
            return new MavenBuildConfig() {
                @Override
                public List< String > getGoals( ) {
                    final List< String > result = new ArrayList<>( );
                    result.add( "clean" );
                    result.add( "package" );
                    return result;
                }

                @Override
                public Properties getProperties( ) {
                    final Properties result = new Properties( );
                    result.setProperty( "failIfNoTests", "false" );
                    result.setProperty( "gwt.compiler.skip", "false" );
                    return result;
                }
            };
        } );
        final Stage<ProjectConfig, BuildConfig> buildSDMConfig = config( "Maven Build Config", (Function<ProjectConfig, BuildConfig>) (s) -> {
            logger.info("Executing function STAGE: Maven Build SDM Config con input: " + s );
        return new MavenBuildConfig() {
            @Override
            public List< String > getGoals( ) {
                final List< String > result = new ArrayList<>( );
                result.add( "package" );
                return result;
            }

            @Override
            public Properties getProperties( ) {
                final Properties result = new Properties( );
                result.setProperty( "failIfNoTests", "false" );
                result.setProperty( "gwt.compiler.skip", "true" );
                return result;
            }
            };
        } );
        final Stage<BuildConfig, BuildConfig> codeServerExec = config( "Start Code Server", (Function<BuildConfig, BuildConfig>) (s) -> {
            logger.info("Executing function STAGE: Start Code Server: " + s );
            return new GWTCodeServerMavenExecConfig() {}; } );

        final Stage<BuildConfig, BinaryConfig> buildExec = config( "Maven Build", (Function<BuildConfig, BinaryConfig>) (s) -> {
            logger.info("Executing function STAGE: Maven Build con input: " + s );
            return new MavenBuildExecConfig() {}; } );


        final Stage<BinaryConfig, ProviderConfig> providerConfig = config( "Wildfly Provider Config", (Function<BinaryConfig, ProviderConfig>) (s) -> {
            logger.info("Executing function STAGE: Wildfly Provider Config con input: " + s );
            return new WildflyProviderConfig() {
                @Override
                public String getName( ) {
                    return "${input.provider-name}";
                }
            }; } );

        final Stage<ProviderConfig, RuntimeConfig> runtimeExec = config( "Wildfly Runtime Exec", (Function<ProviderConfig, RuntimeConfig>) (s) ->  {
            logger.info("Executing function STAGE: Widlfly Runtime Exec: " + s );
            return new ContextAwareWildflyRuntimeExecConfig(); }
        );

        final Stage<ProviderConfig, RuntimeConfig> appFormerRuntimeExec = config( "AppFormer Wildfly Runtime Exec", (Function<ProviderConfig, RuntimeConfig>) (s) ->  {
            logger.info("Executing function STAGE: AppFormer Widlfly Runtime Exec: " + s );
            return new AppFormerWildflyRuntimeExecConfig() {
                @Override
                public String getRedeployStrategy( ) {
                    return "auto";
                }
            };
            }
        );

        final Pipeline wildflyPipeline = PipelineFactory
                .startFrom( sourceConfig )
                .andThen( projectConfig )
                .andThen( buildConfig )
                .andThen( buildExec )
                .andThen( providerConfig )
                .andThen( appFormerRuntimeExec ).buildAs( "wildfly pipeline" );
        //Registering the Wildfly Pipeline to be available to the whole workbench
        pipelineRegistry.registerPipeline(wildflyPipeline);

        final Pipeline wildflySDMPipeline = PipelineFactory
                .startFrom( sourceConfig )
                .andThen( projectConfig )
                .andThen( buildSDMConfig )
                .andThen( codeServerExec )
                .andThen( buildExec )
                .andThen( providerConfig )
                .andThen( runtimeExec ).buildAs( "wildfly sdm pipeline" );
        //Registering the Wildfly Pipeline to be available to the whole workbench
        pipelineRegistry.registerPipeline(wildflySDMPipeline);


    }

    private ConfigGroup getGlobalConfiguration() {
        //Global Configurations used by many of Drools Workbench editors
        final ConfigGroup group = configurationFactory.newConfigGroup( ConfigType.GLOBAL,
                                                                       GLOBAL_SETTINGS,
                                                                       "" );
        group.addConfigItem( configurationFactory.newConfigItem( "drools.dateformat",
                                                                 "dd-MMM-yyyy" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "drools.datetimeformat",
                                                                 "dd-MMM-yyyy hh:mm:ss" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "drools.defaultlanguage",
                                                                 "en" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "drools.defaultcountry",
                                                                 "US" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "build.enable-incremental",
                                                                 "true" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "rule-modeller-onlyShowDSLStatements",
                                                                 "false" ) );
        group.addConfigItem( configurationFactory.newConfigItem( "support.runtime.deploy",
                                                                 "false" ) );
        return group;
    }
}