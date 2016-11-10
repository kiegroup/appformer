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

import org.junit.Before;
import org.junit.Test;
import org.kie.workbench.common.forms.data.modeller.model.DataObjectFormModel;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;

import static junit.framework.TestCase.*;

/**
 *
 * @author jsoltes
 */
public class RoasterFormModelSourceGeneratorTest {

    private final String FORM_ID = "myFormID";
    private final String FORM_NAME = "myFormName";
    private final String FORM_URI = "/test/";
    private final String PACKAGE_NAME = "org.juraj.mypackage";
    private final List<String> FIELD_NAMES = Arrays.asList("employee");
    private final List<String> FIELD_ANNOTATIONS = Arrays.asList("Valid");
    private final List<String> FIELDS = Arrays.asList("Employee employee");
    private final int PACKAGE_NAME_INDEX = 8;
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

        formDefinition.setModel( new DataObjectFormModel( "employee", "org.juraj.mypackage.Employee" ) );

        path = PathFactory.newPath(FORM_NAME, FORM_URI);
        shared = new org.guvnor.common.services.project.model.Package(path, null, null, null, null, PACKAGE_NAME, "caption", "relativeCaption");

        context = new SourceGenerationContext(formDefinition, path, root, local, shared, null, projectForms);
        generatedSource = sourceGenerator.generateJavaSource(context);
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

    private String getStringThatEndsWithBreakPoint(final int start, final char breakPoint, final String generatedSource) {
        return generatedSource.substring(start, generatedSource.indexOf(breakPoint, start));
    }

    private String getPackagename(final String generatedSource) {
        return getStringThatEndsWithBreakPoint(PACKAGE_NAME_INDEX, ';', generatedSource);
    }

    private String getNamedParameter(final String generatedSource) {
        return getStringThatEndsWithBreakPoint(generatedSource.indexOf( "@Named(\"" ) + 8, '"', generatedSource );
    }

    private List<String> getAllStringsThatStartAfterSearchPhrase(final String searchPhrase, final char breakPoint, final String generatedSource) {
        final List<String> fields = new ArrayList<>();
        final int shift = searchPhrase.length();
        int nextIndex = generatedSource.indexOf(searchPhrase, generatedSource.indexOf( "{" )) + shift;
        while (nextIndex != -1 + shift && nextIndex < gslen) {
            fields.add(getStringThatEndsWithBreakPoint(nextIndex, breakPoint, generatedSource));
            nextIndex = generatedSource.indexOf(searchPhrase, nextIndex + 1) + shift;
        }
        return fields;
    }

    private List<String> getFields(final String generatedSource) {
        return getAllStringsThatStartAfterSearchPhrase("private ", ';', generatedSource);
    }

    private List<String> getConstructorFields(final String generatedSource) {
        return getAllStringsThatStartAfterSearchPhrase("@MapsTo(\"", '"', generatedSource);
    }

    private List<String> getFieldAnnotations(final String generatedSource, final List<String> fields) {
        final List<String> fieldAnnotations = new ArrayList<>();
        final int shift = "@".length();
        int start = generatedSource.indexOf("@", generatedSource.indexOf( "{" )) + shift;
        for (final String f : fields) {
            fieldAnnotations.add(getStringThatEndsWithBreakPoint(start, '\n', generatedSource));
            start = generatedSource.indexOf("@", start + 1) + shift;
        }
        return fieldAnnotations;
    }
}
