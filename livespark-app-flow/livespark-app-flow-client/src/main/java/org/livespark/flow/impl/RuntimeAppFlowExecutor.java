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

import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.AppFlowExecutor;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.Unit;

@ApplicationScoped
public class RuntimeAppFlowExecutor implements AppFlowExecutor {

    private final Function<RuntimeAppFlow<?, ?>, FlowContext> contextSupplier;

    public RuntimeAppFlowExecutor() {
        this( flow -> new FlowContext( flow ) );
    }

    public RuntimeAppFlowExecutor( final Function<RuntimeAppFlow<?, ?>, FlowContext> contextSupplier ) {
        this.contextSupplier = contextSupplier;
    }

    @Override
    public <INPUT, OUTPUT> void execute( final INPUT input, final AppFlow<INPUT, OUTPUT> flow, final Consumer<? super OUTPUT> callback ) {
        executeRuntimeFlow( input, assertRuntimeFlow( flow ), callback );
    }

    private <INPUT, OUTPUT> void executeRuntimeFlow( final INPUT input,
                                                     final RuntimeAppFlow<INPUT, OUTPUT> flow,
                                                     final Consumer<? super OUTPUT> callback ) {
        final FlowContext context = contextSupplier.apply( flow );
        context.start( input );
        context.pushCallback( callback );
        continueFlow( context );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void continueFlow( final FlowContext context ) {
        while ( !context.isFinished() ) {
            final FlowNode<?, ?> curNode = getCurrentNode( context );
            if ( curNode instanceof TransformationNode ) {
                final TransformationNode<?, ?> node = (TransformationNode< ? , ? >) curNode;
                final Object newInput = pollOutput( context );
                final Object newOutput = applyTransformation( node.transformation, newInput );
                context.pushOutput( newOutput );
            } else if ( curNode instanceof StepNode ) {
                final StepNode<?, ?> node = (StepNode<?, ?>) curNode;
                final Object newInput = pollOutput( context );
                executeStep( newInput, node.step, context );
                return;
            } else if ( curNode instanceof TransitionNode ) {
                final TransitionNode<?, ?> node = (TransitionNode<?, ?>) curNode;
                final Object newInput = pollOutput( context );
                executeTransition( newInput, (Function) node.transition, context );
                return;
            } else {
                throw new RuntimeException( "Unrecognized " + FlowNode.class.getSimpleName() + " subtype: " + curNode.getClass().getName() );
            }
        }
        if ( context.isFinished() ) {
            final Object output = pollOutput( context );
            while ( context.hasCallbacks() ) {
                context.applyCallbackAndPop( output );
            }
        }
    }

    private <INPUT, OUTPUT> void executeTransition( final INPUT newInput,
                                                    final Function<Object, AppFlow<Unit, OUTPUT>> transition,
                                                    final FlowContext context ) {
        try {
            final RuntimeAppFlow<Unit, ?> newFlow = assertRuntimeFlow( transition.apply( newInput ) );
            if ( context.isOnTerminalNode() ) {
                context.flattenTailFlow( newFlow );
                continueFlow( context );
            } else {
                execute( Unit.INSTANCE, newFlow, output -> {
                    context.pushOutput( output );
                    continueFlow( context );
                } );
            }
        } catch ( final Throwable t ) {
            throw new RuntimeException( "An error occurred while executing a transition process.", t );
        }
    }

    @SuppressWarnings( "unchecked" )
    private void executeStep( final Object newInput, @SuppressWarnings( "rawtypes" ) final Step step, final FlowContext context ) {
        try {
            step.execute( newInput, output -> {
                context.pushOutput( output );
                continueFlow( context );
            } );
        } catch ( final Throwable t ) {
            throw new RuntimeException( "An error occurred while executing the " + (step == null ? "null" : step.getName()) + " step.", t);
        }
    }

    private static FlowNode<?, ?> getCurrentNode( final FlowContext context ) {
        final FlowNode<?, ?> curNode = context
                .getCurrentNode()
                .orElseThrow( () -> new IllegalStateException( "There was no current node even though the process has not finished." ) );
        return curNode;
    }

    private static Object pollOutput( final FlowContext context ) {
        final Object newInput = context.pollOutput()
                .orElseThrow( () -> new IllegalStateException( "The " + FlowContext.class.getSimpleName() + " was polled with no previous output." ) );
        return newInput;
    }

    @SuppressWarnings( "unchecked" )
    private static Object applyTransformation( @SuppressWarnings( "rawtypes" ) final Function transformation,
                                        final Object newInput) {
        try {
            return transformation.apply( newInput );
        } catch ( final ClassCastException e ) {
            throw new RuntimeException( "Failed to apply a transformation.", e );
        }
    }

    private <INPUT, OUTPUT> RuntimeAppFlow<INPUT, OUTPUT> assertRuntimeFlow( final AppFlow<INPUT, OUTPUT> flow ) {
        if ( flow instanceof RuntimeAppFlow ) {
            return (RuntimeAppFlow<INPUT, OUTPUT>) flow;
        } else {
            throw new RuntimeException( "This " + AppFlowExecutor.class.getSimpleName() + " can only execute a " + RuntimeAppFlow.class.getSimpleName() );
        }
    }
}
