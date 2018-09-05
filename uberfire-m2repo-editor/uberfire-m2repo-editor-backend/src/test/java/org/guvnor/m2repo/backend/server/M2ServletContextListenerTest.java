/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.guvnor.m2repo.backend.server;

import java.io.File;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.guvnor.m2repo.backend.server.helpers.HttpGetHelper;
import org.guvnor.m2repo.backend.server.helpers.HttpPostHelper;
import org.guvnor.m2repo.backend.server.helpers.HttpPutHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class M2ServletContextListenerTest {

    private static Logger logger = LoggerFactory.getLogger(M2ServletContextListenerTest.class);

    @ArquillianResource
    private URL deploymentUrl;

    @Deployment
    public static Archive getDeployment() {

        final WebArchive war = ShrinkWrap.create(WebArchive.class, "M2.war");
        final File[] metaInfFilesFiles;

        war.setWebXML(new File("target/test-classes/web.xml"));
        metaInfFilesFiles = new File("target/test-classes/META-INF").listFiles();


        war.addClasses(M2ServletContextListener.class, M2Servlet.class,
                       HttpPostHelper.class, HttpPutHelper.class, HttpGetHelper.class, ApplicationScopedProducer.class);


       // war.addPackages(true, "org.guvnor.m2repo.backend.server");
        for (final File file : metaInfFilesFiles) {
            war.addAsManifestResource(file);
        }

        String settings = "src/test/settings.xml" ;

        final File[] files = Maven.configureResolver().
                fromFile(settings).
                loadPomFromFile("./pom.xml")
                .resolve(
                        "org.assertj:assertj-core:?",
                        "org.apache.maven:maven-core:?",
                        "org.apache.maven:maven-model:?",
                        "org.apache.maven:maven-settings:?",

                        "com.google.inject.extensions:guice-servlet:?",
                        "org.codehaus.plexus:plexus-utils:?",

                        "org.eclipse.aether:aether-api:?",
                        "org.eclipse.aether:aether-util:?",

                        "org.uberfire:uberfire-m2repo-editor-backend:?",
                        "org.uberfire:uberfire-project-backend:?",
                        "org.uberfire:uberfire-server:?",
                        "org.uberfire:uberfire-project-api:?",
                        "org.uberfire:uberfire-servlet-security:?",
                        "org.uberfire:uberfire-commons-editor-backend:?",
                        "org.uberfire:uberfire-structure-backend:?",
                        "org.uberfire:uberfire-project-backend:?",

                        "org.jboss.errai:errai-jboss-as-support:?",
                        "org.jboss.errai:errai-security-server:?",

                        "commons-io:commons-io:?",
                        "com.thoughtworks.xstream:xstream:?").withTransitivity()
                .asFile();

        for (final File file : files) {
            war.addAsLibrary(file);
        }
        System.out.println(war.toString(true));
        return war;
    }

    @Test
    public void getTest() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(deploymentUrl.toString() + "/maven2/");
        Invocation invocation = target.request().buildGet();
        Response response = invocation.invoke();
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(200);
        //assertThat(response.readEntity(String.class)).isEqualTo(maven);

    }
}
