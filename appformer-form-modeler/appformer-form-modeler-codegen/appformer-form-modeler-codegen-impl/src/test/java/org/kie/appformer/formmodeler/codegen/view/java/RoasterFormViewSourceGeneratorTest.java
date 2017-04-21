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

package org.kie.appformer.formmodeler.codegen.view.java;

import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.PropertySource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.view.impl.java.RoasterFormViewSourceGenerator;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.kie.appformer.formmodeler.codegen.view.java.test.TestRoasterFormViewSourceGenerator;
import org.kie.appformer.formmodeler.codegen.view.java.test.util.InputCreatorHelpersProvider;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.checkBox.definition.CheckBoxFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.datePicker.definition.DatePickerFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.slider.definition.DoubleSliderDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.slider.definition.IntegerSliderDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textArea.definition.TextAreaFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textBox.definition.CharacterBoxFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textBox.definition.TextBoxFieldDefinition;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.BEFORE_DISPLAY_METHOD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.DO_EXTRA_VALIDATIONS_METHOD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BOUND;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_DATAFIELD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_TEMPLATED;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.FORM_VIEW_CLASS;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INIT_FORM_METHOD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INJECT_INJECT;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INJECT_NAMED;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.JAVA_LANG_OVERRIDE;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.SET_READONLY_METHOD;

@RunWith(MockitoJUnitRunner.class)
public class RoasterFormViewSourceGeneratorTest extends AbstractRoasterFormGenerationTest {

    protected RoasterFormViewSourceGenerator generator;

    @Override
    public void initTest() {
        super.initTest();

        generator = new TestRoasterFormViewSourceGenerator(classSource,
                                                           creatorHelpers);
    }

    @Override
    protected List<FieldDefinition> getFieldsToTest() {
        List<FieldDefinition> fields = new ArrayList<>();

        fields.add(initFieldDefinition(new TextBoxFieldDefinition(), "textbox"));
        fields.add(initFieldDefinition(new TextAreaFieldDefinition(), "textarea"));
        fields.add(initFieldDefinition(new CheckBoxFieldDefinition(), "checkbox"));
        fields.add(initFieldDefinition(new CharacterBoxFieldDefinition(), "characterbox"));
        fields.add(initFieldDefinition(new DatePickerFieldDefinition(), "datepicker"));

        DatePickerFieldDefinition date = new DatePickerFieldDefinition();
        date.setShowTime(Boolean.FALSE);
        fields.add(initFieldDefinition(date, "shortdate"));

        fields.add(initFieldDefinition(new IntegerSliderDefinition(), "intslider"));
        fields.add(initFieldDefinition(new DoubleSliderDefinition(), "doubleslider"));

        return fields;
    }

    protected FieldDefinition initFieldDefinition(FieldDefinition fieldDefinition, String name) {
        fieldDefinition.setName(name);
        fieldDefinition.setId(name);
        fieldDefinition.setBinding(name);
        return fieldDefinition;
    }

    @Override
    protected List<InputCreatorHelper> getInputHelpersToTest() {
        return InputCreatorHelpersProvider.getAllInputCreatorHelpers();
    }

    @Test
    public void testFormViewGeneration() {
        String result = generator.generateJavaSource(context);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertNotNull(classSource.getAnnotation(ERRAI_TEMPLATED));
        assertNotNull(classSource.getAnnotation(INJECT_NAMED));

        assertEquals(classSource.getSuperType(), FORM_VIEW_CLASS + "<" + FORM_NAME + "," + FORM_NAME + SourceGenerationContext.FORM_MODEL_SUFFIX + ">");

        checkMethod(INIT_FORM_METHOD);
        checkMethod(BEFORE_DISPLAY_METHOD);
        checkMethod(DO_EXTRA_VALIDATIONS_METHOD);
        String readonlyMethodSource = checkMethod(SET_READONLY_METHOD, boolean.class.getName()).getBody();

        formDefinition.getFields().forEach(fieldDefinition -> {
            InputCreatorHelper helper = helpers.stream().filter(inputCreatorHelper -> inputCreatorHelper.getSupportedFieldTypeCode().equals(fieldDefinition.getFieldType().getTypeName())).findFirst().orElse(null);

            assertNotNull(helper);

            // it should exist a property for each form field
            PropertySource<JavaClassSource> property = classSource.getProperty(fieldDefinition.getName());

            assertNotNull(property);

            assertNotNull(property.getAnnotation(ERRAI_DATAFIELD));
            assertNotNull(property.getAnnotation(ERRAI_BOUND));
            if (helper.isInputInjectable()) {
                assertNotNull(property.getAnnotation(INJECT_INJECT));
            }

            assertTrue(readonlyMethodSource.contains(fieldDefinition.getName() + "."));
        });
    }

    protected MethodSource checkMethod(String methodName, String... paramTypes) {
        MethodSource<JavaClassSource> method = classSource.getMethod(methodName, paramTypes);
        assertNotNull(method);
        assertNotNull(method.getAnnotation(JAVA_LANG_OVERRIDE));
        assertNotNull(method.getBody());
        return method;
    }
}
