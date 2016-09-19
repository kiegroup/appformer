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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.AppFlowExecutor;
import org.livespark.flow.api.AppFlowFactory;
import org.livespark.flow.api.Command;
import org.livespark.flow.api.Displayer;
import org.livespark.flow.api.UIComponent;
import org.livespark.flow.api.Unit;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.AppFlowReferenceDescriptor;
import org.livespark.flow.api.descriptor.DescriptorFactory;
import org.livespark.flow.api.descriptor.StepDescriptor;
import org.livespark.flow.api.descriptor.conversion.Converter;
import org.livespark.flow.api.descriptor.conversion.DescriptorRegistry;
import org.livespark.flow.api.descriptor.display.DisplayerDescriptor;
import org.livespark.flow.api.descriptor.display.UIComponentDescriptor;
import org.livespark.flow.api.descriptor.display.UIStepDescriptor;
import org.livespark.flow.api.descriptor.function.FeedbackDescriptor;
import org.livespark.flow.api.descriptor.function.PredicateDescriptor;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;
import org.livespark.flow.api.descriptor.transition.OptionalTransitionDescriptor;
import org.livespark.flow.api.descriptor.transition.PredicateTransitionDescriptor;
import org.livespark.flow.api.descriptor.transition.TransitionDescriptor;
import org.livespark.flow.api.descriptor.type.Type;
import org.livespark.flow.api.descriptor.type.TypeFactory;
import org.livespark.flow.api.descriptor.type.Type.GenericType;
import org.livespark.flow.api.descriptor.type.Type.ParameterizedType;
import org.livespark.flow.api.descriptor.type.Type.SimpleType;
import org.livespark.flow.api.descriptor.type.Type.TypeVariable;
import org.livespark.flow.impl.RuntimeAppFlowExecutor;
import org.livespark.flow.impl.RuntimeAppFlowFactory;
import org.livespark.flow.impl.StepUtil;
import org.livespark.flow.impl.descriptor.ConverterImpl;
import org.livespark.flow.impl.descriptor.DescriptorFactoryImpl;
import org.livespark.flow.impl.descriptor.DescriptorRegistryImpl;
import org.livespark.flow.impl.descriptor.TypeFactoryImpl;
import org.livespark.flow.util.Ref;

public class FlowDescriptorConverterTest {

    private AppFlowFactory flowFactory;
    private AppFlowExecutor executor;

    private TypeFactory typeFactory;

    private Converter converter;
    private DescriptorFactory descriptorFactory;
    private DescriptorRegistry registry;

    @Before
    public void setup() {
        flowFactory = new RuntimeAppFlowFactory();
        executor = new RuntimeAppFlowExecutor();

        typeFactory = new TypeFactoryImpl();

        descriptorFactory = new DescriptorFactoryImpl();
        registry = new DescriptorRegistryImpl();
        converter = new ConverterImpl( flowFactory );
    }

    @Test
    public void flowWithNoTransitions() throws Exception {
        final TransformationDescriptor doublerDescriptor = descriptorFactory.createTransformationDescriptor( "doubler",
                                                                                                   typeFactory.simpleType( Integer.class ),
                                                                                                   typeFactory.simpleType( Integer.class ) );
        registry.addTransformation( doublerDescriptor, () -> (final Integer n) -> 2*n );
        final StepDescriptor toStringDescriptor = descriptorFactory.createStepDescriptor( "toString",
                                                                                typeFactory.simpleType( Object.class ),
                                                                                typeFactory.simpleType( String.class ) );
        registry.addStep( toStringDescriptor, () -> StepUtil.wrap( "toString", o -> o.toString() ) );
        final AppFlowReferenceDescriptor reverserDescriptor = descriptorFactory.createAppFlowDescriptor( "reverser",
                                                                                            typeFactory.simpleType( String.class ),
                                                                                            typeFactory.simpleType( String.class ) );
        registry.addFlow( reverserDescriptor, () -> flowFactory.buildFromFunction( StringUtils::reverse ) );

        final AppFlowDescriptor flowDescriptor = descriptorFactory
            .createAppFlowDescriptor( doublerDescriptor )
            .andThen( toStringDescriptor )
            .andThen( reverserDescriptor );

        try {
            @SuppressWarnings( "unchecked" )
            final AppFlow<Integer, String> flow = (AppFlow<Integer, String>) converter.convert( registry, flowDescriptor );
            final Ref<String> retVal = new Ref<>();
            executor.execute( 12, flow, val -> retVal.val = val );
            assertNotNull( retVal.val );
            assertEquals( "42", retVal.val );
        } catch ( final AssertionError ae ) {
            throw ae;
        } catch ( final Throwable t ) {
            throw new AssertionError( t );
        }
    }

