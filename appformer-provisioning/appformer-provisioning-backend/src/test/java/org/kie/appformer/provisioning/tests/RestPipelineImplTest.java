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

package org.kie.appformer.provisioning.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.arquillian.cube.CubeController;
import org.arquillian.cube.HostIp;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.guvnor.ala.build.Project;

import org.guvnor.ala.build.maven.config.MavenBuildConfig;
import org.guvnor.ala.build.maven.config.MavenBuildExecConfig;
import org.guvnor.ala.build.maven.config.MavenProjectConfig;
import org.guvnor.ala.build.maven.config.gwt.GWTCodeServerMavenExecConfig;
import org.guvnor.ala.build.maven.executor.MavenBuildConfigExecutor;
import org.guvnor.ala.build.maven.executor.MavenBuildExecConfigExecutor;
import org.guvnor.ala.build.maven.executor.MavenProjectConfigExecutor;
import org.guvnor.ala.build.maven.executor.gwt.GWTCodeServerMavenExecConfigExecutor;
import org.guvnor.ala.build.maven.executor.gwt.GWTCodeServerPortLeaserImpl;
import org.guvnor.ala.config.BinaryConfig;
import org.guvnor.ala.config.BuildConfig;
import org.guvnor.ala.config.ProjectConfig;

import org.guvnor.ala.config.ProviderConfig;
import org.guvnor.ala.config.RuntimeConfig;
import org.guvnor.ala.config.SourceConfig;
import org.guvnor.ala.registry.local.InMemoryRuntimeRegistry;
import org.guvnor.ala.wildfly.executor.WildflyProviderConfigExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.PipelineFactory;
import org.guvnor.ala.pipeline.Stage;
import static org.guvnor.ala.pipeline.StageUtil.config;
import org.guvnor.ala.pipeline.execution.PipelineExecutor;
import org.guvnor.ala.registry.BuildRegistry;
import org.guvnor.ala.registry.SourceRegistry;
import org.guvnor.ala.registry.local.InMemoryBuildRegistry;
import org.guvnor.ala.registry.local.InMemorySourceRegistry;
import org.guvnor.ala.source.Repository;
import org.guvnor.ala.source.Source;
import org.guvnor.ala.source.git.GitRepository;
import org.guvnor.ala.source.git.UFLocal;
import org.guvnor.ala.source.git.config.GitConfig;
import org.guvnor.ala.source.git.executor.GitConfigExecutor;
import org.guvnor.ala.wildfly.access.WildflyAccessInterface;
import org.guvnor.ala.wildfly.access.impl.WildflyAccessInterfaceImpl;
import org.guvnor.ala.wildfly.config.WildflyProviderConfig;
import org.guvnor.ala.wildfly.config.impl.ContextAwareWildflyRuntimeExecConfig;
import org.guvnor.ala.wildfly.executor.WildflyRuntimeExecExecutor;
import org.guvnor.ala.wildfly.model.WildflyRuntime;
import org.guvnor.ala.wildfly.service.WildflyRuntimeManager;


import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * Test that shows how run Pipeline to provision an app to wildfly
 */
@Ignore
@RunWith( ArquillianConditionalRunner.class )
public class RestPipelineImplTest {

    private static File tempPath;

    @HostIp
    private String ip;

    @ArquillianResource
    private CubeController cc;

    private static final String CONTAINER = "swarm";

    @BeforeClass
    public static void setUp() throws IOException {
        tempPath = Files.createTempDirectory( "aaa" ).toFile();
    }

    @AfterClass
    public static void tearDown() {
        FileUtils.deleteQuietly( tempPath );
    }

    @Test
    @InSequence( 1 )
    public void shouldBeAbleToCreateAndStartTest() {
        cc.create( CONTAINER );
        cc.start( CONTAINER );
    }

