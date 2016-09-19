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


package org.livespark.flow.api.descriptor.conversion;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.Displayer;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.UIComponent;
import org.livespark.flow.api.descriptor.AppFlowReferenceDescriptor;
import org.livespark.flow.api.descriptor.StepDescriptor;
import org.livespark.flow.api.descriptor.display.DisplayerDescriptor;
import org.livespark.flow.api.descriptor.display.UIComponentDescriptor;
import org.livespark.flow.api.descriptor.function.FeedbackDescriptor;
import org.livespark.flow.api.descriptor.function.PredicateDescriptor;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;

/**
 * A registry mapping descriptors of flow parts to flow parts.
 */
public interface DescriptorRegistry {

    /**
     * Maps a {@link StepDescriptor} to a {@link Step}.
     *
     * @param descriptor
     *            The descriptor being mapped from. Must not be null.
     * @param step
     *            A supplier for the step being mapped to. Must not be null. Must not supply null.
     * @throws IllegalArgumentException
     *             Thrown if there already exists a mapping for the given descriptor.
     */
    void addStep( StepDescriptor descriptor, Supplier<Step<?, ?>> step );

    /**
     * Maps a {@link TransformationDescriptor} to a {@link AppFlow#andThen(Function) tranformation}.
     *
     * @param descriptor
     *            The descriptor being mapped from. Must not be null.
     * @param transformation
     *            A supplier for the transformation being mapped to. Must not be null. Must not
     *            supply null.
     * @throws IllegalArgumentException
     *             Thrown if there already exists a mapping for the given descriptor.
     */
    void addTransformation( TransformationDescriptor descriptor, Supplier<Function<?, ?>> transformation );

    /**
     * Maps a {@link AppFlowReferenceDescriptor} to an {@link AppFlow}.
     *
     * @param descriptor
     *            The descriptor being mapped from. Must not be null.
     * @param flow
     *            The flow being mapped to. Must not be null.
     * @throws IllegalArgumentException
     *             Thrown if there already exists a mapping for the given descriptor.
     */
    void addFlow( AppFlowReferenceDescriptor descriptor, Supplier<AppFlow<?, ?>> flow );

    /**
     * Maps a {@link PredicateDescriptor} to a {@link Predicate}.
     *
     * @param descriptor
     *            The descriptor being mapped from. Must not be null.
     * @param predicate
     *            A supplier for the predicate being mapped to. Must not be null. Must not supply
     *            null.
     * @throws IllegalArgumentException
     *             Thrown if there already exists a mapping for the given descriptor.
     */
    void addPredicate( PredicateDescriptor descriptor, Supplier<Predicate<?>> predicate );

    /**
     * Maps a {@link FeedbackDescriptor} to a {@link BiFunction}.
     *
     * @param descriptor
     *            The descriptor being mapped from. Must not be null.
     * @param feedback
     *            A supplier for the feedback function being mapped to. Must not be null. Must not supply
     *            null.
     * @throws IllegalArgumentException
     *             Thrown if there already exists a mapping for the given descriptor.
     */
    <INPUT> void addFeedback( FeedbackDescriptor descriptor, Supplier<BiFunction<INPUT, ?, Optional<INPUT>>> feedback );

    /**
     * Maps a {@link UIComponentDescriptor} to a {@link UIComponent}.
     *
     * @param descriptor
     *            The descriptor being mapped from. Must not be null.
     * @param component
     *            A supplier for the component being mapped to. Must not be null. Must not supply
     *            null.
     * @throws IllegalArgumentException
     *             Thrown if there already exists a mapping for the given descriptor.
     */
    void addUIComponent( UIComponentDescriptor descriptor, Supplier<UIComponent<?, ?, ?>> component );

