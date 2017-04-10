/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.flowset.interpeter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kie.appformer.flow.api.CrudOperation.CREATE;
import static org.kie.appformer.flow.api.CrudOperation.DELETE;
import static org.kie.appformer.flow.api.CrudOperation.UPDATE;
import static org.kie.appformer.flow.api.FormOperation.CANCEL;
import static org.kie.appformer.flow.api.FormOperation.NEXT;
import static org.kie.appformer.flow.api.FormOperation.PREVIOUS;
import static org.kie.appformer.flow.api.FormOperation.SUBMIT;
import static org.kie.appformer.flowset.interpeter.InterpeterTest.Choice.ONE;
import static org.kie.appformer.flowset.interpeter.InterpeterTest.Choice.TWO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.kie.appformer.flow.api.AppFlow;
import org.kie.appformer.flow.api.AppFlowExecutor;
import org.kie.appformer.flow.api.AppFlowFactory;
import org.kie.appformer.flow.api.Command;
import org.kie.appformer.flow.api.CrudOperation;
import org.kie.appformer.flow.api.FormOperation;
import org.kie.appformer.flow.api.Unit;
import org.kie.appformer.flow.impl.RuntimeAppFlowExecutor;
import org.kie.appformer.flow.impl.RuntimeAppFlowFactory;
import org.kie.appformer.flowset.api.definition.DecisionGateway;
import org.kie.appformer.flowset.api.definition.FlowPart;
import org.kie.appformer.flowset.api.definition.FormPart;
import org.kie.appformer.flowset.api.definition.JoinGateway;
import org.kie.appformer.flowset.api.definition.MatcherGateway;
import org.kie.appformer.flowset.api.definition.MultiStep;
import org.kie.appformer.flowset.api.definition.SequenceFlow;
import org.kie.appformer.flowset.api.definition.StartNoneEvent;
import org.kie.appformer.flowset.api.definition.property.form.PropertyExpression;
import org.kie.appformer.flowset.api.definition.property.gateway.MatchedOperation;
import org.kie.appformer.flowset.api.definition.property.general.Name;
import org.kie.appformer.flowset.interpeter.res.Address;
import org.kie.appformer.flowset.interpeter.res.User;
import org.kie.appformer.flowset.interpeter.util.TestDisplayer;
import org.kie.appformer.flowset.interpeter.util.TestFormView;
import org.kie.appformer.flowset.interpeter.util.TestModelOracle;
import org.kie.appformer.flowset.interpeter.util.TestUIComponent;
import org.kie.appformer.flowset.interpreter.Interpreter;
import org.kie.appformer.flowset.interpreter.ModelOracle;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewImpl;
import org.kie.workbench.common.stunner.core.graph.impl.EdgeImpl;
import org.kie.workbench.common.stunner.core.graph.impl.GraphImpl;
import org.kie.workbench.common.stunner.core.graph.impl.NodeImpl;
import org.kie.workbench.common.stunner.core.graph.store.GraphNodeStore;
import org.kie.workbench.common.stunner.core.graph.store.GraphNodeStoreImpl;

public class InterpeterTest {

    private final Random rand = new Random( 1234 );

    private final AppFlowFactory flowFactory = new RuntimeAppFlowFactory();
    private final AppFlowExecutor executor = new RuntimeAppFlowExecutor();

    private Graph<?, Node> graph;
    private GraphNodeStore<Node> nodeStore;

    private Map<String, AppFlow<?, ?>> flowParts;
    private Map<String, TestUIComponent<?>> formSteps;
    private TestDisplayer<TestFormView<?>> displayer;
    TestModelOracle modelOracle;
    private Interpreter<TestFormView<?>> interpreter;

    @Before
    public void setup() {
        nodeStore = new GraphNodeStoreImpl();
        graph = new GraphImpl<>( "graph", nodeStore );
        flowParts = new HashMap<>();
        formSteps = new HashMap<>();
        displayer = new TestDisplayer<>();
        final HashMap<Class<?>, ModelOracle> oraclesByType = new HashMap<>();
        oraclesByType.put( User.class, new User.UserOracle() );
        oraclesByType.put( org.kie.appformer.flowset.interpeter.res.Name.class,
                           new org.kie.appformer.flowset.interpeter.res.Name.NameOracle() );
        oraclesByType.put( Address.class, new Address.AddressOracle() );
        modelOracle = new TestModelOracle( oraclesByType );
        final Set<Class<? extends Enum<?>>> enums = new HashSet<>( Arrays.asList( FormOperation.class,
                                                                                  CrudOperation.class,
                                                                                  Choice.class ) );
        interpreter = new Interpreter<>( flowParts,
                                         name -> Optional.ofNullable( formSteps.get( name ) ),
                                         displayer,
                                         modelOracle,
                                         enums,
                                         flowFactory );
    }

