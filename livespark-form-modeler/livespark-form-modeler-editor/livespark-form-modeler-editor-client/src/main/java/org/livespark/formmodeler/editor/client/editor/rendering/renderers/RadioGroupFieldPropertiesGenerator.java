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


import java.util.List;
import javax.enterprise.context.Dependent;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.model.impl.basic.selectors.RadioGroupFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.selectors.SelectorOption;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorType;

@Dependent
public class RadioGroupFieldPropertiesGenerator extends SelectorFieldPropertiesGenerator<RadioGroupFieldDefinition> {

    private Column inputContainer = new Column( ColumnSize.MD_12 );

    @Override
    public String getSupportedFieldDefinitionCode() {
        return RadioGroupFieldDefinition._CODE;
    }

    @Override
    protected List<PropertyEditorFieldInfo> getCustomFieldSettings() {
        List<PropertyEditorFieldInfo> result = super.getCustomFieldSettings();

        result.add(new PropertyEditorFieldInfo(FieldProperties.INSTANCE.inline(), String.valueOf(field.getInline()), PropertyEditorType.BOOLEAN) {
            @Override
            public void setCurrentStringValue(final String currentStringValue) {
                super.setCurrentStringValue(currentStringValue);
                field.setInline( Boolean.valueOf( currentStringValue ) );
                refreshSelectorOptions();
            }
        });
        return result;
    }
}
