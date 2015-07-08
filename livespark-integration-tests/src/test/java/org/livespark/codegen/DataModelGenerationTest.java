package org.livespark.codegen;

import static org.junit.Assert.assertTrue;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.ENTITY_SERVICE_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_MODEL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.LIST_ITEM_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.LIST_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_IMPL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_SERVICE_SUFFIX;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.guvnor.common.services.project.model.Project;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.livespark.test.BaseIntegrationTest;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;

@RunWith( Arquillian.class )
public class DataModelGenerationTest extends BaseIntegrationTest {

    private static final String DATA_OBJECT_PACKAGE = "test";

    @Deployment
    public static WebArchive createDeployment() {
        return BaseIntegrationTest.createLiveSparkDeployment();
    }

    private static Map<String, Object> defaultOptions( final String name ) {
        final Map<String, Object> options = new HashMap<String, Object>();
        options.put( "persistable", true );
        options.put( "tableName", name );

        return options;
    }

    @Inject
    private DataModelerService dataModelerService;

    @Test
    public void javaSourceGeneratedForNewModel() throws Exception {
        prepareServiceTest();

        final Project project = getProject();

        final String sharedPackageURI = getSharedPackageURI( project );
        final String localPackageURI = getLocalPackageURI( project );
        final String serverPackageURI = getServerPackageURI( project );

        final String dataObjectName = "Customer";

        final String[] localTypes = getLocalTypes( dataObjectName );
        final String[] sharedTypes = getSharedTypes( dataObjectName );
        final String[] serverTypes = getServerTypes( dataObjectName );

        final Path sharedPath = PathFactory.newPath( "/", sharedPackageURI );

        // Test
        dataModelerService.createJavaFile( sharedPath, dataObjectName + ".java", "", defaultOptions( dataObjectName ) );

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

    private String[] getServerTypes( final String dataObjectName ) {
        return new String[]{
                                                   dataObjectName + REST_IMPL_SUFFIX,
                                                   dataObjectName + ENTITY_SERVICE_SUFFIX
        };
    }

    private String[] getSharedTypes( final String dataObjectName ) {
        return new String[]{
                                                   dataObjectName + FORM_MODEL_SUFFIX,
                                                   dataObjectName + REST_SERVICE_SUFFIX
        };
    }

    private String[] getLocalTypes( final String dataObjectName ) {
        return new String[]{
                                                  dataObjectName + FORM_VIEW_SUFFIX,
                                                  dataObjectName + LIST_ITEM_VIEW_SUFFIX,
                                                  dataObjectName + LIST_VIEW_SUFFIX
        };
    }

    private void assertTypesInPackage( final String[] types, final String packageURI ) {
        for ( final String type : types ) {
            final String fileName = type + ".java";
            final org.uberfire.java.nio.file.Path filePath = Paths.convert( PathFactory.newPath( fileName, packageURI + "/" + fileName ) );
            assertTrue( "The following file was not created: " + filePath.toUri(), ioService.exists( filePath ) );
        }
    }

    private String getLocalPackageURI( Project project ) {
        return getSrcMainPackageHelper( project, "/" + DATA_OBJECT_PACKAGE + "/client/local" );
    }

    private String getSharedPackageURI( Project project ) {
        return getSrcMainPackageHelper( project, "/" + DATA_OBJECT_PACKAGE + "/client/shared" );
    }

    private String getServerPackageURI( Project project ) {
        return getSrcMainPackageHelper( project, "/" + DATA_OBJECT_PACKAGE + "/server" );
    }
}
