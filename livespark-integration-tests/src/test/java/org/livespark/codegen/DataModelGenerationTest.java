package org.livespark.codegen;

import static org.junit.Assert.assertTrue;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.ENTITY_SERVICE_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_MODEL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.LIST_ITEM_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.LIST_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_IMPL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_SERVICE_SUFFIX;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.project.model.Package;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndListener;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.api.RpcContext;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.io.IOService;

@RunWith( Arquillian.class )
public class DataModelGenerationTest {

    private final class MockQueueSession
        implements
        QueueSession {
        @Override
        public void setAttribute( String attribute,
                                  Object value ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object removeAttribute( String attribute ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasAttribute( String attribute ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getSessionId() {
            return "test-id";
        }

        @Override
        public String getParentSessionId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> getAttributeNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T getAttribute( Class<T> type,
                                   String attribute ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean endSession() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addSessionEndListener( SessionEndListener listener ) {
            throw new UnsupportedOperationException();
        }
    }

    private static final String DATA_OBJECT_PACKAGE = "test";

    @Deployment
    public static WebArchive createDeployment() {
        /*
         * FIXME This file only exists if livespark-webapp has been built prior to this test.
         */
        return ShrinkWrap.createFromZipFile( WebArchive.class, new File( "../livespark-webapp/target/livespark-webapp.war" ) );
    }

    private static Map<String, Object> defaultOptions( final String name ) {
        final Map<String, Object> options = new HashMap<String, Object>();
        options.put( "persistable", true );
        options.put( "tableName", name );

        return options;
    }

    @Inject
    private DataModelerService dataModelerService;

    @Inject
    private KieProjectService projectService;

    @Inject
    private RepositoryService repoService;

    @Inject
    private AuthenticationService authService;

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Resource
    private ManagedExecutorService execService;

    @Test
    public void javaSourceGeneratedForNewModel() throws Exception {
        // Prepare for test
        final Project project = getProject();
        final String dataObjectName = "Customer";

        final String[] localTypes = new String[]{
                                                  dataObjectName + FORM_VIEW_SUFFIX,
                                                  dataObjectName + LIST_ITEM_VIEW_SUFFIX,
                                                  dataObjectName + LIST_VIEW_SUFFIX
        };
        final String[] sharedTypes = new String[]{
                                                   dataObjectName + FORM_MODEL_SUFFIX,
                                                   dataObjectName + REST_SERVICE_SUFFIX
        };
        final String[] serverTypes = new String[]{
                                                   dataObjectName + REST_IMPL_SUFFIX,
                                                   dataObjectName + ENTITY_SERVICE_SUFFIX
        };

        final String sharedPackageURI = getSharedPackageURI( project );
        final Path sharedPath = PathFactory.newPath( "/", sharedPackageURI );

        authService.login( "admin", "admin" );

        final Message message = MessageBuilder.createMessage("for testing").signalling().done().getMessage();
        message.setResource( "Session", new MockQueueSession() );
        RpcContext.set( message );

        // Test
        dataModelerService.createJavaFile( sharedPath, dataObjectName + ".java", "", defaultOptions( dataObjectName ) );

        // Assertions
        final String localPackageURI = getLocalPackageURI( project );
        final String serverPackageURI = getServerPackageURI( project );

        /*
         * If these assertions are run immediately, they may fail before the java files have been written.
         * Therefore, we will attempt the assertions multiple times, calling Thread.sleep between attempts.
         */
        runAssertions( new Runnable() {
            @Override
            public void run() {
                assertTypesInPackage( localTypes, localPackageURI );
                assertTypesInPackage( sharedTypes, sharedPackageURI );
                assertTypesInPackage( serverTypes, serverPackageURI );
            }
        }, 5 );

    }

    private void runAssertions( final Runnable assertions, int attempts ) throws Exception {
        do {
            try {
                assertions.run();
                return;
            } catch ( AssertionError e ) {
                if ( --attempts <= 0 ) {
                    throw e;
                } else {
                    Thread.sleep( 500 );
                }
            }
        } while ( true );
    }

    private void assertTypesInPackage( final String[] types, final String packageURI ) {
        for ( final String type : types ) {
            final String fileName = type + ".java";
            final org.uberfire.java.nio.file.Path filePath = Paths.convert( PathFactory.newPath( fileName, packageURI + "/" + fileName ) );
            assertTrue( "The following file was not created: " + filePath.toUri(), ioService.exists( filePath ) );
        }
    }

    private String getLocalPackageURI( Project project ) {
        return getPackageHelper( project, "/client/local" );
    }

    private String getSharedPackageURI( Project project ) {
        return getPackageHelper( project, "/client/shared" );
    }

    private String getServerPackageURI( Project project ) {
        return getPackageHelper( project, "/server" );
    }

    private String getPackageHelper( Project project, String subPath ) {
        final Package defaultPackage = projectService.resolveDefaultPackage( project );

        return defaultPackage.getPackageMainSrcPath().toURI() + "/" + DATA_OBJECT_PACKAGE + subPath;
    }

    private Project getProject() {
        // There should only be one repo with one project
        for ( final Repository repo : repoService.getRepositories() ) {
            for ( final Project project : projectService.getProjects( repo, "master" ) ) {
                return project;
            }
        }

        // Should never happen
        throw new IllegalStateException();
    }
}
