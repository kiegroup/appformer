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


package org.livespark.flow.impl;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.livespark.flow.api.Step;
import org.livespark.flow.api.Unit;

public abstract class StepUtil {

    private StepUtil() {}

    public static <INPUT, OUTPUT> Step<INPUT, OUTPUT> wrap( final String name, final Function<INPUT, OUTPUT> f ) {
        return new Step<INPUT, OUTPUT>() {

            @Override
            public void execute( final INPUT input, final Consumer<OUTPUT> callback ) {
                callback.accept( f.apply( input ) );
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    public static <OUTPUT> Step<Unit, OUTPUT> wrap( final String name, final Supplier<OUTPUT> s ) {
        return wrap( name, ignore -> s.get() );
    }

    public static <INPUT, OUTPUT> Step<INPUT, OUTPUT> wrap( final String name, final BiConsumer<INPUT, Consumer<OUTPUT>> consumer ) {
        return new Step<INPUT, OUTPUT>() {

            @Override
            public void execute( final INPUT input, final Consumer<OUTPUT> callback ) {
                consumer.accept( input, callback );
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    public static <T> Step<T, T> identity() {
        return wrap("Identity", Function.identity());
    }

}
