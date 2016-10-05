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


package org.livespark.flow.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.livespark.flow.impl.StepUtil.wrap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.AppFlowExecutor;
import org.livespark.flow.api.AppFlowFactory;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.Unit;
import org.livespark.flow.impl.FlowContext;
import org.livespark.flow.impl.RuntimeAppFlowExecutor;
import org.livespark.flow.impl.RuntimeAppFlowFactory;
import org.livespark.flow.util.Ref;

public class FlowBuildAndExecutionTest {

    private AppFlowFactory factory;
    private AppFlowExecutor executor;
    private List<FlowContext> contexts;

    @Before
    public void setup() {
        contexts = new ArrayList<>();
        factory = new RuntimeAppFlowFactory();
        executor = new RuntimeAppFlowExecutor( flow -> {
            final FlowContext context = new FlowContext( flow );
            contexts.add( context );

            return context;
        } );
    }

    @Test
    public void sequentialSteps() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Step<Integer, Integer> add10 = wrap( "Increment", (final Integer n) -> n + 10);
        final Step<Object, String> stringify = wrap( "Stringify", o -> o.toString() );
        final Step<String, String> reverse = wrap( "Reverse String", (final String s) -> new StringBuilder( s ).reverse().toString() );

        final AppFlow<Unit, String> flow = factory
            .buildFromStep( zero )
            .andThen( add10 )
            .andThen( stringify )
            .andThen( reverse );

