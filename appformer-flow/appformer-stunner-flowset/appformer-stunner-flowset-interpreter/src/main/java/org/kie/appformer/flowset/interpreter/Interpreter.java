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

package org.kie.appformer.flowset.interpreter;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.kie.appformer.flow.api.AppFlow;
import org.kie.appformer.flow.api.AppFlowFactory;
import org.kie.appformer.flow.api.Command;
import org.kie.appformer.flow.api.CrudOperation;
import org.kie.appformer.flow.api.Displayer;
import org.kie.appformer.flow.api.FormOperation;
import org.kie.appformer.flow.api.Sequenced;
import org.kie.appformer.flow.api.Step;
import org.kie.appformer.flow.api.UIComponent;
import org.kie.appformer.flowset.api.definition.DecisionGateway;
import org.kie.appformer.flowset.api.definition.FlowPart;
import org.kie.appformer.flowset.api.definition.FormPart;
import org.kie.appformer.flowset.api.definition.JoinGateway;
import org.kie.appformer.flowset.api.definition.MatcherGateway;
import org.kie.appformer.flowset.api.definition.MultiStep;
import org.kie.appformer.flowset.api.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;

public class Interpreter<V extends Sequenced> {

    private final Map<String, AppFlow<?, ?>> flowParts;
    private final AppFlowFactory             factory;
    private final Map<String, Class<? extends Enum<?>>> enumsBySimpleName;
    private final Function<String, Optional<? extends UIComponent<?, ? extends Command<?, ?>, ? extends V>>> formSteps;
    private final Displayer<V> formDisplayer;
    private final ModelOracle modelOracle;

    public Interpreter( final Map<String, AppFlow<?, ?>> flowParts,
                        final Function<String, Optional<? extends UIComponent<?, ? extends Command<?, ?>, ? extends V>>> formSteps,
                        final Displayer<V> formDisplayer,
                        final ModelOracle modelOracle,
                        final Set<Class<? extends Enum<?>>> supportedEnums,
                        final AppFlowFactory factory ) {
        this.flowParts = flowParts;
        this.formSteps = formSteps;
        this.formDisplayer = formDisplayer;
        this.modelOracle = modelOracle;
        this.factory = factory;
        enumsBySimpleName = new HashMap<>();
        supportedEnums.forEach( eType -> enumsBySimpleName.put( eType.getSimpleName(), eType ) );
    }

    public AppFlow<?, ?> convert( final Graph<?, Node> graph ) {
        final List<Node> startNodes = nodeStream( graph ).filter( node -> isStart( node ) ).collect( Collectors.toList() );
        if ( startNodes.size() == 1 ) {
            final Node<Definition<StartNoneEvent>, Edge> start = startNodes.get( 0 );
            return convert( start );
        } else {
            throw new IllegalStateException( "There must be exactly 1 start node but found " + startNodes.size()
                                             + "." );
        }
    }

    private boolean isStart( final Node node ) {
        return node.getContent() instanceof Definition
               && ((Definition) node.getContent()).getDefinition() instanceof StartNoneEvent;
    }

