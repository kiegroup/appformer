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

import org.kie.appformer.flow.lang.AST.ConstructorPattern;

/**
 * <p>
 * Implementations are used to match objects against {@link ConstructorPattern constructor patterns}
 * at runtime.
 *
 * <p>
 * For example, a matcher for the constructor <code>Ctor(a, b)</code> will match against some
 * objects at runtime, and provide a way for extracting the values <code>a</code> and
 * <code>b</code>.
 */
public interface PatternMatcher {

    /**
     * @return The constant number of arguments that the {@link ConstructorPattern} should have.
     */
    int argLength();

    /**
     * <p>
     * Extracts arguments from the object for the {@link ConstructorPattern} that are matched
     * recursively.
     *
     * @param o
     *            The object from which to extract arguments. Never null. It will always be the case
     *            that {@link #matches(Object)} called with this argument returns <code>true</code>.
     * @param index
     *            The index of the constructor argument that should be extracted.
     * @return The value of the constructor argument for the given object and index.
     */
    Object get( Object o, int index );

    /**
     * @return True iff this object is a match for this pattern such that {@link #get(Object, int)}
     *         will return argument values for any valid index.
     */
    boolean matches( Object candidate );
}