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

package org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl;

import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.RequiresValueConverter;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textBox.definition.TextBoxBaseDefinition;

public class TextBoxHelper extends AbstractInputCreatorHelper<TextBoxBaseDefinition> implements RequiresValueConverter<TextBoxBaseDefinition> {

    @Override
    public String getSupportedFieldTypeCode() {
        return TextBoxBaseDefinition.FIELD_TYPE.getTypeName();
    }

    @Override
    public String getInputWidget(TextBoxBaseDefinition fieldDefinition) {
        return "org.gwtbootstrap3.client.ui.TextBox";
    }
}
