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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.livespark.flow.api.UIComponent;
import org.livespark.flow.cdi.api.FlowInput;
import org.livespark.flow.cdi.api.FlowOutput;
import org.livespark.flow.client.local.CDIFlowComponentFactory;
import org.livespark.flow.client.local.CDIFlowComponentFactory.BaseFlowIO;
import org.livespark.flow.util.Ref;

public class CDIFlowComponentFactoryTest {

    private CDIFlowComponentFactory factory;

    @Before
    public void setup() {
        factory = new CDIFlowComponentFactory();
    }

    @Test
    public void sequentialStartCallsInputsAndOutputs() throws Exception {
        final List<Integer> inputs = new ArrayList<>();
        final List<Integer> outputs = new ArrayList<>();

        final UIComponent<Integer, Integer, Object> component =
                factory.createUIComponent( () -> new Object(),
                                       o -> {
                                           final FlowInput<Integer> flowInput = factory.createInput();
                                           final FlowOutput<Integer> flowOutput = factory.createOutput();
                                           setKey( o, flowInput );
                                           setKey( o, flowOutput );
                                           inputs.add( flowInput.get() );
                                           final int output = flowInput.get() + 1;
                                           outputs.add( output );
                                           flowOutput.submit( output );
                                       },
                                       o -> {}, "Adder" );

        component.start( 0, n1 -> {
            component.start( n1, n2 -> {
                component.start( n2, n3 -> {} );
            } );
        } );

        assertEquals( asList( 0, 1, 2 ), inputs );
        assertEquals( asList( 1, 2, 3 ), outputs );
    }

    @Test
    public void nestedStartCallsInputsAndOutputs() throws Exception {
        final List<Integer> inputs = new ArrayList<>();
        final List<Integer> outputs = new ArrayList<>();

        final Function<Function<Integer, Integer>, UIComponent<Integer, Integer, Object>> componentFunc =
                consumer ->
            factory.createUIComponent( () -> new Object(),
                                   o -> {
                                       final FlowInput<Integer> flowInput = factory.createInput();
                                       final FlowOutput<Integer> flowOutput = factory.createOutput();
                                       setKey( o, flowInput );
                                       setKey( o, flowOutput );
                                       final Integer beforeInput = flowInput.get();
                                       inputs.add( beforeInput );
                                       final int intermediateOutput = beforeInput + 1;
                                       final Integer finalOutput = consumer.apply( intermediateOutput );
                                       assertEquals( beforeInput, flowInput.get() );
                                       outputs.add( finalOutput );
                                       flowOutput.submit( finalOutput );
                                   },
                                   o -> {}, "Adder" );

        componentFunc.apply( n1 -> {
            final Ref<Integer> ref1 = new Ref<>();
            componentFunc.apply( n2 -> {
                final Ref<Integer> ref2 = new Ref<>();
                componentFunc.apply( n3 -> n3 + 1 ).start( n2, val -> { ref2.val = val; } );
                return ref2.val;
            } ).start( n1, val -> { ref1.val = val; } );
            return ref1.val;
        } ).start( 0, val -> {} );

        assertEquals( asList( 0, 1, 2 ), inputs );
        assertEquals( asList( 4, 4, 4 ), outputs );
    }

    private void setKey( final Object key, final FlowInput<?> flowInput ) {
        setKey( key, (BaseFlowIO) flowInput );
    }

    private void setKey( final Object key, final FlowOutput<?> flowOutput ) {
        setKey( key, (BaseFlowIO) flowOutput );
    }

    private void setKey( final Object key, final BaseFlowIO flowIO ) {
        flowIO.setKey( key );
    }

}
