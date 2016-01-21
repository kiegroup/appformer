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
package org.livespark.formmodeler.codegen.layout.impl;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.formmodeler.codegen.layout.FormLayoutTemplateGenerator;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.FormLayoutComponent;
import org.uberfire.ext.layout.editor.api.editor.LayoutColumn;
import org.uberfire.ext.layout.editor.api.editor.LayoutComponent;
import org.uberfire.ext.layout.editor.api.editor.LayoutRow;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;

/**
 * Generates a default LayoutTemplate for the form
 */
@ApplicationScoped
public class FormLayoutTemplateGeneratorImpl implements FormLayoutTemplateGenerator {
    public static final String DRAGGABLE_TYPE = "org.livespark.formmodeler.editor.client.editor.rendering.DraggableFieldComponent";

    @Override
    public LayoutTemplate generateLayoutTemplate( FormDefinition formDefinition ) {
        LayoutTemplate template = new LayoutTemplate();

        for ( FieldDefinition field : formDefinition.getFields() ) {
            LayoutComponent layoutComponent = new LayoutComponent( DRAGGABLE_TYPE );
            layoutComponent.addProperty( FormLayoutComponent.FORM_ID, formDefinition.getId() );
            layoutComponent.addProperty( FormLayoutComponent.FIELD_ID, field.getId() );

            LayoutColumn column = new LayoutColumn("12");
            column.addLayoutComponent( layoutComponent );

            LayoutRow row = new LayoutRow();
            row.add( column );

            template.addRow( row );
        }

        return template;
    }
}