    @Test
    @InSequence( 2 )
    public void testPipelineForDeployingToWildfly() {

        final SourceRegistry sourceRegistry = new InMemorySourceRegistry();
        final BuildRegistry buildRegistry = new InMemoryBuildRegistry();
        final InMemoryRuntimeRegistry runtimeRegistry = new InMemoryRuntimeRegistry();
        final WildflyAccessInterface wildflyAccessInterface = new WildflyAccessInterfaceImpl();

        final Stage<Input, SourceConfig> sourceConfig = config( "Git Source", (Function<Input, SourceConfig>) (s) -> new GitConfig() {
        } );
        final Stage<SourceConfig, ProjectConfig> projectConfig = config( "Maven Project", (Function<SourceConfig, ProjectConfig>) (s) -> new MavenProjectConfig() {
        } );
        final Stage<ProjectConfig, BuildConfig> buildConfig = config( "Maven Build Config", (Function<ProjectConfig, BuildConfig>) (s) -> new MavenBuildConfig() {
        } );

        final Stage<BuildConfig, BinaryConfig> buildExec = config( "Maven Build", (Function<BuildConfig, BinaryConfig>) (s) -> new MavenBuildExecConfig() {
        } );
        final Stage<BinaryConfig, ProviderConfig> providerConfig = config( "Wildfly Provider Config", (Function<BinaryConfig, ProviderConfig>) (s) -> new WildflyProviderConfig() {
        } );

        final Stage<ProviderConfig, RuntimeConfig> runtimeExec = config( "Wildfly Runtime Exec", (s) -> new ContextAwareWildflyRuntimeExecConfig() );

        final Pipeline pipe = PipelineFactory
                .startFrom( sourceConfig )
                .andThen( projectConfig )
                .andThen( buildConfig )
                .andThen( buildExec )
                .andThen( providerConfig )
                .andThen( runtimeExec ).buildAs( "my pipe" );
        final WildflyRuntimeExecExecutor wildflyRuntimeExecExecutor = new WildflyRuntimeExecExecutor( runtimeRegistry, wildflyAccessInterface );
        final PipelineExecutor executor = new PipelineExecutor( asList( new GitConfigExecutor( sourceRegistry ),
                new MavenProjectConfigExecutor( sourceRegistry ),
                new MavenBuildConfigExecutor(),
                new MavenBuildExecConfigExecutor( buildRegistry ),
                new WildflyProviderConfigExecutor( runtimeRegistry ),
                wildflyRuntimeExecExecutor ) );

        executor.execute( new Input() {
            {
                put( "repo-name", "drools-workshop-deployment" );
                put( "create-repo", "true" );
                put( "branch", "master" );
                put( "out-dir", tempPath.getAbsolutePath() );
                put( "origin", "https://github.com/salaboy/drools-workshop" );
                put( "project-dir", "drools-webapp-example" );
                put( "wildfly-user", "admin" );
                put( "wildfly-password", "Admin#70365" );
                put( "host", ip );
                put( "port", "8080" );
                put( "management-port", "9990" );

            }
        }, pipe, System.out::println );

        List<org.guvnor.ala.runtime.Runtime> allRuntimes = runtimeRegistry.getRuntimes( 0, 10, "", true );

        assertEquals( 1, allRuntimes.size() );

        org.guvnor.ala.runtime.Runtime runtime = allRuntimes.get( 0 );

        assertTrue( runtime instanceof WildflyRuntime );

        WildflyRuntime wildflyRuntime = ( WildflyRuntime ) runtime;

        final WildflyRuntimeManager runtimeManager = new WildflyRuntimeManager( runtimeRegistry, wildflyAccessInterface );

        runtimeManager.start( wildflyRuntime );

        allRuntimes = runtimeRegistry.getRuntimes( 0, 10, "", true );

        assertEquals( 1, allRuntimes.size() );

        runtime = allRuntimes.get( 0 );

        assertTrue( runtime instanceof WildflyRuntime );

        wildflyRuntime = ( WildflyRuntime ) runtime;

        assertEquals( "Running", wildflyRuntime.getState().getState() );
        runtimeManager.stop( wildflyRuntime );

        allRuntimes = runtimeRegistry.getRuntimes( 0, 10, "", true );

        assertEquals( 1, allRuntimes.size() );

        runtime = allRuntimes.get( 0 );

        assertTrue( runtime instanceof WildflyRuntime );

        wildflyRuntime = ( WildflyRuntime ) runtime;

        assertEquals( "NA", wildflyRuntime.getState().getState() );

        wildflyRuntimeExecExecutor.destroy( wildflyRuntime );

        wildflyAccessInterface.dispose();

    }

