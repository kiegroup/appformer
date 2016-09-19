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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.AppFlowFactory;
import org.livespark.flow.api.Command;
import org.livespark.flow.api.Displayer;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.UIComponent;
import org.livespark.flow.api.Unit;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.AppFlowReferenceDescriptor;
import org.livespark.flow.api.descriptor.StepReferenceDescriptor;
import org.livespark.flow.api.descriptor.StepDescriptor;
import org.livespark.flow.api.descriptor.common.FlowPartDescriptor;
import org.livespark.flow.api.descriptor.common.HasIdentifier;
import org.livespark.flow.api.descriptor.conversion.Converter;
import org.livespark.flow.api.descriptor.conversion.DescriptorRegistry;
import org.livespark.flow.api.descriptor.display.DisplayerDescriptor;
import org.livespark.flow.api.descriptor.display.UIComponentDescriptor;
import org.livespark.flow.api.descriptor.display.UIStepDescriptor;
import org.livespark.flow.api.descriptor.function.FeedbackDescriptor;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;
import org.livespark.flow.api.descriptor.transition.CommandTransitionDescriptor;
import org.livespark.flow.api.descriptor.transition.OptionalTransitionDescriptor;
import org.livespark.flow.api.descriptor.transition.PredicateTransitionDescriptor;
import org.livespark.flow.api.descriptor.transition.TransitionDescriptor;

@ApplicationScoped
public class ConverterImpl implements Converter {

    private static class Context {
        Map<InstanceKey<AppFlowReferenceDescriptor>, AppFlow<?, ?>> flows = new HashMap<>();
        Map<InstanceKey<FeedbackDescriptor>, BiFunction<?, ?, Optional<?>>> feedbacks = new HashMap<>();
        Map<InstanceKey<TransformationDescriptor>, Function<?, ?>> transformations = new HashMap<>();
        Map<InstanceKey<StepReferenceDescriptor>, Step<?, ?>> steps = new HashMap<>();
        Map<InstanceKey<UIComponentDescriptor>, UIComponent<?, ?, ?>> uiComponents = new HashMap<>();
        Map<InstanceKey<DisplayerDescriptor>, Displayer<?>> displayers = new HashMap<>();
    }

    private static class InstanceKey<D extends HasIdentifier> {
        final D descriptor;
        InstanceKey( final D descriptor ) {
            this.descriptor = descriptor;
        }
        @Override
        public int hashCode() {
            return descriptor.hashCode() ^ descriptor.getInstanceId();
        }
        @Override
        public boolean equals( final Object obj ) {
            if ( obj instanceof InstanceKey ) {
                final InstanceKey<?> other = (InstanceKey<?>) obj;
                return other.descriptor.equals( descriptor ) && other.descriptor.getInstanceId() == descriptor.getInstanceId();
            }

            return false;
        }
    }

    private final AppFlowFactory factory;

    protected ConverterImpl() {
        this( null );
    }

    @Inject
    public ConverterImpl( final AppFlowFactory factory ) {
        this.factory = factory;
    }

    @Override
    public AppFlow<?, ?> convert( final DescriptorRegistry registry,
                                  final AppFlowDescriptor descriptor ) {
        return convert( registry, new Context(), descriptor );
    }

    private AppFlow<?, ?> convert( final DescriptorRegistry registry, final Context context, final AppFlowDescriptor descriptor ) {
        if ( descriptor instanceof AppFlowReferenceDescriptor ) {
            return context
                    .flows
                    .computeIfAbsent( new InstanceKey<>( (AppFlowReferenceDescriptor) descriptor ),
                                           key -> registry
                                                     .getAppFlow( key.descriptor )
                                                     .orElseThrow( () -> new RuntimeException( "Could not find descriptor in registry: "
                                                                                                                                 + key.descriptor ) ) );
        }
        else if ( descriptor instanceof SequentialAppFlowDescriptor ) {
            return convert( registry, context, ((SequentialAppFlowDescriptor) descriptor).parts );
        }
        else if (descriptor instanceof LoopedAppFlowDescriptor ) {
            return convert( registry, context, ((LoopedAppFlowDescriptor) descriptor).getLoopedFlow(), ((LoopedAppFlowDescriptor) descriptor).getFeedback() );
        }
        else {
            throw new IllegalArgumentException( "Unrecognized AppFlowDescriptor implementation [" + descriptor + "]" );
        }
    }

    private AppFlow<?, ?> convert( final DescriptorRegistry registry,
                                   final Context context,
                                   final AppFlowDescriptor loopedFlow,
                                   final FeedbackDescriptor feedbackDescriptor ) {
        final AppFlow<?, ?> looped = convert( registry, context, loopedFlow );
        final BiFunction<?, ?, Optional<?>> feedback =
                context
                    .feedbacks
                                .computeIfAbsent( new InstanceKey<>( feedbackDescriptor ),
                                                  key -> registry
                                                            .getFeedback( key.descriptor )
                                                            .orElseThrow( () -> new IllegalArgumentException( missingDescriptorMessage( feedbackDescriptor ) ) ) );

        return ((AppFlow) looped).loop( factory, feedback );
    }

