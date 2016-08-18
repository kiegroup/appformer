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
 * A convenience class for {@link Step steps} or {@link AppFlow flows} that return two values.
 */
public class Tuple2<V1, V2> {

    private final V1 one;

    private final V2 two;

    public Tuple2( final V1 one, final V2 two ) {
        this.one = one;
        this.two = two;
    }

    public V1 getOne() {
        return one;
    }

    public V2 getTwo() {
        return two;
    }

    public Tuple2<V2, V1> swap() {
        return new Tuple2<>( two, one );
    }

}
