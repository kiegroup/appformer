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

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.AppFlowReferenceDescriptor;
import org.livespark.flow.api.descriptor.DescriptorFactory;
import org.livespark.flow.api.descriptor.StepDescriptor;
import org.livespark.flow.api.descriptor.common.FlowPartDescriptor;
import org.livespark.flow.api.descriptor.display.DisplayerDescriptor;
import org.livespark.flow.api.descriptor.display.UIComponentDescriptor;
import org.livespark.flow.api.descriptor.display.UIStepDescriptor;
import org.livespark.flow.api.descriptor.function.FeedbackDescriptor;
import org.livespark.flow.api.descriptor.function.PredicateDescriptor;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;
import org.livespark.flow.api.descriptor.transition.CommandTransitionDescriptor;
import org.livespark.flow.api.descriptor.transition.OptionalTransitionDescriptor;
import org.livespark.flow.api.descriptor.transition.PredicateTransitionDescriptor;
import org.livespark.flow.api.descriptor.transition.TransitionDescriptor;
import org.livespark.flow.api.descriptor.type.Type;

@ApplicationScoped
public class DescriptorFactoryImpl implements DescriptorFactory {

    private int seed = 1;

    @Override
    public StepDescriptor createStepDescriptor( final String identifier,
                                                final Type inputType,
                                                final Type outputType ) {
        return new StepDescriptorImpl( identifier, seed++, inputType, outputType );
    }

    @Override
    public FeedbackDescriptor createFeedbackDescriptor( final String identifier,
                                                        final Type inputType,
                                                        final Type outputType ) {
        return new FeedbackDescriptorImpl( identifier, seed++, inputType, outputType );
    }

    @Override
    public UIComponentDescriptor createUIComponentDescriptor( final String identifier,
                                                              final Type inputType,
                                                              final Type outputType,
                                                              final Type componentType ) {
        return new UIComponentDescriptorImpl( identifier, seed++, inputType, outputType, componentType );
    }

    @Override
    public UIStepDescriptor createUIStepDescriptor( final DisplayerDescriptor displayerIdentifier,
                                                    final UIStepDescriptor.Action action,
                                                    final UIComponentDescriptor uiComponentInstanceDescriptor ) {
        return new UIStepDescriptorImpl( displayerIdentifier, uiComponentInstanceDescriptor, action );
    }

    @Override
    public DisplayerDescriptor createDisplayerDescriptor( final String identifier,
                                                          final Type componentType ) {
        return new DisplayerDescriptorImpl( identifier, seed++, componentType );
    }

    @Override
    public TransformationDescriptor createTransformationDescriptor( final String identifier,
                                                                    final Type inputType,
                                                                    final Type outputType ) {
        return new TranformationDescriptorImpl( identifier, seed++, inputType, outputType );
    }

    @Override
    public <E extends Enum<E>> CommandTransitionDescriptor createCommandTransitionDescriptor( final Map<E, AppFlowDescriptor> mapping,
                                                                                             final Type inputTypeName ) {
        return new CommandTransitionDescriptorImpl( inputTypeName, mapping );
    }

    @Override
    public AppFlowDescriptor createAppFlowDescriptor( final FlowPartDescriptor flowPart ) {
        if ( flowPart instanceof AppFlowDescriptor ) {
            return (AppFlowDescriptor) flowPart;
        }
        else if ( flowPart instanceof StepDescriptor
                  || flowPart instanceof TransformationDescriptor
                  || flowPart instanceof TransitionDescriptor ) {
            return new SequentialAppFlowDescriptor( flowPart );
        }
        else {
            throw new IllegalArgumentException( "Unrecognized FlowPartDescriptor: " + flowPart );
        }
    }

    @Override
    public AppFlowReferenceDescriptor createAppFlowDescriptor( final String identifier,
                                                            final Type inputType,
                                                            final Type outputType ) {
        return new AppFlowReferenceDescriptorImpl( identifier, seed++, inputType, outputType );
    }

    @Override
    public PredicateDescriptor createPredicateDescriptor( final String identifier,
                                                          final Type inputType ) {
        return new PredicateDescriptorImpl( identifier, seed++, inputType );
    }

    @Override
    public PredicateTransitionDescriptor createPredicateTransitionDescriptor( final PredicateDescriptor predicate,
                                                                              final AppFlowDescriptor ifTrue,
                                                                              final AppFlowDescriptor ifFalse ) {
        return new PredicateTransitionDescriptorImpl( predicate, ifTrue, ifFalse );
    }

    @Override
    public OptionalTransitionDescriptor createOptionalTransitionDescriptor( final AppFlowDescriptor ifPresent,
                                                                            final AppFlowDescriptor ifAbsent ) {
        return new OptionalTransitionDescriptorImpl( ifPresent, ifAbsent );
    }

}
