/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl;

import java.math.BigDecimal;
import java.util.HashMap;

import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.AbstractNumberBoxHelper;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.decimalBox.definition.DecimalBoxFieldDefinition;

public class DecimalBoxHelper extends AbstractNumberBoxHelper<DecimalBoxFieldDefinition> {

    @Override
    public String getSupportedFieldTypeCode() {
        return DecimalBoxFieldDefinition.FIELD_TYPE.getTypeName();
    }

    @Override
    public String getInputWidget(DecimalBoxFieldDefinition fieldDefinition) {
        return "org.kie.workbench.common.forms.common.rendering.client.widgets.decimalBox.DecimalBox";
    }

    @Override
    public String getReadonlyMethod(String fieldName,
                                    String readonlyParam) {
        return fieldName + ".setEnabled( !" + readonlyParam + ");";
    }

    @Override
    public String getConverterClassName(DecimalBoxFieldDefinition field) {

        HashMap<String, String> converterMap = new HashMap<>();

        String pkgPrefix = "org.kie.workbench.common.forms.common.rendering.client.widgets.decimalBox.converters.";

        converterMap.put(Float.class.getCanonicalName(),
                         pkgPrefix + "FloatToDoubleConverter");
        converterMap.put(Double.class.getCanonicalName(),
                         "");
        converterMap.put(BigDecimal.class.getCanonicalName(),
                         pkgPrefix + "BigDecimalToDoubleConverter");

        return converterMap.get(field.getStandaloneClassName());
    }
}