    /**
     * Maps a {@link DisplayerDescriptor} to a {@link Displayer}.
     *
     * @param descriptor
     *            The descriptor being mapped from. Must not be null.
     * @param displayer
     *            A supplier for the displayer being mapped to. Must not be null. Must not supply
     *            null.
     * @throws IllegalArgumentException
     *             Thrown if there already exists a mapping for the given descriptor.
     */
    void addDisplayer( DisplayerDescriptor descriptor, Supplier<Displayer<?>> displayer );

    /**
     * @param descriptor
     *            Must not be null.
     * @return An {@link Optional} containing the {@link Step} mapped to by the given descriptor if
     *         one exists, or else {@link Optional#empty() empty}.
     */
    Optional<Step<?, ?>> getStep( StepDescriptor descriptor );

    /**
     * @param descriptor
     *            Must not be null.
     * @return An {@link Optional} containing the {@link AppFlow#andThen(Function) transformation} mapped to by the given descriptor if
     *         one exists, or else {@link Optional#empty() empty}.
     */
    Optional<Function<?, ?>> getTransformation( TransformationDescriptor descriptor );

    /**
     * @param descriptor
     *            Must not be null.
     * @return An {@link Optional} containing the {@link Predicate predicate} mapped to by the given
     *         descriptor if one exists, or else {@link Optional#empty() empty}.
     */
    Optional<Predicate<?>> getPredicate( PredicateDescriptor descriptor );

    /**
     * @param descriptor
     *            Must not be null.
     * @return An {@link Optional} containing the {@link BiFunction bifunction} mapped to by the given
     *         descriptor if one exists, or else {@link Optional#empty() empty}.
     */
    Optional<BiFunction<?, ?, Optional<?>>> getFeedback( FeedbackDescriptor descriptor );

    /**
     * @param descriptor
     *            Must not be null.
     * @return An {@link Optional} containing the {@link UIComponent} mapped to by the given
     *         descriptor if one exists, or else {@link Optional#empty() empty}.
     */
    Optional<UIComponent<?, ?, ?>> getUIComponent( UIComponentDescriptor descriptor );

    /**
     * @param descriptor
     *            Must not be null.
     * @return An {@link Optional} containing the {@link Displayer} mapped to by the given
     *         descriptor if one exists, or else {@link Optional#empty() empty}.
     */
    Optional<Displayer<?>> getDisplayer( DisplayerDescriptor descriptor );

    /**
     * @param descriptor
     *            Must not be null.
     * @return An {@link Optional} containing the {@link AppFlow} mapped to by the given descriptor if
     *         one exists, or else {@link Optional#empty() empty}.
     */
    Optional<AppFlow<?, ?>> getAppFlow( AppFlowReferenceDescriptor descriptor );

    /**
     * @return A collection of every {@link StepDescriptor} for which this registry has mappings.
     *         Never null.
     */
    Collection<StepDescriptor> getStepDescriptors();

    /**
     * @return A collection of every {@link TransformationDescriptor} for which this registry has
     *         mappings. Never null.
     */
    Collection<TransformationDescriptor> getTransformationDescriptors();

    /**
     * @return A collection of every {@link PredicateDescriptor} for which this registry has
     *         mappings. Never null.
     */
    Collection<PredicateDescriptor> getPredicateDescriptors();

    /**
     * @return A collection of every {@link FeedbackDescriptor} for which this registry has
     *         mappings. Never null.
     */
    Collection<FeedbackDescriptor> getFeedbackDescriptors();

    /**
     * @return A collection of every {@link UIComponentDescriptor} for which this registry has
     *         mappings. Never null.
     */
    Collection<UIComponentDescriptor> getUIComponentDescriptors();

    /**
     * @return A collection of every {@link DisplayerDescriptor} for which this registry has
     *         mappings. Never null.
     */
    Collection<DisplayerDescriptor> getDisplayerDescriptors();

    /**
     * @return A collection of every {@link AppFlowReferenceDescriptor} for which this registry has
     *         mappings. Never null.
     */
    Collection<AppFlowReferenceDescriptor> getFlowDescriptors();

}
