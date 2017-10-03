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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.arquillian.cube.CubeController;
import org.arquillian.cube.HostIp;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.guvnor.ala.build.maven.config.MavenBuildConfig;
import org.guvnor.ala.build.maven.config.MavenBuildExecConfig;
import org.guvnor.ala.build.maven.config.MavenProjectConfig;
import org.guvnor.ala.build.maven.config.gwt.GWTCodeServerMavenExecConfig;
import org.guvnor.ala.build.maven.executor.MavenBuildConfigExecutor;
import org.guvnor.ala.build.maven.executor.MavenBuildExecConfigExecutor;
import org.guvnor.ala.build.maven.executor.MavenProjectConfigExecutor;
import org.guvnor.ala.build.maven.executor.gwt.GWTCodeServerMavenExecConfigExecutor;
import org.guvnor.ala.build.maven.executor.gwt.GWTCodeServerPortLeaserImpl;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.PipelineConfigStage;
import org.guvnor.ala.pipeline.PipelineFactory;
import org.guvnor.ala.pipeline.execution.PipelineExecutor;
import org.guvnor.ala.registry.BuildRegistry;
import org.guvnor.ala.registry.SourceRegistry;
import org.guvnor.ala.registry.inmemory.InMemoryBuildRegistry;
import org.guvnor.ala.registry.inmemory.InMemoryRuntimeRegistry;
import org.guvnor.ala.registry.inmemory.InMemorySourceRegistry;
import org.guvnor.ala.runtime.RuntimeState;
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
import org.guvnor.ala.wildfly.executor.WildflyProviderConfigExecutor;
import org.guvnor.ala.wildfly.executor.WildflyRuntimeExecExecutor;
import org.guvnor.ala.wildfly.model.WildflyRuntime;
import org.guvnor.ala.wildfly.service.WildflyRuntimeManager;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 * Test that shows how run Pipeline to provision an app to wildfly
 */
@Ignore
@RunWith(ArquillianConditionalRunner.class)
public class RestPipelineImplTest {

    private static File tempPath;

    @HostIp
    private String ip;

    @ArquillianResource
    private CubeController cc;

    private static final String CONTAINER = "swarm";

    @BeforeClass
    public static void setUp() throws IOException {
        tempPath = Files.createTempDirectory("aaa").toFile();
    }

    @AfterClass
    public static void tearDown() {
        FileUtils.deleteQuietly(tempPath);
    }

    @Test
    @InSequence(1)
    public void shouldBeAbleToCreateAndStartTest() {
        cc.create(CONTAINER);
        cc.start(CONTAINER);
    }

