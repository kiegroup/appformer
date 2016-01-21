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
package org.livespark.formmodeler.codegen.layout;

import org.livespark.formmodeler.codegen.template.FormTemplateGenerator;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.FormLayoutComponent;
import org.uberfire.ext.layout.editor.api.editor.LayoutColumn;
import org.uberfire.ext.layout.editor.api.editor.LayoutComponent;
import org.uberfire.ext.layout.editor.api.editor.LayoutRow;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates a default LayoutTemplate for the form
 */
@ApplicationScoped
public class FormLayoutTemplateGeneratorImpl implements FormLayoutTemplateGenerator {
    public static final String DRAGGABLE_TYPE = "org.livespark.formmodeler.editor.client.editor.rendering.DraggableFieldComponent";
    @Inject
    protected Instance<FormLayoutTemplateComponent> installedComponents;

    @Inject
    protected FormTemplateGenerator formTemplateGenerator;

    protected Map<String, FormLayoutTemplateComponent> layoutComponents = new HashMap<String, FormLayoutTemplateComponent>();

    @PostConstruct
    protected void init() {
        for ( FormLayoutTemplateComponent component : installedComponents ) {
            layoutComponents.put( component.getSupportedFieldTypeCode(), component );
        }
    }

    @Override
    public String generateLayoutTemplate( FormDefinition form ) {
        if ( form.getLayoutTemplate() == null ) {
            form.setLayoutTemplate( generateTemplate( form ) );
        }
        return formTemplateGenerator.generateFormTemplate( form );
    }

    @Override
    public LayoutTemplate generateTemplate( FormDefinition formDefinition ) {
        LayoutTemplate template = new LayoutTemplate();

        for ( FieldDefinition field : formDefinition.getFields() ) {
            FormLayoutTemplateComponent component = layoutComponents.get( field.getCode() );
            if ( component != null ) {
                LayoutComponent layoutComponent = new LayoutComponent( DRAGGABLE_TYPE );
                layoutComponent.addProperty( FormLayoutComponent.FORM_ID, formDefinition.getId() );
                layoutComponent.addProperty( FormLayoutComponent.FIELD_ID, field.getId() );

                LayoutColumn column = new LayoutColumn("12");
                column.addLayoutComponent( layoutComponent );

                LayoutRow row = new LayoutRow();
                row.add( column );

                template.addRow( row );
            }
        }

        return template;
    }
}
