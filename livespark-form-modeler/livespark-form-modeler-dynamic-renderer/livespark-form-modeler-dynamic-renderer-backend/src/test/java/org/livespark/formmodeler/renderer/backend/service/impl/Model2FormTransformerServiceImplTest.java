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

package org.livespark.formmodeler.renderer.backend.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.codegen.layout.FormLayoutTemplateGenerator;
import org.livespark.formmodeler.codegen.layout.impl.DynamicFormLayoutTemplateGenerator;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.impl.basic.checkBox.CheckBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.datePicker.DatePickerFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.selectors.listBox.EnumListBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.textArea.TextAreaFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.textBox.TextBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.relations.SubFormFieldDefinition;
import org.livespark.formmodeler.renderer.backend.service.impl.fieldInitializers.EnumSelectorFieldInitializer;
import org.livespark.formmodeler.renderer.backend.service.impl.fieldInitializers.FieldInitializer;
import org.livespark.formmodeler.renderer.backend.service.impl.fieldInitializers.MultipleSubFormFieldInitializer;
import org.livespark.formmodeler.renderer.backend.service.impl.fieldInitializers.SubFormFieldInitializer;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.DefaultFieldAnnotationProcessor;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.FieldAnnotationProcessor;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.ListBoxFieldAnnotationProcessor;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.RadioGroupFieldAnnotationProcessor;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.SliderAnnotationProcessor;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.TextAreaAnnotationProcessor;
import org.livespark.formmodeler.renderer.test.model.Address;
import org.livespark.formmodeler.renderer.test.model.Employee;
import org.livespark.formmodeler.renderer.test.model.Person;
import org.livespark.formmodeler.renderer.test.model.Title;
import org.livespark.formmodeler.service.FieldManager;
import org.livespark.formmodeler.service.mock.MockFieldManager;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Model2FormTransformerServiceImplTest {

    private Instance<FieldAnnotationProcessor> annotationProcessors;

    private Instance<FieldInitializer<? extends FieldDefinition>> fieldInitializers;

    private FormLayoutTemplateGenerator layoutTemplateGenerator;

    private Model2FormTransformerServiceImpl service;

    @SuppressWarnings( "unchecked" )
    @Before
    public void init() {
        final FieldManager fieldManager = new MockFieldManager();
        final List<FieldAnnotationProcessor> processors = Arrays.asList( new DefaultFieldAnnotationProcessor( fieldManager ),
                                                                         new ListBoxFieldAnnotationProcessor( fieldManager ),
                                                                         new RadioGroupFieldAnnotationProcessor( fieldManager ),
                                                                         new SliderAnnotationProcessor( fieldManager ),
                                                                         new TextAreaAnnotationProcessor( fieldManager ) );

        final List<FieldInitializer> initializers = Arrays.asList( new SubFormFieldInitializer(),
                new MultipleSubFormFieldInitializer(),
                new EnumSelectorFieldInitializer() );

        annotationProcessors = mock( Instance.class );
        when( annotationProcessors.iterator() ).then( inv -> processors.iterator() );


        fieldInitializers = mock( Instance.class );
        when( fieldInitializers.iterator() ).then( inv -> initializers.iterator() );

        layoutTemplateGenerator = new DynamicFormLayoutTemplateGenerator();

        service = new Model2FormTransformerServiceImpl( annotationProcessors, fieldInitializers, layoutTemplateGenerator );
    }

    @Test
    public void testFormGeneration() {
        testFormGeneration( new Person(), 4 );
    }

    @Test
    public void testFormGenerationWithInheritance() {
        FormDefinition form = testFormGeneration( new Employee(), 8 );

        checkMarried( form.getFieldById( "married" ) );
        checkAge( form.getFieldById( "age" ) );
        checkAddress( form.getFieldById( "address" ) );
        checkRole( form.getFieldById( "roleDescription" ) );
    }

    protected FormDefinition testFormGeneration( Person model, int expectedFields ) {
        FormDefinition form = service.createContext( model ).getRootForm();

        assertNotNull( "Form shouldn't be null!", form );

        assertNotNull( "Form must contain fields!", form.getFields() );

        assertEquals( "Form should have " + expectedFields + " fields", expectedFields, form.getFields().size() );

        for ( FieldDefinition field : form.getFields() ) {
            assertNotNull( "Field should have an ID!", field.getId() );
            assertNotNull( "Field should have a name!", field.getName() );
            assertNotNull( "Field should have a label!", field.getLabel() );
            assertNotNull( "Field should have a model!", field.getModelName() );
        }

        checkTitleField(  form.getFieldById( "title" ) );
        checkSurname(  form.getFieldById( "surname" ) );
        checkBirthday(  form.getFieldById( "birthday" ) );

        return form;
    }

    protected void checkTitleField( FieldDefinition field ) {
        assertNotNull( field );
        assertTrue( field instanceof EnumListBoxFieldDefinition );
        assertEquals( Title.class.getName(), field.getFieldTypeInfo().getType() );
        assertTrue( field.getFieldTypeInfo().isEnum() );
    }

    protected void checkSurname( FieldDefinition field ) {
        assertNotNull( field );
        assertTrue( field instanceof TextBoxFieldDefinition );
    }

    protected void checkBirthday( FieldDefinition field ) {
        assertNotNull( field );
        assertTrue( field instanceof DatePickerFieldDefinition );
    }

    protected void checkMarried( FieldDefinition field ) {
        assertNotNull( field );
        assertTrue( field instanceof CheckBoxFieldDefinition );
    }

    protected void checkAge( FieldDefinition field ) {
        assertNotNull( field );
        assertTrue( field instanceof TextBoxFieldDefinition );
        assertEquals( Integer.class.getName(), field.getStandaloneClassName() );
    }

    protected void checkAddress( FieldDefinition field ) {
        assertNotNull( field );
        assertTrue( field instanceof SubFormFieldDefinition );
        assertEquals( Address.class.getName(), field.getStandaloneClassName() );
    }

    protected void checkRole( FieldDefinition field ) {
        assertNotNull( field );
        assertTrue( field instanceof TextAreaFieldDefinition );
        TextAreaFieldDefinition textArea = (TextAreaFieldDefinition) field;
        assertSame( 4, textArea.getRows() );
        assertEquals( "Role Description", textArea.getPlaceHolder() );
    }
}
