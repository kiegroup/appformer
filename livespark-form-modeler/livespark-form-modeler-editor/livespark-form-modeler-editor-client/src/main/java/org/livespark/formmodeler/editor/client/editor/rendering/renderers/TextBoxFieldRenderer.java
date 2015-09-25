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
import org.gwtbootstrap3.client.ui.TextBox;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.editor.model.impl.basic.TextBoxFieldDefinition;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorType;

import javax.enterprise.context.Dependent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pefernan on 9/21/15.
 */
@Dependent
public class TextBoxFieldRenderer extends FieldRenderer<TextBoxFieldDefinition> {

    @Override
    public String getName() {
        return "TextBox";
    }

    @Override
    public IsWidget renderWidget() {
        FormGroup group = new FormGroup(  );
        FormLabel label = new FormLabel(  );
        TextBox box = new TextBox();
        box.setId( field.getName() );
        box.setPlaceholder(field.getPlaceHolder());
        box.setWidth(field.getSize().intValue() + "em");
        box.setMaxLength(field.getMaxLength());
        box.setReadOnly(field.getReadonly());
        label.setText( field.getLabel() );
        label.setFor(box.getId());
        group.add( label );
        group.add( box );
        group.add( new HelpBlock(  ) );
        return group;
    }

    @Override
    public String getSupportedFieldDefinition() {
        return TextBoxFieldDefinition.class.getName();
    }

    @Override
    protected List<PropertyEditorFieldInfo> getCustomFieldProperties() {
        List<PropertyEditorFieldInfo> result = new ArrayList<PropertyEditorFieldInfo>();
        result.add(new PropertyEditorFieldInfo(FieldProperties.INSTANCE.placeholder(), field.getPlaceHolder(), PropertyEditorType.TEXT) {
            @Override
            public void setCurrentStringValue(final String currentStringValue) {
                super.setCurrentStringValue(currentStringValue);
                field.setPlaceHolder(currentStringValue);
            }
        });
        result.add(new PropertyEditorFieldInfo(FieldProperties.INSTANCE.size(), String.valueOf(field.getSize()), PropertyEditorType.NATURAL_NUMBER) {
            @Override
            public void setCurrentStringValue(final String currentStringValue) {
                super.setCurrentStringValue(currentStringValue);
                field.setSize(Integer.decode(currentStringValue));
            }
        });
        result.add(new PropertyEditorFieldInfo(FieldProperties.INSTANCE.maxLength(), String.valueOf(field.getMaxLength()), PropertyEditorType.NATURAL_NUMBER) {
            @Override
            public void setCurrentStringValue(final String currentStringValue) {
                super.setCurrentStringValue(currentStringValue);
                field.setMaxLength(Integer.decode(currentStringValue));
            }
        });
        return result;
    }
}
