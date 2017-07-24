/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.formmodeler.codegen.view.impl.inputs.impl;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.TextBoxHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.util.SourceGenerationValueConvertersFactory;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textBox.definition.TextBoxBaseDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textBox.type.TextBoxFieldType;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TextBoxHelperTest extends AbstractValueConverterAwareInputCreatorHelperTest<TextBoxHelper, TextBoxBaseDefinition> {

    private static final String INPUT_WIDGET_CLASS_NAME = "org.gwtbootstrap3.client.ui.TextBox";

    @Test
    public void testGetReadOnlyMethod() {

        String fieldName = "testFieldName";
        String readOnlyParam = "readOnly";
        String actualString = helper.getReadonlyMethod(fieldName,
                                                       readOnlyParam);

        String expectedString = fieldName + ".setReadOnly( " + readOnlyParam + ");";

        assertEquals(actualString,
                     expectedString);
    }

    @Test
    public void testGetConverterClassNameString() {
        testGetConverterClassName(String.class,
                                  null);
    }

    @Test
    public void testGetConverterClassNameBigCharacter() {
        testGetConverterClassName(Character.class,
                                  SourceGenerationValueConvertersFactory.CHARACTER_TO_STRING_CONVERTER);
    }

    @Test
    public void testGetConverterClassNameBigChar() {
        testGetConverterClassName(char.class,
                                  SourceGenerationValueConvertersFactory.CHARACTER_TO_STRING_CONVERTER);
    }

    @Override
    TextBoxHelper getHelper() {
        return new TextBoxHelper();
    }

    @Override
    List<String> getOldConverterClassNames() {
        return Collections.emptyList();
    }

    @Override
    String getFieldTypeName() {
        return TextBoxFieldType.NAME;
    }

    @Override
    String getInputWidget() {
        return INPUT_WIDGET_CLASS_NAME;
    }
}
