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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

class Success<F, S> implements Try<F, S> {
    private final S success;
    Success( final S success ) {
        this.success = success;
    }
    @Override
    public Optional<S> success() {
        return Optional.of( success );
    }
    @Override
    public Optional<F> failure() {
        return Optional.empty();
    }
    @Override
    public <S1> Try<F, S1> successMap( final Function<S, S1> f ) {
        return new Success<>( f.apply( success ) );
    }
    @Override
    public <S1> Try<F, S1> successFlatMap( final Function<S, Try<F, S1>> f ) {
        return f.apply( success );
    }
    @Override
    public Try<F, S> ifSuccess( final Consumer<S> c ) {
        c.accept( success );
        return this;
    }
    @Override
    public Try<F, S> ifFailure( final Consumer<F> c ) {
        return this;
    }
}