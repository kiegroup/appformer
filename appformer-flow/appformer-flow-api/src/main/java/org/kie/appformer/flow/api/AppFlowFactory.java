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
import java.util.function.Supplier;

/**
 * <p>
 * Create {@link AppFlow} instances from {@link #buildFromStep(Step) steps},
 * {@link #buildFromFunction(Function) transformations}, {@link #buildFromTransition(Function)
 * transitions}, and {@link #buildFromConstant(Object) constants}.
 */
public interface AppFlowFactory {

    /**
     * Create an {@link AppFlow} from a single {@link Step}.
     *
     * @param step
     *            Must not be null.
     * @param <INPUT>
     *            The input type of the given step and the returned flow.
     * @param <OUTPUT>
     *            The output type of the given step and the returned flow.
     * @return A flow consisting of the given step. Executing the returned flow is equivalent to
     *         executing the given step.
     */
    <INPUT, OUTPUT> AppFlow<INPUT, OUTPUT> buildFromStep( Step<INPUT, OUTPUT> step );

    /**
     * Create an {@link AppFlow} from a single {@link Function}.
     *
     * @param transformation
     *            Must not be null.
     * @param <INPUT>
     *            The input type of the given function and the returned flow.
     * @param <OUTPUT>
     *            The output type of the given function and the returned flow.
     * @return A flow consisting of the given function. Executing the returned flow is equivalent to
     *         executing the given function.
     */
    <INPUT, OUTPUT> AppFlow<INPUT, OUTPUT> buildFromFunction( Function<INPUT, OUTPUT> transformation );

    /**
     * Create an {@link AppFlow} from a single transition {@link Function}.
     *
     * @param transition
     *            Must not be null.
     * @param <INPUT>
     *            The input type of the given transition function and the returned flow.
     * @param <OUTPUT>
     *            The output type of the flow returned by the given transition function and of the
     *            flow returned by this method.
     * @return A flow consisting of the given transition function. Executing the returned flow is
     *         equivalent to executing the given transition function, and then executing the flow
     *         returned by the given transition function.
     */
    <INPUT, OUTPUT> AppFlow<INPUT, OUTPUT> buildFromTransition( Function<INPUT, AppFlow<Unit, OUTPUT>> transition );

    /**
     * Create an {@link AppFlow} from a constant.
     *
     * @param constant
     *            Must not be null.
     * @param <OUTPUT>
     *            The type of the given constant and the output type of the returned flow.
     * @return A flow consisting of constant value. Executing the returned flow immediately returns the given constant.
     */
    default <OUTPUT> AppFlow<Unit, OUTPUT> buildFromConstant( final OUTPUT constant ) {
        if ( constant == null ) {
            throw new NullPointerException( "Null values not permitted in flows." );
        }
        return buildFromFunction( u -> constant );
    }

    /**
     * Create an {@link AppFlow} from a {@link Supplier}.
     *
     * @param supplier
     *            Must not be null.
     * @param <OUTPUT>
     *            The output type of the given supplier and of the returned flow.
     * @return A flow consisting of a single supplied value. Executing the returned flow immediately
     *         calls the given supplier, returning the supplied value.
     */
    default <OUTPUT> AppFlow<Unit, OUTPUT> buildFromSupplier( final Supplier<OUTPUT> supplier ) {
        return buildFromFunction( u -> supplier.get() );
    }

    /**
     * Create a noop flow that effectively inputs and outputs nothing.
     *
     * @return A flow consisting of the {@link Unit} singleton. Executing this flow immediately
     *         returns the {@link Unit} singleton instance.
     */
    default AppFlow<Unit, Unit> unitFlow() {
        return buildFromConstant( Unit.INSTANCE );
    }
}
