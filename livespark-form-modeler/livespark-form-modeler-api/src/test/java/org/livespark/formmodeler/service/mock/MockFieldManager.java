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
package org.livespark.formmodeler.service.mock;

import java.util.Collection;

import org.livespark.formmodeler.service.impl.AbstractFieldManager;
import org.livespark.formmodeler.service.impl.fieldProviders.BasicTypeFieldProvider;
import org.livespark.formmodeler.service.impl.fieldProviders.CheckBoxFieldProvider;
import org.livespark.formmodeler.service.impl.fieldProviders.DatePickerFieldProvider;
import org.livespark.formmodeler.service.impl.fieldProviders.ListBoxFieldProvider;
import org.livespark.formmodeler.service.impl.fieldProviders.MultipleSubFormFieldProvider;
import org.livespark.formmodeler.service.impl.fieldProviders.ObjectSelectorFieldProvider;
import org.livespark.formmodeler.service.impl.fieldProviders.RadioGroupFieldProvider;
import org.livespark.formmodeler.service.impl.fieldProviders.SliderFieldProvider;
import org.livespark.formmodeler.service.impl.fieldProviders.SubFormFieldProvider;
import org.livespark.formmodeler.service.impl.fieldProviders.TextAreaFieldProvider;
import org.livespark.formmodeler.service.impl.fieldProviders.TextBoxFieldProvider;

public class MockFieldManager extends AbstractFieldManager {

    public MockFieldManager() {
        registerFieldProvider( new TextBoxFieldProvider() {
            {doRegisterFields();}
        } );
        registerFieldProvider( new TextAreaFieldProvider(){
            {doRegisterFields();}
        } );
        registerFieldProvider( new CheckBoxFieldProvider(){
            {doRegisterFields();}
        } );
        registerFieldProvider( new ListBoxFieldProvider(){
            {doRegisterFields();}
        } );
        registerFieldProvider( new RadioGroupFieldProvider(){
            {doRegisterFields();}
        } );
        registerFieldProvider( new DatePickerFieldProvider(){
            {doRegisterFields();}
        } );
        registerFieldProvider( new SliderFieldProvider(){
            {doRegisterFields();}
        } );
        registerFieldProvider( new SubFormFieldProvider() );
        registerFieldProvider( new MultipleSubFormFieldProvider() );
        registerFieldProvider( new ObjectSelectorFieldProvider() );
    }

    public Collection<BasicTypeFieldProvider> getAllBasicTypeProviders() {
        return basicProviders;
    }
}
