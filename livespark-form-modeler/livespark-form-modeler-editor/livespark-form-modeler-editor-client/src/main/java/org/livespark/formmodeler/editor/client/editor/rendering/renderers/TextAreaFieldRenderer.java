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
package org.livespark.formmodeler.editor.client.editor.rendering.renderers;

import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextArea;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.editor.model.impl.basic.TextAreaFieldDefinition;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorType;

import javax.enterprise.context.Dependent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pefernan on 9/21/15.
 */
@Dependent
public class TextAreaFieldRenderer extends FieldRenderer<TextAreaFieldDefinition> {

    @Override
    public String getName() {
        return "TextArea";
    }

    @Override
    public IsWidget renderWidget() {
        FormGroup group = new FormGroup(  );
        FormLabel label = new FormLabel(  );
        TextArea textArea = new TextArea();
        textArea.setPlaceholder( field.getPlaceHolder() );
        textArea.setVisibleLines(field.getRows());
        textArea.setWidth( field.getSize() + "em" );
        textArea.setReadOnly(field.getReadonly());
        label.setText( field.getLabel());
        label.setFor(textArea.getId());
        group.add(label);
        group.add(textArea);
        group.add(new HelpBlock());
        return group;
    }

    @Override
    public String getSupportedFieldDefinitionCode() {
        return TextAreaFieldDefinition._CODE;
    }

    @Override
    protected List<PropertyEditorFieldInfo> getCustomFieldSettings() {
        List<PropertyEditorFieldInfo> result = new ArrayList<PropertyEditorFieldInfo>();
        result.add(new PropertyEditorFieldInfo(FieldProperties.INSTANCE.placeholder(), field.getPlaceHolder(), PropertyEditorType.TEXT) {
            @Override
            public void setCurrentStringValue(final String currentStringValue) {
                super.setCurrentStringValue(currentStringValue);
                field.setPlaceHolder(currentStringValue);
            }
        });
        result.add(new PropertyEditorFieldInfo( FieldProperties.INSTANCE.rows(), String.valueOf( field.getRows() ), PropertyEditorType.TEXT ) {
            @Override
            public void setCurrentStringValue( final String currentStringValue ) {
                super.setCurrentStringValue( currentStringValue );
                field.setRows(Integer.decode(currentStringValue));
            }
        });
        result.add(new PropertyEditorFieldInfo( FieldProperties.INSTANCE.size(), String.valueOf( field.getSize() ), PropertyEditorType.TEXT ) {
            @Override
            public void setCurrentStringValue( final String currentStringValue ) {
                super.setCurrentStringValue( currentStringValue );
                field.setSize(Integer.decode(currentStringValue));
            }
        });
        return result;
    }
}