    @Test
    @InSequence(2)
    public void testPipelineForDeployingToWildfly() {

        final SourceRegistry sourceRegistry = new InMemorySourceRegistry();
        final BuildRegistry buildRegistry = new InMemoryBuildRegistry();
        final InMemoryRuntimeRegistry runtimeRegistry = new InMemoryRuntimeRegistry();
        final WildflyAccessInterface wildflyAccessInterface = new WildflyAccessInterfaceImpl();

        final PipelineConfigStage sourceConfig = new PipelineConfigStage("Git Source",
                                                                         new GitConfig() {
                                                                         });
        final PipelineConfigStage projectConfig = new PipelineConfigStage("Maven Project",
                                                                          new MavenProjectConfig() {
                                                                          });
        final PipelineConfigStage buildConfig = new PipelineConfigStage("Maven Build Config",
                                                                        new MavenBuildConfig() {
                                                                        });

        final PipelineConfigStage buildExec = new PipelineConfigStage("Maven Build",
                                                                      new MavenBuildExecConfig() {
                                                                      });
        final PipelineConfigStage providerConfig = new PipelineConfigStage("Wildfly Provider Config",
                                                                           new WildflyProviderConfig() {
                                                                           });

        final PipelineConfigStage runtimeExec = new PipelineConfigStage("Wildfly Runtime Exec",
                                                                        new ContextAwareWildflyRuntimeExecConfig());

        final Pipeline pipe = PipelineFactory
                .newBuilder()
                .addConfigStage(sourceConfig)
                .addConfigStage(projectConfig)
                .addConfigStage(buildConfig)
                .addConfigStage(buildExec)
                .addConfigStage(providerConfig)
                .addConfigStage(runtimeExec).buildAs("my pipe");
        final WildflyRuntimeExecExecutor wildflyRuntimeExecExecutor = new WildflyRuntimeExecExecutor(runtimeRegistry,
                                                                                                     wildflyAccessInterface);
        final PipelineExecutor executor = new PipelineExecutor(asList(new GitConfigExecutor(sourceRegistry),
                                                                      new MavenProjectConfigExecutor(sourceRegistry),
                                                                      new MavenBuildConfigExecutor(),
                                                                      new MavenBuildExecConfigExecutor(buildRegistry),
                                                                      new WildflyProviderConfigExecutor(runtimeRegistry),
                                                                      wildflyRuntimeExecExecutor));

        executor.execute(new Input() {
                             {
                                 put("repo-name",
                                     "drools-workshop-deployment");
                                 put("create-repo",
                                     "true");
                                 put("branch",
                                     "master");
                                 put("out-dir",
                                     tempPath.getAbsolutePath());
                                 put("origin",
                                     "https://github.com/salaboy/drools-workshop");
                                 put("project-dir",
                                     "drools-webapp-example");
                                 put("wildfly-user",
                                     "admin");
                                 put("wildfly-password",
                                     "Admin#70365");
                                 put("host",
                                     ip);
                                 put("port",
                                     "8080");
                                 put("management-port",
                                     "9990");
                             }
                         },
                         pipe,
                         System.out::println);

        List<org.guvnor.ala.runtime.Runtime> allRuntimes = runtimeRegistry.getRuntimes(0,
                                                                                       10,
                                                                                       "",
                                                                                       true);

        assertEquals(1,
                     allRuntimes.size());

        org.guvnor.ala.runtime.Runtime runtime = allRuntimes.get(0);

        assertTrue(runtime instanceof WildflyRuntime);

        WildflyRuntime wildflyRuntime = (WildflyRuntime) runtime;

        final WildflyRuntimeManager runtimeManager = new WildflyRuntimeManager(runtimeRegistry,
                                                                               wildflyAccessInterface);

        runtimeManager.start(wildflyRuntime);

        allRuntimes = runtimeRegistry.getRuntimes(0,
                                                  10,
                                                  "",
                                                  true);

        assertEquals(1,
                     allRuntimes.size());

        runtime = allRuntimes.get(0);

        assertTrue(runtime instanceof WildflyRuntime);

        wildflyRuntime = (WildflyRuntime) runtime;

        assertEquals(RuntimeState.RUNNING,
                     wildflyRuntime.getState().getState());
        runtimeManager.stop(wildflyRuntime);

        allRuntimes = runtimeRegistry.getRuntimes(0,
                                                  10,
                                                  "",
                                                  true);

        assertEquals(1,
                     allRuntimes.size());

        runtime = allRuntimes.get(0);

        assertTrue(runtime instanceof WildflyRuntime);

        wildflyRuntime = (WildflyRuntime) runtime;

        assertEquals(RuntimeState.UNKNOWN,
                     wildflyRuntime.getState().getState());

        wildflyRuntimeExecExecutor.destroy(wildflyRuntime);

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

        final PipelineConfigStage sourceConfig = new PipelineConfigStage("Git Source",
                                                                         new GitConfig() {
                                                                         });
        final PipelineConfigStage projectConfig = new PipelineConfigStage("Maven Project",
                                                                          new MavenProjectConfig() {
                                                                          });
        final PipelineConfigStage buildConfig = new PipelineConfigStage("Maven Build Config",
                                                                        new MavenBuildConfig() {
                                                                            @Override
                                                                            public List<String> getGoals() {
                                                                                final List<String> result = new ArrayList<>();
                                                                                result.add("package");

                                                                                return result;
                                                                            }

                                                                            @Override
                                                                            public Properties getProperties() {
                                                                                final Properties result = new Properties();
                                                                                result.setProperty("failIfNoTests",
                                                                                                   "false");
                                                                                result.setProperty("gwt.compiler.skip",
                                                                                                   "true");
                                                                                return result;
                                                                            }
                                                                        });

        final PipelineConfigStage codeServerExec = new PipelineConfigStage("Start Code Server",
                                                                           new GWTCodeServerMavenExecConfig() {

                                                                           });

        final PipelineConfigStage buildExec = new PipelineConfigStage("Maven Build",
                                                                      new MavenBuildExecConfig() {
                                                                      });

        final PipelineConfigStage providerConfig = new PipelineConfigStage("Wildfly Provider Config",
                                                                           new WildflyProviderConfig() {
                                                                           });

        final PipelineConfigStage runtimeExec = new PipelineConfigStage("Wildfly Runtime Exec",
                                                                        new ContextAwareWildflyRuntimeExecConfig());

        final Pipeline pipeCodeServer = PipelineFactory
                .newBuilder()
                .addConfigStage(sourceConfig)
                .addConfigStage(projectConfig)
                .addConfigStage(buildConfig)
                .addConfigStage(codeServerExec)
                .addConfigStage(buildExec)
                .addConfigStage(providerConfig)
                .addConfigStage(runtimeExec).buildAs("my pipe");
        final WildflyRuntimeExecExecutor wildflyRuntimeExecExecutor = new WildflyRuntimeExecExecutor(runtimeRegistry,
                                                                                                     wildflyAccessInterface);
        final PipelineExecutor executor = new PipelineExecutor(asList(new GitConfigExecutor(sourceRegistry),
                                                                      new MavenProjectConfigExecutor(sourceRegistry),
                                                                      new MavenBuildConfigExecutor(),
                                                                      new MavenBuildExecConfigExecutor(buildRegistry),
                                                                      new GWTCodeServerMavenExecConfigExecutor(leaser),
                                                                      new WildflyProviderConfigExecutor(runtimeRegistry),
                                                                      wildflyRuntimeExecExecutor));

        executor.execute(new Input() {
                             {
                                 put("repo-name",
                                     "ls-users-new");
                                 put("create-repo",
                                     "true");
                                 put("branch",
                                     "master");
                                 put("out-dir",
                                     tempPath.getAbsolutePath());
                                 put("origin",
                                     "https://github.com/mbarkley/app-former-playground");
                                 put("project-dir",
                                     "users-new");
                                 put("wildfly-user",
                                     "admin");
                                 put("wildfly-password",
                                     "Admin#70365");
                                 put("bindAddress",
                                     "localhost");
                                 put("host",
                                     ip);
                                 put("port",
                                     "8080");
                                 put("management-port",
                                     "9990");
                             }
                         },
                         pipeCodeServer,
                         System.out::println);

        List<org.guvnor.ala.runtime.Runtime> allRuntimes = runtimeRegistry.getRuntimes(0,
                                                                                       10,
                                                                                       "",
                                                                                       true);

        assertEquals(1,
                     allRuntimes.size());
        final List<Repository> allRepositories = sourceRegistry.getAllRepositories();
        assertEquals(1,
                     allRepositories.size());
        final UFLocal local = new UFLocal();
        final GitRepository repository = (GitRepository) local.getRepository("ls-users-new",
                                                                             Collections.emptyMap());
        final Source source = repository.getSource("master");
        assertNotNull(source);

        final Input wildflyInput = new Input() {

            {
                put("repo-name",
                    "ls-users-new");
                put("branch",
                    "master");
                put("project-dir",
                    "users-new");
                put("wildfly-user",
                    "admin");
                put("wildfly-password",
                    "Admin#70365");
                put("host",
                    ip);
                put("port",
                    "8080");
                put("management-port",
                    "9990");
            }
        };

        Optional<Repository> repo = sourceRegistry.getAllRepositories()
                .stream()
                .filter(r -> r.getName().equals(repository.getName()))
                .findFirst();

        Optional<org.guvnor.ala.build.Project> project = Optional.empty();

        if (repo.isPresent()) {
            project = sourceRegistry.getAllProjects(repo.get())
                    .stream()
                    .filter(p -> p.getName().equals("users-new"))
                    .findFirst();
        }

        if (!project.isPresent()) {
            fail();
        }

        final String tempDir = project.get().getTempDir();

        wildflyInput.put("project-temp-dir",
                         tempDir);

        executor.execute(wildflyInput,
                         pipeCodeServer,
                         System.out::println);

        allRuntimes = runtimeRegistry.getRuntimes(0,
                                                  10,
                                                  "",
                                                  true);

        assertEquals(1,
                     allRuntimes.size());

        wildflyAccessInterface.dispose();
    }

    @Test
    @InSequence(4)
    public void shouldBeAbleToStopAndDestroyTest() {
        cc.stop(CONTAINER);
        cc.destroy(CONTAINER);
    }
}