    private AppFlow<?, ?> convert( final DescriptorRegistry registry, final Context context, final List<FlowPartDescriptor> parts ) {
        assertNonEmpty( parts );
        AppFlow<?, ?> flow = flowStart( registry, context, parts.get( 0 ) );
        for ( int i = 1; i < parts.size(); i++ ) {
            flow = append( registry, context, flow, parts.get( i ) );
        }

        return flow;
    }

    private AppFlow<?, ?> flowStart( final DescriptorRegistry registry, final Context context, final FlowPartDescriptor partDescriptor ) {
        if ( partDescriptor instanceof StepDescriptor ) {
            final Step<?, ?> step = convert( registry, context, (StepDescriptor) partDescriptor );

            return factory.buildFromStep( step );
        }
        else if ( partDescriptor instanceof TransformationDescriptor ) {
            final Function<?, ?> transformation = lookupTransformation( registry, context, (TransformationDescriptor) partDescriptor );

            return factory.buildFromFunction( transformation );
        }
        else if ( partDescriptor instanceof TransitionDescriptor ) {
            final Function<?, AppFlow<Unit, ?>> transition = createTransition( registry, (TransitionDescriptor) partDescriptor, context );
            return factory.buildFromTransition( (Function) transition );
        }
        else if ( partDescriptor instanceof AppFlowDescriptor ) {
            return convert( registry, context, (AppFlowDescriptor) partDescriptor );
        }
        else {
            throw new IllegalArgumentException( "Unrecognized FlowPartDescriptor: " + partDescriptor );
        }
    }

    private Function<?, ?> lookupTransformation( final DescriptorRegistry registry,
                                      final Context context, final TransformationDescriptor descriptor ) {
        return context
                .transformations
                .computeIfAbsent( new InstanceKey<>( descriptor ),
                                  key -> registry
                                          .getTransformation( descriptor )
                                          .orElseThrow( () -> new IllegalArgumentException( missingDescriptorMessage( descriptor ) ) ) );
    }

    private AppFlow<?, ?> append( final DescriptorRegistry registry,
                                  final Context context,
                                  final AppFlow<?, ?> flow,
                                  final FlowPartDescriptor partDescriptor ) {
        if ( partDescriptor instanceof StepDescriptor ) {
            final Step<?, ?> step = convert( registry, context, (StepDescriptor) partDescriptor );

            return flow.andThen( (Step) step );
        }
        else if ( partDescriptor instanceof TransformationDescriptor ) {
            final Function<?, ?> transformation = lookupTransformation( registry, context, (TransformationDescriptor) partDescriptor );

            return flow.andThen( (Function) transformation );
        }
        else if ( partDescriptor instanceof TransitionDescriptor ) {
            final Function<?, AppFlow<Unit, ?>> transition = createTransition( registry, (TransitionDescriptor) partDescriptor, context );

            return flow.transitionTo( (Function ) transition );
        }
        else if ( partDescriptor instanceof AppFlowDescriptor ) {
            return flow.andThen( (AppFlow) convert( registry, context, (AppFlowDescriptor) partDescriptor ) );
        }
        else {
            throw new IllegalArgumentException( "Unrecognized FlowPartDescriptor: " + partDescriptor );
        }
    }

    private Step<?, ?> convert( final DescriptorRegistry registry, final Context context, final StepDescriptor descriptor ) {
        if ( descriptor instanceof StepReferenceDescriptor ) {
            return context
                    .steps
                    .computeIfAbsent( new InstanceKey<>( (StepReferenceDescriptor) descriptor ),
                                      key -> registry
                                              .getStep( key.descriptor )
                                              .orElseThrow( () -> new IllegalArgumentException( missingDescriptorMessage( (StepReferenceDescriptor) descriptor ) ) ) );
        }
        else {
            final UIStepDescriptor uiStepDescriptor = (UIStepDescriptor) descriptor;
            final UIComponentDescriptor uiComponentDescriptor = uiStepDescriptor.getUIComponent();
            final DisplayerDescriptor displayerDescriptor = uiStepDescriptor.getDisplayerDescriptor();

            final UIComponent uiComponent = context
                .uiComponents
                .computeIfAbsent( new InstanceKey<>( uiComponentDescriptor ),
                                  key -> registry
                                             .getUIComponent( key.descriptor )
                                             .orElseThrow( () -> new IllegalArgumentException( missingDescriptorMessage( uiComponentDescriptor ) ) ) );
            final Displayer displayer = context
                .displayers
                .computeIfAbsent( new InstanceKey<>( displayerDescriptor ),
                                  key -> registry
                                             .getDisplayer( key.descriptor )
                                             .orElseThrow( () -> new IllegalArgumentException( missingDescriptorMessage( displayerDescriptor ) ) ) );

            switch ( uiStepDescriptor.getAction() ) {
                case SHOW :
                    return new Step() {

                        @Override
                        public void execute( final Object input,
                                             final Consumer callback ) {
                            displayer.show( uiComponent );
                            uiComponent.start( input, callback );
                        }

                        @Override
                        public String getName() {
                            return "Show " + uiComponent.getName();
                        }
                    };
                case HIDE :
                    return new Step() {

                        @Override
                        public void execute( final Object input,
                                             final Consumer callback ) {
                            displayer.hide( uiComponent );
                            callback.accept( input );
                        }

                        @Override
                        public String getName() {
                            return "Hide " + uiComponent.getName();
                        }
                    };
                case SHOW_AND_HIDE :
                    return new Step() {

                        @Override
                        public void execute( final Object input,
                                             final Consumer callback ) {
                            displayer.show( uiComponent );
                            uiComponent.start( input, output -> {
                                displayer.hide( uiComponent );
                                callback.accept( output );
                            } );
                        }

                        @Override
                        public String getName() {
                            return "Show and hide " + uiComponent.getName();
                        }
                    };
                default:
                    throw new IllegalArgumentException( "Unrecognized action: " + uiStepDescriptor.getAction() );
            }
        }
    }