    private AppFlow<?, ?> convert( final Node<Definition<StartNoneEvent>, Edge> start ) {
        Node<?, Edge> curNode = getNextNodeViaSingleEdge( start ).orElse( null );
        final Deque<AppFlow> flows = new LinkedList<>();
        final Deque<DecisionFrame> decisionStack = new LinkedList<>();
        flows.push( factory.buildFromFunction( Function.identity() ) );
        while ( curNode != null ) {
            final Object content = getContent( curNode );
            if ( content instanceof FlowPart ) {
                curNode = processFlowPart( curNode,
                                           flows,
                                           (FlowPart) content );
            } else if ( content instanceof FormPart ) {
                curNode = processFormPart( curNode, flows, (FormPart) content );
            } else if ( content instanceof DecisionGateway ) {
                curNode = processDecisionGateway( curNode,
                                                  flows,
                                                  decisionStack );
            } else if ( content instanceof MatcherGateway ) {
                // Always processed when DescisionGateway is found
                throw new IllegalStateException( "MatcherGateway must have a single inbound edge from a DecisionGateway." );
            } else if ( content instanceof JoinGateway ) {
                curNode = processTopOfDecisionStack( Optional.of( curNode ), flows, decisionStack ).orElse( null );
            } else if ( content instanceof StartNoneEvent ) {
                // Already validated that there is exactly one start so this is a back-edge
                final AppFlow flow = flows.pop();
                flows.push( flow.andThen( () -> flows.getFirst() ) );
                curNode = null;
            } else {
                throw new IllegalStateException( "Found unexpected node of type ["
                                                 + (content != null ? content.getClass().getSimpleName() : "null")
                                                 + "] while traversing the graph." );
            }

            /*
             * If we're at the end of path in a decision that doesn't explicitly join, need to process decisions still.
             */
            while ( curNode == null && !decisionStack.isEmpty() ) {
                curNode = processTopOfDecisionStack( Optional.empty(), flows, decisionStack ).orElse( null );
            }
        }

        return flows.peek();
    }

    private Node<?, Edge> processFormPart( final Node<?, Edge> firstStep,
                                           final Deque<AppFlow> flows,
                                           final FormPart content ) {
        final List<FormPart> formSequence = getFormStepSequence( firstStep );
        final List<FormStep> steps = getFormSteps( formSequence );
        final List<String> properties = getStepPropertyExpressions( formSequence );

        final AppFlow<Object, Object> multiStepFlow = createMultiStepFlow( steps, properties );

        flows.push( flows.pop().andThen( multiStepFlow ) );

        return getNodeAfterMultiStepForm( firstStep );
    }

    private AppFlow<Object, Object> createMultiStepFlow( final List<FormStep> steps, final List<String> properties ) {
        @SuppressWarnings( { "unchecked", "rawtypes" } )
        final AppFlow<Object, Object> multiStepFlow = factory
                .buildFromTransition( multiModel -> {
                    final Object workingMultiModel = modelOracle.workingCopy( multiModel );
                    final List<Object> stepModels = properties
                      .stream()
                      .map( property -> Optional.ofNullable( modelOracle.getProperty( workingMultiModel, property ) )
                                                .orElseGet( () -> {
                                                    final Object nested = modelOracle.createNestedModel( workingMultiModel, property );
                                                    modelOracle.setProperty( workingMultiModel, property, nested );
                                                    return nested;
                                                } ) )
                      .collect( toList() );
                    final List<AppFlow> formFlows = new ArrayList<>();
                    for ( int i = 0; i < steps.size(); i++ ) {
                        final Object model = stepModels.get( i );
                        final FormStep step = steps.get( i );
                        final int flowIndex = i;
                        formFlows.add( factory
                            .buildFromConstant( model )
                            .andThen( step )
                            .transitionTo( o -> {
                                final Command<FormOperation, Object> c = (Command<FormOperation, Object>) o;
                                return interStepTransition( multiModel,
                                                            workingMultiModel,
                                                            formFlows,
                                                            flowIndex,
                                                            c );
                                } ) );
                    }

                    return formFlows.get( 0 );
                } );
        return multiStepFlow;
    }

    private Object interStepTransition( final Object multiModel,
                                        final Object workingMultiModel,
                                        final List<AppFlow> formFlows,
                                        final int flowIndex,
                                        final Command<FormOperation, Object> c ) {
        switch( c.commandType ) {
            case CANCEL :
                return transitionToCancel( multiModel,
                                           c );
            case PREVIOUS : {
                return transitionToPrevious( formFlows,
                                             flowIndex );
            }
            case NEXT : {
                return transitionToNext( formFlows,
                                         flowIndex );
            }
            case SUBMIT : {
                return transitionToSubmit( multiModel,
                                           workingMultiModel,
                                           formFlows,
                                           flowIndex,
                                           c );
            }
            default :
                throw new IllegalArgumentException( "Unrecognized FormOperation " + c.commandType );

        }
    }

