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
import org.gwtbootstrap3.client.ui.*;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.editor.model.impl.basic.selectors.ListBoxFieldDefinition;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorType;

import javax.enterprise.context.Dependent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pefernan on 9/21/15.
 */
@Dependent
public class ListBoxFieldRenderer extends FieldRenderer<ListBoxFieldDefinition> {

    @Override
    public String getName() {
        return "ListBox";
    }

    @Override
    public IsWidget renderWidget() {
        FormGroup group = new FormGroup(  );
        FormLabel label = new FormLabel(  );
        ListBox listBox = new ListBox();
        listBox.setWidth( field.getSize() + "em");
        listBox.setEnabled(!field.getReadonly());
        label.setText( field.getLabel());
        label.setFor(listBox.getId());
        group.add(label);
        group.add(listBox);
        group.add(new HelpBlock());
        return group;
    }

    @Override
    public String getSupportedFieldDefinitionCode() {
        return ListBoxFieldDefinition._CODE;
    }

    @Override
    protected List<PropertyEditorFieldInfo> getCustomFieldSettings() {
        List<PropertyEditorFieldInfo> result = new ArrayList<PropertyEditorFieldInfo>();
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
