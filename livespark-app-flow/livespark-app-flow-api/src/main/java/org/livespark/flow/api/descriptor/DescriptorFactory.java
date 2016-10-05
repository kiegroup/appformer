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

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.Command;
import org.livespark.flow.api.Displayer;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.UIComponent;
import org.livespark.flow.api.Unit;
import org.livespark.flow.api.descriptor.common.FlowPartDescriptor;
import org.livespark.flow.api.descriptor.common.HasIdentifier;
import org.livespark.flow.api.descriptor.conversion.DescriptorRegistry;
import org.livespark.flow.api.descriptor.display.DisplayerDescriptor;
import org.livespark.flow.api.descriptor.display.UIComponentDescriptor;
import org.livespark.flow.api.descriptor.display.UIStepDescriptor;
import org.livespark.flow.api.descriptor.display.UIStepDescriptor.Action;
import org.livespark.flow.api.descriptor.function.FeedbackDescriptor;
import org.livespark.flow.api.descriptor.function.PredicateDescriptor;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;
import org.livespark.flow.api.descriptor.transition.CommandTransitionDescriptor;
import org.livespark.flow.api.descriptor.transition.OptionalTransitionDescriptor;
import org.livespark.flow.api.descriptor.transition.PredicateTransitionDescriptor;
import org.livespark.flow.api.descriptor.type.Type;

/**
 * <p>
 * Used to create flow parts (such as {@link StepDescriptor}, {@link TransformationDescriptor},
 * etc.) that can be combined to build {@link AppFlowDescriptor AppFlowDescriptors}.
 *
 * <p>
 * There are two kinds of descriptors: those that implement {@link HasIdentifier} and those that do
 * not.
 *
 * <p>
 * Descriptors implementing {@link HasIdentifier} are simple references to a part of an
 * {@link AppFlow} defined elsewhere. The {@link DescriptorRegistry} is used to map descriptions to
 * actual flow parts when converting from an {@link AppFlowDescriptor} to an {@link AppFlow}.
 *
 * <p>
 * Descriptors that do not implement {@link HasIdentifier} are complex combinations of other
 * descriptors.
 *
 * @see AppFlowDescriptor
 * @see DescriptorRegistry
 */
public interface DescriptorFactory {

    /**
     * @param identifier
     *            Must not be null.
     * @param inputType
     *            Must not be null.
     * @param outputType
     *            Must not be null.
     * @return A description of a {@link Step step}. Never null.
     */
    StepDescriptor createStepDescriptor( String identifier, Type inputType, Type outputType );

    /**
     * @param identifier
     *            Must not be null.
     * @param inputType
     *            Must not be null.
     * @param outputType
     *            Must not be null.
     * @return A description of a {@link AppFlow#andThen(java.util.function.Function)
     *         transformation}. Never null.
     */
    TransformationDescriptor createTransformationDescriptor( String identifier, Type inputType, Type outputType );

    /**
     * @param identifier
     *            Must not be null.
     * @param inputType
     *            Must not be null.
     * @return A description of a {@link Predicate} for use in a
     *         {@link PredicateTransitionDescriptor}. Never null.
     */
    PredicateDescriptor createPredicateDescriptor( String identifier, Type inputType );

    /**
     * @param identifier
     *            Must not be null.
     * @param inputType
     *            Must not be null.
     * @param outputType
     *            Must not be null.
     * @return A description of a {@link BiFunction} for use in with
     *         {@link AppFlowDescriptor#loop(FeedbackDescriptor)}. Never null.
     */
    FeedbackDescriptor createFeedbackDescriptor( String identifier, Type inputType, Type outputType );

    /**
     * @param identifier
     *            Must not be null.
     * @param inputType
     *            Must not be null.
     * @param outputType
     *            Must not be null.
     * @param componentType
     *            Must not be null.
     * @return A description of a {@link UIComponent} that can be used to create a
     *         {@link UIStepDescriptor}. Never null.
     */
    UIComponentDescriptor createUIComponentDescriptor( String identifier, Type inputType, Type outputType, Type componentType );

    /**
     * @param displayer
     *            Descriptor of the {@link Displayer} for displaying the {@link UIComponent} in this
     *            step. Must not be null.
     * @param action
     *            The display action to be taken in this step. Must not be null.
     * @param uiComponent
     *            Descriptor of the {@link UIComponent} to be showin in this step. Must not be null.
     * @return A description of a {@link Step} that shows and/or hides a {@link UIComponent}. Never
     *         null.
     */
    UIStepDescriptor createUIStepDescriptor( DisplayerDescriptor displayer, Action action, UIComponentDescriptor uiComponent );

