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


package org.livespark.flow.api;

/**
 * <p>
 * {@code Unit} is a singleton type meant to represent the absence of a value. It is like
 * {@link Void} except that there is a single instance of {@code Unit}.
 *
 * <p>
 * We use {@code Unit} instead of {@link Void} because the latter is not instantiable, meaning we
 * would have to pass {@code null} values to execute flows with {@link Void} inputs.
 */
public class Unit {

    /**
     * The single instance of {@link Unit}.
     */
    public static Unit INSTANCE = new Unit();

    private Unit() {}

}
