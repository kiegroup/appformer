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

package org.livespark.formmodeler.service;

import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.MultipleField;
import org.livespark.formmodeler.model.impl.basic.CheckBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.DateBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.TextBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.relations.EntityRelationField;
import org.livespark.formmodeler.model.impl.relations.MultipleSubFormFieldDefinition;
import org.livespark.formmodeler.model.impl.relations.SubFormFieldDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by pefernan on 4/29/15.
 */
public abstract class AbstractFieldManager implements FieldManager {
    private static transient Logger log = LoggerFactory.getLogger( FieldManager.class );

    protected Map<String, FieldDefinition> basicFieldDefinitions = new HashMap<String, FieldDefinition>(  );
    protected Map<String, List<String>> basicSingleCompatibleDefinitions = new HashMap<String, List<String>>(  );

    protected Map<String, FieldDefinition> basicListDefinitions = new HashMap<String, FieldDefinition>(  );
    protected Map<String, List<String>> basicListCompatibleDefinitions = new HashMap<String, List<String>>(  );

    protected String defaultSingleEntity = SubFormFieldDefinition._CODE;
    protected Map<String, FieldDefinition> singleEntityDefinitions = new HashMap<String, FieldDefinition>(  );

    protected String defaultMultipleEntity = MultipleSubFormFieldDefinition._CODE;
    protected Map<String, FieldDefinition> multipleEntityDefinitions = new HashMap<String, FieldDefinition>(  );

    protected Map<String, String> defaultBasicTypes = new HashMap<String, String>(  );
    protected Map<String, String> defaultBasicListsTypes = new HashMap<String, String>(  );

