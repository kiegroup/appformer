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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * Base interface representing a Java type, similarly to {@link java.lang.reflect.Type}.
 *
 * <p>
 * This interface exists to provide an API for querying type information that is usable in
 * GWT-translatable code.
 */
public interface Type {

    /**
     * @return An {@link Optional} containing this type as a {@link SimpleType} iff this is a
     *         {@link SimpleType}.
     */
    default Optional<SimpleType> asSimpleType() {
        return Optional.empty();
    }
    /**
     * @return An {@link Optional} containing this type as a {@link GenericType} iff this is a
     *         {@link GenericType}.
     */
    default Optional<GenericType> asGenericType() {
        return Optional.empty();
    }
    /**
     * @return An {@link Optional} containing this type as a {@link ParameterizedType} iff this is a
     *         {@link ParameterizedType}.
     */
    default Optional<ParameterizedType> asParameterizedType() {
        return Optional.empty();
    }
    /**
     * @return An {@link Optional} containing this type as a {@link TypeVariable} iff this is a
     *         {@link TypeVariable}.
     */
    default Optional<TypeVariable> asTypeVariable() {
        return Optional.empty();
    }
    /**
     * @return An {@link Optional} containing this type as a {@link Wildcard} iff this is a
     *         {@link Wildcard}.
     */
    default Optional<Wildcard> asWildcard() {
        return Optional.empty();
    }

    /**
     * @param type
     *            Must not be null.
     * @return True if this type equals the argument. Equality requires to types to be the same kind
     *         (i.e. {@link SimpleType} or {@link TypeVariable}). Equality requires equal names for
     *         type kinds with names, and similarly it requires equal bounds for type kinds with
     *         bounds.
     */
    boolean equals( Type type );
    /**
     * @return Must return the same hashcode for types that are {@link #equals(Type) equal}.
     */
    @Override int hashCode();

    interface HasName {
        /**
         * @return The fully-qualified name of this type.
         */
        String getName();
        /**
         * @return The simple name of this type.
         */
        String getSimpleName();
    }
    interface HasBounds {
        /**
         * @return An array of types that are upper-bounds for this type variable or wildcard.
         */
        List<Type> getUpperBounds();
        /**
         * @return An array of types that are lower-bounds for this type variable or wildcard.
         */
        List<Type> getLowerBounds();
    }
    interface HasTypeParameters {
        /**
         * @return The erased type (without type parameters) for this parameterized or generic type.
         */
        SimpleType getErasure();
        /**
         * @return The type parameters of this type.
         */
        List<TypeVariable> getTypeParameters();
    }

    /**
     * Represents a type with no type parameters or arguments. For example {@code java.util.Object}.
     */
    interface SimpleType extends Type, HasName {
        @Override
        default Optional<SimpleType> asSimpleType() {
            return Optional.of( this );
        }
        @Override
        default boolean equals( final Type type ) {
            return type
                    .asSimpleType()
                    .filter( t -> t.getName().equals( getName() ) )
                    .isPresent();
        }
    }

    /**
     * Represents a type with type parameters but no arguments. For example {@code java.util.List<T>}.
     */
    interface GenericType extends Type, HasName, HasTypeParameters {
        @Override
        default Optional<GenericType> asGenericType() {
            return Optional.of( this );
        }
        @Override
        default boolean equals( final Type type ) {
            return type
                    .asGenericType()
                    .filter( t -> t.getName().equals( getName() ) )
                    .filter( t -> Stream
                                     .iterate( 0, n -> n+1 )
                                     .limit( getTypeParameters().size() )
                                     .allMatch( i -> t.getTypeParameters().get( i ).equals( getTypeParameters().get( i ) ) ) )
                    .isPresent();
        }
    }

    /**
     * Represents a type with type parameters and arguments. For example {@code java.util.List<String>}.
     */
    interface ParameterizedType extends Type, HasName, HasTypeParameters {
        /**
         * @return The type arguments for this parameterized type.
         */
        List<Type> getTypeArguments();
        @Override
        default Optional<ParameterizedType> asParameterizedType() {
            return Optional.of( this );
        }
        @Override
        default boolean equals( final Type type ) {
            return type
                    .asParameterizedType()
                    .filter( t -> t.getName().equals( getName() ) )
                    .filter( t -> t.getTypeArguments().size() == getTypeArguments().size() )
                    .filter( t -> Stream
                                     .iterate( 0, n -> n+1 )
                                     .limit( getTypeArguments().size() )
                                     .allMatch( i -> t.getTypeArguments().get( i ).equals( getTypeArguments().get( i ) ) ) )
                    .isPresent();
        }
    }

    /**
     * Represents a type variable. For example {@code T} in {@code java.util.List<T>}.
     */
    interface TypeVariable extends Type, HasName, HasBounds {
        @Override
        default Optional<TypeVariable> asTypeVariable() {
            return Optional.of( this );
        }
        @Override
        default boolean equals( final Type type ) {
            return type
                    .asTypeVariable()
                    .filter( t -> t.getName().equals( getName() ) )
                    .filter( t -> t.getUpperBounds().size() == getUpperBounds().size() )
                    .filter( t -> Stream
                                     .iterate( 0, n -> n+1 )
                                     .limit( getUpperBounds().size() )
                                     .allMatch( i -> t.getUpperBounds().get( i ).equals( getUpperBounds().get( i ) ) ) )
                    .filter( t -> t.getLowerBounds().size() == getLowerBounds().size() )
                    .filter( t -> Stream
                                     .iterate( 0, n -> n+1 )
                                     .limit( getLowerBounds().size() )
                                     .allMatch( i -> t.getLowerBounds().get( i ).equals( getLowerBounds().get( i ) ) ) )
                    .isPresent();
        }
    }

    /**
     * Represents a wildcard type. For example, the type argument of {@code java.util.List<? extends Object>}.
     */
    interface Wildcard extends Type, HasBounds {
        @Override
        default Optional<Wildcard> asWildcard() {
            return Optional.of( this );
        }
        @Override
        default boolean equals( final Type type ) {
            return type
                    .asWildcard()
                    .filter( t -> t.getUpperBounds().size() == getUpperBounds().size() )
                    .filter( t -> Stream
                                     .iterate( 0, n -> n+1 )
                                     .limit( getUpperBounds().size() )
                                     .allMatch( i -> t.getUpperBounds().get( i ).equals( getUpperBounds().get( i ) ) ) )
                    .filter( t -> t.getLowerBounds().size() == getLowerBounds().size() )
                    .filter( t -> Stream
                                     .iterate( 0, n -> n+1 )
                                     .limit( getLowerBounds().size() )
                                     .allMatch( i -> t.getLowerBounds().get( i ).equals( getLowerBounds().get( i ) ) ) )
                    .isPresent();
        }
    }

}
