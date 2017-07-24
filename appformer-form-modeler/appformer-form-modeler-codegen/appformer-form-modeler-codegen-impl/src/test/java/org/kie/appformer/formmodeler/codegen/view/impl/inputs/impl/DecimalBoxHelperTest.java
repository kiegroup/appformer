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

package org.kie.appformer.formmodeler.codegen.view.impl.inputs.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.DecimalBoxHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.util.SourceGenerationValueConvertersFactory;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.decimalBox.definition.DecimalBoxFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.decimalBox.type.DecimalBoxFieldType;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DecimalBoxHelperTest extends AbstractValueConverterAwareInputCreatorHelperTest<DecimalBoxHelper, DecimalBoxFieldDefinition> {

    private static final String INPUT_WIDGET_CLASS_NAME = "org.kie.workbench.common.forms.common.rendering.client.widgets.decimalBox.DecimalBox";

    @Test
    public void testGetReadOnlyMethod() {

        String fieldName = "testFieldName";
        String readOnlyParam = "readOnly";
        String actualString = helper.getReadonlyMethod(fieldName,
                                                       readOnlyParam);

        String expectedString = fieldName + ".setEnabled( !" + readOnlyParam + ");";

        assertEquals(actualString,
                     expectedString);
    }

    @Test
    public void testGetConverterClassNameFloat() {
        testGetConverterClassName(Float.class,
                                  SourceGenerationValueConvertersFactory.FLOAT_TO_DOUBLE_CONVERTER);
    }

    @Test
    public void testGetConverterClassNameBigDecimal() {
        testGetConverterClassName(BigDecimal.class,
                                  SourceGenerationValueConvertersFactory.BIG_DECIMAL_TO_DOUBLE_CONVERTER);
    }

    @Override
    DecimalBoxHelper getHelper() {
        return new DecimalBoxHelper();
    }

    @Override
    List<String> getOldConverterClassNames() {
        List oldConverterNames = new ArrayList<>();
        oldConverterNames.add("org.kie.workbench.common.forms.common.rendering.client.widgets.decimalBox.converters.FloatToDoubleValueConverter");
        oldConverterNames.add("org.kie.workbench.common.forms.common.rendering.client.widgets.decimalBox.converters.BigDecimalToDoubleValueConverter");
        return oldConverterNames;
    }

    @Override
    String getFieldTypeName() {
        return DecimalBoxFieldType.NAME;
    }

    @Override
    String getInputWidget() {
        return INPUT_WIDGET_CLASS_NAME;
    }
}
