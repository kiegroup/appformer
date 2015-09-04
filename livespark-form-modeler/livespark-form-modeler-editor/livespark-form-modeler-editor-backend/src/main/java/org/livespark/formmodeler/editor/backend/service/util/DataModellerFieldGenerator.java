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
package org.livespark.formmodeler.editor.backend.service.util;

import org.apache.commons.lang3.ArrayUtils;
import org.kie.workbench.common.screens.datamodeller.model.maindomain.MainDomainAnnotations;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.livespark.formmodeler.codegen.util.SourceGenerationUtil;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.AbstractIntputFieldDefinition;
import org.livespark.formmodeler.editor.service.FieldManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pefernan on 8/28/15.
 */
public class DataModellerFieldGenerator {
    public static final String[] RESTRICTED_PROPERTY_NAMES = new String[]{"serialVersionUID"};

    @Inject
    private FieldManager fieldManager;

    public List<FieldDefinition> getFieldsFromDataObject(String holderName, DataObject dataObject) {
        List<FieldDefinition> result = new ArrayList<FieldDefinition>( );
        if (dataObject != null) {
            for (ObjectProperty property : dataObject.getProperties()) {
                if ( ArrayUtils.contains( RESTRICTED_PROPERTY_NAMES, property.getName() ) ) continue;

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

                if (field instanceof AbstractIntputFieldDefinition ) {
                    ((AbstractIntputFieldDefinition) field).setPlaceHolder( label );
                }
                result.add( field );
            }
        }
        return result;
    }

    private String getPropertyLabel( ObjectProperty property ) {
        Annotation labelAnnotation = property.getAnnotation( MainDomainAnnotations.LABEL_ANNOTATION );
        if ( labelAnnotation != null ) return labelAnnotation.getValue( MainDomainAnnotations.VALUE_PARAM ).toString();

        return property.getName();
    }
}
