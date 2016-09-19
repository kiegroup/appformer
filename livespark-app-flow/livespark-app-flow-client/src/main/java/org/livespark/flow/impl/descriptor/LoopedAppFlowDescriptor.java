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

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.StepDescriptor;
import org.livespark.flow.api.descriptor.function.FeedbackDescriptor;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;
import org.livespark.flow.api.descriptor.transition.TransitionDescriptor;
import org.livespark.flow.api.descriptor.type.Type;

@Portable
public class LoopedAppFlowDescriptor implements AppFlowDescriptor {

    private final AppFlowDescriptor flow;
    private final FeedbackDescriptor feedback;

    public LoopedAppFlowDescriptor( final @MapsTo("flow") AppFlowDescriptor flow, final @MapsTo("feedback") FeedbackDescriptor feedback ) {
        this.flow = flow;
        this.feedback = feedback;
    }

    @Override
    public Type getInputType() {
        return flow.getInputType();
    }

    @Override
    public Type getOutputType() {
        return flow.getOutputType();
    }

    public FeedbackDescriptor getFeedback() {
        return feedback;
    }

    public AppFlowDescriptor getLoopedFlow() {
        return flow;
    }

    @Override
    public AppFlowDescriptor andThen( final StepDescriptor step ) {
        final SequentialAppFlowDescriptor newFlow = new SequentialAppFlowDescriptor( this );
        return newFlow.andThen( step );
    }

    @Override
    public AppFlowDescriptor andThen( final TransformationDescriptor transformation ) {
        final SequentialAppFlowDescriptor newFlow = new SequentialAppFlowDescriptor( this );
        return newFlow.andThen( transformation );
    }

    @Override
    public AppFlowDescriptor transitionTo( final TransitionDescriptor transition ) {
        final SequentialAppFlowDescriptor newFlow = new SequentialAppFlowDescriptor( this );
        return newFlow.transitionTo( transition );
    }

    @Override
    public AppFlowDescriptor andThen( final AppFlowDescriptor flow ) {
        final SequentialAppFlowDescriptor newFlow = new SequentialAppFlowDescriptor( this );
        return newFlow.andThen( flow );
    }

    @Override
    public AppFlowDescriptor loop( final FeedbackDescriptor descriptor ) {
        final SequentialAppFlowDescriptor newFlow = new SequentialAppFlowDescriptor( this );
        return newFlow.loop( descriptor );
    }

}
