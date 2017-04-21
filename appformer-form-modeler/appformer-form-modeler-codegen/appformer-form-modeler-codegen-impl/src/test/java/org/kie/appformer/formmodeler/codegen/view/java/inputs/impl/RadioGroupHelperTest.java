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

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.runner.RunWith;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.kie.appformer.formmodeler.codegen.view.java.inputs.AbstractInputHelperTest;
import org.kie.appformer.formmodeler.codegen.view.java.test.util.InputCreatorHelpersProvider;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.StringSelectorOption;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.radioGroup.definition.StringRadioGroupFieldDefinition;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.RadioGroupHelper.INLINE_RADIO_NAME;
import static org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.RadioGroupHelper.LOAD_RADIO_GROUP_VALUES;
import static org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.RadioGroupHelper.NESTED_RADIO_SUFFIX;
import static org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.RadioGroupHelper.RADIO_NAME;

@RunWith(MockitoJUnitRunner.class)
public class RadioGroupHelperTest extends AbstractInputHelperTest {

    @Override
    protected void runFieldTests(FieldDefinition field,
                                 InputCreatorHelper helper) {
        super.runFieldTests(field,
                            helper);

        StringRadioGroupFieldDefinition radioGroup = (StringRadioGroupFieldDefinition) field;

        for (int i = 0; i < radioGroup.getOptions().size(); i++) {
            FieldSource<JavaClassSource> radio = classSource.getField(radioGroup.getName() + NESTED_RADIO_SUFFIX + i);

            assertNotNull("There must be " + radioGroup.getOptions().size() + " Radios",
                          radio);

            String inputClassName;

            if (radioGroup.getInline()) {
                inputClassName = INLINE_RADIO_NAME;
            } else {
                inputClassName = RADIO_NAME;
            }

            assertEquals("Radio type must be '" + inputClassName + "'",
                         inputClassName,
                         radio.getType().getName());
        }

        String initListMethodName = LOAD_RADIO_GROUP_VALUES + field.getName();

        assertNotNull("'" + initListMethodName + "' is missing!",
                      classSource.getMethod(initListMethodName));
    }

    @Override
    protected List<FieldDefinition> getFieldsToTest() {

        StringRadioGroupFieldDefinition radioGroup = new StringRadioGroupFieldDefinition();
        radioGroup.getOptions().add(new StringSelectorOption("op1",
                                                             "op1",
                                                             true));
        radioGroup.getOptions().add(new StringSelectorOption("op2",
                                                             "op2",
                                                             false));

        StringRadioGroupFieldDefinition inlineRadioGroup = new StringRadioGroupFieldDefinition();
        inlineRadioGroup.setInline(Boolean.TRUE);
        inlineRadioGroup.getOptions().add(new StringSelectorOption("op1",
                                                                   "op1",
                                                                   true));
        inlineRadioGroup.getOptions().add(new StringSelectorOption("op2",
                                                                   "op2",
                                                                   false));

        return Arrays.asList(initFieldDefinition(radioGroup),
                             initFieldDefinition(inlineRadioGroup));
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
