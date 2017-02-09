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


package org.kie.appformer.flow.lang;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * <p>
 * A simple implementation of {@link PatternMatcher} that uses a single converter function and a
 * list of argument extracting functions.
 */
public class SimplePatternMatcher<T> implements PatternMatcher {

    private final List<Function<T, Object>> argExtractors;
    private final Function<Object, Optional<T>> converter;

    /**
     * @param converter
     *            Used to test if an object matches this pattern. Object is a match if the returned
     *            optional is present. Must not be null.
     * @param argExtractors
     *            A list of argument extracting functions. A function at index <code>i</code>
     *            extracts the argument at index <code>i</code>.
     */
    public SimplePatternMatcher( final Function<Object, Optional<T>> converter, final List<Function<T, Object>> argExtractors ) {
        this.converter = converter;
        this.argExtractors = argExtractors;
    }

    @Override
    public int argLength() {
        return argExtractors.size();
    }

    @Override
    public Object get( final Object o, final int index ) {
        final Optional<T> ot = converter.apply( o );
        return ot.map( argExtractors.get( index ) ).orElseThrow( () -> new IllegalArgumentException( "Given argument does not match this pattern: " + o ) );
    }

    @Override
    public boolean matches( final Object candidate ) {
        return converter.apply( candidate ).isPresent();
    }

}