    @Test
    public void flowWithCommandTransition() throws Exception {
        final TypeVariable enumTypeVar = typeFactory.typeVariable( "E", new Type[0], new Type[0] );
        final TransformationDescriptor toCommandDescriptor =
                descriptorFactory.createTransformationDescriptor( "toCommand",
                                                         typeFactory.simpleType( Integer.class ),
                                                         typeFactory.parameterizedType( typeFactory.simpleType( Command.class ),
                                                                                    new TypeVariable[]{ enumTypeVar,
                                                                                                        typeFactory.typeVariable( "T" ) },
                                                                                    new Type[]{ typeFactory.simpleType( Parity.class ),
                                                                                                typeFactory.simpleType( Integer.class ) } ) );

        registry.addTransformation( toCommandDescriptor, () -> (final Integer n) -> new Command<>( n % 2 == 0 ? Parity.EVEN : Parity.ODD, n ) );

        final TransformationDescriptor doublerDescriptor = descriptorFactory.createTransformationDescriptor( "doubler",
                                                                                                   typeFactory.simpleType( Integer.class ),
                                                                                                   typeFactory.simpleType( Integer.class ) );
        registry.addTransformation( doublerDescriptor, () -> (final Integer n) -> 2*n );
        final TransformationDescriptor identityTranformationDescriptor = descriptorFactory.createTransformationDescriptor( "identity",
                                                                                                                 typeFactory.simpleType( String.class ),
                                                                                                                 typeFactory.simpleType( String.class ) );
        registry.addTransformation( identityTranformationDescriptor, () -> (final Integer n) -> n );
        final AppFlowDescriptor doublingFlowDescriptor = descriptorFactory.createAppFlowDescriptor( doublerDescriptor );
        final AppFlowDescriptor identityFlowDescriptor = descriptorFactory.createAppFlowDescriptor( identityTranformationDescriptor );

        final Map<Parity, AppFlowDescriptor> transitionMap = new HashMap<>();
        transitionMap.put( Parity.EVEN, identityFlowDescriptor );
        transitionMap.put( Parity.ODD, doublingFlowDescriptor );
        final TransitionDescriptor transitionDescriptor = descriptorFactory.createCommandTransitionDescriptor( transitionMap, typeFactory.simpleType( Command.class ) );

        final StepDescriptor toStringDescriptor = descriptorFactory.createStepDescriptor( "toString",
                                                                                typeFactory.simpleType( Object.class ),
                                                                                typeFactory.simpleType( String.class ) );
        registry.addStep( toStringDescriptor, () -> StepUtil.wrap( "toString", o -> o.toString() ) );

        final AppFlowDescriptor fullFlowDescriptor = descriptorFactory
            .createAppFlowDescriptor( toCommandDescriptor )
            .transitionTo( transitionDescriptor )
            .andThen( toStringDescriptor );

        try {
            @SuppressWarnings( "unchecked" )
            final AppFlow<Integer, String> flow = (AppFlow<Integer, String>) converter.convert( registry, fullFlowDescriptor );
            final Ref<String> retVal = new Ref<>();
            executor.execute( 1, flow, val -> retVal.val = val );
            assertNotNull( retVal.val );
            assertEquals( "2", retVal.val );

            retVal.val = null;
            executor.execute( 4, flow, val -> retVal.val = val );
            assertNotNull( retVal.val );
            assertEquals( "4", retVal.val );
        } catch ( final AssertionError ae ) {
            throw ae;
        } catch ( final Throwable t ) {
            throw new AssertionError( t );
        }
    }

