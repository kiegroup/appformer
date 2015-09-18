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
package org.livespark.formmodeler.editor.client.editor.fields;

import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextBox;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.editor.model.impl.basic.AbstractIntputFieldDefinition;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pefernan on 9/2/15.
 */
public abstract class AbstractInputLayoutComponent<D extends AbstractIntputFieldDefinition> extends FieldLayoutComponent<D> {

    public IsWidget generateWidget( ) {
        if (fieldDefinition == null) return null;

        if (!fieldDefinition.getName().equals(fieldName)) {
            fieldDefinition = null;
            getCurrentField(new HashMap<String, String>());
            return null;
        }

        FormGroup group = new FormGroup(  );
        FormLabel label = new FormLabel(  );
        TextBox box = new TextBox();
        box.setId( fieldDefinition.getName() );
        box.setPlaceholder(fieldDefinition.getPlaceHolder());
        box.setWidth(fieldDefinition.getSize().intValue() + "em");
        box.setMaxLength(fieldDefinition.getMaxLength());
        box.setReadOnly(fieldDefinition.getReadonly());
        label.setText( fieldDefinition.getLabel() );
        label.setFor(box.getId());
        group.add( label );
        group.add( box );
        group.add( new HelpBlock(  ) );
        return group;
    }

    @Override
    protected List<PropertyEditorFieldInfo> getCustomFieldProperties() {
        List<PropertyEditorFieldInfo> result = new ArrayList<PropertyEditorFieldInfo>();
        result.add(new PropertyEditorFieldInfo(FieldProperties.INSTANCE.placeholder(), fieldDefinition.getPlaceHolder(), PropertyEditorType.TEXT) {
            @Override
            public void setCurrentStringValue(final String currentStringValue) {
                super.setCurrentStringValue(currentStringValue);
                fieldDefinition.setPlaceHolder(currentStringValue);
            }
        });
        result.add(new PropertyEditorFieldInfo(FieldProperties.INSTANCE.size(), String.valueOf(fieldDefinition.getSize()), PropertyEditorType.NATURAL_NUMBER) {
            @Override
            public void setCurrentStringValue(final String currentStringValue) {
                super.setCurrentStringValue(currentStringValue);
                fieldDefinition.setSize(Integer.decode(currentStringValue));
            }
        });
        result.add(new PropertyEditorFieldInfo(FieldProperties.INSTANCE.maxLength(), String.valueOf(fieldDefinition.getMaxLength()), PropertyEditorType.NATURAL_NUMBER) {
            @Override
            public void setCurrentStringValue(final String currentStringValue) {
                super.setCurrentStringValue(currentStringValue);
                fieldDefinition.setMaxLength(Integer.decode(currentStringValue));
            }
        });
        return result;
    }
}
