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

package org.livespark.formmodeler.renderer.backend.service.impl.fieldInitializers;

import javax.enterprise.context.Dependent;

import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.impl.relations.SubFormFieldDefinition;
import org.livespark.formmodeler.renderer.backend.service.impl.DMOBasedTransformerContext;
import org.livespark.formmodeler.renderer.backend.service.impl.FieldSetting;

/**
 * @author Pere Fernandez <pefernan@redhat.com>
 */
@Dependent
public class SubFormFieldInitializer extends FormAwareFieldInitializer<SubFormFieldDefinition> {

    @Override
    public boolean supports( FieldDefinition field ) {
        return field instanceof SubFormFieldDefinition;
    }

    @Override
    public void initializeField( SubFormFieldDefinition field, FieldSetting setting, DMOBasedTransformerContext context ) {
        FormDefinition form = context.getRenderingContext().getAvailableForms().get( field.getStandaloneClassName() );
        if ( form == null ) {
            form = formGenerator.generateFormDefinitionForType( setting.getType(), context );
            context.getRenderingContext().getAvailableForms().put( field.getStandaloneClassName(), form );
        }

        field.setNestedForm( form.getId() );
    }
}
