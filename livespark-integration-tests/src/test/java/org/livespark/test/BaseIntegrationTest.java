package org.livespark.test;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.project.model.Package;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.api.RpcContext;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.test.mock.MockQueueSession;
import org.uberfire.io.IOService;

import com.google.common.io.Files;

public class BaseIntegrationTest {

    public static WebArchive createLiveSparkDeployment() {
        final File warFile = Maven.configureResolver()
                                  .workOffline()
                                  .loadPomFromFile( "pom.xml" )
                                  .resolve( "org.livespark:livespark-webapp:war:?" )
                                  .withoutTransitivity()
                                  .asSingleFile();

        final File targetWarFile = new File( "target/livespark-webapp.war" );

        /*
         * If we do not copy the war file into the target directory this error causes tests to fail: JBAS016071
         */
        try {
            Files.copy( warFile, targetWarFile );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

        return ShrinkWrap.createFromZipFile( WebArchive.class, targetWarFile )
                         .addClasses( BaseIntegrationTest.class, MockQueueSession.class );
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

    protected void prepareServiceTest() {
        authService.login( "admin", "admin" );
        final Message message = MessageBuilder.createMessage("for testing").signalling().done().getMessage();
        message.setResource( "Session", new MockQueueSession( "test-id" ) );
        RpcContext.set( message );
    }

    protected void runAssertions( final Runnable assertions, int attempts, final long delayInMs ) throws Exception {
        do {
            try {
                assertions.run();
                return;
            } catch ( AssertionError e ) {
                if ( --attempts <= 0 ) {
                    throw e;
                } else {
                    Thread.sleep( delayInMs );
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

}