        assertEquals( "01", getSyncFlowOutput( flow ) );
    }

    @Test
    public void simpleTransition() throws Exception {
        final Step<Unit, Boolean> t = wrap( "True", () -> true );
        final Step<Unit, Boolean> f = wrap( "False", () -> false );
        final Step<Unit, String> tString = wrap( "True String", () -> "true" );
        final Step<Unit, String> fString = wrap( "False String", () -> "false" );
        final Function<Boolean, AppFlow<Unit, String>> transition = b -> factory.buildFromStep( b ? tString : fString );

        final AppFlow<Unit, String> tFlow = factory
            .buildFromStep( t )
            .transitionTo( transition );

        final AppFlow<Unit, String> fFlow = factory
            .buildFromStep( f )
            .transitionTo( transition );

        assertEquals( "true", getSyncFlowOutput( tFlow ) );
        assertEquals( "false", getSyncFlowOutput( fFlow ) );
    }

    @Test
    public void sequentialStepsWithTransformationsAfter() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Function<Integer, Integer> add10 = n -> n + 10;
        final Function<Object, String> stringify = o -> o.toString();
        final Function<String, String> reverse = s -> new StringBuilder( s ).reverse().toString();

        final AppFlow<Unit, String> flow = factory
            .buildFromStep( zero )
            .andThen( add10 )
            .andThen( stringify )
            .andThen( reverse );

        assertEquals( "01", getSyncFlowOutput( flow ) );
    }

    @Test
    public void executeFlowInStep() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Step<Integer, Integer> add10 = wrap( "Add 10", n -> n + 10 );
        final AppFlow<Integer, Integer> add10Flow = factory.buildFromStep( add10 );
        final Step<Integer, Integer> stepCallingFlow = wrap( "Step Calling Flow", (n, callback) -> executor.execute( n, add10Flow, callback ) );

        final AppFlow<Unit, Integer> flow = factory
            .buildFromStep( zero )
            .andThen( stepCallingFlow );

        assertEquals( Integer.valueOf( 10 ), getSyncFlowOutput( flow ) );
    }

    @Test
    public void callingStepAndThenOnFlowDoesNotModifyOriginalFlow() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Step<Integer, Integer> add10 = wrap( "Add 10", n -> n + 10 );
        final Step<Integer, Unit> throwing = wrap( "Throwing", n -> { throw new RuntimeException(); } );

        final AppFlow<Unit, Integer> original =
                factory.buildFromStep( zero )
                       .andThen( add10 );

        original.andThen( throwing );

        try {
            executor.execute( original );
        } catch ( final RuntimeException e ) {
            fail();
        }
    }

    @Test
    public void callingSupplierWithInputOnFlowDoesNotModifyOriginalFlow() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Step<Integer, Integer> add10 = wrap( "Add 10", n -> n + 10 );
        final Supplier<Unit> throwing = () -> { throw new RuntimeException(); };

        final AppFlow<Unit, Integer> original =
                factory.buildFromStep( zero )
                       .andThen( add10 );

        original.withInput( throwing );

        try {
            executor.execute( original );
        } catch ( final RuntimeException e ) {
            fail();
        }
    }

    @Test
    public void callingFlowAndThenOnFlowDoesNotModifyOriginalFlow() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Step<Integer, Integer> add10 = wrap( "Add 10", n -> n + 10 );
        final AppFlow<Integer, Unit> throwing = factory.buildFromStep( wrap( "Throwing", n -> { throw new RuntimeException(); } ) );

        final AppFlow<Unit, Integer> original =
                factory.buildFromStep( zero )
                       .andThen( add10 );

        original.andThen( throwing );

        try {
            executor.execute( original );
        } catch ( final RuntimeException e ) {
            fail();
        }
    }

    @Test
    public void callingFunctionAndThenOnFlowDoesNotModifyOriginalFlow() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Step<Integer, Integer> add10 = wrap( "Add 10", n -> n + 10 );
        final Function<Integer, Unit> throwing = n -> { throw new RuntimeException(); };

        final AppFlow<Unit, Integer> original =
                factory.buildFromStep( zero )
                       .andThen( add10 );

        original.andThen( throwing );

        try {
            executor.execute( original );
        } catch ( final RuntimeException e ) {
            fail();
        }
    }

    @Test
    public void callingTransitionToOnFlowDoesNotModifyOriginalFlow() throws Exception {
        final Step<Unit, Integer> zero = wrap( "Produce Zero", () -> 0 );
        final Step<Integer, Integer> add10 = wrap( "Add 10", n -> n + 10 );
        final Function<Integer, AppFlow<Unit, Unit>> throwing = n -> { throw new RuntimeException(); };

        final AppFlow<Unit, Integer> original =
                factory.buildFromStep( zero )
                       .andThen( add10 );

        original.andThen( throwing );

        try {
            executor.execute( original );
        } catch ( final RuntimeException e ) {
            fail();
        }
    }

    @Test
    public void flowStartingWithFunction() throws Exception {
        final Step<Integer, Integer> add1 = wrap( "Add 1", n -> n + 1 );
        final Function<Unit, Integer> one = u -> 1;

        final AppFlow<Unit, Integer> flow = factory.buildFromFunction( one ).andThen( add1 );

        assertEquals( Integer.valueOf( 2 ), getSyncFlowOutput( flow ) );
    }

    @Test
    public void flowWithTransitionAndButFirst() throws Exception {
        final Step<Integer, Integer> add1 = wrap( "Add 1", n -> n + 1 );
        final Function<Integer, AppFlow<Unit, Integer>> transition = n -> factory
                                                                            .buildFromStep( wrap( "Add 2", (final Integer x) -> x + 2 ) )
                                                                            .withInput( n );

        final AppFlow<Unit, Integer> flow = factory
            .buildFromStep( add1 )
            .transitionTo( transition )
            .withInput( 1 );

        assertEquals( Integer.valueOf( 4 ), getSyncFlowOutput( flow ) );
    }

    @Test
    public void terminalTransitionInFlowDoesNotCreateAdditionalFlowContext() throws Exception {
        final Ref<Consumer<Unit>> callbackRef = new Ref<>();
        final AppFlow<Unit, Unit> nonTerminatingFlow = factory.buildFromConstant( Unit.INSTANCE )
               .transitionTo( n ->
                   factory
                       .buildFromStep( new Step<Unit, Unit>() {

                        @Override
                        public void execute( final Unit input,
                                             final Consumer<Unit> callback ) {
                            callbackRef.val = callback;
                        }

                        @Override
                        public String getName() {
                            return "Unending";
                        }
                    } ) );

        executor.execute( nonTerminatingFlow );

        assertEquals( 1, contexts.size() );
    }

    private <OUTPUT> OUTPUT getSyncFlowOutput( final AppFlow<Unit, OUTPUT> flow ) {
        return getSyncFlowOutput( Unit.INSTANCE, flow );
    }

    private <INPUT, OUTPUT> OUTPUT getSyncFlowOutput( final INPUT in, final AppFlow<INPUT, OUTPUT> flow ) {
        final Ref<OUTPUT> ref = new Ref<>();
        executor.execute( in, flow, val -> { ref.val = val; } );

        return ref.val;
    }

}
