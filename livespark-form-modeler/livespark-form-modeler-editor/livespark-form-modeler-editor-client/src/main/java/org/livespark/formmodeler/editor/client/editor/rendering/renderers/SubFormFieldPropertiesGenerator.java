/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.livespark.formmodeler.editor.client.editor.rendering.renderers;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.livespark.formmodeler.editor.client.editor.rendering.FieldDefinitionPropertiesModal;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.model.impl.relations.SubFormFieldDefinition;
import org.livespark.formmodeler.editor.service.FormFinderSerivce;
import org.livespark.formmodeler.editor.service.SubFormData;
import org.livespark.formmodeler.renderer.client.rendering.renderers.relations.subform.SubFormFieldRenderer;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorType;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pefernan on 9/21/15.
 */
@Dependent
public class SubFormFieldPropertiesGenerator extends FieldPropertiesGenerator<SubFormFieldDefinition> {
    @Inject
    private Caller<FormFinderSerivce> finderServiceCaller;

    private List<SubFormData> subForms;

    private List<PropertyEditorFieldInfo> customProperties = new ArrayList<PropertyEditorFieldInfo>();

    @Override
    public String getSupportedFieldDefinitionCode() {
        return SubFormFieldDefinition._CODE;
    }

    @Override
    public void loadFieldProperties( final FieldDefinitionPropertiesModal modal ) {
        finderServiceCaller.call(new RemoteCallback<List<SubFormData>>() {
            @Override
            public void callback(List<SubFormData> response) {
                subForms = response;

                customProperties.clear();

                PropertyEditorFieldInfo subFormsProperty = new PropertyEditorFieldInfo(
                        FieldProperties.INSTANCE.embeddedForm(),
                        field.getEmbeddedModel(), PropertyEditorType.COMBO) {
                    @Override
                    public void setCurrentStringValue(String currentStringValue) {
                        super.setCurrentStringValue(currentStringValue);
                        if ( currentStringValue.equals("") ) {
                            renderHTMLContent("");
                        } else {
                            boolean found = false;
                            for (int i = 0; i < subForms.size() && !found; i++) {
                                SubFormData data = subForms.get(i);
                                if (data.getFormModelClass().equals(currentStringValue)) {
                                    field.setEmbeddedModel(data.getFormModelClass());
                                    field.setEmbeddedFormView(data.getViewClass());
                                    loadTemplate();
                                    found = true;
                                }
                            }
                        }
                    }
                };

                List<String> comboValues = new ArrayList<String>();
                comboValues.add("");
                for ( SubFormData data : subForms ) {
                    comboValues.add( data.getFormModelClass() );
                }
                subFormsProperty.withComboValues(comboValues);
                customProperties.add(subFormsProperty);

                SubFormFieldPropertiesGenerator.super.loadFieldProperties(modal);
            }
        }).getAvailableFormsByType(field.getStandaloneClassName(), draggableFieldComponent.getFormPath());
    }

    protected void loadTemplate() {
        finderServiceCaller.call(new RemoteCallback<String>() {
            @Override
            public void callback(String response) {
                renderHTMLContent( response );
            }
        }).getSubFormTemplate( field, draggableFieldComponent.getFormPath() );
    }

    private void renderHTMLContent( String template ) {
        ((SubFormFieldRenderer)draggableFieldComponent.getFieldRenderer()).setHtmlContent( template );
    }

    @Override
    protected List<PropertyEditorFieldInfo> getCustomFieldSettings() {
        return customProperties;
    }
}
