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

package org.livespark.formmodeler.codegen.services.datamodeller.impl;

import org.apache.commons.lang3.text.WordUtils;
import org.kie.workbench.common.screens.datamodeller.model.maindomain.MainDomainAnnotations;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.FormSourcesGenerator;
import org.livespark.formmodeler.codegen.services.datamodeller.DataModellerFormGenerator;
import org.livespark.formmodeler.editor.backend.service.util.DataModellerFieldGenerator;
import org.livespark.formmodeler.model.DataHolder;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.MultipleField;
import org.livespark.formmodeler.model.impl.relations.EmbeddedFormField;
import org.livespark.formmodeler.service.FieldManager;
import org.livespark.formmodeler.editor.service.FormFinderSerivce;
import org.livespark.formmodeler.editor.service.SubFormData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by pefernan on 4/29/15.
 */
public class DataModellerFormGeneratorImpl implements DataModellerFormGenerator {
    private static transient Logger log = LoggerFactory.getLogger( DataModellerFormGeneratorImpl.class );

    @Inject
    protected DataModelerService dataModelerService;

    @Inject
    protected KieProjectService projectService;

    @Inject
    protected FieldManager fieldManager;

    @Inject
    protected FormSourcesGenerator formSourcesGenerator;

    @Inject
    protected DataModellerFieldGenerator fieldGenerator;

    @Inject
    protected FormFinderSerivce formFinderSerivce;

    @Override
    public void generateFormForDataObject( DataObject dataObject, Path path ) {

        if (dataObject.getProperties().isEmpty()) return;

        FormDefinition form = formFinderSerivce.getNewFormInstance();

        form.setName( dataObject.getName() );

        String holderName = WordUtils.uncapitalize( dataObject.getName() );

        DataHolder holder = new DataHolder( holderName, dataObject.getClassName() );

        form.addDataHolder( holder );

        List<FieldDefinition> availabeFields = fieldGenerator.getFieldsFromDataObject(holderName, dataObject);

        for (FieldDefinition field : availabeFields ) {
            if (field instanceof EmbeddedFormField) {
                if ( !loadEmbeddedFormConfig( field, path ) ) continue;
            }
            form.getFields().add( field );
        }

        if (form.getFields().isEmpty()) return;

        formSourcesGenerator.generateEntityFormSources(form, path);

    }

    protected boolean loadEmbeddedFormConfig ( FieldDefinition field, Path path ) {
        if ( !(field instanceof EmbeddedFormField) ) return false;

        List<SubFormData> subForms;

        if ( field instanceof  MultipleField ) {
            subForms = formFinderSerivce.getAvailableMultipleSubFormsByType( field.getStandaloneClassName(), path );
        } else {
            subForms = formFinderSerivce.getAvailableFormsByType( field.getStandaloneClassName(), path);
        }

        if ( subForms == null || subForms.isEmpty() ) return false;

        SubFormData data = subForms.get( 0 );

        EmbeddedFormField embeddedFormField = (EmbeddedFormField)field;
        embeddedFormField.setEmbeddedModel( data.getFormModelClass() );
        embeddedFormField.setEmbeddedFormView( data.getViewClass() );

        return true;
    }

    private String getPropertyLabel( ObjectProperty property ) {
        Annotation labelAnnotation = property.getAnnotation( MainDomainAnnotations.LABEL_ANNOTATION );
        if ( labelAnnotation != null ) return labelAnnotation.getValue( MainDomainAnnotations.VALUE_PARAM ).toString();

        return property.getName();
    }
}