    private Function<?, AppFlow<Unit, ?>> createTransition( final DescriptorRegistry registry,
                                                            final TransitionDescriptor partDescriptor,
                                                            final Context context ) {
        if ( partDescriptor instanceof CommandTransitionDescriptor ) {
            return createCommandTransition( registry, (CommandTransitionDescriptor) partDescriptor, context );
        }
        else if ( partDescriptor instanceof PredicateTransitionDescriptor ) {
            return createPredicateTransition( registry, (PredicateTransitionDescriptor) partDescriptor, context );
        }
        else if ( partDescriptor instanceof OptionalTransitionDescriptor ) {
            return createOptionalTransition( registry, (OptionalTransitionDescriptor) partDescriptor, context );
        }
        else {
            throw new UnsupportedOperationException( "Unrecognized " + TransitionDescriptor.class.getSimpleName()
                                                     + " subtype, [" + partDescriptor.getClass().getName() + "]." );
        }
    }

    private Function<Optional<?>, AppFlow<Unit, ?>> createOptionalTransition( final DescriptorRegistry registry,
                                                                              final OptionalTransitionDescriptor partDescriptor,
                                                                              final Context context ) {
        final AppFlow<?, ?> ifPresent = convert( registry, context, partDescriptor.getMappingFor( true ) );
        final AppFlow<?, ?> ifAbsent = convert( registry, context, partDescriptor.getMappingFor( false ) );

        return o -> o.map( value -> ((AppFlow) ifPresent).withInput( value ) ).orElse( ifAbsent );
    }

    private Function<?, AppFlow<Unit, ?>> createPredicateTransition( final DescriptorRegistry registry,
                                                                     final PredicateTransitionDescriptor partDescriptor,
                                                                     final Context context ) {
        final AppFlow<?, ?> ifTrue = convert( registry, context, partDescriptor.getMappingFor( true ) );
        final AppFlow<?, ?> ifFalse = convert( registry, context, partDescriptor.getMappingFor( false ) );

        final Predicate<?> predicate = registry
                                           .getPredicate( partDescriptor.getPredicate() )
                                           .orElseThrow( () -> new IllegalArgumentException( missingDescriptorMessage( partDescriptor.getPredicate() ) ) );

        return o -> ((Predicate) predicate).test( o ) ? ((AppFlow) ifTrue).withInput( o ) : ((AppFlow) ifFalse).withInput( o );
    }

    private Function<Command<?, ?>, AppFlow<Unit, ?>> createCommandTransition( final DescriptorRegistry registry,
                                                                               final CommandTransitionDescriptor partDescriptor,
                                                                               final Context context ) {
        final Map<? extends Enum<?>, AppFlow<?, ?>> transitionMap = new HashMap<>();
        for ( final Entry<? extends Enum<?>, AppFlowDescriptor> entry : partDescriptor.getMapping().entrySet() ) {
            final AppFlowDescriptor flowDescriptor = entry.getValue();
            final AppFlow<?, ?> flow = convert( registry, context, flowDescriptor );
            ((Map) transitionMap).put( entry.getKey(), flow );
        }

        return (final Command<?, ?> command) -> {
            final AppFlow<?, ?> flow = transitionMap.get( command.commandType );
            if ( flow == null ) {
                throw new IllegalStateException( "Transition does not have mapping for " + command.commandType );
            }

            return ((AppFlow) flow).withInput( command.value );
        };
    }

    private void assertNonEmpty( final List<FlowPartDescriptor> parts ) {
        if ( parts.isEmpty() ) {
            throw new IllegalArgumentException( "Cannot convert empty " + AppFlowDescriptor.class.getSimpleName() + ".");
        }
    }

    private String missingDescriptorMessage( final HasIdentifier descriptor ) {
        return "Could not find descriptor with id " + descriptor.getDescriptorIdentifier() + ": " + descriptor.toString();
    }

}
