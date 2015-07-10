package org.livespark.codegen;

import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.ENTITY_SERVICE_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_MODEL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.LIST_ITEM_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.LIST_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_IMPL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_SERVICE_SUFFIX;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BINDABLE;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BOUND;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_DATAFIELD;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_PORTABLE;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.VALIDATION_VALID;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.shared.metadata.MetadataService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaClass;
import org.jboss.forge.roaster.model.Property;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProject;
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

    @Inject
    private MetadataService metadataService;

    @Test
    public void javaSourceGeneratedForNewModel() throws Exception {
        prepareServiceTest();

        final Project project = getProject();

        final String dataObjectName = "Customer";

        final String sharedPackageURI = getSharedPackageURI( project );
        final String localPackageURI = getLocalPackageURI( project );
        final String serverPackageURI = getServerPackageURI( project );

        final String[] localTypes = getLocalTypes( dataObjectName );
        final String[] sharedTypes = getSharedTypes( dataObjectName );
        final String[] serverTypes = getServerTypes( dataObjectName );

        final Path sharedPath = PathFactory.newPath( "/", sharedPackageURI );

        maybeCreateDataObject( sharedPath, dataObjectName );

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
        }, 30, 1000 );

    }

    @Test
    public void formViewHasAllBindingsExceptId() throws Exception {
        prepareServiceTest();

        final Project project = getProject();

        final String sharedPackageURI = getSharedPackageURI( project );
        final Path sharedPath = PathFactory.newPath( "/", sharedPackageURI );

        final String dataObjectName = "RockStar";

        maybeCreateDataObject( sharedPath, dataObjectName );

        final DataModel dataModel = dataModelerService.loadModel( (KieProject) project );
        final DataObject dataObject = dataModel.getDataObject( DATA_OBJECT_PACKAGE + ".client.shared." + dataObjectName );

        // Simulate adding fields
        dataObject.addProperty( "name", "java.lang.String" );
        dataObject.addProperty( "numberOfAlbums", "java.lang.Integer" );
        dataObject.addProperty( "dob", "java.util.Date" );
        final org.uberfire.java.nio.file.Path dataObjectPath = org.uberfire.java.nio.file.Paths.get( URI.create( sharedPackageURI + "/" + dataObjectName + ".java" ) );
        updateDataObject( dataObject, dataObjectPath );

        final String localPackageURI = getLocalPackageURI( project );
        final String bindNamePrefix = dataObjectName.substring( 0, 1 ).toLowerCase() + dataObjectName.substring( 1 );
        /*
         * If these assertions are run immediately, they may fail before the java files have been written.
         * Therefore, we will attempt the assertions multiple times, calling Thread.sleep between attempts.
         */
        runAssertions( new Runnable() {
            @Override
            public void run() {
                final org.uberfire.java.nio.file.Path formViewPath =
                        org.uberfire.java.nio.file.Paths.get( URI.create( localPackageURI + "/" + dataObjectName + FORM_VIEW_SUFFIX + ".java" ) );
                assertTrue( "Precondition failed: expected form view to be generated at " + formViewPath.toUri(), ioService.exists( formViewPath ) );

                final String source = ioService.readAllString( formViewPath );
                @SuppressWarnings( "unchecked" )
                final JavaClass<JavaClassSource> clazz = Roaster.parse( JavaClass.class, source );

                assertViewProperty( bindNamePrefix + "_name", clazz );
                assertViewProperty( bindNamePrefix + "_numberOfAlbums", clazz );
                assertViewProperty( bindNamePrefix + "_dob", clazz );
            }
        }, 30, 1000 );
    }

    @Test
    public void listItemViewHasAllBindingsIncludingId() throws Exception {
        prepareServiceTest();

        final Project project = getProject();

        final String sharedPackageURI = getSharedPackageURI( project );
        final Path sharedPath = PathFactory.newPath( "/", sharedPackageURI );

        final String dataObjectName = "RockStar";

        maybeCreateDataObject( sharedPath, dataObjectName );

        final DataModel dataModel = dataModelerService.loadModel( (KieProject) project );
        final DataObject dataObject = dataModel.getDataObject( DATA_OBJECT_PACKAGE + ".client.shared." + dataObjectName );

        // Simulate adding fields
        dataObject.addProperty( "name", "java.lang.String" );
        dataObject.addProperty( "numberOfAlbums", "java.lang.Integer" );
        dataObject.addProperty( "dob", "java.util.Date" );
        final org.uberfire.java.nio.file.Path dataObjectPath = org.uberfire.java.nio.file.Paths.get( URI.create( sharedPackageURI + "/" + dataObjectName + ".java" ) );
        updateDataObject( dataObject, dataObjectPath );

        final String localPackageURI = getLocalPackageURI( project );
        final String bindNamePrefix = dataObjectName.substring( 0, 1 ).toLowerCase() + dataObjectName.substring( 1 );
        /*
         * If these assertions are run immediately, they may fail before the java files have been written.
         * Therefore, we will attempt the assertions multiple times, calling Thread.sleep between attempts.
         */
        runAssertions( new Runnable() {
            @Override
            public void run() {
                final org.uberfire.java.nio.file.Path listItemViewPath =
                        org.uberfire.java.nio.file.Paths.get( URI.create( localPackageURI + "/" + dataObjectName + LIST_ITEM_VIEW_SUFFIX + ".java" ) );
                assertTrue( "Precondition failed: expected form view to be generated at " + listItemViewPath.toUri(), ioService.exists( listItemViewPath ) );

                final String source = ioService.readAllString( listItemViewPath );
                @SuppressWarnings( "unchecked" )
                final JavaClass<JavaClassSource> clazz = Roaster.parse( JavaClass.class, source );

                assertViewProperty( bindNamePrefix + "_id", clazz );
                assertViewProperty( bindNamePrefix + "_name", clazz );
                assertViewProperty( bindNamePrefix + "_numberOfAlbums", clazz );
                assertViewProperty( bindNamePrefix + "_dob", clazz );
            }
        }, 30, 1000 );
    }

    @Test
    public void formModelIsPortableBindableAndHasValidEntityField() throws Exception {
        prepareServiceTest();

        final Project project = getProject();

        final String sharedPackageURI = getSharedPackageURI( project );
        final Path sharedPath = PathFactory.newPath( "/", sharedPackageURI );

        final String dataObjectName = "RockStar";

        maybeCreateDataObject( sharedPath, dataObjectName );

        /*
         * If these assertions are run immediately, they may fail before the java files have been written.
         * Therefore, we will attempt the assertions multiple times, calling Thread.sleep between attempts.
         */
        runAssertions( new Runnable() {
            @Override
            public void run() {
                final org.uberfire.java.nio.file.Path formModelPath =
                        org.uberfire.java.nio.file.Paths.get( URI.create( sharedPackageURI + "/" + dataObjectName + FORM_MODEL_SUFFIX + ".java" ) );
                assertTrue( "Precondition failed: expected form view to be generated at " + formModelPath.toUri(), ioService.exists( formModelPath ) );

                final String source = ioService.readAllString( formModelPath );
                @SuppressWarnings( "unchecked" )
                final JavaClass<JavaClassSource> clazz = Roaster.parse( JavaClass.class, source );
                final Property<JavaClassSource> entityProp = clazz.getProperty( uncapitalize( dataObjectName ) );

                assertNotNull( "Form model must be @Bindable.", clazz.getAnnotation( ERRAI_BINDABLE ) );
                assertNotNull( "Form model must be @Portable.", clazz.getAnnotation( ERRAI_PORTABLE ) );
                assertNotNull( "Form model must have entity field.", entityProp );
                assertNotNull( "Form model entity field must be @Valid.", entityProp.getAnnotation( VALIDATION_VALID ) );
                assertNotNull( "Form model entity field must have getter method.", entityProp.getAccessor() );
                assertNotNull( "Form model entity field must have setter method.", entityProp.getMutator() );
            }
        }, 30, 1000 );
    }

    private void maybeCreateDataObject( final Path sharedPath, final String dataObjectName ) {
        final String fileName = dataObjectName + ".java";
        final URI fileURI = URI.create( sharedPath.toURI() + "/" + fileName );

        if ( ioService.notExists( org.uberfire.java.nio.file.Paths.get( fileURI ) ) ) {
            dataModelerService.createJavaFile( sharedPath, fileName, "", defaultOptions( dataObjectName ) );
        }
    }

    private void updateDataObject( final DataObject dataObject, final org.uberfire.java.nio.file.Path dataObjectPath ) {
        final String updatedSource = ioService.readAllString( dataObjectPath );
        dataModelerService.saveSource( updatedSource,
                                       Paths.convert( dataObjectPath ),
                                       dataObject,
                                       metadataService.getMetadata( Paths.convert( dataObjectPath ) ),
                                       "add properties to test entity" );
    }

    private void assertViewProperty( final String name, final JavaClass<JavaClassSource> formViewClass ) {
        final Property<JavaClassSource> prop = formViewClass.getProperty( name );

        assertNotNull( "Binding " + name + " was not generated.", prop );
        assertNotNull( "Property " + name + " was generated without @DataField.", prop.getAnnotation( ERRAI_DATAFIELD ) );
        assertNotNull( "Property " + name + " was generated without @Bound.", prop.getAnnotation( ERRAI_BOUND ) );
        assertEquals( "Property " + name + " was generated with incorrect binding expression.",
                      name.replace( '_',
                                    '.' ),
                      prop.getAnnotation( ERRAI_BOUND ).getStringValue( "property" ) );
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
