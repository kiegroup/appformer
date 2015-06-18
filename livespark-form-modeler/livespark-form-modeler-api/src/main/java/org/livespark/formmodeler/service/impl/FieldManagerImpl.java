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

package org.livespark.formmodeler.service.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.impl.BigDecimalBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.BigIntegerBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.ByteBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.CharacterBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.CheckBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.DateBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.DoubleBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.FloatBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.IntegerBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.LongBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.ShortBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.TextBoxFieldDefinition;
import org.livespark.formmodeler.service.FieldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pefernan on 4/29/15.
 */
@ApplicationScoped
public class FieldManagerImpl implements FieldManager {
    private static transient Logger log = LoggerFactory.getLogger( FieldManagerImpl.class );

    @Inject
    private Instance<FieldDefinition> instances;

    protected Map<String, String> defaultFields = new HashMap<String, String>(  );

    {
        defaultFields.put( BigDecimal.class.getName(), BigDecimalBoxFieldDefinition.class.getName() );
        defaultFields.put( BigInteger.class.getName(), BigIntegerBoxFieldDefinition.class.getName() );
        defaultFields.put( Byte.class.getName(), ByteBoxFieldDefinition.class.getName() );
        defaultFields.put( byte.class.getName(), ByteBoxFieldDefinition.class.getName() );
        defaultFields.put( Boolean.class.getName(), CheckBoxFieldDefinition.class.getName() );
        defaultFields.put( boolean.class.getName(), CheckBoxFieldDefinition.class.getName() );
        defaultFields.put( Character.class.getName(), CharacterBoxFieldDefinition.class.getName() );
        defaultFields.put( char.class.getName(), CharacterBoxFieldDefinition.class.getName() );
        defaultFields.put( Date.class.getName(), DateBoxFieldDefinition.class.getName() );
        defaultFields.put( Double.class.getName(), DoubleBoxFieldDefinition.class.getName() );
        defaultFields.put( double.class.getName(), DoubleBoxFieldDefinition.class.getName() );
        defaultFields.put( Float.class.getName(), FloatBoxFieldDefinition.class.getName() );
        defaultFields.put( float.class.getName(), FloatBoxFieldDefinition.class.getName() );
        defaultFields.put( Integer.class.getName(), IntegerBoxFieldDefinition.class.getName() );
        defaultFields.put( int.class.getName(), IntegerBoxFieldDefinition.class.getName() );
        defaultFields.put( Long.class.getName(), LongBoxFieldDefinition.class.getName() );
        defaultFields.put( long.class.getName(), LongBoxFieldDefinition.class.getName() );
        defaultFields.put( Short.class.getName(), ShortBoxFieldDefinition.class.getName() );
        defaultFields.put( short.class.getName(), ShortBoxFieldDefinition.class.getName() );
        defaultFields.put( String.class.getName(), TextBoxFieldDefinition.class.getName() );
    }

    protected Map<String, FieldDefinition> fieldDefinitions = new HashMap<String, FieldDefinition>(  );

    protected Map<String, List<String>> compatibleFields = new HashMap<String, List<String>>(  );


    @PostConstruct
    protected void init() {

        for (FieldDefinition definition : instances) {
            fieldDefinitions.put( definition.getCode(), definition );
            List<String> compatibles = compatibleFields.get( definition.getStandaloneClassName() );
            if (compatibles == null) {
                compatibles = new ArrayList<String>(  );
                compatibleFields.put( definition.getStandaloneClassName(), compatibles );
                if (definition.getStandaloneClassName().equals( Boolean.class.getName() )) {
                    compatibleFields.put( boolean.class.getName(), compatibles );
                } else if (definition.getStandaloneClassName().equals( Byte.class.getName() ) ) {
                    compatibleFields.put( byte.class.getName(), compatibles );
                } else if (definition.getStandaloneClassName().equals( Character.class.getName() )) {
                    compatibleFields.put( char.class.getName(), compatibles );
                } else if (definition.getStandaloneClassName().equals( Double.class.getName() ) ) {
                    compatibleFields.put( double.class.getName(), compatibles );
                } else if (definition.getStandaloneClassName().equals( Float.class.getName() ) ) {
                    compatibleFields.put( float.class.getName(), compatibles );
                } else if (definition.getStandaloneClassName().equals( Integer.class.getName() ) ) {
                    compatibleFields.put( int.class.getName(), compatibles );
                } else if (definition.getStandaloneClassName().equals( Long.class.getName() ) ) {
                    compatibleFields.put( long.class.getName(), compatibles );
                } else if (definition.getStandaloneClassName().equals( Short.class.getName() ) ) {
                    compatibleFields.put( short.class.getName(), compatibles );
                }
            }
            compatibles.add( definition.getCode() );
        }
    }

    @Override
    public FieldDefinition getDefinitionByType( String typeCode ) {
        FieldDefinition definition = fieldDefinitions.get( typeCode );
        if (definition != null) {
            try {
                return definition.getClass().newInstance();
            } catch ( Exception e ) {
                log.warn( "Error creating FieldDefinition: ", e );
            }
        }
        return null;
    }

    @Override
    public FieldDefinition getDefinitionByValueType( Class clazz ) {
        return getDefinitionByValueType( clazz.getName() );
    }

    @Override
    public FieldDefinition getDefinitionByValueType( String className ) {
        FieldDefinition definition = fieldDefinitions.get( defaultFields.get( className ) );
        if (definition != null) {
            try {
                return definition.getClass().newInstance();
            } catch ( Exception e ) {
                log.warn( "Error creating FieldDefinition: ", e );
            }
        }
        return null;
    }
}
