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
        defaultBasicTypes.put( BigDecimal.class.getName(), BigDecimalBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( BigInteger.class.getName(), BigIntegerBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Byte.class.getName(), ByteBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( byte.class.getName(), ByteBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Boolean.class.getName(), CheckBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( boolean.class.getName(), CheckBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Character.class.getName(), CharacterBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( char.class.getName(), CharacterBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Date.class.getName(), DateBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Double.class.getName(), DoubleBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( double.class.getName(), DoubleBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Float.class.getName(), FloatBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( float.class.getName(), FloatBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Integer.class.getName(), IntegerBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( int.class.getName(), IntegerBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Long.class.getName(), LongBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( long.class.getName(), LongBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( Short.class.getName(), ShortBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( short.class.getName(), ShortBoxFieldDefinition.class.getName() );
        defaultBasicTypes.put( String.class.getName(), TextBoxFieldDefinition.class.getName() );
    }

    protected Map<String, String> defaultMultiplesTypes = new HashMap<String, String>(  );

    @PostConstruct
    protected void init() {

        for (FieldDefinition definition : definitions ) {
            if (definition instanceof EntityRelationField) {
                if (definition instanceof MultipleField) multipleEntityDefinitions.put( definition.getCode(), definition );
                else singleEntityDefinitions.put( definition.getCode(), definition );
            } else {
                if (definition instanceof MultipleField) {
                    basicMultipleDefinitions.put( definition.getCode(), definition );
                } else {
                    basicFieldDefinitions.put( definition.getCode(), definition );
                    List<String> compatibles = basicSingleCompatibleDefinitions.get( definition.getStandaloneClassName() );
                    if (compatibles == null) {
                        compatibles = new ArrayList<String>(  );
                        basicSingleCompatibleDefinitions.put( definition.getStandaloneClassName(), compatibles );
                        if (definition.getStandaloneClassName().equals( Boolean.class.getName() )) {
                            basicSingleCompatibleDefinitions.put( boolean.class.getName(), compatibles );
                        } else if (definition.getStandaloneClassName().equals( Byte.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( byte.class.getName(), compatibles );
                        } else if (definition.getStandaloneClassName().equals( Character.class.getName() )) {
                            basicSingleCompatibleDefinitions.put( char.class.getName(), compatibles );
                        } else if (definition.getStandaloneClassName().equals( Double.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( double.class.getName(), compatibles );
                        } else if (definition.getStandaloneClassName().equals( Float.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( float.class.getName(), compatibles );
                        } else if (definition.getStandaloneClassName().equals( Integer.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( int.class.getName(), compatibles );
                        } else if (definition.getStandaloneClassName().equals( Long.class.getName() ) ) {
                            basicSingleCompatibleDefinitions.put( long.class.getName(), compatibles );
                        } else if (definition.getStandaloneClassName().equals( Short.class.getName() ) ) {
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
                if (definition != null)
                    return definition.getClass().newInstance();
                else {
                    definition = multipleEntityDefinitions.get( defaultMultipleEntity ).getClass().newInstance();
                    ((EntityRelationField)definition).setStandaloneType( type );
                    return definition;
                }
            } else {
                FieldDefinition definition = basicFieldDefinitions.get( defaultBasicTypes.get( className ) );
                if (definition != null) {
                    return definition.getClass().newInstance();
                } else {
                    definition = singleEntityDefinitions.get( defaultSingleEntity ).getClass().newInstance();
                    ((EntityRelationField)definition).setStandaloneType( className );
                    return definition;
                }
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
