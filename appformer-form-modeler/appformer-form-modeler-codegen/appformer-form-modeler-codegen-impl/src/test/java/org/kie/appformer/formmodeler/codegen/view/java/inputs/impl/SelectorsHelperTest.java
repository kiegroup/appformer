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

package org.kie.appformer.formmodeler.codegen.view.java.inputs.impl;

import java.util.Arrays;
import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.runner.RunWith;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.kie.appformer.formmodeler.codegen.view.java.inputs.AbstractInputHelperTest;
import org.kie.appformer.formmodeler.codegen.view.java.test.util.InputCreatorHelpersProvider;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.EnumSelectorOption;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.StringSelectorOption;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.listBox.definition.EnumListBoxFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.listBox.definition.StringListBoxFieldDefinition;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.BEFORE_DISPLAY_METHOD;
import static org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.ListBoxHelper.LISTBOX_RENDERER_SUFFIX;
import static org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.ListBoxHelper.LOAD_LIST_VALUES_METHOD_NAME;

@RunWith(MockitoJUnitRunner.class)
public class SelectorsHelperTest extends AbstractInputHelperTest {

    @Override
    protected void runFieldTests(FieldDefinition field,
                                 InputCreatorHelper helper) {
        super.runFieldTests(field,
                            helper);

        assertNotNull("ListBox must have a ValueRenderer!",
                      classSource.getField(field.getName() + LISTBOX_RENDERER_SUFFIX));

        String initListMethodName = LOAD_LIST_VALUES_METHOD_NAME + field.getName();

        assertNotNull("'" + initListMethodName + "' is missing!",
                      classSource.getMethod(initListMethodName));

        MethodSource<JavaClassSource> beforeDisplayMethod = classSource.getMethod(BEFORE_DISPLAY_METHOD);

        assertNotNull("Class must have a '" + BEFORE_DISPLAY_METHOD + "'",
                      beforeDisplayMethod);
    }

    @Override
    protected List<FieldDefinition> getFieldsToTest() {

        StringListBoxFieldDefinition stringListBox = new StringListBoxFieldDefinition();

        stringListBox.getOptions().add(new StringSelectorOption("op1",
                                                                "op1"));
        stringListBox.getOptions().add(new StringSelectorOption("op2",
                                                                "op2"));

        stringListBox.setDefaultValue("op1");

        EnumListBoxFieldDefinition enumListBox = new EnumListBoxFieldDefinition();
        enumListBox.getOptions().add(new EnumSelectorOption(TestEnum.VAL1,
                                                            "Val1"));
        enumListBox.getOptions().add(new EnumSelectorOption(TestEnum.VAL2,
                                                            "Val2"));
        enumListBox.setDefaultValue(TestEnum.VAL1);

        return Arrays.asList(initFieldDefinition(stringListBox),
                             initFieldDefinition(enumListBox));
    }

    @Override
    protected List<InputCreatorHelper> getInputHelpersToTest() {
        return InputCreatorHelpersProvider.getSelectorHelpers();
    }

    public enum TestEnum {
        VAL1,
        VAL2
    }
}
