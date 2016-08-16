/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.livespark.formmodeler.codegen.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.kie.workbench.common.forms.model.DataHolder;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;

/**
 *
 * @author jsoltes
 */
public class RoasterFormModelSourceGeneratorTest {

    private final String FORM_ID = "myFormID";
    private final String FORM_NAME = "myFormName";
    private final String FORM_URI = "/test/";
    private final String PACKAGE_NAME = "org.juraj.mypackage";
    private final List<String> FIELD_NAMES = Arrays.asList("Employee", "Age", "Salary");
    private final List<String> FIELD_TYPES = Arrays.asList("String", "int", "Double");
    private final List<String> FIELD_ANNOTATIONS = Arrays.asList("Valid", "NotNull", "Valid");
    private final List<String> FIELDS = Arrays.asList("String Employee", "int Age", "Double Salary");
    private final int NAMED_PARAMETER_INDEX = 485;
    private final int PACKAGE_NAME_INDEX = 8;
    private final int CLASS_NAME_INDEX = 520;
    private int gslen;
    private String generatedSource;

    private RoasterFormModelSourceGenerator sourceGenerator;
    private FormDefinition formDefinition;
    private Path path;
    private org.guvnor.common.services.project.model.Package root;
    private org.guvnor.common.services.project.model.Package local;
    private org.guvnor.common.services.project.model.Package shared;
    private List<FormDefinition> projectForms;
    private SourceGenerationContext context;

    @Before
    public void init() {
        sourceGenerator = new RoasterFormModelSourceGenerator(new ConstructorGenerator());

        formDefinition = new FormDefinition();
        formDefinition.setId(FORM_ID);
        formDefinition.setName(FORM_NAME);
        for (int i = 0; i < 3; i++) {
            formDefinition.addDataHolder(new DataHolder(FIELD_NAMES.get(i), FIELD_TYPES.get(i)));
        }
        path = PathFactory.newPath(FORM_NAME, FORM_URI);
        shared = new org.guvnor.common.services.project.model.Package(path, null, null, null, null, PACKAGE_NAME, "caption", "relativeCaption");

        context = new SourceGenerationContext(formDefinition, path, root, local, shared, null, projectForms);
        generatedSource = sourceGenerator.generateFormModelSource(context);
        gslen = generatedSource.length();
    }

    @Test
    public void generateFormModelSourceTest() {
        assertEquals(PACKAGE_NAME, getPackagename(generatedSource));
        assertEquals(FORM_NAME + SourceGenerationContext.FORM_MODEL_SUFFIX, getNamedParameter(generatedSource));
        assertEquals(FIELDS, getFields(generatedSource));
        assertEquals(FIELD_ANNOTATIONS, getFieldAnnotations(generatedSource, FIELDS));
        assertEquals(FIELD_NAMES, getConstructorFields(generatedSource));
    }

    private String getStringThatEndsWithBreakPoint(int start, char breakPoint, String generatedSource) {
        return generatedSource.substring(start, generatedSource.indexOf(breakPoint, start));
    }

    private String getPackagename(String generatedSource) {
        return getStringThatEndsWithBreakPoint(PACKAGE_NAME_INDEX, ';', generatedSource);
    }

    private String getNamedParameter(String generatedSource) {
        return getStringThatEndsWithBreakPoint(NAMED_PARAMETER_INDEX, '"', generatedSource);
    }

    private List<String> getAllStringsThatStartAfterSearchPhrase(String searchPhrase, char breakPoint, String generatedSource) {
        List<String> fields = new ArrayList<>();
        int shift = searchPhrase.length();
        int nextIndex = generatedSource.indexOf(searchPhrase, CLASS_NAME_INDEX) + shift;
        while (nextIndex != -1 + shift && nextIndex < gslen) {
            fields.add(getStringThatEndsWithBreakPoint(nextIndex, breakPoint, generatedSource));
            nextIndex = generatedSource.indexOf(searchPhrase, nextIndex + 1) + shift;
        }
        return fields;
    }

    private List<String> getFields(String generatedSource) {
        return getAllStringsThatStartAfterSearchPhrase("private ", ';', generatedSource);
    }

    private List<String> getConstructorFields(String generatedSource) {
        return getAllStringsThatStartAfterSearchPhrase("@MapsTo(\"", '"', generatedSource);
    }

    private List<String> getFieldAnnotations(String generatedSource, List<String> fields) {
        List<String> fieldAnnotations = new ArrayList<>();
        int shift = "@".length();
        int start = generatedSource.indexOf("@", CLASS_NAME_INDEX) + shift;
        for (String f : fields) {
            fieldAnnotations.add(getStringThatEndsWithBreakPoint(start, '\n', generatedSource));
            start = generatedSource.indexOf("@", start + 1) + shift;
        }
        return fieldAnnotations;
    }
}
