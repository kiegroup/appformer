/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.renderer.test.util;

import java.util.Date;

import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.impl.basic.checkBox.CheckBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.datePicker.DatePickerFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.textBox.TextBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.relations.SubFormFieldDefinition;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.livespark.formmodeler.renderer.test.model.Address;
import org.livespark.formmodeler.renderer.test.model.Employee;

public class TestFormGenerator {

    public static FormRenderingContext getContextForEmployee( Employee employee ) {
        FormDefinition form = getEmployeeForm();
        MockFormRenderingContext context = new MockFormRenderingContext();
        context.setRootForm( form );
        context.setModel( employee );
        context.getAvailableForms().put( form.getId(), form );

        form = getAddressForm();
        context.getAvailableForms().put( form.getId(), form );

        return context;
    }

    public static FormDefinition getEmployeeForm() {
        FormDefinition form = new FormDefinition();
        form.setName( "Employee" );
        form.setId( "Employee" );

        TextBoxFieldDefinition name = new TextBoxFieldDefinition();
        name.setId( "name" );
        name.setName( "name" );
        name.setLabel( "Name" );
        name.setPlaceHolder( "Name" );
        name.setModelName( "name" );
        name.setStandaloneClassName( String.class.getName() );

        TextBoxFieldDefinition lastName = new TextBoxFieldDefinition();
        lastName.setId( "surname" );
        lastName.setName( "surname" );
        lastName.setLabel( "Surname" );
        lastName.setPlaceHolder( "SurName" );
        lastName.setModelName( "surname" );
        lastName.setStandaloneClassName( String.class.getName() );

        DatePickerFieldDefinition birthday = new DatePickerFieldDefinition();
        birthday.setId( "birthday" );
        birthday.setName( "birthday" );
        birthday.setLabel( "Birthday" );
        birthday.setModelName( "birthday" );
        birthday.setStandaloneClassName( Date.class.getName() );

        TextBoxFieldDefinition age = new TextBoxFieldDefinition();
        age.setId( "age" );
        age.setName( "age" );
        age.setLabel( "Age" );
        age.setPlaceHolder( "age" );
        age.setModelName( "age" );
        age.setBoundPropertyName( "value" );
        age.setStandaloneClassName( Integer.class.getName() );

        CheckBoxFieldDefinition married = new CheckBoxFieldDefinition();
        married.setId("married");
        married.setName( "married" );
        married.setLabel( "Married" );
        married.setModelName( "married" );
        married.setStandaloneClassName( Boolean.class.getName() );

        SubFormFieldDefinition address = new SubFormFieldDefinition();
        address.setId( "address" );
        address.setName( "address" );
        address.setLabel( "Address" );
        address.setModelName( "address" );
        address.setNestedForm( "Address" );
        address.setStandaloneClassName( Address.class.getName() );

        form.getFields().add( name );
        form.getFields().add( lastName );
        form.getFields().add( birthday );
        form.getFields().add( age );
        form.getFields().add( married );
        form.getFields().add( address );

        return form;
    }

    public static FormDefinition getAddressForm() {
        FormDefinition form = new FormDefinition();
        form.setName( "Address" );
        form.setId( "Address" );

        TextBoxFieldDefinition name = new TextBoxFieldDefinition();
        name.setId( "street" );
        name.setName( "street" );
        name.setLabel( "Street Name" );
        name.setPlaceHolder( "Street Name" );
        name.setModelName( "street" );
        name.setStandaloneClassName( String.class.getName() );

        TextBoxFieldDefinition num = new TextBoxFieldDefinition();
        num.setId( "num" );
        num.setName( "num" );
        num.setLabel( "#" );
        num.setPlaceHolder( "#" );
        num.setModelName( "num" );
        num.setStandaloneClassName( Integer.class.getName() );


        form.getFields().add( name );
        form.getFields().add( num );

        return form;
    }

}