    private Object transitionToCancel( final Object multiModel,
                              final Command<FormOperation, Object> c ) {
        return factory.buildFromConstant( c.map( ignore -> multiModel ) );
    }

    private Object transitionToSubmit( final Object multiModel,
                              final Object workingMultiModel,
                              final List<AppFlow> formFlows,
                              final int flowIndex,
                              final Command<FormOperation, Object> c ) {
        if ( flowIndex == formFlows.size()-1 ) {
            return factory.buildFromConstant( c.map( ignore -> {
                modelOracle.mergeChanges( multiModel, workingMultiModel );
                return multiModel;
            } ) );
        }
        else {
            throw new IllegalStateException( "Can't submit before terminal form step." );
        }
    }

    private Object transitionToNext( final List<AppFlow> formFlows,
                              final int flowIndex ) {
        if ( flowIndex < formFlows.size()-1 ) {
            return formFlows.get( flowIndex+1 );
        }
        else {
            throw new IllegalStateException( "Can't go to next on terminal form step." );
        }
    }

    private Object transitionToPrevious( final List<AppFlow> formFlows,
                              final int flowIndex ) {
        if ( flowIndex > 0 ) {
            return formFlows.get( flowIndex - 1 );
        }
        else {
            throw new IllegalStateException( "Can't go to previous on initial form step." );
        }
    }

    private List<String> getStepPropertyExpressions( final List<FormPart> formSequence ) {
        final List<String> properties = formSequence
        .stream()
        .map( step -> step.getProperty().getValue() )
        .collect( toList() );
        return properties;
    }

    private List<FormStep> getFormSteps( final List<FormPart> formSequence ) {
        final List<FormStep> steps = formSequence
        .stream()
        .map( step -> step.getName().getValue() )
        .map( name -> formSteps
              .apply( name )
              .orElseThrow( () -> new IllegalArgumentException( "Unrecognized component [" + name + "]." ) ) )
        .map( c -> new FormStep( c ) )
        .collect( toList() );

        ((Sequenced) steps.get( 0 ).component.asComponent()).setStart();
        ((Sequenced) steps.get( steps.size()-1 ).component.asComponent()).setEnd();
        return steps;
    }

    private Node<?, Edge> getNodeAfterMultiStepForm( Node<?, Edge> cur ) {
        final Node parent = getParentMultiStep( cur );
        while ( cur != null && getContent( cur ) instanceof FormPart && getParentMultiStep( cur ) == parent ) {
            cur = getNextNodeViaSingleEdge( cur ).orElse( null );
        }

        return cur;
    }

    private List<FormPart> getFormStepSequence( final Node<?, Edge> firstStep ) {
        final List<FormPart> formSequence = new ArrayList<>();
        Node<?, Edge> cur = firstStep;
        final Node multi = getParentMultiStep( cur );
        while ( cur != null ) {
            final Node curParent = getParentMultiStep( cur );
            if ( curParent == multi ) {
                formSequence.add( (FormPart) getContent( cur ) );
                cur = getNextNodeViaSingleEdge( cur )
                        .filter( node -> getContent( node ) instanceof FormPart )
                        .orElse( null );
            }
            else {
                cur = null;
            }
        }
        return formSequence;
    }

    private Node getParentMultiStep( final Node<?, Edge> cur ) {
        // Don't remove these assignments. Can cause type errors with some compilers.
        final Optional<Node> oNode = cur
         .getInEdges()
         .stream()
         .filter( edge -> edge.getContent() instanceof Child )
         .findFirst()
         .map( edge -> edge.getSourceNode() )
         .filter( node -> getContent( node ) instanceof MultiStep );

        final Node retVal = oNode
         .orElseThrow( () -> new IllegalStateException( "Form steps must be contained in a MultiStep." ) );

        return retVal;
    }

