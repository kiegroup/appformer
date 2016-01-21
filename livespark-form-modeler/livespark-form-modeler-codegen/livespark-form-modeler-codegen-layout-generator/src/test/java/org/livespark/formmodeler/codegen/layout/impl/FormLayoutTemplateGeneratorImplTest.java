package org.livespark.formmodeler.codegen.layout.impl;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.FormLayoutComponent;
import org.livespark.formmodeler.model.impl.basic.CheckBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.DateBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.TextBoxFieldDefinition;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.layout.editor.api.editor.LayoutColumn;
import org.uberfire.ext.layout.editor.api.editor.LayoutComponent;
import org.uberfire.ext.layout.editor.api.editor.LayoutRow;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FormLayoutTemplateGeneratorImplTest {

    private FormDefinition form;

    private FormLayoutTemplateGeneratorImpl templateGenerator;

    @Before
    public void init() {
        templateGenerator = new FormLayoutTemplateGeneratorImpl();

        form = new FormDefinition();
        form.setName( "Test" );
        form.setId( "Test-ID" );

        TextBoxFieldDefinition name = new TextBoxFieldDefinition();
        name.setId( "name" );
        name.setName( "employee_name" );
        name.setLabel( "Name" );
        name.setPlaceHolder( "Name" );
        name.setModelName( "employee" );
        name.setBoundPropertyName( "name" );
        name.setStandaloneClassName( String.class.getName() );

        TextBoxFieldDefinition lastName = new TextBoxFieldDefinition();
        lastName.setId( "lastName" );
        lastName.setName( "employee_lastName" );
        lastName.setLabel( "Last Name" );
        lastName.setPlaceHolder( "Last Name" );
        lastName.setModelName( "employee" );
        lastName.setBoundPropertyName( "lastName" );
        lastName.setStandaloneClassName( String.class.getName() );

        DateBoxFieldDefinition birthday = new DateBoxFieldDefinition();
        birthday.setId( "birthday" );
        birthday.setName( "employee_birthday" );
        birthday.setLabel( "Birthday" );
        birthday.setModelName( "employee" );
        birthday.setBoundPropertyName( "birthday" );
        birthday.setStandaloneClassName( Date.class.getName() );

        CheckBoxFieldDefinition married = new CheckBoxFieldDefinition();
        married.setId("married");
        married.setName( "employee_married" );
        married.setLabel( "Married" );
        married.setModelName( "employee" );
        married.setBoundPropertyName( "married" );
        married.setStandaloneClassName( Boolean.class.getName() );

        form.getFields().add( name );
        form.getFields().add( lastName );
        form.getFields().add( birthday );
        form.getFields().add( married );
    }

    @Test
    public void testTemplateGeneration() {
        LayoutTemplate layout = templateGenerator.generateLayoutTemplate( form );

        assertNotNull( layout );

        assertNotNull( layout.getRows() );

        assertEquals( 4, layout.getRows().size() );

        for( LayoutRow row : layout.getRows() ) {
            assertEquals( 1, row.getLayoutColumns().size() );

            for ( LayoutColumn col : row.getLayoutColumns() ) {
                assertEquals( "12", col.getSpan() );

                assertEquals( 0, col.getRows().size() );

                assertEquals( 1, col.getLayoutComponents().size() );

                for ( LayoutComponent component : col.getLayoutComponents() ) {
                    assertEquals( FormLayoutTemplateGeneratorImpl.DRAGGABLE_TYPE, component.getDragTypeName() );

                    assertEquals( form.getId(), component.getProperties().get( FormLayoutComponent.FORM_ID ) );

                    String fieldId = component.getProperties().get( FormLayoutComponent.FIELD_ID );

                    assertNotNull( fieldId );

                    assertNotNull( form.getFieldById( fieldId ) );

                }

            }

        }

    }

}