    @Test
    public void graphWithNoGateways() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> one -> double -> toString
         */
        flowParts.put( "one", flowFactory.buildFromConstant( 1 ) );
        flowParts.put( "double", flowFactory.buildFromFunction( (final Integer x) -> 2*x ) );
        flowParts.put( "toString", flowFactory.buildFromFunction( Object::toString ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<FlowPart>> one = addNode( flowPartWithName( "one" ) );
        final NodeImpl<View<FlowPart>> doubleNode = addNode( flowPartWithName( "double" ) );
        final NodeImpl<View<FlowPart>> toString = addNode( flowPartWithName( "toString" ) );
        sequence( start, one );
        sequence( one, doubleNode );
        sequence( doubleNode, toString );

        // Test
        final AppFlow<Unit, String> flow = (AppFlow<Unit, String>) interpreter.convert( graph );
        assertNotNull( flow );
        final String str = executeSynchronously( flow );
        assertEquals( "2", str );
    }

    @Test
    public void graphWithDecisionAndJoin() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> decision  -> matcher(CREATE) -> one   \
         *                     \-> matcher(UPDATE) -> two    |-> join
         *                     \-> matcher(DELETE) -> three /
         */
        flowParts.put( "one", flowFactory.buildFromFunction( (final String s) -> "one:" + s ) );
        flowParts.put( "two", flowFactory.buildFromFunction( (final String s) -> "two:" + s ) );
        flowParts.put( "three", flowFactory.buildFromFunction( (final String s) -> "three:" + s ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<DecisionGateway>> decision = addNode( decision() );
        final NodeImpl<View<MatcherGateway>> createMatcher = addNode( matcherWithOperation( CREATE ) );
        final NodeImpl<View<MatcherGateway>> updateMatcher = addNode( matcherWithOperation( UPDATE ) );
        final NodeImpl<View<MatcherGateway>> deleteMatcher = addNode( matcherWithOperation( DELETE ) );
        final NodeImpl<View<FlowPart>> one = addNode( flowPartWithName( "one" ) );
        final NodeImpl<View<FlowPart>> two = addNode( flowPartWithName( "two" ) );
        final NodeImpl<View<FlowPart>> three = addNode( flowPartWithName( "three" ) );
        final NodeImpl<View<JoinGateway>> join = addNode( join() );
        sequence( start, decision );
        sequence( decision, createMatcher );
        sequence( decision, updateMatcher );
        sequence( decision, deleteMatcher );
        sequence( createMatcher, one );
        sequence( updateMatcher, two );
        sequence( deleteMatcher, three );
        sequence( one, join );
        sequence( two, join );
        sequence( three, join );

        // Test
        final AppFlow<Command<CrudOperation, String>, Integer> flow = (AppFlow<Command<CrudOperation, String>, Integer>) interpreter.convert( graph );
        assertNotNull( flow );
        assertEquals( "one:a", executeSynchronously( new Command<>( CREATE, "a" ), flow ) );
        assertEquals( "two:b", executeSynchronously( new Command<>( UPDATE, "b" ), flow ) );
        assertEquals( "three:c", executeSynchronously( new Command<>( DELETE, "c" ), flow ) );
    }

    @Test
    public void graphWithDecisionOnOtherEnum() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> decision  -> matcher(ONE) -> one   \
         *                     \-> matcher(TWO) -> two    |-> join
         */
        flowParts.put( "one", flowFactory.buildFromFunction( (final String s) -> "one:" + s ) );
        flowParts.put( "two", flowFactory.buildFromFunction( (final String s) -> "two:" + s ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<DecisionGateway>> decision = addNode( decision() );
        final NodeImpl<View<MatcherGateway>> oneMatcher = addNode( matcherWithOperation( ONE ) );
        final NodeImpl<View<MatcherGateway>> twoMatcher = addNode( matcherWithOperation( TWO ) );
        final NodeImpl<View<FlowPart>> one = addNode( flowPartWithName( "one" ) );
        final NodeImpl<View<FlowPart>> two = addNode( flowPartWithName( "two" ) );
        final NodeImpl<View<JoinGateway>> join = addNode( join() );
        sequence( start, decision );
        sequence( decision, oneMatcher );
        sequence( decision, twoMatcher );
        sequence( oneMatcher, one );
        sequence( twoMatcher, two );
        sequence( one, join );
        sequence( two, join );

        // Test
        final AppFlow<Command<Choice, String>, Integer> flow = (AppFlow<Command<Choice, String>, Integer>) interpreter.convert( graph );
        assertNotNull( flow );
        assertEquals( "one:a", executeSynchronously( new Command<>( ONE, "a" ), flow ) );
        assertEquals( "two:b", executeSynchronously( new Command<>( TWO, "b" ), flow ) );
    }

    @Test
    public void graphWithDecisionAndNoJoin() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> toCommand -> decision  -> matcher(CREATE) -> one
         *                                   \-> matcher(UPDATE) -> two
         *                                   \-> matcher(DELETE) -> three
         */
        flowParts.put( "toCommand", flowFactory.buildFromFunction( (final String s) -> new Command<>( CREATE, s ) ) );
        flowParts.put( "one", flowFactory.buildFromFunction( (final String s) -> "one:" + s ) );
        flowParts.put( "two", flowFactory.buildFromFunction( (final String s) -> "two:" + s ) );
        flowParts.put( "three", flowFactory.buildFromFunction( (final String s) -> "three:" + s ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<FlowPart>> toCommand = addNode( flowPartWithName( "toCommand" ) );
        final NodeImpl<View<DecisionGateway>> decision = addNode( decision() );
        final NodeImpl<View<MatcherGateway>> createMatcher = addNode( matcherWithOperation( CREATE ) );
        final NodeImpl<View<MatcherGateway>> updateMatcher = addNode( matcherWithOperation( UPDATE ) );
        final NodeImpl<View<MatcherGateway>> deleteMatcher = addNode( matcherWithOperation( DELETE ) );
        final NodeImpl<View<FlowPart>> one = addNode( flowPartWithName( "one" ) );
        final NodeImpl<View<FlowPart>> two = addNode( flowPartWithName( "two" ) );
        final NodeImpl<View<FlowPart>> three = addNode( flowPartWithName( "three" ) );
        sequence( start, toCommand );
        sequence( toCommand, decision );
        sequence( decision, createMatcher );
        sequence( decision, updateMatcher );
        sequence( decision, deleteMatcher );
        sequence( createMatcher, one );
        sequence( updateMatcher, two );
        sequence( deleteMatcher, three );

        // Test
        final AppFlow<String, Integer> flow = (AppFlow<String, Integer>) interpreter.convert( graph );
        assertNotNull( flow );
        assertEquals( "one:a", executeSynchronously( "a", flow ) );
        assertEquals( "one:b", executeSynchronously( "b", flow ) );
    }

    @Test
    public void recursiveFlow() throws Exception {
        /*
         * Setup. Graph looks like:
         *   start -> decision  -> matcher(CREATE) -> one -> start
         *                     \-> matcher(UPDATE) -> two -> start
         *                     \-> matcher(DELETE) -> three -> join
         */
        flowParts.put( "one", flowFactory.buildFromFunction( (final String s) -> new Command<>( UPDATE, s ) ) );
        flowParts.put( "two", flowFactory.buildFromFunction( (final String s) -> new Command<>( DELETE, s ) ) );
        flowParts.put( "three", flowFactory.buildFromFunction( Function.identity() ) );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<DecisionGateway>> decision = addNode( decision() );
        final NodeImpl<View<MatcherGateway>> createMatcher = addNode( matcherWithOperation( CREATE ) );
        final NodeImpl<View<MatcherGateway>> updateMatcher = addNode( matcherWithOperation( UPDATE ) );
        final NodeImpl<View<MatcherGateway>> deleteMatcher = addNode( matcherWithOperation( DELETE ) );
        final NodeImpl<View<FlowPart>> one = addNode( flowPartWithName( "one" ) );
        final NodeImpl<View<FlowPart>> two = addNode( flowPartWithName( "two" ) );
        final NodeImpl<View<FlowPart>> three = addNode( flowPartWithName( "three" ) );
        final NodeImpl<View<JoinGateway>> join = addNode( join() );
        sequence( start, decision );
        sequence( decision, createMatcher );
        sequence( decision, updateMatcher );
        sequence( decision, deleteMatcher );
        sequence( createMatcher, one );
        sequence( updateMatcher, two );
        sequence( deleteMatcher, three );
        sequence( one, start );
        sequence( two, start );
        sequence( three, join );

        // Test
        final AppFlow<Command<CrudOperation, String>, Integer> flow = (AppFlow<Command<CrudOperation, String>, Integer>) interpreter.convert( graph );
        assertNotNull( flow );
        assertEquals( "a", executeSynchronously( new Command<>( CREATE, "a" ), flow ) );
        assertEquals( "b", executeSynchronously( new Command<>( UPDATE, "b" ), flow ) );
        assertEquals( "c", executeSynchronously( new Command<>( DELETE, "c" ), flow ) );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void multiStepFormHappyPath() throws Exception {
        /*
         * Setup. Graph looks like:
         *                        -----------------------------------------
         *   start -> newUser  -> | name -> primaryAddress -> mailAddress |
         *                        -----------------------------------------
         */
        flowParts.put( "newUser", flowFactory.buildFromSupplier( User::new ) );
        final TestUIComponent<org.kie.appformer.flowset.interpeter.res.Name> nameUI =
                new TestUIComponent<>( n -> {
                    n.setFirst( "John" );
                    n.setLast( "Doe" );
                    return new Command<>( NEXT, n );
                } );
        final TestUIComponent<Address> primaryUI = new TestUIComponent<>( a -> {
            a.setStreet( "Fake Street" );
            a.setNumber( "123" );
            return new Command<>( NEXT, a );
        } );
        final TestUIComponent<Address> mailUI = new TestUIComponent<>( a -> {
            a.setStreet( "Penny Lane" );
            a.setNumber( "321" );
            return new Command<>( SUBMIT, a );
        } );
        formSteps.put( "name", nameUI );
        formSteps.put( "primaryAddress", primaryUI );
        formSteps.put( "mailAddress", mailUI );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<FlowPart>> newUser = addNode( flowPartWithName( "newUser" ) );
        final NodeImpl<View<MultiStep>> multi = addNode( multiStep() );
        final NodeImpl<View<FormPart>> nameStep = addNode( formPart( "name", "name" ) );
        final NodeImpl<View<FormPart>> primaryStep = addNode( formPart( "primaryAddress", "primary" ) );
        final NodeImpl<View<FormPart>> mailingStep = addNode( formPart( "mailAddress", "mailing" ) );
        sequence( start, newUser );
        sequence( newUser, nameStep );
        sequence( nameStep, primaryStep );
        sequence( primaryStep, mailingStep );
        containment( multi, nameStep );
        containment( multi, primaryStep );
        containment( multi, mailingStep );

        final AppFlow<Unit, Command<FormOperation, User>> flow = (AppFlow<Unit, Command<FormOperation, User>>) interpreter.convert( graph );
        assertNotNull( flow );
        final User expected = new User();
        expected.setName( new org.kie.appformer.flowset.interpeter.res.Name() );
        expected.setPrimary( new Address() );
        expected.setMailing( new Address() );
        expected.getName().setFirst( "John" );
        expected.getName().setLast( "Doe" );
        expected.getPrimary().setNumber( "123" );
        expected.getPrimary().setStreet( "Fake Street" );
        expected.getMailing().setStreet( "Penny Lane" );
        expected.getMailing().setNumber( "321" );
        final Command<FormOperation, User> observed = executeSynchronously( flow );
        assertEquals( SUBMIT, observed.commandType );
        assertEquals( expected.toString(), observed.value.toString() );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void multiStepFormEarlyCancel() throws Exception {
        /*
         * Setup. Graph looks like:
         *                        -----------------------------------------
         *   start -> newUser  -> | name -> primaryAddress -> mailAddress |
         *                        -----------------------------------------
         */
        flowParts.put( "newUser", flowFactory.buildFromSupplier( User::new ) );
        final TestUIComponent<org.kie.appformer.flowset.interpeter.res.Name> nameUI =
                new TestUIComponent<>( n -> {
                    n.setFirst( "John" );
                    n.setLast( "Doe" );
                    return new Command<>( NEXT, n );
                } );
        final TestUIComponent<Address> primaryUI = new TestUIComponent<>( a -> {
            return new Command<>( CANCEL, a );
        } );
        final TestUIComponent<Address> mailUI = new TestUIComponent<>( a -> {
            a.setStreet( "Penny Lane" );
            a.setNumber( "321" );
            return new Command<>( SUBMIT, a );
        } );
        formSteps.put( "name", nameUI );
        formSteps.put( "primaryAddress", primaryUI );
        formSteps.put( "mailAddress", mailUI );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<FlowPart>> newUser = addNode( flowPartWithName( "newUser" ) );
        final NodeImpl<View<MultiStep>> multi = addNode( multiStep() );
        final NodeImpl<View<FormPart>> nameStep = addNode( formPart( "name", "name" ) );
        final NodeImpl<View<FormPart>> primaryStep = addNode( formPart( "primaryAddress", "primary" ) );
        final NodeImpl<View<FormPart>> mailingStep = addNode( formPart( "mailAddress", "mailing" ) );
        sequence( start, newUser );
        sequence( newUser, nameStep );
        sequence( nameStep, primaryStep );
        sequence( primaryStep, mailingStep );
        containment( multi, nameStep );
        containment( multi, primaryStep );
        containment( multi, mailingStep );

        final AppFlow<Unit, Command<FormOperation, User>> flow = (AppFlow<Unit, Command<FormOperation, User>>) interpreter.convert( graph );
        assertNotNull( flow );
        final User expected = new User();
        expected.setName( new org.kie.appformer.flowset.interpeter.res.Name() );
        expected.setPrimary( new Address() );
        expected.setMailing( new Address() );
        expected.getName().setFirst( "John" );
        expected.getName().setLast( "Doe" );
        expected.getPrimary().setNumber( null );
        expected.getPrimary().setStreet( null );
        expected.getMailing().setStreet( null );
        expected.getMailing().setNumber( null );
        final Command<FormOperation, User> observed = executeSynchronously( flow );
        assertEquals( CANCEL, observed.commandType );
        assertEquals( expected.toString(), observed.value.toString() );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void multiStepFormBackAndForth() throws Exception {
        /*
         * Setup. Graph looks like:
         *                        -----------------------------------------
         *   start -> newUser  -> | name -> primaryAddress -> mailAddress |
         *                        -----------------------------------------
         */
        flowParts.put( "newUser", flowFactory.buildFromSupplier( User::new ) );
        final TestUIComponent<org.kie.appformer.flowset.interpeter.res.Name> nameUI =
                new TestUIComponent<>( n -> {
                    n.setFirst( "John" );
                    n.setLast( "Doe" );
                    return new Command<>( NEXT, n );
                },
                n -> {
                    n.setFirst( "Johnathon" );
                    n.setLast( "Doe" );
                    return new Command<>( NEXT, n );
                } );
        final TestUIComponent<Address> primaryUI = new TestUIComponent<>( a -> {
            a.setStreet( "Fake Street" );
            a.setNumber( "123" );
            return new Command<>( PREVIOUS, a );
        }, a -> new Command<>( NEXT, a ) );
        final TestUIComponent<Address> mailUI = new TestUIComponent<>( a -> {
            a.setStreet( "Penny Lane" );
            a.setNumber( "321" );
            return new Command<>( SUBMIT, a );
        } );
        formSteps.put( "name", nameUI );
        formSteps.put( "primaryAddress", primaryUI );
        formSteps.put( "mailAddress", mailUI );
        final NodeImpl<View<StartNoneEvent>> start = addNode( start() );
        final NodeImpl<View<FlowPart>> newUser = addNode( flowPartWithName( "newUser" ) );
        final NodeImpl<View<MultiStep>> multi = addNode( multiStep() );
        final NodeImpl<View<FormPart>> nameStep = addNode( formPart( "name", "name" ) );
        final NodeImpl<View<FormPart>> primaryStep = addNode( formPart( "primaryAddress", "primary" ) );
        final NodeImpl<View<FormPart>> mailingStep = addNode( formPart( "mailAddress", "mailing" ) );
        sequence( start, newUser );
        sequence( newUser, nameStep );
        sequence( nameStep, primaryStep );
        sequence( primaryStep, mailingStep );
        containment( multi, nameStep );
        containment( multi, primaryStep );
        containment( multi, mailingStep );

        final AppFlow<Unit, Command<FormOperation, User>> flow = (AppFlow<Unit, Command<FormOperation, User>>) interpreter.convert( graph );
        assertNotNull( flow );
        final User expected = new User();
        expected.setName( new org.kie.appformer.flowset.interpeter.res.Name() );
        expected.setPrimary( new Address() );
        expected.setMailing( new Address() );
        expected.getName().setFirst( "Johnathon" );
        expected.getName().setLast( "Doe" );
        expected.getPrimary().setNumber( "123" );
        expected.getPrimary().setStreet( "Fake Street" );
        expected.getMailing().setStreet( "Penny Lane" );
        expected.getMailing().setNumber( "321" );
        final Command<FormOperation, User> observed = executeSynchronously( flow );
        assertEquals( SUBMIT, observed.commandType );
        assertEquals( expected.toString(), observed.value.toString() );
    }

    private FormPart formPart( final String name, final String property ) {
        final FormPart part = new FormPart.FormPartBuilder().build();
        part.setName( new Name( name ) );
        part.setProperty( new PropertyExpression( property ) );

        return part;
    }

    private MultiStep multiStep() {
        final MultiStep multiStep = new MultiStep.MultiStepBuilder().build();
        return multiStep;
    }

    private JoinGateway join() {
        return new JoinGateway.JoinGatewayBuilder().build();
    }

    private MatcherGateway matcherWithOperation( final Enum<?> op ) {
        final MatcherGateway matcher = new MatcherGateway.JoinGatewayBuilder().build();
        matcher.getGeneral().getName().setValue( op.getClass().getSimpleName() );
        matcher.setOperation( new MatchedOperation( op.name() ) );
        return matcher;
    }

    private DecisionGateway decision() {
        return new DecisionGateway.DecisionGatewayBuilder().build();
    }

    private StartNoneEvent start() {
        return new StartNoneEvent.StartNoneEventBuilder().build();
    }

    private FlowPart flowPartWithName( final String name ) {
        final FlowPart part = new FlowPart.FlowPartBuilder().build();
        part.setName( new Name( name ) );
        return part;
    }

    private <T> NodeImpl<View<T>> addNode( final T content ) {
        final NodeImpl<View<T>> node = new NodeImpl<>( Long.toString( rand.nextLong() ) );
        final View<T> view = new ViewImpl<>( content, null );
        node.setContent( view );
        graph.addNode( node );
        return node;
    }

    private void sequence( final NodeImpl<?> source, final NodeImpl<?> target ) {
        final EdgeImpl<SequenceFlow> edgeImpl = new EdgeImpl<>( Long.toString( rand.nextLong() ) );
        edgeImpl.setContent( new SequenceFlow() );
        addEdge( source, target, edgeImpl );
    }

    private void addEdge( final NodeImpl<?> source,
                            final NodeImpl<?> target,
                            final EdgeImpl<?> edgeImpl ) {
        edgeImpl.setSourceNode( source );
        edgeImpl.setTargetNode( target );
        source.getOutEdges().add( edgeImpl );
        target.getInEdges().add( edgeImpl );
    }

    private void containment( final NodeImpl<?> parent, final NodeImpl<?> child ) {
        final EdgeImpl<Child> edgeImpl = new EdgeImpl<>( Long.toString( rand.nextLong() ) );
        edgeImpl.setContent( new Child() );
        addEdge( parent, child, edgeImpl );
    }

    private <O> O executeSynchronously( final AppFlow<Unit, O> flow ) {
        return executeSynchronously( Unit.INSTANCE, flow );
    }

    private <I, O> O executeSynchronously( final I input, final AppFlow<I, O> flow ) {
        class Ref {
            O t;
        }
        final Ref ref = new Ref();
        executor.execute( input, flow, t -> ref.t = t );
        if (ref.t != null) {
            return ref.t;
        }
        else {
            throw new AssertionError( "Given flow did not execute synchronously." );
        }
    }

    public static enum Choice {
        ONE, TWO
    }

}
