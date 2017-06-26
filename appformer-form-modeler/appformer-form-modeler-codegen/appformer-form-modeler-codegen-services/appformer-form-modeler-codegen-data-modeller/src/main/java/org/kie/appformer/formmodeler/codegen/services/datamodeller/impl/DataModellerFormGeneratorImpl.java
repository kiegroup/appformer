/*
 * Copyright 2015 JBoss Inc
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
import java.util.List;
import javax.inject.Inject;

import org.kie.appformer.formmodeler.codegen.FormSourcesGenerator;
import org.kie.appformer.formmodeler.codegen.services.datamodeller.DataModellerFormGenerator;
import org.kie.workbench.common.forms.data.modeller.model.DataObjectFormModel;
import org.kie.workbench.common.forms.data.modeller.service.impl.DataObjectFormModelHandler;
import org.kie.workbench.common.forms.editor.service.shared.VFSFormFinderService;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.EmbedsForm;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.EntityRelationField;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.TableColumnMeta;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.multipleSubform.definition.MultipleSubFormFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.subForm.definition.SubFormFieldDefinition;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.kie.workbench.common.forms.service.shared.FieldManager;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;

public class DataModellerFormGeneratorImpl implements DataModellerFormGenerator {

    private static transient Logger log = LoggerFactory.getLogger(DataModellerFormGeneratorImpl.class);

    protected DataModelerService dataModelerService;

    protected KieProjectService projectService;

    protected FieldManager fieldManager;

    protected FormSourcesGenerator formSourcesGenerator;

    protected DataObjectFormModelHandler formModelHandler;

    protected VFSFormFinderService vfsFormFinderService;

    @Inject
    public DataModellerFormGeneratorImpl(DataModelerService dataModelerService,
                                         KieProjectService projectService,
                                         FieldManager fieldManager,
                                         FormSourcesGenerator formSourcesGenerator,
                                         DataObjectFormModelHandler formModelHandler,
                                         VFSFormFinderService vfsFormFinderService) {
        this.dataModelerService = dataModelerService;
        this.projectService = projectService;
        this.fieldManager = fieldManager;
        this.formSourcesGenerator = formSourcesGenerator;
        this.formModelHandler = formModelHandler;
        this.vfsFormFinderService = vfsFormFinderService;
    }

    @Override
    public FormDefinition generateFormForDataObject(DataObject dataObject,
                                          Path path) {

        if (dataObject.getProperties().isEmpty()) {
            return null;
        }

        DataObjectFormModel model = formModelHandler.createFormModel(dataObject,
                                                                     path);

        FormDefinition form = new FormDefinition(model);

        form.setId(dataObject.getClassName());

        form.setName(dataObject.getName());

        formModelHandler.init(model,
                              path);


        formModelHandler.getAllFormModelFields().forEach(field -> {
            if (field instanceof EmbedsForm) {
                if (!loadEmbeddedFormConfig(field,
                                            path)) {
                    return;
                }
            }
            form.getFields().add(field);
        });

        formSourcesGenerator.generateEntityFormSources(form,
                                                       path);

        return form;
    }

    protected boolean loadEmbeddedFormConfig(FieldDefinition field,
                                             Path path) {
        if (!(field instanceof EmbedsForm)) {
            return false;
        }

        List<FormDefinition> subForms = vfsFormFinderService.findFormsForType(field.getStandaloneClassName(),
                                                                              path);

        if (subForms == null || subForms.isEmpty()) {
            return false;
        }

        if (field instanceof MultipleSubFormFieldDefinition) {
            MultipleSubFormFieldDefinition multipleSubFormFieldDefinition = (MultipleSubFormFieldDefinition) field;
            FormDefinition form = subForms.get(0);
            multipleSubFormFieldDefinition.setCreationForm(form.getId());
            multipleSubFormFieldDefinition.setEditionForm(form.getId());

            List<TableColumnMeta> columnMetas = new ArrayList<>();
            for (FieldDefinition nestedField : form.getFields()) {
                if (nestedField instanceof EntityRelationField) {
                    continue;
                }
                TableColumnMeta meta = new TableColumnMeta(nestedField.getLabel(),
                                                           nestedField.getBinding());
                columnMetas.add(meta);
            }

            multipleSubFormFieldDefinition.setColumnMetas(columnMetas);
        } else {
            SubFormFieldDefinition subFormField = (SubFormFieldDefinition) field;
            subFormField.setNestedForm(subForms.get(0).getId());
        }

        return true;
    }
}
