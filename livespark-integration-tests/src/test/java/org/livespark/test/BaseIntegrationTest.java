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

package org.livespark.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.guvnor.common.services.project.model.Package;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.shared.metadata.MetadataService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.api.RpcContext;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.test.mock.MockHttpSession;
import org.livespark.test.mock.MockQueueSession;
import org.livespark.test.mock.MockServletContext;
import org.livespark.test.mock.MockServletRequest;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.FileAlreadyExistsException;

import com.google.common.io.Files;

public class BaseIntegrationTest {

    /**
     * Creates war deployment for LiveSpark integration test.
     *
     * @param suffix Used in name of deployed war file.
     */
    public static WebArchive createLiveSparkDeployment( final String suffix ) {
        clearDotFiles();
        return resolveAndCopyLiveSparkWar( suffix );
    }

    private static WebArchive resolveAndCopyLiveSparkWar( final String suffix ) {
        final File warFile = Maven.configureResolver()
                                  .workOffline()
                                  .loadPomFromFile( "pom.xml" )
                                  .resolve( "org.livespark:livespark-webapp:war:?" )
                                  .withoutTransitivity()
                                  .asSingleFile();

        final File targetWarFile = new File( "target/livespark-webapp-" + suffix + ".war" );

        /*
         * If we do not copy the war file into the target directory this error causes tests to fail: JBAS016071
         */
        try {
            Files.copy( warFile, targetWarFile );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

        final WebArchive archive = ShrinkWrap.createFromZipFile( WebArchive.class, targetWarFile )
                         .addClasses( BaseIntegrationTest.class,
                                      MockQueueSession.class,
                                      MockHttpSession.class,
                                      MockServletRequest.class,
                                      MockServletContext.class );
        // Wildfly doesn't show console logging for per-deployment configured logging.
        archive.delete( "WEB-INF/classes/log4j.xml" );
        archive.delete( "WEB-INF/classes/logback.xml" );

        return archive;
    }

    public static void clearDotFiles() {
        final String[] dirs = new String[] {
                                            ".niogit",
                                            ".index"
        };

        for ( final String dir : dirs ) {
            FileUtils.deleteQuietly( new File( dir ) );
        }
    }

    @Inject
    protected KieProjectService projectService;

    @Inject
    private RepositoryService repoService;

    @Inject
    private AuthenticationService authService;

    @Inject
    @Named( "ioStrategy" )
    protected IOService ioService;

    @Inject
    protected DataModelerService dataModelerService;

    @Inject
    protected MetadataService metadataService;

    protected void prepareServiceTest() {
        loginAsAdmin();
        setupRpcContext();
    }

    protected void setupRpcContext() {
        RpcContext.set( createMockRpcContextMessage() );
    }

    protected Message createMockRpcContextMessage() {
        final Message message = MessageBuilder.createMessage("for testing").signalling().done().getMessage();
        final MockServletRequest mockRequest = new MockServletRequest();
        final MockServletContext mockContext = new MockServletContext();

        // FIXME don't hardcode this.
        // Note: it doesn't matter that this path doesn't exist.
        final File webXml = new File( "target/wildfly-8.1.0.Final/fakeDir/WEB-INF/web.xml" );

        mockRequest.setServletContext( mockContext.addRealPath( "/WEB-INF/web.xml", webXml.getAbsolutePath() ) )
        // As long as we don't load the app, these values don't matter
                   .setServerName( "hostname" )
                   .setServerPort( 8080 );

        message.setResource( "Session", new MockQueueSession( "test-queuesession-id", new MockHttpSession( "test-httpsession-id" ) ) );
        message.setResource( HttpServletRequest.class.getName(), mockRequest );
        return message;
    }

    protected void loginAsAdmin() {
        authService.login( "admin", "admin" );
    }

    protected void runAssertions( final Runnable assertions, int attempts, final long delayInMs ) {
        runAssertions( assertions, attempts, delayInMs , 0);
    }

    protected void runAssertions( final Runnable assertions, int attempts, final long delayInMs, final long initDelayInMs ) {
        try {
            if ( initDelayInMs > 0 ) {
                Thread.sleep( initDelayInMs );
            }
        } catch ( Exception e ) {
            final RuntimeException wrapper = new RuntimeException( "There was an error while trying to sleep before running assertions.", e );
            wrapper.addSuppressed( e );
            throw wrapper;
        }

        do {
            try {
                assertions.run();
                return;
            } catch ( AssertionError e ) {
                if ( --attempts <= 0 ) {
                    throw e;
                } else {
                    try {
                        Thread.sleep( delayInMs );
                    } catch ( Exception ex ) {
                        final RuntimeException wrapper = new RuntimeException( "There was an error while trying to sleep between running assertions.", ex );
                        wrapper.addSuppressed( e );
                        throw wrapper;
                    }
                }
            }
        } while ( true );
    }

    protected Project getProject() {
        // There should only be one repo with one project
        for ( final Repository repo : repoService.getRepositories() ) {
            for ( final Project project : projectService.getProjects( repo, "master" ) ) {
                return project;
            }
        }

        // Should never happen
        throw new IllegalStateException();
    }

    protected String getSrcMainPackageHelper( Project project, String absPackagePath ) {
        final Package defaultPackage = projectService.resolveDefaultPackage( project );

        return defaultPackage.getPackageMainSrcPath().toURI() + absPackagePath;
    }

    protected void maybeCreateDataObject( final Path sharedPath, final String dataObjectName ) {
        final String fileName = dataObjectName + ".java";

        try {
            dataModelerService.createJavaFile( sharedPath, fileName, "", defaultOptions( dataObjectName ) );
        } catch ( FileAlreadyExistsException ignore ) {
        }
    }

    protected void updateDataObject( final DataObject dataObject,
                                        final org.uberfire.java.nio.file.Path dataObjectPath ) {
                                            final String updatedSource = ioService.readAllString( dataObjectPath );
                                            dataModelerService.saveSource( updatedSource,
                                                                           Paths.convert( dataObjectPath ),
                                                                           dataObject,
                                                                           metadataService.getMetadata( Paths.convert( dataObjectPath ) ),
                                                                           "add properties to test entity" );
                                        }

    public static Map<String, Object> defaultOptions( final String name ) {
        final Map<String, Object> options = new HashMap<String, Object>();
        options.put( "persistable", true );
        options.put( "tableName", name );

        return options;
    }

    public static org.uberfire.java.nio.file.Path makePath( String packageURI, final String fileName ) {
        if ( !packageURI.endsWith( "/" ) ) {
            packageURI = packageURI + "/";
        }
        return org.uberfire.java.nio.file.Paths.get( URI.create( packageURI + fileName ) );
    }

}
