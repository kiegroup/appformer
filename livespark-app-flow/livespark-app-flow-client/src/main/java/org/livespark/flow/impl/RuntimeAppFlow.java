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
import java.util.function.Supplier;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.Unit;

public class RuntimeAppFlow<INPUT, OUTPUT> implements AppFlow<INPUT, OUTPUT> {

    FlowNode<INPUT, ?> start;
    FlowNode<?, OUTPUT> end;

    RuntimeAppFlow( final FlowNode<INPUT, ?> start, final FlowNode<?, OUTPUT> end ) {
        this.start = start;
        this.end = end;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Override
    public <T> AppFlow<INPUT, T> andThen( final Step<? super OUTPUT, T> nextStep ) {
        final RuntimeAppFlow<INPUT, OUTPUT> copy = copy();
        copy.addLast( new StepNode( nextStep ) );

        return (AppFlow<INPUT, T>) copy;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Override
    public <T> AppFlow<INPUT, T> andThen( final Function<? super OUTPUT, T> transformation ) {
        final RuntimeAppFlow<INPUT, OUTPUT> copy = copy();
        copy.addLast( new TransformationNode( transformation ) );

        return (AppFlow<INPUT, T>) copy;
    }

    @Override
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public <T> AppFlow<INPUT, T> transitionTo( final Function<? super OUTPUT, AppFlow<Unit, T>> transition ) {
        final RuntimeAppFlow<INPUT, OUTPUT> copy = copy();
        copy.addLast( new TransitionNode( transition ) );

        return (AppFlow<INPUT, T>) copy;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public AppFlow<Unit, OUTPUT> withInput( final Supplier<INPUT> input ) {
        final RuntimeAppFlow<INPUT, OUTPUT> copy = copy();
        copy.addFirst( new TransformationNode<>( ( final Unit u ) -> input.get() ) );

        return (AppFlow<Unit, OUTPUT>) copy;
    }

    @SuppressWarnings( "unchecked" )
    private void addLast( final FlowNode<OUTPUT, ?> node ) {
        node.prev = Optional.of( end );
        end.next = Optional.of( node );
        end = (FlowNode< ? , OUTPUT>) node;
    }

    @SuppressWarnings( "unchecked" )
    private void addFirst( final FlowNode<?, INPUT> node ) {
        node.next = Optional.of( start );
        start.prev = Optional.of( node );
        start = (FlowNode<INPUT, ? >) node;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private RuntimeAppFlow<INPUT, OUTPUT> copy() {
        final FlowNode newStart = start.copy();
        FlowNode curCopy = newStart;
        FlowNode curOriginal = start;
        while ( curOriginal.next.isPresent() ) {
            final FlowNode nextOriginal = (FlowNode) curOriginal.next.get();
            final FlowNode nextCopy = nextOriginal.copy();
            curCopy.next = Optional.of( nextCopy );
            nextCopy.prev = Optional.of( curCopy );

            curCopy = nextCopy;
            curOriginal = nextOriginal;
        }

        return new RuntimeAppFlow<>( newStart, curCopy );
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( "[\n\t    " );

        FlowNode<?, ?> cur = start;
        while ( true ) {
            sb.append( cur.toString() );
            cur = cur.next.orElse( null );
            if ( cur != null ) {
                sb.append( "\n\t -> " );
            } else {
                sb.append( "\n]" );
                break;
            }
        }

        return sb.toString();
    }

}