    {
        defaultBasicTypes.put( BigDecimal.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( BigInteger.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( Byte.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( byte.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( Boolean.class.getName(), CheckBoxFieldDefinition._CODE );
        defaultBasicTypes.put( boolean.class.getName(), CheckBoxFieldDefinition._CODE );
        defaultBasicTypes.put( Character.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( char.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( Date.class.getName(), DateBoxFieldDefinition._CODE );
        defaultBasicTypes.put( Double.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( double.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( Float.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( float.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( Integer.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( int.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( Long.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( long.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( Short.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( short.class.getName(), TextBoxFieldDefinition._CODE );
        defaultBasicTypes.put( String.class.getName(), TextBoxFieldDefinition._CODE );

        // TODO: register multipletypes when defined
    }


    protected abstract FieldDefinition createNewInstance( FieldDefinition definition ) throws Exception;

    protected void registerFieldDefinition( FieldDefinition definition ) {
        if (definition instanceof EntityRelationField) {
            if (definition instanceof MultipleField) multipleEntityDefinitions.put( definition.getCode(), definition );
            else singleEntityDefinitions.put( definition.getCode(), definition );
        } else {
            if (definition instanceof MultipleField) {
                registerBasicType( definition, basicListDefinitions, basicListCompatibleDefinitions);
                basicListDefinitions.put(definition.getCode(), definition);
            } else {
                registerBasicType(definition, basicFieldDefinitions, basicSingleCompatibleDefinitions);
            }
        }
    }

    protected void registerBasicType( FieldDefinition definition,
                                      Map<String, FieldDefinition> definitionMap,
                                      Map<String, List<String>> compatiblesMap) {
        definitionMap.put(definition.getCode(), definition);

        for (String type : definition.getSupportedTypes()) {
            List<String> compatibles = compatiblesMap.get( type );
            if (compatibles == null) {
                compatibles = new ArrayList<String>(  );
                compatiblesMap.put( type, compatibles );
                if (type.equals( Boolean.class.getName() )) {
                    compatiblesMap.put( boolean.class.getName(), compatibles );
                } else if (type.equals( Byte.class.getName() ) ) {
                    compatiblesMap.put( byte.class.getName(), compatibles );
                } else if (type.equals( Character.class.getName() )) {
                    compatiblesMap.put( char.class.getName(), compatibles );
                } else if (type.equals( Double.class.getName() ) ) {
                    compatiblesMap.put( double.class.getName(), compatibles );
                } else if (type.equals( Float.class.getName() ) ) {
                    compatiblesMap.put( float.class.getName(), compatibles );
                } else if (type.equals( Integer.class.getName() ) ) {
                    compatiblesMap.put( int.class.getName(), compatibles );
                } else if (type.equals( Long.class.getName() ) ) {
                    compatiblesMap.put( long.class.getName(), compatibles );
                } else if (type.equals( Short.class.getName() ) ) {
                    compatiblesMap.put( short.class.getName(), compatibles );
                }
            }
            compatibles.add( definition.getCode() );
        }
    }

    @Override
    public List<FieldDefinition> getBaseTypes() {
        List<FieldDefinition> definitions = new ArrayList<FieldDefinition>();

        definitions.addAll( createBaseInstances( basicFieldDefinitions ) );
        definitions.addAll( createBaseInstances( basicListDefinitions ) );
        definitions.addAll( createBaseInstances( singleEntityDefinitions ) );
        definitions.addAll( createBaseInstances( multipleEntityDefinitions ) );

        return definitions;
    }

    protected List<FieldDefinition> createBaseInstances( Map<String, FieldDefinition> definitionMap ) {
        List<FieldDefinition> definitions = new ArrayList<FieldDefinition>();

        for ( FieldDefinition definition : definitionMap.values() ) {
            try {
                FieldDefinition instance = createNewInstance( definition );

                instance.setName( UNBINDED_FIELD_NAME_PREFFIX + instance.getId() );

                instance.setLabel( definition.getCode() );

                definitions.add( instance );
            } catch ( Exception e ) {
                log.warn( "Error creating Default Field Instance for '" + definition.getCode() + "':", e);
            }

        }

        return definitions;
    }

    @Override
    public FieldDefinition getDefinitionByTypeCode( String typeCode ) {
        try {
            FieldDefinition definition = basicFieldDefinitions.get( typeCode );

            if ( definition == null ) {
                definition = basicListDefinitions.get( typeCode );
                if ( definition == null ) {
                    definition = singleEntityDefinitions.get(typeCode);
                    if (definition == null) {
                        definition = multipleEntityDefinitions.get(typeCode);
                    }
                }
            }

            if (definition != null) return createNewInstance( definition );
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
            FieldDefinition definition;
            if (isListType( className )) {
                definition = basicListDefinitions.get(defaultBasicListsTypes.get(type));
                if (definition == null) {
                    definition = multipleEntityDefinitions.get(defaultMultipleEntity);
                }
                FieldDefinition instance = createNewInstance( definition );
                instance.setStandaloneClassName( type );
                return instance;
            } else {
                definition = basicFieldDefinitions.get( defaultBasicTypes.get( className ) );
                if (definition == null) {
                    definition = singleEntityDefinitions.get(defaultSingleEntity);
                }
                FieldDefinition instance = createNewInstance( definition );
                instance.setStandaloneClassName( className );
                return instance;
            }
        } catch ( Exception e ) {
            log.warn( "Error creating FieldDefinition: ", e );
        }
        return null;
    }

    @Override
    public List<String> getCompatibleFieldTypes( FieldDefinition fieldDefinition ) {
        if (fieldDefinition.getStandaloneClassName() != null ) {
            if (fieldDefinition instanceof MultipleField) {
                if ( fieldDefinition instanceof EntityRelationField ) {
                    return new ArrayList<String>(multipleEntityDefinitions.keySet());
                }
                return basicListCompatibleDefinitions.get(fieldDefinition.getStandaloneClassName());
            } else {
                if (fieldDefinition instanceof EntityRelationField) {
                    return new ArrayList<String>(singleEntityDefinitions.keySet());
                }
                return basicSingleCompatibleDefinitions.get(fieldDefinition.getStandaloneClassName());
            }
        } else {
            if ( fieldDefinition instanceof EntityRelationField ) {
                if (fieldDefinition instanceof  MultipleField) {
                    return new ArrayList<String>(multipleEntityDefinitions.keySet());
                }
                return new ArrayList<String>(singleEntityDefinitions.keySet());
            }
            Set result = new TreeSet();

            for ( String type : fieldDefinition.getSupportedTypes() ) {
                result.addAll(basicSingleCompatibleDefinitions.get( type ));
            }
            return new ArrayList<String>(result);
        }
    }



    protected boolean isListType( String className ) {
        return List.class.getName().equals( className );
    }
}
