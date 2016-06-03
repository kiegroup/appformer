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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.bus.server.annotations.Service;
import org.livespark.formmodeler.codegen.layout.Dynamic;
import org.livespark.formmodeler.codegen.layout.FormLayoutTemplateGenerator;
import org.livespark.formmodeler.metaModel.FieldDef;
import org.livespark.formmodeler.model.DefaultFieldTypeInfo;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.renderer.backend.service.impl.fieldInitializers.FieldInitializer;
import org.livespark.formmodeler.renderer.backend.service.impl.fieldInitializers.FormAwareFieldInitializer;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.FieldAnnotationProcessor;
import org.livespark.formmodeler.renderer.service.FormDefintionGenerator;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.livespark.formmodeler.renderer.service.Model2FormTransformerService;
import org.livespark.formmodeler.renderer.service.impl.DynamicRenderingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Dependent
public class Model2FormTransformerServiceImpl implements Model2FormTransformerService, FormDefintionGenerator {

    private static final Logger logger = LoggerFactory.getLogger( Model2FormTransformerServiceImpl.class );

    private FormLayoutTemplateGenerator layoutGenerator;

    private List<FieldAnnotationProcessor> processors = new ArrayList<>();

    private List<FieldInitializer<? extends FieldDefinition>> fieldInitializers = new ArrayList<>();

    private FieldAnnotationProcessor defaultAnnotationProcessor;

    @Inject
    public Model2FormTransformerServiceImpl( Instance<FieldAnnotationProcessor> installedProcessors,
                                             Instance<FieldInitializer<? extends FieldDefinition>> installedInitializers,
                                             @Dynamic FormLayoutTemplateGenerator layoutGenerator) {
        this.layoutGenerator = layoutGenerator;
        for ( FieldAnnotationProcessor processor : installedProcessors ) {
            if ( processor.isDefault() ) {
                defaultAnnotationProcessor = processor;
            } else {
                processors.add( processor );
            }
        }

        for( FieldInitializer initializer : installedInitializers ) {
            if ( initializer instanceof FormAwareFieldInitializer ) {
                ( (FormAwareFieldInitializer) initializer ).setTransformerService( this );
            }
            fieldInitializers.add( initializer );
        }
    }

    @Override
    public FormRenderingContext createContext( Object model ) {
        DynamicRenderingContext context = new DynamicRenderingContext();

        FormDefinition form = generateFormDefinitionForModel( model, context );
        context.setRootForm( form );

        return context;
    }

    @Override
    public FormDefinition generateFormDefinitionForModel( Object model, DynamicRenderingContext context ) {
        if ( model == null) {
            return null;
        }
        return generateFormDefinitionForClass( model.getClass(), context );
    }

    @Override
    public FormDefinition generateFormDefinitionForClass( Class clazz, DynamicRenderingContext context ) {
        FormDefinition form = new FormDefinition();

        form.setId( clazz.getName() );

        Set<FieldSetting> settings = getClassFieldSettings( clazz );

        for ( FieldSetting setting : settings ) {
            FieldDefinition field = null;

            for( Annotation annotation : setting.getAnnotations() ) {
                for ( FieldAnnotationProcessor processor : processors ) {
                    if ( processor.supportsAnnotation( annotation ) ) {
                        field = processor.getFieldDefinition( annotation, setting );
                    }
                }
            }

            if ( field == null ) {
                field = defaultAnnotationProcessor.getFieldDefinition( null, setting );
            }

            if ( field == null ) {
                continue;
            }

            for ( FieldInitializer initializer : fieldInitializers ) {
                if ( initializer.supports( field ) ) {
                    initializer.initializeField( field, setting, context );
                }
            }

            form.getFields().add( field );
        }
        form.setLayoutTemplate( layoutGenerator.generateLayoutTemplate( form ) );

        return form;
    }

    protected Set<FieldSetting> getClassFieldSettings( Class clazz ) {
        TreeSet<FieldSetting> settings = new TreeSet<FieldSetting>();
        for ( Field field : clazz.getDeclaredFields() ) {
            for ( Annotation annotation : field.getAnnotations() ) {
                if ( annotation instanceof FieldDef ) {
                    FieldDef fieldDef = (FieldDef) annotation;
                    Class fieldType = getFieldType( field, fieldDef );

                    Class realType = fieldType;

                    if ( field.getGenericType() instanceof ParameterizedType ) {
                        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        Type paramArg = parameterizedType.getActualTypeArguments()[0];
                        realType = (Class) paramArg;
                    }

                    FieldSetting setting = new FieldSetting( field.getName(),
                            new DefaultFieldTypeInfo( realType.getName(),
                                    fieldType.isAssignableFrom( List.class ),
                                    fieldType.isEnum()), realType, fieldDef, field.getAnnotations() );

                    settings.add( setting );
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
