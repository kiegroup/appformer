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


package org.livespark.flow.api.descriptor.transition;

import java.util.Map;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.Command;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.DescriptorFactory;
import org.livespark.flow.api.descriptor.conversion.DescriptorRegistry;

/**
 * <p>
 * Describes a {@link AppFlow#transitionTo(java.util.function.Function) transition} based on the
 * enum part of a {@link Command}.
 *
 * @see DescriptorFactory
 * @see DescriptorRegistry
 * @see AppFlowDescriptor
 * @see AppFlow
 * @see Command
 */
public interface CommandTransitionDescriptor extends TransitionDescriptor {

    /**
     * @return The mapping from enum parts of a {@link Command} to descriptors of {@link AppFlow
     *         AppFlows} to be executed. The flow descriptors should convert to flows that take the
     *         non-enum part of a {@link Command} as input.
     */
    <E extends Enum<E>> Map<E, AppFlowDescriptor> getMapping();

}
