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

/**
 * <p>
 * A type wrapping a result that can be either a success of type {@code S} or a failure of type
 * {@code F}.
 */
public interface Try<F, S> {

    /**
     * @return An empty optional if this is a failure, or else an optional containing a successful
     *         result.
     */
    Optional<S> success();

    /**
     * @return An empty optional if this is a success, or else an optional containing a failure
     *         result.
     */
    Optional<F> failure();

    /**
     * <p>
     * Applies the given function to the contained value if this is a success.
     *
     * @param f
     *            The function to apply if this is a success. Must not be null.
     * @return A {@link Try} containing the result of the function invocation if this is a success,
     *         or else the same failure.
     */
    <S1> Try<F, S1> successMap( Function<S, S1> f );

    /**
     * <p>
     * Applies a function to the contained value if a success, and flattens the resulting
     * {@link Try}.
     *
     * @param f
     *            The function to apply if the value is a success. Must not be null.
     * @return The {@link Try} returned by the given function if this is a success, or else this
     *         failure.
     */
    <S1> Try<F, S1> successFlatMap( Function<S, Try<F, S1>> f );

    /**
     * <p>
     * Consume the wrapped value if it is a success.
     *
     * @param c
     *            A consumer for a success value. Must not be null.
     */
    Try<F, S> ifSuccess( final Consumer<S> c );

    /**
     * <p>
     * Consume the wrapped value if it is a failure.
     *
     * @param c
     *            A consumer for a failure value. Must not be null.
     */
    Try<F, S> ifFailure( final Consumer<F> c );

    /**
     * <p>
     * Wrap a success value in a {@link Try}.
     */
    static <F, S> Try<F, S> success( final S s ) {
        return new Success<>( s );
    }

    /**
     * <p>
     * Wrap a failure value in a {@link Try}.
     */
    static <F, S> Try<F, S> failure( final F f ) {
        return new Failure<>( f );
    }
}