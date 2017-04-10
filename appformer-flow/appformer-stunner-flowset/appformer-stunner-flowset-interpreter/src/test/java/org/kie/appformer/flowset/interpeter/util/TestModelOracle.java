/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.flowset.interpeter.util;

import java.util.Map;

import org.kie.appformer.flowset.interpreter.ModelOracle;

public class TestModelOracle implements ModelOracle {

    public final Map<Class<?>, ModelOracle> oraclesByType;

    public TestModelOracle( final Map<Class<?>, ModelOracle> oraclesByType ) {
        this.oraclesByType = oraclesByType;
    }

    @Override
    public void setProperty( final Object model,
                             final String property,
                             final Object value ) {
        final int i = property.indexOf( '.' );
        if ( i == -1 ) {
            oraclesByType.get( model.getClass() ).setProperty( model, property, value );
        }
        else {
            final Object subModel = getProperty( model, property.substring( 0, i ) );
            setProperty( subModel, property.substring( i+1 ), value );
        }
    }

    @Override
    public Object getProperty( final Object model,
                               final String property ) {
        final int i = property.indexOf( '.' );
        if ( i == -1 ) {
            return oraclesByType.get( model.getClass() ).getProperty( model, property );
        }
        else {
            final Object subModel = getProperty( model, property.substring( 0, i ) );
            return getProperty( subModel, property.substring( i+1 ) );
        }
    }

    @Override
    public Object createNestedModel( final Object model,
                                     final String property ) {
        final int i = property.indexOf( '.' );
        if ( i == -1 ) {
            return oraclesByType.get( model.getClass() ).createNestedModel( model, property );
        }
        else {
            final Object subModel = createNestedModel( model, property.substring( 0, i ) );
            return createNestedModel( subModel, property.substring( i+1 ) );
        }
    }

    @Override
    public Object workingCopy( final Object model ) {
        return oraclesByType.get( model.getClass() ).workingCopy( model );
    }

    @Override
    public void mergeChanges( final Object original,
                              final Object copy ) {
        oraclesByType.get( original.getClass() ).mergeChanges( original, copy );
    }

}
