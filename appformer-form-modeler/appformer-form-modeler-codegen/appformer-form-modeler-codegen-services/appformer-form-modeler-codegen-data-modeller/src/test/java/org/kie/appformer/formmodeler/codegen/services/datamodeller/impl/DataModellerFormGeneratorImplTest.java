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

package org.kie.appformer.formmodeler.codegen.services.datamodeller.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.appformer.formmodeler.codegen.FormSourcesGenerator;
import org.kie.appformer.formmodeler.codegen.services.datamodeller.impl.model.Address;
import org.kie.workbench.common.forms.data.modeller.model.DataObjectFormModel;
import org.kie.workbench.common.forms.data.modeller.service.DataObjectFinderService;
import org.kie.workbench.common.forms.data.modeller.service.impl.DataObjectFinderServiceImpl;
import org.kie.workbench.common.forms.data.modeller.service.impl.DataObjectFormModelHandler;
import org.kie.workbench.common.forms.editor.service.shared.VFSFormFinderService;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textBox.definition.TextBoxFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.multipleSubform.definition.MultipleSubFormFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.subForm.definition.SubFormFieldDefinition;
import org.kie.workbench.common.forms.fields.test.TestFieldManager;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.kie.workbench.common.forms.model.FormModel;
import org.kie.workbench.common.forms.model.ModelProperty;
import org.kie.workbench.common.forms.service.shared.FieldManager;
import org.kie.workbench.common.screens.datamodeller.backend.server.handler.JPADomainHandler;
import org.kie.workbench.common.screens.datamodeller.model.maindomain.MainDomainAnnotations;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.backend.project.ProjectClassLoaderHelper;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.kie.workbench.common.services.datamodeller.core.impl.AnnotationDefinitionImpl;
import org.kie.workbench.common.services.datamodeller.core.impl.AnnotationImpl;
import org.kie.workbench.common.services.datamodeller.core.impl.DataModelImpl;
import org.kie.workbench.common.services.datamodeller.core.impl.PropertyTypeFactoryImpl;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataModellerFormGeneratorImplTest {

    static final int NESTED_FORM_FIELDS = 2;
    static final int BASE_FORM_FIELDS = 24;
    static final int EXPECTED_FIELDS = BASE_FORM_FIELDS + NESTED_FORM_FIELDS;

    @Mock
    DataModelerService dataModelerService;

    @Mock
    KieProjectService projectService;

    FieldManager fieldManager = new TestFieldManager();

    @Mock
    FormSourcesGenerator formSourcesGenerator;

    @Mock
    Path path;

    DataObjectFinderService finderService;

    @Mock
    KieProject project;

    @Mock
    ProjectClassLoaderHelper projectClassLoaderHelper;

    DataObjectFormModelHandler formModelHandler;

    @Mock
    VFSFormFinderService vfsFormFinderService;

    DataModellerFormGeneratorImpl dataModellerFormGenerator;

    DataModel dataModel;

    DataObject dataObject;

    @Before
    public void init() {

        createModel();

        when(vfsFormFinderService.findFormsForType(Address.class.getName(),
                                                   path)).then(this::getAddressForm);
        when(projectService.resolveProject(any())).thenReturn(project);
        when(projectClassLoaderHelper.getProjectClassLoader(project)).thenReturn(this.getClass().getClassLoader());
        when(dataModelerService.loadModel(any())).thenReturn(dataModel);

        finderService = new DataObjectFinderServiceImpl(projectService,
                                                        dataModelerService);

        formModelHandler = spy(new DataObjectFormModelHandler(projectService,
                                                              projectClassLoaderHelper,
                                                              finderService,
                                                              fieldManager));

        dataModellerFormGenerator = spy(new DataModellerFormGeneratorImpl(dataModelerService,
                                                                          projectService,
                                                                          fieldManager,
                                                                          formSourcesGenerator,
                                                                          formModelHandler,
                                                                          vfsFormFinderService));
    }

    List<FormDefinition> getAddressForm(InvocationOnMock invocationOnMock) {
        List<FormDefinition> forms = new ArrayList<>();
        FormDefinition form = new FormDefinition();
        form.setId(Address.class.getName());

        TextBoxFieldDefinition address = new TextBoxFieldDefinition();
        address.setName("address");
        address.setId("address");
        address.setBinding("address");
        address.setPlaceHolder("address");

        form.getFields().add(address);

        SubFormFieldDefinition city = new SubFormFieldDefinition();
        city.setName("city");
        city.setId("city");
        city.setBinding("city");

        form.getFields().add(city);

        forms.add(form);

        return forms;
    }

    @Test
    public void testFormGeneration() {
        FormDefinition form = dataModellerFormGenerator.generateFormForDataObject(dataObject,
                                                                                  path);

        assertNotNull(form);

        FormModel model = form.getModel();

        assertNotNull(model);

        assertTrue(model instanceof DataObjectFormModel);

        DataObjectFormModel dataObjectFormModel = (DataObjectFormModel) model;

        // checking model has the same number of properties than dataObject (dataObject has 2 more, persistence id &  serialVersionUID that won't be on the FormModel)
        assertEquals(dataObject.getProperties().size() - 2,
                     dataObjectFormModel.getProperties().size());

        assertEquals(dataObjectFormModel.getProperties().size(),
                     form.getFields().size());

        dataObjectFormModel.getProperties().forEach(property -> validateFormField(property,
                                                                                  form));

        verify(formModelHandler).createFormModel(dataObject,
                                                 path);
        verify(formModelHandler).init(any(),
                                      any());
        verify(formModelHandler).getAllFormModelFields();

        verify(dataModellerFormGenerator,
               times(NESTED_FORM_FIELDS)).loadEmbeddedFormConfig(any(),
                                                                 any());

        verify(formSourcesGenerator).generateEntityFormSources(any(),
                                                               any());
    }

    protected void validateFormField(ModelProperty property,
                                     FormDefinition form) {
        FieldDefinition field = form.getFieldByBoundProperty(property);

        assertNotNull(field);
        assertEquals(property.getName(),
                     field.getName());
        assertEquals(property.getName(),
                     field.getBinding());
        assertEquals(property.getTypeInfo().getClassName(),
                     field.getStandaloneClassName());
        assertEquals(fieldManager.getDefinitionByDataType(property.getTypeInfo()).getClass(),
                     field.getClass());
        if (field instanceof SubFormFieldDefinition) {
            SubFormFieldDefinition subFormFieldDefinition = (SubFormFieldDefinition) field;
            assertNotNull(subFormFieldDefinition.getNestedForm());
            assertEquals(Address.class.getName(), subFormFieldDefinition.getNestedForm());
        } else if (field instanceof MultipleSubFormFieldDefinition) {
            MultipleSubFormFieldDefinition multipleSubFormFieldDefinition = (MultipleSubFormFieldDefinition) field;
            assertNotNull(multipleSubFormFieldDefinition.getCreationForm());
            assertEquals(Address.class.getName(), multipleSubFormFieldDefinition.getCreationForm());
            assertNotNull(multipleSubFormFieldDefinition.getEditionForm());
            assertEquals(Address.class.getName(), multipleSubFormFieldDefinition.getEditionForm());
            assertNotNull(multipleSubFormFieldDefinition.getColumnMetas());
            assertFalse(multipleSubFormFieldDefinition.getColumnMetas().isEmpty());
        }
    }

    void createModel() {
        dataModel = new DataModelImpl();
        dataObject = dataModel.addDataObject("Person1");

        //makeTheClassPersistable
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("persistable",
                   true);
        JPADomainHandler jpaDomainHandler = new JPADomainHandler();
        jpaDomainHandler.setDefaultValues(dataObject,
                                          params);

        // adding serialVersionUID field
        addProperty(dataObject,
                    DataObjectFormModelHandler.SERIAL_VERSION_UID,
                    Long.class.getName(),
                    false);

        //add all base type properties
        PropertyTypeFactoryImpl propertyTypeFactory = new PropertyTypeFactoryImpl();

        propertyTypeFactory.getBasePropertyTypes().forEach(baseProperty -> addProperty(dataObject,
                                                                                       baseProperty.getName(),
                                                                                       baseProperty.getClassName(),
                                                                                       false));

        //add data object property
        addProperty(dataObject,
                    "address",
                    Address.class.getName(),
                    false);

        //add list of data objects
        addProperty(dataObject,
                    "address_list",
                    Address.class.getName(),
                    true);
    }

    protected void addProperty(DataObject dataObject,
                               String propertyName,
                               String className,
                               boolean multiple) {

        ObjectProperty property = dataObject.addProperty(propertyName,
                                                         className,
                                                         multiple);

        Annotation labelAnnotation = new AnnotationImpl(new AnnotationDefinitionImpl(MainDomainAnnotations.LABEL_ANNOTATION));
        labelAnnotation.setValue(MainDomainAnnotations.VALUE_PARAM,
                                 propertyName);
        property.addAnnotation(labelAnnotation);
    }
}