    @Test
    public void flowWithPredicateTransition() throws Exception {
        final PredicateDescriptor predicateDescriptor = descriptorFactory.createPredicateDescriptor( "ifEven", typeFactory.simpleType( Integer.class ) );
        registry.addPredicate( predicateDescriptor, () -> (final Integer n) -> n % 2 == 0 );
        final TransformationDescriptor doublerDescriptor = descriptorFactory.createTransformationDescriptor( "doubler",
                                                                                                   typeFactory.simpleType( Integer.class ),
                                                                                                   typeFactory.simpleType( Integer.class ) );
        registry.addTransformation( doublerDescriptor, () -> (final Integer n) -> 2*n );
        final TransformationDescriptor identityTranformationDescriptor = descriptorFactory.createTransformationDescriptor( "identity",
                                                                                                                 typeFactory.simpleType( String.class ),
                                                                                                                 typeFactory.simpleType( String.class ) );
        registry.addTransformation( identityTranformationDescriptor, () -> (final Integer n) -> n );
        final AppFlowDescriptor doublingFlowDescriptor = descriptorFactory.createAppFlowDescriptor( doublerDescriptor );
        final AppFlowDescriptor identityFlowDescriptor = descriptorFactory.createAppFlowDescriptor( identityTranformationDescriptor );

        final PredicateTransitionDescriptor transitionDescriptor = descriptorFactory.createPredicateTransitionDescriptor( predicateDescriptor,
                                                                                                                    identityFlowDescriptor,
                                                                                                                    doublingFlowDescriptor );
        final AppFlowDescriptor flowDescriptor = descriptorFactory.createAppFlowDescriptor( transitionDescriptor );

        try {
            @SuppressWarnings( "unchecked" )
            final AppFlow<Integer, Integer> flow = (AppFlow<Integer, Integer>) converter.convert( registry, flowDescriptor );
            final Ref<Integer> retVal = new Ref<>();
            executor.execute( 1, flow, val -> retVal.val = val );
            assertNotNull( retVal.val );
            assertEquals( Integer.valueOf( 2 ), retVal.val );

            retVal.val = null;
            executor.execute( 4, flow, val -> retVal.val = val );
            assertNotNull( retVal.val );
            assertEquals( Integer.valueOf( 4 ), retVal.val );
        } catch ( final AssertionError ae ) {
            throw ae;
        } catch ( final Throwable t ) {
            throw new AssertionError( t );
        }
    }

    @Test
    public void flowWithOptionalTransition() throws Exception {
        final GenericType genericOptional = typeFactory.genericType( Optional.class, "T" );
        final SimpleType integer = typeFactory.simpleType( Integer.class );
        final ParameterizedType optionalOfInteger = typeFactory.parameterizedType( genericOptional, integer );

        final TransformationDescriptor evenFilter = descriptorFactory.createTransformationDescriptor( "filterEven", integer, optionalOfInteger );
        registry.addTransformation( evenFilter, () -> (final Integer n) -> Optional.of( n ).filter( val -> val % 2 != 0 ) );
        final TransformationDescriptor optionalOf = descriptorFactory.createTransformationDescriptor( "optionalOf", integer, optionalOfInteger );
        registry.addTransformation( optionalOf, () -> (final Integer n) -> Optional.of( n ) );
        final TransformationDescriptor doublerDescriptor = descriptorFactory.createTransformationDescriptor( "doubler", integer, integer );
        registry.addTransformation( doublerDescriptor, () -> (final Integer n) -> 2*n );

        final AppFlowDescriptor doublingFlowDescriptor = descriptorFactory.createAppFlowDescriptor( doublerDescriptor ).andThen( optionalOf );
        final AppFlowReferenceDescriptor unitToEmpty = descriptorFactory.createAppFlowDescriptor( "unitToEmpty", typeFactory.simpleType( Unit.class ), optionalOfInteger );
        registry.addFlow( unitToEmpty, () -> flowFactory.buildFromConstant( Optional.<Integer>empty() ) );

        final OptionalTransitionDescriptor optionalTransition = descriptorFactory.createOptionalTransitionDescriptor( doublingFlowDescriptor, unitToEmpty );

        final AppFlowDescriptor flowDescriptor = descriptorFactory
                .createAppFlowDescriptor( evenFilter )
                .transitionTo( optionalTransition );

        try {
            @SuppressWarnings( "unchecked" )
            final AppFlow<Integer, Optional<Integer>> flow = (AppFlow<Integer, Optional<Integer>>) converter.convert( registry, flowDescriptor );
            final Ref<Optional<Integer>> retVal = new Ref<>();
            executor.execute( 1, flow, val -> retVal.val = val );
            assertNotNull( retVal.val );
            assertEquals( Optional.of( 2 ), retVal.val );

            retVal.val = null;
            executor.execute( 4, flow, val -> retVal.val = val );
            assertNotNull( retVal.val );
            assertEquals( Optional.empty(), retVal.val );
        } catch ( final AssertionError ae ) {
            throw ae;
        } catch ( final Throwable t ) {
            throw new AssertionError( t );
        }
    }

