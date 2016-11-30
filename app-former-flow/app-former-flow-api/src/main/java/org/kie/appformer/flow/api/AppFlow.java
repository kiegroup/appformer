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

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 * An {@code AppFlow<INPUT, OUTPUT>} is a process takes an {@code INPUT} and produces an
 * {@code OUTPUT} through a complex arrangement of {@link Step Steps} and {@link Function
 * Functions}. In particular, this type is meant to model the page flow of a CRUD application by
 * modelling forms, lists, calls to rest services, etc. as independent steps that can be combined
 * arbitrarily to define an entire application lifecycle.
 *
 * <p>
 * Use the {@link AppFlowFactory} to create an initial {@code AppFlow} from a {@link Function},
 * input value, or a {@link Step}. Executing a flow from one of these factory methods is the same as
 * executing the given {@link Step}/{@link Function}. Once you have an {@code AppFlow} instance, you
 * can use the combinator methods on this interface to chain together more {@link Step Steps} and
 * {@link Function Functions}, creating larger and more complex flows.
 *
 * <p>
 * Use the {@link #andThen(Step)} combinator to chain together steps into a pipeline. Use the
 * {@link #transitionTo(Function)} to handle decision points, where you decide which steps to
 * execute next based on the output of a previous step.
 *
 * <p>
 * Once you have fully defined an {@code AppFlow} it can be run with the {@link AppFlowExecutor}.
 * Executing an {@code AppFlow} requires an input value unless the input type is {@link Unit}.
 *
 * <p>
 * An {@code AppFlow} is a non-mutating structure and none of the operations on this interface are
 * mutating. Because of this an {@code AppFlow} can be reused indefinitely, either for use with
 * combinators to define new flows, or for repeated execution.
 *
 * @param <INPUT>
 *            The type of input value required by this flow. If the input type is {@link Unit} then
 *            this flow does not require an input value to be executed.
 * @param <OUTPUT>
 *            The type of output value produced by this flow.
 *
 * @see AppFlowFactory
 * @see AppFlowExecutor
 * @see Step
 */
public interface AppFlow<INPUT, OUTPUT> {

    /**
     * A combinator for appending a step to the end of this flow.
     *
     * @param nextStep
     *            A step that consumes the output of this flow as its input, and produces a new
     *            output.
     * @param <T> The output type of the given step (and consequently of the returned flow).
     * @return A new flow that, when executed, is the equivalent to executing this flow and then
     *         executing the given step with the output of this flow.
     */
    <T> AppFlow<INPUT, T> andThen( Step<? super OUTPUT, T> nextStep );

    /**
     * A combinator for appending a function to the end of this flow.
     *
     * @param transformation
     *            A function that consumes the output of this flow as its input, and produces a new
     *            output.
     * @param <T> The output type of the given function (and consequently of the returned flow).
     * @return A new flow that, when executed, is the equivalent to executing this flow and then
     *         executing the given function with the output of this flow.
     */
    <T> AppFlow<INPUT, T> andThen( Function<? super OUTPUT, T> transformation );

    /**
     * A combinator that appends a flow to this one at runtime based on the output of this flow.
     *
     * @param transition
     *            A function that consumes the output of this flow as its input, and produces a new
     *            flow that is executed after this flow.
     * @param <T>
     *            The return type of the flow returned by the given function (and consequently the
     *            return type of the returned flow).
     * @return A new flow that, when executed, is the equivalent to executing this flow, executing
     *         the given function with the output of this flow, and then executing the flow returned
     *         by the given function.
     */
    <T> AppFlow<INPUT, T> transitionTo( Function<? super OUTPUT, AppFlow<Unit, T>> transition );

    /**
     * A combinator that fixes the input of this flow. A flow that has the input type {@link Unit}
     * effectively has no input argument and can be run by the {@link AppFlowExecutor} without an
     * input argument.
     *
     * @param input
     *            A supplier that provides an initial value for this flow. This supplier is called
     *            each time the resulting flow is executed.
     * @return A new flow that, when executed, is the equivalent to executing this flow with input
     *         from the given supplier.
     */
    AppFlow<Unit, OUTPUT> withInput( Supplier<INPUT> input );

    /**
     * A combinator for appending a flow to the end of this flow.
     *
     * @param supplier
     *            A {@link Supplier} that provides a flow that consumes the output of this flow as
     *            its input, and produces a new output.
     * @param <T>
     *            The output type of the given flow (and consequently of the returned flow).
     * @return A new flow that, when executed, is the equivalent to executing this flow and then
     *         executing the flow returned by the given supplier with the output of this flow.
     */
    default <T> AppFlow<INPUT, T> andThen( final Supplier<AppFlow<OUTPUT, T>> supplier ) {
        return transitionTo( (final OUTPUT output) -> supplier.get().withInput( output ) );
    }

    /**
     * A combinator for appending a flow to the end of this flow.
     *
     * @param nextFlow
     *            A flow that consumes the output of this flow as its input, and produces a new
     *            output.
     * @param <T>
     *            The output type of the given flow (and consequently of the returned flow).
     * @return A new flow that, when executed, is the equivalent to executing this flow and then
     *         executing the given flow with the output of this flow.
     */
    default <T> AppFlow<INPUT, T> andThen( final AppFlow<OUTPUT, T> nextFlow ) {
        return andThen( () -> nextFlow );
    }

    /**
     * A combinator for appending a task to the end of a flow that does not affect the output.
     *
     * @param task
     *            A runnable that is executed at the end of the returned flow.
     * @return A new flow that, when executed, is the equivalent to executing this flow and then
     *         executing the given runnable before finally returning the output of this flow.
     */
    default AppFlow<INPUT, OUTPUT> andThen( final Runnable task ) {
        return andThen( output -> {
            task.run();
            return output;
        } );
    }

    /**
     * A combinator for ignoring the output of this flow and instead outputing {@link Unit}.
     *
     * @return A new flow that, when executed, is the equivalent to executing this flow, ignoring
     *         the output and outputing {@link Unit} instead.
     */
    default AppFlow<INPUT, Unit> toUnit() {
        return andThen( ignore -> Unit.INSTANCE );
    }

    /**
     * A combinator that fixes the input of this flow. A flow that has the input type {@link Unit}
     * effectively has no input argument and can be run by the {@link AppFlowExecutor} without an
     * input argument.
     *
     * @param input
     *            The initial input of this flow used when executing the returned flow.
     * @return A new flow that, when executed, is the equivalent to executing this flow with the
     *         given input.
     */
    default AppFlow<Unit, OUTPUT> withInput( final INPUT input ) {
        return withInput( () -> input );
    }

    /**
     * A combinator for defining a flow that repeatedly executes at runtime.
     *
     * @param factory
     *            Must not be null.
     * @param feedback
     *            At execution time when this flow is looped, every iteration will have its own
     *            input and output. At the end of each iteration, this feedback function is called
     *            with those values. Its return value is either an {@link Optional#empty() empty}
     *            {@link Optional} if the last iteration has been executed, or an {@link Optional}
     *            of some value if a futher iteration is desired.
     * @return A new flow that, when executed, takes an input, executes this flow, takes the
     *         resulting output, passes both the input and the ouput to the feedback function, and
     *         then finishes if the feedback function returns an empty {@link Optional} or else
     *         repeats again using the value inside the {@link Optional} returned by the feedback
     *         function as the next input.
     */
    default AppFlow<INPUT, OUTPUT> loop( final AppFlowFactory factory, final BiFunction<INPUT, OUTPUT, Optional<INPUT>> feedback ) {
        return factory
                .buildFromTransition( ( final INPUT input ) -> withInput( input ).andThen( output -> new Tuple2<>( output, input ) ) )
                .transitionTo( tuple -> {
                    final INPUT input = tuple.getTwo();
                    final OUTPUT output = tuple.getOne();
                    final Optional<INPUT> oNextInput = feedback.apply( input, output );

                    return oNextInput
                            .map( nextInput -> loop( factory, feedback ).withInput( nextInput ) )
                            .orElseGet( () -> factory.buildFromConstant( output ) );
                 } );
    }

}
