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


package org.livespark.flow.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;
import org.livespark.flow.api.descriptor.type.Type;
import org.livespark.flow.api.descriptor.type.TypeFactory;
import org.livespark.flow.impl.descriptor.TypeFactoryImpl;

public class TypeTest {

    TypeFactory factory = new TypeFactoryImpl();

    @Test
    public void simpleTypeEquals() throws Exception {
        final Supplier<Type.SimpleType> stringSupplier = () -> factory.simpleType( String.class );
        assertEquals( stringSupplier.get(), stringSupplier.get() );
        assertNotEquals( factory.simpleType( Integer.class ), stringSupplier.get() );
    }

    @Test
    public void simpleTypeHashCode() throws Exception {
        final Supplier<Type.SimpleType> stringSupplier = () -> factory.simpleType( String.class );
        assertEquals( stringSupplier.get().hashCode(), stringSupplier.get().hashCode() );
    }

    @Test
    public void genericTypeEquals() throws Exception {
        final Supplier<Type.GenericType> listSupplier = () -> factory.genericType( List.class, "E" );
        assertEquals( listSupplier.get(), listSupplier.get() );
        assertNotEquals( factory.genericType( List.class, "T" ), listSupplier.get() );
        assertNotEquals( factory.genericType( ArrayList.class, "E" ), listSupplier.get() );
    }

    @Test
    public void genericTypeHashCode() throws Exception {
        final Supplier<Type.GenericType> listSupplier = () -> factory.genericType( List.class, "E" );
        assertEquals( listSupplier.get().hashCode(), listSupplier.get().hashCode() );
    }

    @Test
    public void parameterizedTypeEquals() throws Exception {
        final BiFunction<Class<?>, Class<?>, Type.ParameterizedType> getSingleParameterized = ( rawType,
                                                                                          parameterType ) -> factory.parameterizedType( factory.genericType( rawType,
                                                                                                                                                             "E" ),
                                                                                                                                        factory.simpleType( parameterType ) );
        final Function<Class<?>, Type.ParameterizedType> listOf = type -> getSingleParameterized.apply( List.class, type );
        assertEquals( listOf.apply( String.class ), listOf.apply( String.class ) );
        assertNotEquals( getSingleParameterized.apply( ArrayList.class, String.class ), listOf.apply( String.class ) );
        assertNotEquals( listOf.apply( Integer.class ), listOf.apply( String.class ) );
    }

    @Test
    public void parameterizedTypeHashCode() throws Exception {
        final BiFunction<Class<?>, Class<?>, Type.ParameterizedType> getSingleParameterized = ( rawType,
                                                                                          parameterType ) -> factory.parameterizedType( factory.genericType( rawType,
                                                                                                                                                             "E" ),
                                                                                                                                        factory.simpleType( parameterType ) );
        final Function<Class<?>, Type.ParameterizedType> listOf = type -> getSingleParameterized.apply( List.class, type );
        assertEquals( listOf.apply( String.class ).hashCode(), listOf.apply( String.class ).hashCode() );
    }

}