    @Test
    @InSequence(3)
    public void testPipelineForDeployingToWildflyWithCodeServer() {

        final SourceRegistry sourceRegistry = new InMemorySourceRegistry();
        final BuildRegistry buildRegistry = new InMemoryBuildRegistry();
        final InMemoryRuntimeRegistry runtimeRegistry = new InMemoryRuntimeRegistry();
        final WildflyAccessInterface wildflyAccessInterface = new WildflyAccessInterfaceImpl();
        final GWTCodeServerPortLeaserImpl leaser = new GWTCodeServerPortLeaserImpl();

        final Stage<Input, SourceConfig> sourceConfig = config("Git Source", (Function<Input, SourceConfig>) (s) -> new GitConfig() {
        });
        final Stage<SourceConfig, ProjectConfig> projectConfig = config("Maven Project", (Function<SourceConfig, ProjectConfig>) (s) -> new MavenProjectConfig() {
        });
        final Stage<ProjectConfig, BuildConfig> buildConfig = config("Maven Build Config", (Function<ProjectConfig, BuildConfig>) (s) -> new MavenBuildConfig() {
            @Override
            public List<String> getGoals() {
                final List<String> result = new ArrayList<>();
                result.add("package");

                return result;
            }

            @Override
            public Properties getProperties() {
                final Properties result = new Properties();
                result.setProperty("failIfNoTests", "false");
                result.setProperty("gwt.compiler.skip", "true");
                return result;
            }

        });

        final Stage<BuildConfig, BuildConfig> codeServerExec = config("Start Code Server", (Function<BuildConfig, BuildConfig>) (s) -> new GWTCodeServerMavenExecConfig() {

        });

        final Stage<BuildConfig, BinaryConfig> buildExec = config("Maven Build", (Function<BuildConfig, BinaryConfig>) (s) -> new MavenBuildExecConfig() {
        });

        final Stage<BinaryConfig, ProviderConfig> providerConfig = config("Wildfly Provider Config", (Function<BinaryConfig, ProviderConfig>) (s) -> new WildflyProviderConfig() {
        });

        final Stage<ProviderConfig, RuntimeConfig> runtimeExec = config("Wildfly Runtime Exec", (s) -> new ContextAwareWildflyRuntimeExecConfig());


        final Pipeline pipeCodeServer = PipelineFactory
                .startFrom(sourceConfig)
                .andThen(projectConfig)
                .andThen(buildConfig)
                .andThen(codeServerExec)
                .andThen(buildExec)
                .andThen(providerConfig)
                .andThen(runtimeExec).buildAs("my pipe");
        final WildflyRuntimeExecExecutor wildflyRuntimeExecExecutor = new WildflyRuntimeExecExecutor(runtimeRegistry, wildflyAccessInterface);
        final PipelineExecutor executor = new PipelineExecutor(asList(new GitConfigExecutor(sourceRegistry),
                new MavenProjectConfigExecutor(sourceRegistry),
                new MavenBuildConfigExecutor(),
                new MavenBuildExecConfigExecutor(buildRegistry),
                new GWTCodeServerMavenExecConfigExecutor(leaser),
                new WildflyProviderConfigExecutor(runtimeRegistry),
                wildflyRuntimeExecExecutor));

        executor.execute(new Input() {
            {
                put("repo-name", "ls-users-new");
                put("create-repo", "true");
                put("branch", "master");
                put("out-dir", tempPath.getAbsolutePath());
                put("origin", "https://github.com/mbarkley/app-former-playground");
                put("project-dir", "users-new");
                put("wildfly-user", "admin");
                put("wildfly-password", "Admin#70365");
                put("bindAddress", "localhost");
                put("host", ip);
                put("port", "8080");
                put("management-port", "9990");

            }
        }, pipeCodeServer, System.out::println);

        List<org.guvnor.ala.runtime.Runtime> allRuntimes = runtimeRegistry.getRuntimes(0, 10, "", true);

        assertEquals(1, allRuntimes.size());
        final List<Repository> allRepositories = sourceRegistry.getAllRepositories();
        assertEquals(1, allRepositories.size());
        final UFLocal local = new UFLocal();
        final GitRepository repository = (GitRepository) local.getRepository("ls-users-new", Collections.emptyMap());
        final Source source = repository.getSource("master");
        assertNotNull(source);


        final Input wildflyInput = new Input() {

            {
                put("repo-name", "ls-users-new");
                put("branch", "master");
                put("project-dir", "users-new");
                put("wildfly-user", "admin");
                put("wildfly-password", "Admin#70365");
                put("host", ip);
                put("port", "8080");
                put("management-port", "9990");

            }

        };

        final Project projectByName = sourceRegistry.getProjectByName("users-new");
        assertNotNull(projectByName);
        final String tempDir = projectByName.getTempDir();

        wildflyInput.put("project-temp-dir", tempDir);

        executor.execute(wildflyInput, pipeCodeServer, System.out::println);

        allRuntimes = runtimeRegistry.getRuntimes(0, 10, "", true);

        assertEquals(1, allRuntimes.size());


        wildflyAccessInterface.dispose();

    }
    @Test
    @InSequence( 4 )
    public void shouldBeAbleToStopAndDestroyTest() {
        cc.stop( CONTAINER );
        cc.destroy( CONTAINER );
    }

}
