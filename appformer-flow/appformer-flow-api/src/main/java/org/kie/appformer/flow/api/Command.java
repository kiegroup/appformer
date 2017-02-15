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


package org.kie.appformer.flow.api;

import java.util.function.Function;

/**
 * <p>
 * A convenience class for {@link Step Steps} that return a command and associated value. For
 * example, the command could be a {@link CrudOperation} and the value an entity to perform the
 * operation on.
 */
public class Command<E extends Enum<E>, T> {

    public final E commandType;
    public final T value;

    public Command(final E commandType, final T value) {
        this.commandType = commandType;
        this.value = value;
    }

    public <U> Command<E, U> map( final Function<T, U> f ) {
        return new Command<>( commandType, f.apply( value ) );
    }

}