    @Test
    public void flowWithLoop() throws Exception {
        final SimpleType integer = typeFactory.simpleType( Integer.class );

        final TransformationDescriptor addOneDescriptor = descriptorFactory.createTransformationDescriptor( "addOne", integer, integer );
        registry.addTransformation( addOneDescriptor, () -> (final Integer n) -> n+1 );
        final FeedbackDescriptor lessThan10Desciptor = descriptorFactory.createFeedbackDescriptor( "lessThan10", integer, integer );
        registry.addFeedback( lessThan10Desciptor, () -> (final Integer input, final Integer output) -> Optional.of( output ).filter( n -> n < 10 ) );

        final AppFlowDescriptor loopFlow = descriptorFactory.createAppFlowDescriptor( addOneDescriptor ).loop( lessThan10Desciptor );

        try {
            @SuppressWarnings( "unchecked" )
            final AppFlow<Integer, Integer> flow = (AppFlow<Integer, Integer>) converter.convert( registry, loopFlow );
            final Ref<Integer> retVal = new Ref<>();
            executor.execute( 0, flow, val -> retVal.val = val );
            assertNotNull( retVal.val );
            assertEquals( Integer.valueOf( 10 ), retVal.val );
        } catch ( final AssertionError ae ) {
            throw ae;
        } catch ( final Throwable t ) {
            throw new AssertionError( t );
        }
    }

    @Test
    public void flowWithUIComponentAndCombinedShowAndHide() throws Exception {
        final SimpleType string = typeFactory.simpleType( String.class );
        final SimpleType object = typeFactory.simpleType( Object.class );
        final UIComponentDescriptor componentDescriptor = descriptorFactory.createUIComponentDescriptor( "StringEditor", string, string, object );
        final Object viewObj = new Object();
        final UIComponent<String, String, Object> component = new UIComponent<String, String, Object>() {

            @Override
            public void start( final String input,
                               final Consumer<String> callback ) {
                final String reversed = StringUtils.reverse( input );
                callback.accept( reversed );
            }

            @Override
            public void onHide() {
            }

            @Override
            public Object asComponent() {
                return viewObj;
            }

            @Override
            public String getName() {
                return "StringEditor";
            }
        };
        final List<CapturedAction> capturedActions = new ArrayList<>();
        final DisplayerDescriptor displayerDescriptor = descriptorFactory.createDisplayerDescriptor( "TestDisplayer", object );
        registry.addDisplayer( displayerDescriptor, () -> new Displayer<Object>() {

            @Override
            public void show( final UIComponent<?, ?, Object> uiComponent ) {
                capturedActions.add( new CapturedAction( UIStepDescriptor.Action.SHOW, component ) );
            }

            @Override
            public void hide( final UIComponent<?, ?, Object> uiComponent ) {
                capturedActions.add( new CapturedAction( UIStepDescriptor.Action.HIDE, component ) );
            }
        } );

        registry.addUIComponent( componentDescriptor, () -> component );
        final UIStepDescriptor uiStepDescriptor = descriptorFactory.createUIStepDescriptor( displayerDescriptor,
                                                                                            UIStepDescriptor.Action.SHOW_AND_HIDE,
                                                                                            componentDescriptor );
        final AppFlowDescriptor uiFlow = descriptorFactory.createAppFlowDescriptor( uiStepDescriptor );

        try {
            @SuppressWarnings( "unchecked" )
            final AppFlow<String, String> flow = (AppFlow<String, String>) converter.convert( registry, uiFlow );
            final Ref<String> retVal = new Ref<>();
            executor.execute( "foo", flow, val -> retVal.val = val );
            assertNotNull( retVal.val );
            assertEquals( "oof", retVal.val );
            assertEquals( Arrays.asList( new CapturedAction( UIStepDescriptor.Action.SHOW,
                                                             component ),
                                         new CapturedAction( UIStepDescriptor.Action.HIDE,
                                                             component ) ),
                          capturedActions );
        } catch ( final AssertionError ae ) {
            throw ae;
        } catch ( final Throwable t ) {
            throw new AssertionError( t );
        }
    }

