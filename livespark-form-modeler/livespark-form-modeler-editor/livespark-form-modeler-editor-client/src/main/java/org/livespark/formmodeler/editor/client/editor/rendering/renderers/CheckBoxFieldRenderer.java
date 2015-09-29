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
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.livespark.formmodeler.editor.model.impl.basic.CheckBoxFieldDefinition;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * Created by pefernan on 9/21/15.
 */
@Dependent
public class CheckBoxFieldRenderer extends FieldRenderer<CheckBoxFieldDefinition> {
    @Override
    public String getName() {
        return "CheckBox";
    }

    @Override
    public IsWidget renderWidget() {
        FormGroup group = new FormGroup();
        CheckBox checkBox = new CheckBox(field.getLabel());
        checkBox.setEnabled(!field.getReadonly());
        group.add(checkBox);
        group.add(new HelpBlock());
        return group;
    }

    @Override
    public String getSupportedFieldDefinitionCode() {
        return CheckBoxFieldDefinition._CODE;
    }

    @Override
    protected List<PropertyEditorFieldInfo> getCustomFieldSettings() {
        return null;
    }
}
