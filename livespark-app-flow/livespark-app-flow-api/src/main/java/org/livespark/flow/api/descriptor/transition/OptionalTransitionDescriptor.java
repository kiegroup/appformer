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

import java.util.Optional;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.Unit;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;

/**
 * A descriptor of a {@link AppFlow#transitionTo(java.util.function.Function) transition} based on
 * an {@link Optional} value.
 *
 * @see AppFlowDescriptor
 */
public interface OptionalTransitionDescriptor extends TransitionDescriptor {

    /**
     * @return A descriptor of an {@link AppFlow} to be run given whether an {@link Optional} was
     *         present. The flow descriptor returned when {@code present == true} should convert to
     *         a flow that takes the value inside the {@link Optional} as an input. The flow
     *         descriptor returned when {@code present != true} should convert to a flow that takes
     *         {@link Unit} as input.
     */
    AppFlowDescriptor getMappingFor( boolean present );

}
