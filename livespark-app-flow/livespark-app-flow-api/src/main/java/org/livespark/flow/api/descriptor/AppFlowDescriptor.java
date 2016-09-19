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


package org.livespark.flow.api.descriptor;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.descriptor.common.FlowPartDescriptor;
import org.livespark.flow.api.descriptor.common.HasIdentifier;
import org.livespark.flow.api.descriptor.conversion.Converter;
import org.livespark.flow.api.descriptor.function.FeedbackDescriptor;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;
import org.livespark.flow.api.descriptor.transition.TransitionDescriptor;

/**
 * <p>
 * Describes an {@link AppFlow} in a serializable way.
 *
 * <p>
 * Where an {@link AppFlow} consists of {@link AppFlow#andThen(Step) steps},
 * {@link AppFlow#andThen(java.util.function.Function) transformations}, and
 * {@link AppFlow#transitionTo(java.util.function.Function) transitions}, an
 * {@code AppFlowDescriptor} describes a flow in terms of references to these parts.
 *
 * <p>
 * Each part of an {@code AppFlowDescriptor} is either a {@link HasIdentifier uniquely identified}
 * reference to a part of an {@link AppFlow}, or else a combination of such references.
 *
 * <p>
 * An {@link AppFlowDescriptor} can be converted to an {@link AppFlow} with the {@link Converter}.
 *
 * @see AppFlow
 * @see StepDescriptor
 * @see TransformationDescriptor
 * @see TransitionDescriptor
 * @see AppFlowReferenceDescriptor
 */
public interface AppFlowDescriptor extends FlowPartDescriptor {

    /**
     * Analogous to {@link AppFlow#andThen(Step)}.
     */
    AppFlowDescriptor andThen( StepDescriptor step );

    /**
     * Analogous to {@link AppFlow#andThen(java.util.function.Function)}.
     */
    AppFlowDescriptor andThen( TransformationDescriptor transformation );

    /**
     * Analogous to {@link AppFlow#andThen(AppFlow)}.
     */
    AppFlowDescriptor andThen( AppFlowDescriptor flow );

    /**
     * Analogous to {@link AppFlow#transitionTo(java.util.function.Function)}.
     */
    AppFlowDescriptor transitionTo( TransitionDescriptor transition );

    /**
     * Analogous to
     * {@link AppFlow#loop(org.livespark.flow.api.AppFlowFactory, java.util.function.BiFunction)}.
     */
    AppFlowDescriptor loop( FeedbackDescriptor descriptor );

}
