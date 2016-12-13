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
 * <p>
 * Steps are the fundamental building block of {@link AppFlow AppFlows}. A {@link Step} is meant to
 * encapsulate a reusable interaction, such as form that is filled out by users (a user
 * interaction), or a REST call that loads data (a system interaction).
 *
 * <p>
 * There are very few restrictions on steps. They can have side-effects. They can have dependencies
 * shared by other steps. They can interact with global state. Some qualities a step should have:
 * <ul>
 * <li>Steps should handle errors whenever possible.
 * <li>Steps should never hang. If it is possible for a step to have a non-recoverable error then
 * that step should have an output type capable of reflecting this (such as an output type that can
 * contain a value or an exception).
 * </ul>
 *
 * @param <INPUT> The type of the input value required for executing this step.
 * @param <OUTPUT> The type of the value output from executing this step.
 * @see AppFlow
 * @see AppFlowFactory
 */
public interface Step<INPUT, OUTPUT> {

    /**
     * Executes a step with the given input and a callback to receive the step output.
     *
     * @param input The input for this execution of the step.
     * @param callback A callback to receive the output. Never null.
     */
    void execute(INPUT input, Consumer<OUTPUT> callback);

    /**
     * @return The name of this step, used in messages when errors or logging occurs.
     */
    String getName();
}