    private Object getContent( final Node<?, Edge> curNode ) {
        return ((Definition) curNode.getContent()).getDefinition();
    }

    private Optional<Node<?, Edge>> processTopOfDecisionStack( final Optional<Node<?, Edge>> oJoin,
                                                               final Deque<AppFlow> flows,
                                                               final Deque<DecisionFrame> decisionStack ) {
        final DecisionFrame curFrame = decisionStack.peek();
        curFrame.index += 1;
        if ( curFrame.index < curFrame.node.getOutEdges().size() ) {
            return Optional.ofNullable(prepareToProcessNextDecisionPath( flows, curFrame ));
        } else {
            processDecisionTransition( flows, decisionStack, curFrame );
            return oJoin.flatMap( join -> getNextNodeViaSingleEdge( join ) );
        }
    }

    private void processDecisionTransition( final Deque<AppFlow> flows,
                            final Deque<DecisionFrame> decisionStack,
                            final DecisionFrame curFrame ) {
        decisionStack.pop();
        @SuppressWarnings( "rawtypes" )
        final Map<Enum<?>, AppFlow> mappedFlows = createTransitionFlowMap( flows,
                                                                                 curFrame );
        final AppFlow<Command<CrudOperation, Object>, Object> decisionFlow = factory.buildFromTransition( ( final Command<CrudOperation, Object> command ) -> {
            @SuppressWarnings( "unchecked" )
            final AppFlow<Object, Object> mappedFlow = mappedFlows.computeIfAbsent( command.commandType,
                                                                                    o -> {
                                                                                        throw new IllegalArgumentException( "Received operation "
                                                                                                                            + o
                                                                                                                            + " but no mapping was provided." );
                                                                                    } );
            return factory.buildFromConstant( command.value ).andThen( mappedFlow );
        } );
        final AppFlow curFlow = flows.pop();
        flows.push( curFlow.andThen( decisionFlow ) );
    }

    private Node<?, Edge> prepareToProcessNextDecisionPath( final Deque<AppFlow> flows,
                                                            final DecisionFrame curFrame ) {
        Node<?, Edge> curNode;
        curNode = validateIndexEdgeIsMatcherAndGetNextNode( curFrame );
        flows.push( factory.buildFromFunction( Function.identity() ) );
        return curNode;
    }

    private Map<Enum<?>, AppFlow> createTransitionFlowMap( final Deque<AppFlow> flows,
                                                                 final DecisionFrame curFrame ) {
        final Map<Enum<?>, AppFlow> mappedFlows = new HashMap<>( curFrame.node.getOutEdges().size() );
        for ( int i = curFrame.node.getOutEdges().size() - 1; i > -1; i-- ) {
            final MatcherGateway matcher = (MatcherGateway) getContent( curFrame.node.getOutEdges().get( i ).getTargetNode() );
            final AppFlow flow = flows.pop();
            final Enum<?> op = getEnumValue( matcher );
            if ( mappedFlows.putIfAbsent( op,
                                          flow ) != null ) {
                throw new IllegalStateException( "Multiple matchers following DecisionGateway mapped to same operation "
                                                 + op );
            }
        }
        return mappedFlows;
    }

    private Enum<?> getEnumValue( final MatcherGateway matcher ) {
        final String enumType = matcher.getGeneral().getName().getValue();
        final String op = matcher.getOperation().getValue();

        final Class<? extends Enum<?>> type = enumsBySimpleName.get( enumType );
        if ( type == null ) {
            throw new IllegalArgumentException( "Unrecognized enum type ["
                    + matcher.getGeneral().getName().getValue() + "]." );
        }
        else {
            return Enum.valueOf( (Class) type, op );
        }
    }

