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

package org.livespark.codegen;

import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.ENTITY_SERVICE_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_MODEL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.LIST_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_IMPL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_SERVICE_SUFFIX;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BINDABLE;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BOUND;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_DATAFIELD;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_PORTABLE;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.VALIDATION_VALID;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Date;

import org.guvnor.common.services.project.model.Project;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaClass;
import org.jboss.forge.roaster.model.Property;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.livespark.test.BaseIntegrationTest;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.java.nio.base.FileTimeImpl;
import org.uberfire.java.nio.file.attribute.FileTime;

@RunWith( Arquillian.class )
public class DataModelGenerationTest extends BaseIntegrationTest {

    private static final String DATA_OBJECT_PACKAGE = "test";
    private Project project;
    private String dataObjectName;

    @Deployment
    public static WebArchive createDeployment() {
        return BaseIntegrationTest.createLiveSparkDeployment( DataModelGenerationTest.class.getSimpleName().toLowerCase() );
    }

    private void assertTestPropertiesWithoutId( final String bindNamePrefix, final JavaClass<JavaClassSource> clazz ) {
        assertViewProperty( bindNamePrefix + "_name", clazz );
        assertViewProperty( bindNamePrefix + "_numberOfAlbums", clazz );
        assertViewProperty( bindNamePrefix + "_dob", clazz );
        assertViewProperty( bindNamePrefix + "_numOfFans", clazz );
        assertViewProperty( bindNamePrefix + "_iq", clazz );
        assertViewProperty( bindNamePrefix + "_favLetter", clazz );
        assertViewProperty( bindNamePrefix + "_popular", clazz );
        assertViewProperty( bindNamePrefix + "_height", clazz );
        assertViewProperty( bindNamePrefix + "_weight", clazz );
        assertViewProperty( bindNamePrefix + "_numOfGuitars", clazz );
        assertViewProperty( bindNamePrefix + "_shortVal", clazz );
        assertViewProperty( bindNamePrefix + "_bac", clazz );
    }

    private void addTestProperties( final DataObject dataObject ) {
        dataObject.addProperty( "name", String.class.getCanonicalName() );
        dataObject.addProperty( "numberOfAlbums", int.class.getCanonicalName() );
        dataObject.addProperty( "dob", Date.class.getCanonicalName() );
        dataObject.addProperty( "bac", BigDecimal.class.getCanonicalName() );
        dataObject.addProperty( "numOfFans", BigInteger.class.getCanonicalName() );
        dataObject.addProperty( "iq", Byte.class.getCanonicalName() );
        dataObject.addProperty( "favLetter", char.class.getCanonicalName() );
        dataObject.addProperty( "popular", boolean.class.getCanonicalName() );
        dataObject.addProperty( "height", double.class.getCanonicalName() );
        dataObject.addProperty( "weight", float.class.getCanonicalName() );
        dataObject.addProperty( "numOfGuitars", long.class.getCanonicalName() );
        dataObject.addProperty( "shortVal", short.class.getCanonicalName() );
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

    private void assertTypesInPackageAreRecent( final String[] types, final String packageURI, final FileTime previousModTime ) {
        for ( final String type : types ) {
            final String fileName = type + ".java";
            final org.uberfire.java.nio.file.Path filePath = Paths.convert( PathFactory.newPath( fileName, packageURI + "/" + fileName ) );
            assertTrue( "The following file was not updated: " + filePath.toUri(), ioService.getLastModifiedTime( filePath ).toMillis() > previousModTime.toMillis() );
        }
    }

    private void assertAllFilesWithPropertiesAreUpdated( final String dataObjectName, final FileTime previousModTime ) {
        final String[] localTypes = new String[] { dataObjectName + FORM_VIEW_SUFFIX };

        assertTypesInPackageAreRecent( localTypes, getLocalPackageURI( project ), previousModTime );
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

    private void assertAllFilesGenerated( final String dataObjectName ) {
        assertTypesInPackage( getLocalTypes( dataObjectName ), getLocalPackageURI( project ) );
        assertTypesInPackage( getSharedTypes( dataObjectName ), getSharedPackageURI( project ) );
        assertTypesInPackage( getServerTypes( dataObjectName ), getServerPackageURI( project ) );
    }

    private void assertAllFilesGeneratedAsPrecondition() throws AssertionError {
        try {
            assertAllFilesGenerated( dataObjectName );
        } catch ( AssertionError e ) {
            throw new AssertionError( "Precondition failed.", e );
        }
    }

    @Before
    public void setup() {
        prepareServiceTest();
        project = getProject();
        dataObjectName = "RockStar";
    }

    @Test
    @InSequence(1)
    public void javaSourceGeneratedForNewModel() throws Exception {
        final Path sharedPath = PathFactory.newPath( "/", getSharedPackageURI( project ) );
        maybeCreateDataObject( sharedPath, dataObjectName );

        /*
         * If these assertions are run immediately, they may fail before the java files have been written.
         * Therefore, we will attempt the assertions multiple times, calling Thread.sleep between attempts.
         */
        runAssertions( new Runnable() {
            @Override
            public void run() {
                assertAllFilesGenerated( dataObjectName );
            }
        }, 30, 1000 );

    }

    @Test
    @InSequence(2)
    public void sourceUpdatedWhenPropertiesAdded() throws Exception {
        assertAllFilesGeneratedAsPrecondition();

        final FileTime previousModTime = new FileTimeImpl( System.currentTimeMillis() );
        final DataModel dataModel = dataModelerService.loadModel( (KieProject) project );
        final DataObject dataObject = dataModel.getDataObject( DATA_OBJECT_PACKAGE + ".client.shared." + dataObjectName );

        // Simulate adding fields
        addTestProperties( dataObject );

        final org.uberfire.java.nio.file.Path dataObjectPath = makePath( getSharedPackageURI( project ), dataObjectName + ".java" );
        updateDataObject( dataObject, dataObjectPath );
        /*
         * If these assertions are run immediately, they may fail before the java files have been written.
         * Therefore, we will attempt the assertions multiple times, calling Thread.sleep between attempts.
         */
        runAssertions( new Runnable() {
            @Override
            public void run() {
                assertAllFilesWithPropertiesAreUpdated( dataObjectName, previousModTime );
            }
        }, 30, 1000 );
    }

    @Test
    @InSequence(3)
    public void formViewHasAllBindingsExceptId() throws Exception {
        assertAllFilesGeneratedAsPrecondition();

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

                assertTestPropertiesWithoutId( bindNamePrefix, clazz );
            }
        }, 30, 1000 );
    }

    @Test
    @InSequence(4)
    public void formModelIsPortableBindableAndHasValidEntityField() throws Exception {
        assertAllFilesGeneratedAsPrecondition();

        final String sharedPackageURI = getSharedPackageURI( project );
        final org.uberfire.java.nio.file.Path formModelPath =
                org.uberfire.java.nio.file.Paths.get( URI.create( sharedPackageURI + "/" + dataObjectName + FORM_MODEL_SUFFIX + ".java" ) );
        /*
         * If these assertions are run immediately, they may fail before the java files have been written.
         * Therefore, we will attempt the assertions multiple times, calling Thread.sleep between attempts.
         */
        runAssertions( new Runnable() {
            @Override
            public void run() {
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
}
