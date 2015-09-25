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

package org.livespark.formmodeler.editor.backend.service.impl;

import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.MultipleField;
import org.livespark.formmodeler.editor.model.impl.basic.*;
import org.livespark.formmodeler.editor.model.impl.relations.EntityRelationField;
import org.livespark.formmodeler.editor.model.impl.relations.MultipleSubFormFieldDefinition;
import org.livespark.formmodeler.editor.model.impl.relations.SubFormFieldDefinition;
import org.livespark.formmodeler.editor.service.FieldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by pefernan on 4/29/15.
 */
@ApplicationScoped
public class FieldManagerImpl implements FieldManager {
    private static transient Logger log = LoggerFactory.getLogger( FieldManagerImpl.class );

    @Inject
    private Instance<FieldDefinition> definitions;

    protected Map<String, FieldDefinition> basicFieldDefinitions = new HashMap<String, FieldDefinition>(  );

    protected Map<String, List<String>> basicSingleCompatibleDefinitions = new HashMap<String, List<String>>(  );

    protected Map<String, FieldDefinition> basicMultipleDefinitions = new HashMap<String, FieldDefinition>(  );

    protected String defaultSingleEntity = SubFormFieldDefinition.class.getName();
    protected Map<String, FieldDefinition> singleEntityDefinitions = new HashMap<String, FieldDefinition>(  );

    protected String defaultMultipleEntity = MultipleSubFormFieldDefinition.class.getName();
    protected Map<String, FieldDefinition> multipleEntityDefinitions = new HashMap<String, FieldDefinition>(  );


    protected Map<String, String> defaultBasicTypes = new HashMap<String, String>(  );

    {
        defaultBasicTypes.put( BigDecimal.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( BigInteger.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Byte.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( byte.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Boolean.class.getName(), CheckBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( boolean.class.getName(), CheckBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Character.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( char.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Date.class.getName(), DateBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Double.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( double.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Float.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( float.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Integer.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( int.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Long.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( long.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Short.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( short.class.getName(), TextBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( String.class.getName(), TextBoxFieldDefinition.class.getName() );
    }

    protected Map<String, String> defaultMultiplesTypes = new HashMap<String, String>(  );

    @PostConstruct
    protected void init() {

        for (FieldDefinition definition : definitions ) {
            registerFieldDefinition( definition );
        }
    }

    protected void registerFieldDefinition( FieldDefinition definition ) {
        if (definition instanceof EntityRelationField) {
            if (definition instanceof MultipleField) multipleEntityDefinitions.put( definition.getCode(), definition );
            else singleEntityDefinitions.put( definition.getCode(), definition );
        } else {
            if (definition instanceof MultipleField) {
                basicMultipleDefinitions.put( definition.getCode(), definition );
            } else {
                basicFieldDefinitions.put(definition.getCode(), definition);

                for (String type : definition.getSupportedTypes()) {
                    List<String> compatibles = basicSingleCompatibleDefinitions.get( type );
                    if (compatibles == null) {
                        compatibles = new ArrayList<String>(  );
                        basicSingleCompatibleDefinitions.put( type, compatibles );
                        if (type.equals( Boolean.class.getName() )) {
                            basicSingleCompatibleDefinitions.put( boolean.class.getName(), compatibles );
                        } else if (type.equals( Byte.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( byte.class.getName(), compatibles );
                        } else if (type.equals( Character.class.getName() )) {
                            basicSingleCompatibleDefinitions.put( char.class.getName(), compatibles );
                        } else if (type.equals( Double.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( double.class.getName(), compatibles );
                        } else if (type.equals( Float.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( float.class.getName(), compatibles );
                        } else if (type.equals( Integer.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( int.class.getName(), compatibles );
                        } else if (type.equals( Long.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( long.class.getName(), compatibles );
                        } else if (type.equals( Short.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( short.class.getName(), compatibles );
                        }
                    }
                    compatibles.add( definition.getCode() );
                }
            }
        }
    }

    @Override
    public FieldDefinition getDefinitionByType( String typeCode ) {
        try {
            FieldDefinition definition = basicFieldDefinitions.get( typeCode );
            if (definition != null) {
                return definition.getClass().newInstance();
            }

            definition = singleEntityDefinitions.get( typeCode );
            if (definition != null) return definition.getClass().newInstance();
        } catch ( Exception e ) {
            log.warn( "Error creating FieldDefinition: ", e );
        }
        return null;
    }

    @Override
    public FieldDefinition getDefinitionByValueType( String className ) {
        return getDefinitionByValueType( className, null );
    }

    @Override
    public  FieldDefinition getDefinitionByValueType( String className, String type ) {
        try {
            if (isListType( className )) {
                FieldDefinition definition = basicMultipleDefinitions.get( defaultMultiplesTypes.get( type ) );
                FieldDefinition instance;
                if (definition != null) {
                    instance = definition.getClass().newInstance();
                } else {
                    instance = multipleEntityDefinitions.get( defaultMultipleEntity ).getClass().newInstance();
                }
                instance.setStandaloneClassName( type );
                return instance;
            } else {
                FieldDefinition definition = basicFieldDefinitions.get( defaultBasicTypes.get( className ) );
                FieldDefinition instance;
                if (definition != null) {
                    instance =  definition.getClass().newInstance();
                } else {
                    instance = singleEntityDefinitions.get( defaultSingleEntity ).getClass().newInstance();
                }
                instance.setStandaloneClassName( className );
                return instance;
            }
        } catch ( Exception e ) {
            log.warn( "Error creating FieldDefinition: ", e );
        }
        return null;
    }

    public boolean isListType(String className) {
        return List.class.getName().equals( className );
    }
}