    private Node<?, Edge> processDecisionGateway( Node<?, Edge> curNode,
                                                  final Deque<AppFlow> flows,
                                                  final Deque<DecisionFrame> decisionStack ) {
        if ( curNode.getOutEdges().isEmpty() ) {
            throw new IllegalStateException( "Found " + DecisionGateway.class.getSimpleName()
                                             + " with no outbound edges." );
        } else {
            final DecisionFrame frame = new DecisionFrame( (Node<Definition<DecisionGateway>, Edge>) curNode );
            decisionStack.push( frame );
            curNode = prepareToProcessNextDecisionPath( flows,
                                                        frame );
        }
        return curNode;
    }

    private Node<?, Edge> processFlowPart( Node<?, Edge> curNode,
                                           final Deque<AppFlow> flows,
                                           final FlowPart content ) {
        final String name = content.getName().getValue();
        final AppFlow<?, ?> flow = flowParts.get( name );
        if ( flow != null ) {
            final AppFlow cur = flows.pop();
            flows.push( cur.andThen( flow ) );
            curNode = getNextNodeViaSingleEdge( curNode ).orElse( null );
        } else {
            throw new IllegalStateException( "No flow part in context for [" + name + "]." );
        }
        return curNode;
    }

    private Node<?, Edge> validateIndexEdgeIsMatcherAndGetNextNode( final DecisionFrame frame ) {
        Node<?, Edge> curNode;
        final Node<MatcherGateway, Edge> matcherNode = validateIndexedEdgeTargetsMatcher( frame );
        curNode = getNextNodeViaSingleEdge( matcherNode ).orElse( null );
        return curNode;
    }

    private Node<MatcherGateway, Edge> validateIndexedEdgeTargetsMatcher( final DecisionFrame frame ) {
        final List<Edge> outEdges = frame.node.getOutEdges();
        if ( frame.index < outEdges.size() ) {
            final Node target = outEdges.get( frame.index ).getTargetNode();
            if ( getContent( target ) instanceof MatcherGateway ) {
                return target;
            } else {
                throw new IllegalStateException( "DecisionGateway must be immediately followed by MatcherGateway but found "
                                                 + (getContent( target ) != null ? getContent( target ).getClass().getSimpleName() : "null") );
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private Optional<Node<?, Edge>> getNextNodeViaSingleEdge( final Node<?, Edge> node ) {
        if ( node.getOutEdges().isEmpty() ) {
            return Optional.empty();
        }
        if ( node.getOutEdges().size() == 1 ) {
            return Optional.of( node.getOutEdges().get( 0 ).getTargetNode() );
        } else {
            throw new IllegalStateException( "Expected node of type ["
                                             + (getContent( node ) != null ? getContent( node ).getClass().getSimpleName() : "null")
                                             + "] to have 1 outbound edge but had " + node.getOutEdges().size() + "." );
        }
    }

    private Stream<Node> nodeStream( final Graph<?, Node> graph ) {
        return StreamSupport.stream( graph.nodes().spliterator(),
                                     false );
    }

    private static class DecisionFrame {
        final Node<Definition<DecisionGateway>, Edge> node;
        int                               index = 0;

        public DecisionFrame( final Node<Definition<DecisionGateway>, Edge> node ) {
            this.node = node;
        }
    }

    private class FormStep<M> implements Step<M, Command<FormOperation, M>> {
        private final UIComponent<M, Command<FormOperation, M>, ? extends V> component;
        public FormStep( final UIComponent<M, Command<FormOperation, M>, ? extends V> component ) {
            this.component = component;
        }

        @Override
        public void execute( final M input,
                             final Consumer<Command<FormOperation, M>> callback ) {
            formDisplayer.show( (UIComponent) component );
            component.start( input, m -> {
                formDisplayer.hide( (UIComponent) component );
                callback.accept( m );
            } );
        }

        @Override
        public String getName() {
            return "Display " + component.getName();
        }
    }

}
