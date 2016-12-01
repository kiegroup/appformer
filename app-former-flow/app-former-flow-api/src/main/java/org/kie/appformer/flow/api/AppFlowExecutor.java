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

import java.util.function.Consumer;

/**
 * Executes {@link AppFlow} instances.
 *
 * @see AppFlow
 * @see AppFlowFactory
 */
public interface AppFlowExecutor {

    /**
     * Execute an {@link AppFlow} with a given input, and a callback to receive the output.
     *
     * @param input The input argument of the flow to be executed.
     * @param flow The flow to be executed.
     * @param callback To be invoked with the given flow output once execution has finished.
     * @param <INPUT> The input type of the flow to be executed.
     * @param <OUTPUT> The output type of the flow to be executed.
     */
    <INPUT, OUTPUT> void execute( INPUT input, AppFlow<INPUT, OUTPUT> flow, Consumer<? super OUTPUT> callback );

    /**
     * Execute an {@link AppFlow} that takes no input.
     *
     * @param flow The flow to be executed.
     * @param <OUTPUT> The output type of the flow to be executed.
     */
    default <OUTPUT> void execute( final AppFlow<Unit, OUTPUT> flow ) {
        execute( flow, o -> {} );
    }

    /**
     * Execute an {@link AppFlow} that takes no input with a callback to receive the output.
     *
     * @param flow The flow to be executed.
     * @param callback To be invoked with the given flow output once execution has finished.
     * @param <OUTPUT> The output type of the flow to be executed.
     */
    default <OUTPUT> void execute( final AppFlow<Unit, OUTPUT> flow, final Consumer<? super OUTPUT> callback ) {
        execute( Unit.INSTANCE, flow, callback );
    }

    /**
     * Execute an {@link AppFlow} with a given input.
     *
     * @param input The input argument of the flow to be executed.
     * @param flow The flow to be executed.
     * @param <INPUT> The input type of the flow to be executed.
     */
    default <INPUT> void execute( final INPUT input, final AppFlow<INPUT, ?> flow ) {
        execute( input, flow, o -> {} );
    }

}
