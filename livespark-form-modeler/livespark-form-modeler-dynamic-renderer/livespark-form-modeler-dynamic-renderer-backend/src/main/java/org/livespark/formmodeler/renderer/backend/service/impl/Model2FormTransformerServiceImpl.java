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

package org.livespark.formmodeler.renderer.backend.service.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.bus.server.annotations.Service;
import org.livespark.formmodeler.codegen.layout.FormLayoutTemplateGenerator;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.annotation.FieldDef;
import org.livespark.formmodeler.model.impl.relations.SubFormFieldDefinition;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.livespark.formmodeler.renderer.service.Model2FormTransformerService;
import org.livespark.formmodeler.renderer.service.impl.DynamicRenderingContext;
import org.livespark.formmodeler.service.FieldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Dependent
public class Model2FormTransformerServiceImpl implements Model2FormTransformerService {

    private static final Logger logger = LoggerFactory.getLogger( Model2FormTransformerServiceImpl.class );

    private FieldManager fieldManager;

    private FormLayoutTemplateGenerator layoutGenerator;

    @Inject
    public Model2FormTransformerServiceImpl( FieldManager fieldManager, FormLayoutTemplateGenerator layoutGenerator) {
        this.fieldManager = fieldManager;
        this.layoutGenerator = layoutGenerator;
    }

    @Override
    public FormRenderingContext createContext( Object model ) {
        DynamicRenderingContext context = new DynamicRenderingContext();

        FormDefinition form = generateFormDefinition( model, context );
        context.setRootForm( form );
        context.setModel( model );

        return context;
    }

    public FormDefinition generateFormDefinition( Object model, DynamicRenderingContext context ) {
        if ( model == null) {
            return null;
        }
        return generateFormDefinition( model.getClass(), context );
    }

    protected FormDefinition generateFormDefinition( Class clazz, DynamicRenderingContext context ) {
        FormDefinition form = new FormDefinition();

        form.setId( DYNAMIC_FORM_ID + UUID.randomUUID().toString() );


        Set<FieldSetting> settings = getClassFieldSettings( clazz );

        for ( FieldSetting setting : settings ) {
            FieldDefinition field = fieldManager.getDefinitionByValueType( setting.getType().getName() );
            if ( field == null ) {
                continue;
            }

            field.setId( setting.getFieldName() );
            field.setName( setting.getFieldName() );
            field.setLabel( setting.getLabel() );
            field.setModelName( setting.getFieldName() );
            if ( !StringUtils.isEmpty( setting.getProperty() ) ) {
                field.setBoundPropertyName( setting.getProperty() );
            }

            if ( field instanceof SubFormFieldDefinition ) {
                FormDefinition nested = generateFormDefinition( setting.getType(), context );
                if ( nested != null ) {
                    ((SubFormFieldDefinition) field).setNestedForm( nested.getId() );
                    context.getAvailableForms().put( nested.getId(), nested );
                }
            }

            form.getFields().add( field );
        }
        form.setLayoutTemplate( layoutGenerator.generateTemplate( form ) );

        return form;
    }

    protected Set<FieldSetting> getClassFieldSettings( Class clazz ) {
        TreeSet<FieldSetting> settings = new TreeSet<FieldSetting>();
        for ( Field field : clazz.getDeclaredFields() ) {
            for ( Annotation annotation : field.getAnnotations() ) {
                if ( annotation instanceof FieldDef ) {
                    FieldDef fieldDef = (FieldDef) annotation;
                    Class fieldType = getFieldType( field, fieldDef );
                    settings.add( new FieldSetting( field.getName(), fieldType, fieldDef ) );
                }
            }
        }
        if ( clazz.getSuperclass() != null ) {
            settings.addAll( getClassFieldSettings( clazz.getSuperclass() ) );
        }
        return settings;
    }

    protected Class getFieldType( Field field, FieldDef definition ) {
        if ( !StringUtils.isEmpty( definition.property() ) ) {
            try {
                Field nestedField = field.getType().getDeclaredField( definition.property() );
                return nestedField.getType();
            } catch ( NoSuchFieldException e ) {
                logger.warn( "Error parsing model: Unable to find field '{}'", definition.property() );
            }
        }
        return field.getType();
    }
}
