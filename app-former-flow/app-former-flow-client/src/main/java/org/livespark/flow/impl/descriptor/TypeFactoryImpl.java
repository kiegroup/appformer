/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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


package org.livespark.flow.impl.descriptor;

import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.flow.api.descriptor.type.Type;
import org.livespark.flow.api.descriptor.type.TypeFactory;
import org.livespark.flow.api.descriptor.type.Type.GenericType;
import org.livespark.flow.api.descriptor.type.Type.ParameterizedType;
import org.livespark.flow.api.descriptor.type.Type.SimpleType;
import org.livespark.flow.api.descriptor.type.Type.TypeVariable;
import org.livespark.flow.api.descriptor.type.Type.Wildcard;

@ApplicationScoped
public class TypeFactoryImpl implements TypeFactory {

    @Override
    public SimpleType simpleType( final String name,
                                  final String simpleName ) {
        return new SimpleTypeImpl( name, simpleName );
    }

    @Override
    public GenericType genericType( final SimpleType rawType,
                                    final TypeVariable... typeVariables ) {
        return new GenericTypeImpl( rawType, Arrays.asList( typeVariables ) );
    }

    @Override
    public ParameterizedType parameterizedType( final SimpleType rawType,
                                                final TypeVariable[] typeVariables,
                                                final Type[] typeArguments ) {
        return new ParameterizedTypeImpl( rawType, Arrays.asList( typeVariables ), Arrays.asList( typeArguments ) );
    }

    @Override
    public TypeVariable typeVariable( final String name,
                                      final Type[] upperBounds,
                                      final Type[] lowerBounds ) {
        return new TypeVariableImpl( name, Arrays.asList( upperBounds ), Arrays.asList( lowerBounds ) );
    }

    @Override
    public Wildcard wildcard( final Type[] upperBounds,
                              final Type[] lowerBounds ) {
        return new WildcardImpl( Arrays.asList( lowerBounds ), Arrays.asList( upperBounds ) );
    }

}
