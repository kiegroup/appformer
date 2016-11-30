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


package org.livespark.flow.impl;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.AppFlowFactory;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.Unit;

@ApplicationScoped
public class RuntimeAppFlowFactory implements AppFlowFactory {

    @Override
    public <INPUT, OUTPUT> AppFlow<INPUT, OUTPUT> buildFromStep( final Step<INPUT, OUTPUT> step ) {
        final FlowNode<INPUT, OUTPUT> node = new StepNode<>( step );
        return new RuntimeAppFlow<>( node, node );
    }

    @Override
    public <INPUT, OUTPUT> AppFlow<INPUT, OUTPUT> buildFromFunction( final Function<INPUT, OUTPUT> transformation ) {
        final FlowNode<INPUT, OUTPUT> node = new TransformationNode<>( transformation );
        return new RuntimeAppFlow<>( node, node );
    }

    @Override
    public <INPUT, OUTPUT> AppFlow<INPUT, OUTPUT> buildFromTransition( final Function<INPUT, AppFlow<Unit, OUTPUT>> transition ) {
        final FlowNode<INPUT, OUTPUT> node = new TransitionNode<>( transition );
        return new RuntimeAppFlow<>( node, node );
    }

}
