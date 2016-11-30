package org.livespark.formmodeler.codegen.flow.impl;

import org.junit.Before;
import org.junit.Test;
import org.kie.workbench.common.forms.data.modeller.model.DataObjectFormModel;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jsoltes on 11/24/16.
 */
public class RoasterFlowProducerSourceGeneratorTest {
    private final String FORM_ID = "myFormID";
    private final String FORM_NAME = "myFormName";
    private final String FORM_URI = "/test/";
    private final String PACKAGE = "org.juraj.mypackage";
    private final String CLASS_ANNOTATION = "@EntryPoint";
    private final String FULL_CLASS_NAME = "public class myFormNameFlowProducer extends FlowProducer<myFormName, myFormNameFormModel, myFormNameFormView, myFormNameListView, myFormNameRestService>";

    private final int PACKAGE_INDEX = 8;
    private final List<String> ABSTRACT_METHODS = Arrays.asList("modelToFormModel", "formModelToModel", "newModel", "getModelType", "getFormModelType");
    private final List<String> ABSTRACT_METHODS_IMPLEMENTATIONS = Arrays
            .asList("return new myFormNameFormModel(model)",
                    "return formModel.getmyFormName()",
                    "return new myFormName()",
                    "return myFormName.class",
                    "return myFormNameFormModel.class");
    private final List<String> PRODUCER_METHODS = Arrays.asList("entityType", "create", "crud", "createAndReview", "view");
    private final List<String> PRODUCER_METHODS_IMPLEMENTATIONS = Arrays
            .asList("return myFormName.class",
                    "return super.create()",
                    "return super.crud()",
                    "return super.createAndReview()",
                    "return super.view()");

    private final List<String> IMPORTS = Arrays.asList("org.juraj.mypackage.myFormName", "org.juraj.mypackage.myFormNameFormModel", "org.juraj.mypackage.myFormNameRestService", "org.livespark.flow.api.Unit", "java.util.Optional");

    private int gslen;
    private String generatedSource;
    private RoasterFlowProducerSourceGenerator sourceGenerator;
    private FormDefinition formDefinition;
    private Path path;
    private org.guvnor.common.services.project.model.Package root;
    private org.guvnor.common.services.project.model.Package local;
    private org.guvnor.common.services.project.model.Package shared;
    private List<FormDefinition> projectForms;
    private SourceGenerationContext context;

    @Before
    public void setUp() {
        sourceGenerator = new RoasterFlowProducerSourceGenerator();

        formDefinition = new FormDefinition();
        formDefinition.setId(FORM_ID);
        formDefinition.setName(FORM_NAME);

        formDefinition.setModel(new DataObjectFormModel("employee", "org.juraj.mypackage.Employee"));

        path = PathFactory.newPath(FORM_NAME, FORM_URI);
        shared = new org.guvnor.common.services.project.model.Package(path, null, null, null, null, PACKAGE, "caption", "relativeCaption");
        local = new org.guvnor.common.services.project.model.Package(path, null, null, null, null, PACKAGE, "caption", "relativeCaption");

        context = new SourceGenerationContext(formDefinition, path, root, local, shared, null, projectForms);
        generatedSource = sourceGenerator.generateJavaSource(context);
        gslen = generatedSource.length();
    }

    @Test
    public void generateJavaSource() {
        assertThat(getImports()).containsAll(IMPORTS);

        assertThat(getPackage()).isEqualTo(PACKAGE);
        assertThat(getClassAnnotation()).isEqualTo(CLASS_ANNOTATION);
        assertThat(getFullClassName()).isEqualTo(FULL_CLASS_NAME);

        assertThat(getAbstractMethodsImplementations()).isEqualTo(ABSTRACT_METHODS_IMPLEMENTATIONS);
        assertThat(getProducerMethodsImplementations()).isEqualTo(PRODUCER_METHODS_IMPLEMENTATIONS);

        Map<String, List<String>> methodsAnnotations = getAllMethodsAnnotations();
        for (String abstractMethod : ABSTRACT_METHODS) {
            assertThat(methodsAnnotations.get(abstractMethod)).isEqualTo(Arrays.asList("Override"));
        }
        for (String producerMethod : PRODUCER_METHODS.subList(1, 5)) {
            assertThat(methodsAnnotations.get(producerMethod)).containsExactlyElementsOf(Arrays.asList("Override", "Produces", "Singleton", "ForEntity(\"org.juraj.mypackage.myFormName\")", "Named(\"" + producerMethod + "\")"));
        }
    }

    private Set<String> getImports() {
        Set<String> imports = new HashSet<>();
        int start = generatedSource.indexOf("import") + 6;
        if (start != 5) {
            int end;
            char nextChar;
            StringBuilder nextImport;
            do {
                nextImport = new StringBuilder();
                end = start + 1;
                do {
                    nextChar = generatedSource.charAt(end++);
                    if (nextChar != ';') {
                        nextImport.append(nextChar);
                    } else {
                        break;
                    }
                } while (true);
                imports.add(nextImport.toString());
                start = end + 7;
            } while (generatedSource.charAt(end + 1) == 'i');
        }
        return imports;
    }

    private String getPackage() {
        return getStringThatEndsWithChar(PACKAGE_INDEX, ';');
    }

    private String getClassAnnotation() {
        return getStringThatEndsWithChar(generatedSource.indexOf("@"), '\n');
    }

    private String getFullClassName() {
        return getStringThatEndsWithChar(generatedSource.indexOf("public"), '\n');
    }

    private List<String> getAbstractMethodsImplementations() {
        return ABSTRACT_METHODS.stream().map(this::getMethodImplementation).collect(Collectors.toList());
    }

    private List<String> getProducerMethodsImplementations() {
        return PRODUCER_METHODS.stream().map(this::getMethodImplementation).collect(Collectors.toList());
    }

    private Map<String, List<String>> getAllMethodsAnnotations() {
        Map<String, List<String>> annotations = new HashMap<>();
        int bodyStartIndex = generatedSource.indexOf('{');
        int start = generatedSource.indexOf('@', bodyStartIndex);
        boolean thereIsMore;
        String annotation;
        while (start != -1) {
            List<String> nextAnnotations = new ArrayList<>();
            do {
                thereIsMore = false;
                annotation = getStringThatEndsWithChar(++start, '\n');
                nextAnnotations.add(annotation);
                start += annotation.length() + 4;
                if (generatedSource.charAt(start) == '@') thereIsMore = true;
            } while (thereIsMore);

            int methodNameEndIndex = generatedSource.indexOf('(', start);
            int methodNameStartIndex = methodNameEndIndex;
            char nextChar = generatedSource.charAt(methodNameStartIndex);
            while (nextChar != ' ') {
                nextChar = generatedSource.charAt(--methodNameStartIndex);
            }
            String methodName = generatedSource.substring(methodNameStartIndex + 1, methodNameEndIndex);
            annotations.put(methodName, nextAnnotations);
            start = generatedSource.indexOf('@', start);
        }
        return annotations;
    }

    private String getMethodImplementation(String methodName) {
        int start = generatedSource.indexOf('{', generatedSource.indexOf(" " + methodName)) + 8;
        return getStringThatEndsWithChar(start, ';');
    }

    private String getStringThatEndsWithChar(int startIndex, char endChar) {
        StringBuilder stringBuilder = new StringBuilder();
        char nextChar;
        for (int i = startIndex; i < gslen; i++) {
            nextChar = generatedSource.charAt(i);
            if (nextChar != endChar) {
                stringBuilder.append(nextChar);
            } else {
                break;
            }
        }
        return stringBuilder.toString();
    }

}