    /**
     * @param identifier
     *            Must not be null.
     * @param componentType
     *            Must not be null.
     * @return A description of a {@link Displayer} that can be used to create a
     *         {@link UIStepDescriptor}. Never null.
     */
    DisplayerDescriptor createDisplayerDescriptor( String identifier, Type componentType );

    /**
     * This creates a transition for a flow with an output type {@link Command Command<E, T>}. The
     * map argument contains enum keys that are mapped to a descriptor of the flow to be executed
     * for a command with that enum. The descriptors must convert to flows that have {@code T} as an
     * input.
     *
     * @param mapping
     *            A mapping of enums to descriptors of flows to be executed in a
     *            {@link AppFlow#transitionTo(java.util.function.Function) transition}. Must not be
     *            null.
     * @param inputType
     *            Must not be null.
     * @return A description of a {@link AppFlow#transitionTo(java.util.function.Function)
     *         transition} based on the enum of a {@link Command}. Never null.
     */
    <E extends Enum<E>> CommandTransitionDescriptor createCommandTransitionDescriptor( Map<E, AppFlowDescriptor> mapping, Type inputType );

    /**
     * <p>
     * This creates a transition for a flow based on the evaluation of a predicate. If the
     * {@link PredicateDescriptor} argument describes a {@link Predicate Predicate<T>} then this
     * transition can be used on a flow with an output type {@code T}.
     * <p>
     * The {@code ifTrue} and {@code ifFalse} arguments must convert to flows with {@code T} as an
     * input. This transition is converted to a function that evaluates the predicate, and calls
     * {@code ifTrue} if the predicate returns true, or else {@code ifFalse}. The value evaluated by
     * the predicate is then passed to the respective flow.
     *
     * @param ifTrue
     *            Descriptor of the flow executed if the predicate returns true. Must not be null.
     * @param ifFalse
     *            Descriptor of the flow executed if the predicate returns false. Must not be null.
     * @return A description of a {@link AppFlow#transitionTo(java.util.function.Function)} based on
     *         a predicate. Never null.
     */
    PredicateTransitionDescriptor createPredicateTransitionDescriptor( PredicateDescriptor predicate, AppFlowDescriptor ifTrue, AppFlowDescriptor ifFalse );

    /**
     * <p>
     * This creates a transition for a flow based on an {@link Optional Optional<T>}, to be used on
     * a flow with the same output type.
     * <p>
     * At runtime, this transition receives an {@code Optional<T>} as input. If the optional is
     * present, the {@code ifPresent} flow is executed with the value contained in the optional as
     * its input. Otherwise, the {@code ifAbsent} flow is executed with {@link Unit} as input.
     *
     * @param ifPresent
     *            Descriptor of the flow executed if the optional is not empty. Must not be null.
     * @param ifAbsent
     *            Descriptor of the flow executed if the optional is empty. Must not be null.
     * @return A description of a {@link AppFlow#transitionTo(java.util.function.Function)} based on
     *         whether an {@link Optional} has a value. Never null.
     */
    OptionalTransitionDescriptor createOptionalTransitionDescriptor( AppFlowDescriptor ifPresent, AppFlowDescriptor ifAbsent );

    /**
     * An {@link AppFlowDescriptor} that implements {@link HasIdentifier} (and consequently is just
     * a reference to an {@link AppFlow} that should be found in a {@link DescriptorRegistry} at
     * conversion-time).
     *
     * @param identifier
     *            Must not be null.
     * @param inputType
     *            Must not be null.
     * @param outputType
     *            Must not be null.
     * @return A description of a {@link AppFlow} that exists in a {@link DescriptorRegistry} at
     *         conversion-time. Never null.
     */
    AppFlowReferenceDescriptor createAppFlowDescriptor( String identifier, Type inputType, Type outputType );

    /**
     * Initializes an {@link AppFlowDescriptor} from a single flow part descriptor. Used for staring
     * an {@link AppFlowDescriptor} that is a combination of flow parts.
     *
     * @param identifier
     *            Must not be null.
     * @param inputTypeName
     *            Must not be null.
     * @param outputTypeName
     *            Must not be null.
     * @return A description of a {@link AppFlow transformation}. Never null.
     */
    AppFlowDescriptor createAppFlowDescriptor( FlowPartDescriptor flowPart );

}
