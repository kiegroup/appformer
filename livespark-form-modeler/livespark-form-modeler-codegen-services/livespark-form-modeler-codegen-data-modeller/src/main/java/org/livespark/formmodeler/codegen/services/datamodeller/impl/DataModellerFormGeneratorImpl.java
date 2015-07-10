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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.kie.workbench.common.screens.datamodeller.model.maindomain.MainDomainAnnotations;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.FormSourcesGenerator;
import org.livespark.formmodeler.codegen.services.datamodeller.DataModellerFormGenerator;
import org.livespark.formmodeler.codegen.util.SourceGenerationUtil;
import org.livespark.formmodeler.editor.model.DataHolder;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.FormDefinition;
import org.livespark.formmodeler.editor.model.MultipleField;
import org.livespark.formmodeler.editor.model.impl.basic.AbstractIntputFieldDefinition;
import org.livespark.formmodeler.editor.model.impl.relations.EmbeddedFormField;
import org.livespark.formmodeler.editor.service.FieldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

/**
 * Created by pefernan on 4/29/15.
 */
public class DataModellerFormGeneratorImpl implements DataModellerFormGenerator {
    private static transient Logger log = LoggerFactory.getLogger( DataModellerFormGeneratorImpl.class );

    public static final String[] RESTRICTED_PROPERTY_NAMES = new String[]{"serialVersionUID"};

    @Inject
    protected DataModelerService dataModelerService;

    @Inject
    protected KieProjectService projectService;

    @Inject
    protected FieldManager fieldManager;

    @Inject
    protected FormSourcesGenerator formSourcesGenerator;

    @Override
    public void generateFormForDataObject( DataObject dataObject, Path path ) {

        if (dataObject.getProperties().isEmpty()) return;

        DataModel model = dataModelerService.loadModel( projectService.resolveProject( path ) );

        FormDefinition form = new FormDefinition();
        form.setName( dataObject.getName() );

        String holderName = WordUtils.uncapitalize( dataObject.getName() );

        DataHolder holder = new DataHolder( holderName, dataObject.getClassName() );

        form.addDataHolder( holder );

        for (ObjectProperty property : dataObject.getProperties()) {
            if ( ArrayUtils.contains(RESTRICTED_PROPERTY_NAMES, property.getName()) ) continue;

            String propertyName = holderName + "_" + property.getName();
            FieldDefinition field = null;

            if (property.getBag() == null) field = fieldManager.getDefinitionByValueType( property.getClassName() );
            else field = fieldManager.getDefinitionByValueType( property.getBag(), property.getClassName() );

            if (field == null) continue;
            field.setAnnotatedId( property.getAnnotation( SourceGenerationUtil.JAVAX_PERSISTENCE_ID ) != null );


            field.setName( propertyName );
            String label = getPropertyLabel( property );
            field.setLabel( label );
            field.setModelName( holderName );
            field.setBoundPropertyName( property.getName() );

            if (field instanceof AbstractIntputFieldDefinition) {
                ((AbstractIntputFieldDefinition) field).setPlaceHolder( label );
            } else if (field instanceof EmbeddedFormField) {
                EmbeddedFormField embeddedForm = ( EmbeddedFormField ) field;

                String viewType;

                if (embeddedForm instanceof MultipleField) viewType = LIST_VIEW_CLASS;
                else viewType = FORM_VIEW_CLASS;

                if ( !loadEmbeddedFormConfig( embeddedForm, viewType, model ) ) continue;
            }
            form.getFields().add( field );
        }

        if (form.getFields().isEmpty()) return;

        formSourcesGenerator.generateFormSources( form, path );

    }

    protected boolean loadEmbeddedFormConfig( EmbeddedFormField embeddedForm, String viewType, DataModel dataModel ) {
        DataObject subFormModel = null;
        Map<String, String> formViews = new HashMap<String, String>(  );
        for ( DataObject object : dataModel.getDataObjects() ) {
            if ( object.getSuperClassName().equals( FORM_MODEL_CLASS ) &&
                    object.getProperties().size() == 1 &&
                    object.getProperties().get( 0 ).getClassName().equals( embeddedForm.getStandaloneType() )) {
                subFormModel = object;
            } else if (object.getSuperClassName().equals( viewType )) {
                Annotation formModelAnnotation = object.getAnnotation( FORM_MODEL_ANNOTATION );
                if (formModelAnnotation != null) {
                    formViews.put( formModelAnnotation.getValue( MainDomainAnnotations.VALUE_PARAM ).toString(), object.getClassName() );
                }
            }
        }

        if (subFormModel == null || formViews.get( subFormModel.getClassName() ) == null) return false;
        embeddedForm.setEmbeddedModel( subFormModel.getClassName() );
        embeddedForm.setEmbeddedFormView( formViews.get( subFormModel.getClassName() ) );
        return true;
    }

    private String getPropertyLabel( ObjectProperty property ) {
        Annotation labelAnnotation = property.getAnnotation( MainDomainAnnotations.LABEL_ANNOTATION );
        if ( labelAnnotation != null ) return labelAnnotation.getValue( MainDomainAnnotations.VALUE_PARAM ).toString();

        return property.getName();
    }


}
