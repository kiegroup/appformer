/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.codegen.view.impl.inputs;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.Instance;

import junit.framework.TestCase;
import org.guvnor.common.services.project.model.Package;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.Before;
import org.junit.Test;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.inputs.mock.MockRoasterJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.kie.workbench.common.forms.model.DataHolder;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.kie.workbench.common.forms.model.impl.relations.EmbeddedFormField;
import org.uberfire.backend.vfs.Path;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;
import static org.mockito.Mockito.*;

public abstract class AbstractInputHelperTest extends TestCase {

    protected MockRoasterJavaTemplateSourceGenerator roasterJavaTemplateSourceGenerator;

    protected Instance<InputCreatorHelper<? extends FieldDefinition>> creatorHelpers;

    private Path path;

    protected Package rootPackage;

    protected Package localPackage;

    protected Package sharedPackage;

    protected Package serverPackage;

    protected JavaClassSource classSource;

    protected FormDefinition formDefinition;

    protected SourceGenerationContext context;

    protected int fieldCount = 0;

    @Before
    public void initTest() {

        creatorHelpers = mock( Instance.class );

        path = mock( Path.class );

        rootPackage = mock( Package.class );

        when( rootPackage.getPackageName() ).thenReturn( "org" );

        localPackage = mock( Package.class );

        when( localPackage.getPackageName() ).thenReturn( "org.client.local" );

        sharedPackage = mock( Package.class );

        when( sharedPackage.getPackageName() ).thenReturn( "org.client.shared" );

        serverPackage = mock( Package.class );

        when( serverPackage.getPackageName() ).thenReturn( "org.server" );

        classSource = Roaster.create( JavaClassSource.class );

        List<InputCreatorHelper> helpers = getInputHelpersToTest();

        when( creatorHelpers.iterator() ).then( it -> helpers.iterator() );

        roasterJavaTemplateSourceGenerator = new MockRoasterJavaTemplateSourceGenerator( classSource, creatorHelpers );

        formDefinition = new FormDefinition();

        formDefinition.setName( "Test" );
        formDefinition.setId( "Test" );
        formDefinition.addDataHolder( new DataHolder( "employee", "org.test.Employee" ) );

        formDefinition.getFields().addAll( getFieldsToTest() );

        assertFalse( "Form should have at least one field", formDefinition.getFields().size() == 0 );

        context = new SourceGenerationContext( formDefinition,
                path,
                rootPackage,
                localPackage,
                sharedPackage,
                serverPackage,
                new ArrayList<>());

        String template = roasterJavaTemplateSourceGenerator.generateJavaTemplateSource( context );

        assertNotNull( "Form template shouldn't be null", template );
    }

    protected abstract List<FieldDefinition> getFieldsToTest();
    protected abstract List<InputCreatorHelper> getInputHelpersToTest();

    @Test
    public void testSourceGenerationForInputs() {
        for ( FieldDefinition field : formDefinition.getFields() ) {
            runFieldTests( field, roasterJavaTemplateSourceGenerator.getHelperForField( field ) );
        }
    }

    protected void runFieldTests( FieldDefinition field, InputCreatorHelper helper ) {
        FieldSource<JavaClassSource> fieldSource = classSource.getField( field.getName() );

        assertNotNull( "There is no property defined for the field!", fieldSource );

        String propertyDeclaration = helper.getInputWidget( field );

        if ( propertyDeclaration.indexOf( "." ) != -1 ) {
            // using qualified className to initialize the input
            assertEquals( "Property type doesn't match", helper.getInputWidget( field ),
                    fieldSource.getType().getQualifiedName() );
        } else {
            // checking if input has custom initilization
            assertEquals( "Property type doesn't match",
                    helper.getInputWidget( field ), fieldSource.getType().toString() );
        }

        if ( !(field instanceof EmbeddedFormField) ) {
            AnnotationSource boundAnnotation = fieldSource.getAnnotation( ERRAI_BOUND );
            assertNotNull( "@Bound annotation missing for field", boundAnnotation );
            assertEquals( "Invalid binding expression for field", field.getBindingExpression(),
                    boundAnnotation.getStringValue( "property" ) );
        }


        assertNotNull( "Missing @DataField annotation for field", fieldSource.getAnnotation( ERRAI_DATAFIELD ) );

        if ( helper.isInputInjectable() ) {
            assertNotNull( "Missing @Ïnject annotation for Field", fieldSource.getAnnotation( INJECT_INJECT ) );
        } else {
            testInitializerMethod( field, fieldSource, helper );
        }

        testReadOnlyMethod( field, helper);
    }

    protected FieldDefinition initFieldDefinition( FieldDefinition field ) {
        field.setName( "test" + fieldCount );
        field.setModelName( "employee" );
        field.setBoundPropertyName( "test" + fieldCount );
        fieldCount ++;
        return field;
    }

    protected void testInitializerMethod( FieldDefinition field, FieldSource fieldSource, InputCreatorHelper helper ) {
        String initializer = fieldSource.getLiteralInitializer();

        assertNotNull( "Field doesn't have any initialization!", initializer );

        initializer = removeEmptySpaces( initializer );

        if ( !initializer.endsWith( ";" )) {
            initializer += ";";
        }

        String helperInitializer = helper.getInputInitLiteral( context, field );

        assertNotNull( "Helper is unable to generate initialization literal for field", helperInitializer );

        helperInitializer = removeEmptySpaces( helperInitializer );

        assertEquals( "Initialization literal on class doesn't match the one generated by the Helper",
                helperInitializer, initializer );

    }

    protected void testReadOnlyMethod( FieldDefinition field, InputCreatorHelper helper ) {
        MethodSource<JavaClassSource> readonlyMethod = classSource.getMethod( "setReadOnly", boolean.class.getName() );

        assertNotNull( "The class must have a setReadOnly method!", readonlyMethod );

        String body = removeEmptySpaces( readonlyMethod.getBody() );

        String fieldReadOnly = removeEmptySpaces( helper.getReadonlyMethod( field.getName(), READONLY_PARAM ) );

        assertTrue( "Field missin on ReadOnlyMethod", body.contains( fieldReadOnly ) );
    }

    protected String removeEmptySpaces( String str ) {
        return str.replaceAll( "\\s", "" );
    }

}
