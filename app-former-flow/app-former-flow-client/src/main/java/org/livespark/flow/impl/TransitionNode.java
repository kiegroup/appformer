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

import java.util.Optional;
import java.util.function.Function;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.Unit;

final class TransitionNode<INPUT, OUTPUT> extends FlowNode<INPUT, OUTPUT> {

    Function<INPUT, AppFlow<Unit, OUTPUT>> transition;

    TransitionNode( final Function<INPUT, AppFlow<Unit, OUTPUT>> transition ) {
        this( transition, Optional.empty(), Optional.empty() );
    }

    TransitionNode( final Function<INPUT, AppFlow<Unit, OUTPUT>> transition,
                    final Optional<FlowNode<?, INPUT>> prev,
                    final Optional<FlowNode<OUTPUT, ? >> next ) {
        super( prev, next );
        this.transition = transition;
    }

    @Override
    FlowNode<INPUT, OUTPUT> copy() {
        return new TransitionNode<>( transition, prev, next );
    }

    @Override
    public String toString() {
        return "TransitionNode(transition=" + transition + ")";
    }

}
