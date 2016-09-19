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


package org.livespark.flow.impl.descriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.Displayer;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.UIComponent;
import org.livespark.flow.api.descriptor.AppFlowReferenceDescriptor;
import org.livespark.flow.api.descriptor.StepDescriptor;
import org.livespark.flow.api.descriptor.conversion.DescriptorRegistry;
import org.livespark.flow.api.descriptor.display.DisplayerDescriptor;
import org.livespark.flow.api.descriptor.display.UIComponentDescriptor;
import org.livespark.flow.api.descriptor.function.FeedbackDescriptor;
import org.livespark.flow.api.descriptor.function.PredicateDescriptor;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;

@ApplicationScoped
public class DescriptorRegistryImpl implements DescriptorRegistry {

    private final Map<StepDescriptor, Supplier<Step<?, ?>>> steps = new HashMap<>();
    private final Map<TransformationDescriptor, Supplier<Function<?, ?>>> transformations = new HashMap<>();
    private final Map<PredicateDescriptor, Supplier<Predicate<?>>> predicates = new HashMap<>();
    private final Map<AppFlowReferenceDescriptor, Supplier<AppFlow<?, ?>>> flows = new HashMap<>();
    private final Map<FeedbackDescriptor, Supplier<BiFunction<?, ?, Optional<?>>>> feedbacks = new HashMap<>();
    private final Map<UIComponentDescriptor, Supplier<UIComponent<?, ?, ?>>> uiComponents = new HashMap<>();
    private final Map<DisplayerDescriptor, Supplier<Displayer<?>>> displayers = new HashMap<>();

    @Override
    public void addStep( final StepDescriptor key,
                         final Supplier<Step<?, ?>> step ) {
        assertKeyIsAbsent( key, steps );
        steps.put( key, step );
    }

    @Override
    public void addTransformation( final TransformationDescriptor key,
                                   final Supplier<Function<?, ?>> transformation ) {
        assertKeyIsAbsent( key, transformations );
        transformations.put( key, transformation );
    }

    @Override
    public void addPredicate( final PredicateDescriptor key,
                              final Supplier<Predicate<?>> predicate ) {
        assertKeyIsAbsent( key, predicates );
        predicates.put( key, predicate );
    }

    @Override
    public <INPUT> void addFeedback( final FeedbackDescriptor key,
                                     final Supplier<BiFunction<INPUT, ?, Optional<INPUT>>> feedback ) {
        assertKeyIsAbsent( key, feedbacks );
        feedbacks.put( key, (Supplier) feedback );
    }

    @Override
    public void addUIComponent( final UIComponentDescriptor key,
                                final Supplier<UIComponent<?, ?, ?>> component ) {
        assertKeyIsAbsent( key, uiComponents );
        uiComponents.put( key, component );
    }

    @Override
    public void addDisplayer( final DisplayerDescriptor descriptor,
                              final Supplier<Displayer<?>> displayer ) {
        assertKeyIsAbsent( descriptor, displayers );
        displayers.put( descriptor, displayer );
    }

    @Override
    public void addFlow( final AppFlowReferenceDescriptor key,
                         final Supplier<AppFlow<?, ?>> flow ) {
        assertKeyIsAbsent( key, flows );
        flows.put( key, flow );
    }

    @Override
    public Optional<Step<?, ?>> getStep( final StepDescriptor descriptor ) {
        return Optional.ofNullable( steps.get( descriptor ) ).map( Supplier::get );
    }

    @Override
    public Optional<Function<?, ?>> getTransformation( final TransformationDescriptor descriptor ) {
        return Optional.ofNullable( transformations.get( descriptor ) ).map( Supplier::get );
    }

    @Override
    public Optional<Predicate<?>> getPredicate( final PredicateDescriptor descriptor ) {
        return Optional.ofNullable( predicates.get( descriptor ) ).map( Supplier::get );
    }

    @Override
    public Optional<BiFunction<?, ?, Optional<?>>> getFeedback( final FeedbackDescriptor descriptor ) {
        return Optional.ofNullable( feedbacks.get( descriptor ) ).map( Supplier::get );
    }

    @Override
    public Optional<UIComponent<?, ?, ?>> getUIComponent( final UIComponentDescriptor descriptor ) {
        return Optional.ofNullable( uiComponents.get( descriptor ) ).map( Supplier::get );
    }

    @Override
    public Optional<Displayer<?>> getDisplayer( final DisplayerDescriptor descriptor ) {
        return Optional.ofNullable( displayers.get( descriptor ) ).map( Supplier::get );
    }

    @Override
    public Optional<AppFlow<?, ?>> getAppFlow( final AppFlowReferenceDescriptor descriptor ) {
        return Optional.ofNullable( flows.get( descriptor ) ).map( Supplier::get );
    }

    @Override
    public Collection<StepDescriptor> getStepDescriptors() {
        return Collections.unmodifiableCollection( steps.keySet() );
    }

    @Override
    public Collection<TransformationDescriptor> getTransformationDescriptors() {
        return Collections.unmodifiableCollection( transformations.keySet() );
    }

    @Override
    public Collection<PredicateDescriptor> getPredicateDescriptors() {
        return Collections.unmodifiableCollection( predicates.keySet() );
    }

    @Override
    public Collection<FeedbackDescriptor> getFeedbackDescriptors() {
        return Collections.unmodifiableCollection( feedbacks.keySet() );
    }

    @Override
    public Collection<UIComponentDescriptor> getUIComponentDescriptors() {
        return Collections.unmodifiableCollection( uiComponents.keySet() );
    }

    @Override
    public Collection<DisplayerDescriptor> getDisplayerDescriptors() {
        return Collections.unmodifiableCollection( displayers.keySet() );
    }

    @Override
    public Collection<AppFlowReferenceDescriptor> getFlowDescriptors() {
        return Collections.unmodifiableCollection( flows.keySet() );
    }

    private static <K> void assertKeyIsAbsent( final K key, final Map<K, ?> map ) {
        if ( map.containsKey( key ) ) {
            throw new IllegalArgumentException( "Cannot register duplicate flow part [" + key + "]." );
        }
    }

}
