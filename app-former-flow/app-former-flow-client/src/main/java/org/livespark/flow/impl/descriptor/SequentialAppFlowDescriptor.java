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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.StepDescriptor;
import org.livespark.flow.api.descriptor.common.FlowPartDescriptor;
import org.livespark.flow.api.descriptor.function.FeedbackDescriptor;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;
import org.livespark.flow.api.descriptor.transition.TransitionDescriptor;
import org.livespark.flow.api.descriptor.type.Type;

@Portable
public class SequentialAppFlowDescriptor implements AppFlowDescriptor {

    List<FlowPartDescriptor> parts;

    // For marshalling
    public SequentialAppFlowDescriptor() {}

    public SequentialAppFlowDescriptor( final FlowPartDescriptor start ) {
        parts = new ArrayList<>();
        parts.add( start );
    }

    @Override
    public AppFlowDescriptor andThen( final StepDescriptor step ) {
        parts.add( step );
        return this;
    }

    @Override
    public AppFlowDescriptor andThen( final TransformationDescriptor transformation ) {
        parts.add( transformation );
        return this;
    }

    @Override
    public AppFlowDescriptor transitionTo( final TransitionDescriptor transition ) {
        parts.add( transition );
        return this;
    }

    @Override
    public AppFlowDescriptor andThen( final AppFlowDescriptor flow ) {
        parts.add( flow );
        return this;
    }

    @Override
    public AppFlowDescriptor loop( final FeedbackDescriptor descriptor ) {
        return new LoopedAppFlowDescriptor( this, descriptor );
    }

    @Override
    public Type getInputType() {
        return parts.get( 0 ).getInputType();
    }

    @Override
    public Type getOutputType() {
        return parts.get( parts.size()-1 ).getOutputType();
    }

}