    @Test
    public void flowWithUIComponentAndSeparateShowAndHide() throws Exception {
        final SimpleType string = typeFactory.simpleType( String.class );
        final SimpleType object = typeFactory.simpleType( Object.class );
        final UIComponentDescriptor componentDescriptor = descriptorFactory.createUIComponentDescriptor( "StringEditor", string, string, object );
        final Object viewObj = new Object();
        final UIComponent<String, String, Object> component = new UIComponent<String, String, Object>() {

            @Override
            public void start( final String input,
                               final Consumer<String> callback ) {
                final String reversed = StringUtils.reverse( input );
                callback.accept( reversed );
            }

            @Override
            public void onHide() {
            }

            @Override
            public Object asComponent() {
                return viewObj;
            }

            @Override
            public String getName() {
                return "StringEditor";
            }
        };
        final List<CapturedAction> capturedActions = new ArrayList<>();
        final DisplayerDescriptor displayerDescriptor = descriptorFactory.createDisplayerDescriptor( "TestDisplayer", object );
        registry.addDisplayer( displayerDescriptor, () -> new Displayer<Object>() {

            @Override
            public void show( final UIComponent<?, ?, Object> uiComponent ) {
                capturedActions.add( new CapturedAction( UIStepDescriptor.Action.SHOW, component ) );
            }

            @Override
            public void hide( final UIComponent<?, ?, Object> uiComponent ) {
                capturedActions.add( new CapturedAction( UIStepDescriptor.Action.HIDE, component ) );
            }
        } );

        registry.addUIComponent( componentDescriptor, () -> component );
        final UIStepDescriptor showStepDescriptor = descriptorFactory.createUIStepDescriptor( displayerDescriptor,
                                                                                              UIStepDescriptor.Action.SHOW,
                                                                                              componentDescriptor );
        final UIStepDescriptor hideStepDescriptor = descriptorFactory.createUIStepDescriptor( displayerDescriptor,
                                                                                              UIStepDescriptor.Action.HIDE,
                                                                                              componentDescriptor );
        final TransformationDescriptor doubleStringDescriptor = descriptorFactory.createTransformationDescriptor( "DoubleString", string, string );
        registry.addTransformation( doubleStringDescriptor, () -> (final String s) -> s + s );

        final AppFlowDescriptor uiFlow =
                descriptorFactory
                    .createAppFlowDescriptor( showStepDescriptor )
                    .andThen( doubleStringDescriptor )
                    .andThen( hideStepDescriptor );

        try {
            @SuppressWarnings( "unchecked" )
            final AppFlow<String, String> flow = (AppFlow<String, String>) converter.convert( registry, uiFlow );
            final Ref<String> retVal = new Ref<>();
            executor.execute( "foo", flow, val -> retVal.val = val );
            assertNotNull( retVal.val );
            assertEquals( "oofoof", retVal.val );
            assertEquals( Arrays.asList( new CapturedAction( UIStepDescriptor.Action.SHOW,
                                                             component ),
                                         new CapturedAction( UIStepDescriptor.Action.HIDE,
                                                             component ) ),
                          capturedActions );
        } catch ( final AssertionError ae ) {
            throw ae;
        } catch ( final Throwable t ) {
            throw new AssertionError( t );
        }
    }

    public static enum Parity {
        ODD, EVEN;
    }

    static class CapturedAction {
        private final UIStepDescriptor.Action action;
        private final UIComponent<?, ?, ?> component;

        CapturedAction( final UIStepDescriptor.Action action, final UIComponent<?, ?, ?> component ) {
            this.action = action;
            this.component = component;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof CapturedAction && ((CapturedAction) obj).action.equals(action) && ((CapturedAction) obj).component == component;
        }
    }

}
