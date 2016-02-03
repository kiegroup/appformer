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

package org.livespark.formmodeler.renderer.backend.service.impl.processors;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.livespark.formmodeler.metaModel.ListBox;
import org.livespark.formmodeler.metaModel.Option;
import org.livespark.formmodeler.model.impl.basic.selectors.ListBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.selectors.SelectorOption;
import org.livespark.formmodeler.renderer.backend.service.impl.FieldSetting;
import org.livespark.formmodeler.service.FieldManager;
import org.livespark.formmodeler.renderer.service.SelectorDataProviderManager;

@Dependent
public class ListBoxFieldAnnotationProcessor extends AbstractFieldAnnotationProcessor {

    @Inject
    public ListBoxFieldAnnotationProcessor( FieldManager fieldManager ) {
        super( fieldManager );
    }

    @Override
    public boolean supportsAnnotation( Annotation annotation ) {
        return annotation instanceof ListBox;
    }

    @Override
    public ListBoxFieldDefinition buildFieldDefinition( Annotation annotation, FieldSetting setting ) {

        ListBoxFieldDefinition fieldDefinition = new ListBoxFieldDefinition();

        if ( !Arrays.asList( fieldDefinition.getSupportedTypes() ).contains( setting.getType().getName() )
                || !supportsAnnotation( annotation )) {
            return null;
        }

        ListBox listBoxDef = (ListBox) annotation;

        if ( !listBoxDef.provider().className().isEmpty() ) {

            String providerId =  listBoxDef.provider().type().getCode()
                    + SelectorDataProviderManager.SEPARATOR
                    + listBoxDef.provider().className();

            fieldDefinition.setDataProvider( providerId );
        } else {
            List<SelectorOption> options = new ArrayList<SelectorOption>();
            for ( Option option : listBoxDef.options() ) {
                SelectorOption selectorOption = new SelectorOption();
                selectorOption.setValue( option.value() );
                selectorOption.setText( option.text() );
                selectorOption.setDefaultValue( option.isDefault() );
            }
            fieldDefinition.setOptions( options );
        }

        return fieldDefinition;
    }
}
