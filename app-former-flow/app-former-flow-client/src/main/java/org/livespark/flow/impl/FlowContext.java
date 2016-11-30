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

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

import org.livespark.flow.api.Unit;

public class FlowContext {

    private Optional<Object> lastOutput = Optional.empty();
    private RuntimeAppFlow<?, ?> flow;
    private Optional<FlowNode<?, ?>> currentNode = Optional.empty();

    private final Deque<Consumer<?>> callbacks = new LinkedList<>();


    public FlowContext( final RuntimeAppFlow<?, ?> flow ) {
        this.flow = flow;
    }

    Optional<Object> pollOutput() {
        return lastOutput;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    void pushOutput(final Object value) {
        lastOutput = Optional.of( value );
        currentNode = (Optional) currentNode.flatMap( node -> node.next );
    }

    void start( final Object initialInput ) {
        if ( isStarted() ) {
            throw new RuntimeException( "Process has already been started." );
        }

        currentNode = Optional.of( flow.start );
        lastOutput = Optional.of( initialInput );
    }

    boolean isStarted() {
        return currentNode.isPresent() || lastOutput.isPresent();
    }

    boolean isFinished() {
        return !currentNode.isPresent() && lastOutput.isPresent() && hasCallbacks();
    }

    boolean isOnTerminalNode() {
        return currentNode.filter( node -> flow.end == node ).isPresent();
    }

    Optional<FlowNode<?, ?>> getCurrentNode() {
        return currentNode;
    }

    RuntimeAppFlow<?, ?> getFlow() {
        return flow;
    }

    void pushCallback( final Consumer<?> callback ) {
        callbacks.push( callback );
    }

    boolean hasCallbacks() {
        return !callbacks.isEmpty();
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    void applyCallbackAndPop( final Object value ) {
        final Consumer callback = callbacks.peek();
        callback.accept( value );
        callbacks.pop();
    }

    void flattenTailFlow( final RuntimeAppFlow<Unit, ?> newFlow ) {
        assert isOnTerminalNode();

        flow = newFlow;
        currentNode = Optional.of( flow.start );
        lastOutput = Optional.of( Unit.INSTANCE );
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append( "[\n\tcurrentNode : " )
                .append( currentNode.map( o -> o.toString() ).orElse( "null" ) )
                .append( "\n\tlastOutput : " )
                .append( lastOutput.map( o -> o.toString() ).orElse( "null" ) )
                .append( "\n\tflow : " )
                .append( flow.toString().replace( "\n", "\n\t" ) )
                .append( "\n]" )
                .toString();
    }
}
