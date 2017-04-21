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

package org.kie.appformer.formmodeler.codegen.view.java.inputs;

import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.Before;
import org.junit.Test;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.kie.appformer.formmodeler.codegen.view.java.AbstractRoasterFormGenerationTest;
import org.kie.appformer.formmodeler.codegen.view.java.test.TestRoasterFormViewSourceGenerator;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.EmbedsForm;
import org.kie.workbench.common.forms.model.FieldDefinition;

import static org.junit.Assert.*;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BOUND;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_DATAFIELD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INJECT_INJECT;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.READONLY_PARAM;

public abstract class AbstractInputHelperTest extends AbstractRoasterFormGenerationTest {

    protected TestRoasterFormViewSourceGenerator roasterJavaTemplateSourceGenerator;

    protected int fieldCount = 0;

    @Before
    public void initTest() {
        super.initTest();

        roasterJavaTemplateSourceGenerator = new TestRoasterFormViewSourceGenerator(classSource,
                                                                                    creatorHelpers);

        String template = roasterJavaTemplateSourceGenerator.generateJavaSource(context);

        assertNotNull("Form template shouldn't be null",
                      template);
    }

    @Test
    public void testSourceGenerationForInputs() {
        for (FieldDefinition field : formDefinition.getFields()) {
            runFieldTests(field,
                          roasterJavaTemplateSourceGenerator.getHelperForField(field));
        }
    }

    protected void runFieldTests(FieldDefinition field,
                                 InputCreatorHelper helper) {
        FieldSource<JavaClassSource> fieldSource = classSource.getField(field.getName());

        assertNotNull("There is no property defined for the field!",
                      fieldSource);

        String propertyDeclaration = helper.getInputWidget(field);

        if (propertyDeclaration.indexOf(".") != -1) {
            // using qualified className to initialize the input
            assertEquals("Property type doesn't match",
                         helper.getInputWidget(field),
                         fieldSource.getType().getQualifiedName());
        } else {
            // checking if input has custom initilization
            assertEquals("Property type doesn't match",
                         helper.getInputWidget(field),
                         fieldSource.getType().toString());
        }

        if (!(field instanceof EmbedsForm)) {
            AnnotationSource boundAnnotation = fieldSource.getAnnotation(ERRAI_BOUND);
            assertNotNull("@Bound annotation missing for field",
                          boundAnnotation);
            assertEquals("Invalid binding expression for field",
                         formDefinition.getModel().getName() + "." + field.getBinding(),
                         boundAnnotation.getStringValue("property"));
        }

        assertNotNull("Missing @DataField annotation for field",
                      fieldSource.getAnnotation(ERRAI_DATAFIELD));

        if (helper.isInputInjectable()) {
            assertNotNull("Missing @Ïnject annotation for Field",
                          fieldSource.getAnnotation(INJECT_INJECT));
        } else {
            testInitializerMethod(field,
                                  fieldSource,
                                  helper);
        }

        testReadOnlyMethod(field,
                           helper);
    }

    protected FieldDefinition initFieldDefinition(FieldDefinition field) {
        field.setName("test" + fieldCount);
        field.setBinding("test" + fieldCount);
        fieldCount++;
        return field;
    }

    protected void testInitializerMethod(FieldDefinition field,
                                         FieldSource fieldSource,
                                         InputCreatorHelper helper) {
        String initializer = fieldSource.getLiteralInitializer();

        assertNotNull("Field doesn't have any initialization!",
                      initializer);

        initializer = removeEmptySpaces(initializer);

        if (!initializer.endsWith(";")) {
            initializer += ";";
        }

        String helperInitializer = helper.getInputInitLiteral(context,
                                                              field);

        assertNotNull("Helper is unable to generate initialization literal for field",
                      helperInitializer);

        helperInitializer = removeEmptySpaces(helperInitializer);

        assertEquals("Initialization literal on class doesn't match the one generated by the Helper",
                     helperInitializer,
                     initializer);
    }

    protected void testReadOnlyMethod(FieldDefinition field,
                                      InputCreatorHelper helper) {
        MethodSource<JavaClassSource> readonlyMethod = classSource.getMethod("setReadOnly",
                                                                             boolean.class.getName());

        assertNotNull("The class must have a setReadOnly method!",
                      readonlyMethod);

        String body = removeEmptySpaces(readonlyMethod.getBody());

        String fieldReadOnly = removeEmptySpaces(helper.getReadonlyMethod(field.getName(),
                                                                          READONLY_PARAM));

        assertTrue("Field missin on ReadOnlyMethod",
                   body.contains(fieldReadOnly));
    }

    protected String removeEmptySpaces(String str) {
        return str.replaceAll("\\s",
                              "");
    }
}
