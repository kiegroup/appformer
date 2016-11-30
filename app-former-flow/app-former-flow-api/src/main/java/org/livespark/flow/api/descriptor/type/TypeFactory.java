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


package org.livespark.flow.api.descriptor.type;

import java.util.Arrays;

import org.livespark.flow.api.descriptor.type.Type.GenericType;
import org.livespark.flow.api.descriptor.type.Type.ParameterizedType;
import org.livespark.flow.api.descriptor.type.Type.SimpleType;
import org.livespark.flow.api.descriptor.type.Type.TypeVariable;
import org.livespark.flow.api.descriptor.type.Type.Wildcard;

/**
 * A factory for {@link Type types}.
 */
public interface TypeFactory {

    /**
     * @param name
     *            The fully-qualified name of a type. Never null.
     * @param simpleName
     *            The simple name of a type. Never null.
     * @return A {@link Type} that is not generic or parametrized (analogous to {@link Class}).
     *         Never null.
     */
    SimpleType simpleType( String name, String simpleName );

    /**
     * @param rawType
     *            The underlying raw type of the returned generic type. Never null.
     * @param typeVariables
     *            An array of type variables that this generic type has.
     * @return A generic type. Never null.
     */
    GenericType genericType( SimpleType rawType, TypeVariable... typeVariables );

    /**
     * @param rawType
     *            The underlying raw type of the returned parameterized type. Never null.
     * @param typeVariables
     *            An array of type variables that the underlying generic type has.
     * @param typeArguments
     *            An array of type arguments that this parameterized type has.
     * @return A parameterized type. Never null.
     */
    ParameterizedType parameterizedType( SimpleType rawType, TypeVariable[] typeVariables, Type[] typeArguments );

    /**
     * @param name
     *            The name of the type parameter. Never null.
     * @param upperBounds
     *            An array of upper-bounds for this type variable. Never null.
     * @param lowerBounds
     *            An array of lower-bounds for this type variable. Never null.
     * @return A type variable with the given name and bounds. Never null.
     */
    TypeVariable typeVariable( String name, Type[] upperBounds, Type[] lowerBounds );

    /**
     * @param upperBounds
     *            An array of upper-bounds for this wildcard. Never null.
     * @param lowerBounds
     *            An array of lower-bounds for this wildcard. Never null.
     * @return A wildcard with the given bounds.
     */
    Wildcard wildcard( Type[] upperBounds, Type[] lowerBounds );

    /**
     * A convenience method for creating a {@link SimpleType} from a {@link Class}.
     *
     * @see #simpleType(String, String)
     */
    default SimpleType simpleType( final Class<?> clazz ) {
        return simpleType( clazz.getName(), clazz.getSimpleName() );
    }

    /**
     * A convenience method for creating a {@link GenericType} using a {@link Class} instead of a
     * {@link SimpleType}.
     *
     * @see #genericType(SimpleType, TypeVariable...)
     */
    default GenericType genericType( final Class<?> rawType, final TypeVariable... typeVariables ) {
        return genericType( simpleType( rawType ), typeVariables );
    }

    /**
     * A convenience method for creating a {@link GenericType} using a {@link Class} instead of a
     * {@link SimpleType} and string names for unbound type variables.
     *
     * @see #genericType(SimpleType, TypeVariable...)
     */
    default GenericType genericType( final Class<?> rawType, final String... typeVariableNames ) {
        return genericType( simpleType( rawType ), Arrays.stream( typeVariableNames ).map( this::typeVariable ).toArray( TypeVariable[]::new ) );
    }

    /**
     * A convenience method for creating a {@link GenericType} string names for unbound type
     * variables.
     *
     * @see #genericType(SimpleType, TypeVariable...)
     */
    default GenericType genericType( final SimpleType rawType, final String... typeVariableNames ) {
        return genericType( rawType, Arrays.stream( typeVariableNames ).map( this::typeVariable ).toArray( TypeVariable[]::new ) );
    }

    /**
     * A convenience method for creating a {@link ParameterizedType} from a {@link GenericType} and
     * type arguments.
     *
     * @see #genericType(SimpleType, TypeVariable...)
     */
    default ParameterizedType parameterizedType( final GenericType unboundGenericType, final Type... typeArguments ) {
        return parameterizedType( unboundGenericType.getErasure(), unboundGenericType.getTypeParameters().toArray( new TypeVariable[0] ), typeArguments );
    }

    /**
     * A convenience method for creating a {@link ParameterizedType} using a {@link Class} instead
     * of a {@link SimpleType}.
     *
     * @see #genericType(SimpleType, TypeVariable...)
     */
    default ParameterizedType parameterizedType( final Class<?> rawType, final TypeVariable[] typeVariables, final Type[] typeArguments ) {
        return parameterizedType( simpleType( rawType ), typeVariables, typeArguments );
    }

    /**
     * A convenience method for creating a type variable with no lower-bounds.
     *
     * @see #typeVariable(String, Type[], Type[])
     */
    default TypeVariable typeVariable( final String name, final Type... upperBounds ) {
        return typeVariable( name, upperBounds, new Type[0] );
    }

    /**
     * A convenience method for creating a type variable with no explicit bounds.
     *
     * @see #typeVariable(String, Type[], Type[])
     */
    default TypeVariable typeVariable( final String name ) {
        return typeVariable( name, new Type[0], new Type[0] );
    }

    /**
     * A convenience method for creating a wildcard with now lower-bounds.
     *
     * @see #wildcard(Type[], Type[])
     */
    default Wildcard wildcard( final Type... upperBounds ) {
        return wildcard( upperBounds, new Type[0] );
    }
}
