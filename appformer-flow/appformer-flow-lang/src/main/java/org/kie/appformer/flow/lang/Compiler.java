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


package org.kie.appformer.flow.lang;

import static java.util.stream.Collectors.groupingBy;
import static org.kie.appformer.flow.lang.AST.flowType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.errai.common.client.api.Assert;
import org.kie.appformer.flow.api.AppFlow;
import org.kie.appformer.flow.api.AppFlowFactory;
import org.kie.appformer.flow.lang.AST.Assignment;
import org.kie.appformer.flow.lang.AST.ConfigExpression;
import org.kie.appformer.flow.lang.AST.ConstructorPattern;
import org.kie.appformer.flow.lang.AST.ExportFlow;
import org.kie.appformer.flow.lang.AST.Expression;
import org.kie.appformer.flow.lang.AST.FlowExpression;
import org.kie.appformer.flow.lang.AST.FlowType;
import org.kie.appformer.flow.lang.AST.Identifier;
import org.kie.appformer.flow.lang.AST.ImportIdentifier;
import org.kie.appformer.flow.lang.AST.Literal;
import org.kie.appformer.flow.lang.AST.MapExpression;
import org.kie.appformer.flow.lang.AST.MatchableExpression;
import org.kie.appformer.flow.lang.AST.SimpleType;
import org.kie.appformer.flow.lang.AST.Statement;
import org.kie.appformer.flow.lang.AST.Type;
import org.kie.appformer.flow.lang.CompilationContext.Builder;
import org.kie.appformer.flow.lang.CompilationContext.ConstantFlowData;
import org.kie.appformer.flow.lang.CompilationContext.ConstantValueData;
import org.kie.appformer.flow.lang.CompilationContext.ContextAwareFlowData;
import org.kie.appformer.flow.lang.CompilationContext.Data;
import org.kie.appformer.flow.lang.CompilationContext.FlowData;
import org.kie.appformer.flow.lang.CompilationContext.RuntimeFlowData;
import org.kie.appformer.flow.lang.CompilationContext.RuntimeValueData;
import org.kie.appformer.flow.lang.CompilationContext.ValueData;

/**
 * <p>
 * Compiles flow language source or a flow language AST into a map of exported {@link AppFlow
 * AppFlows}.
 */
public class Compiler {

    private final Parser parser;
    private final CompilationContext builtinCtx;
    private final AppFlowFactory factory;

    /**
     * @param parser
     *            For parsing source files. Must not be null.
     * @param builtinCtx
     *            A context containing all built-in flows and other data that can be used in
     *            compiled source. Mut not be null.
     * @param factory
     *            A factory for constructing {@link AppFlow AppFlows}. Must not be null.
     */
    public Compiler( final Parser parser, final CompilationContext builtinCtx, final AppFlowFactory factory ) {
        this.factory = Assert.notNull( factory );
        this.builtinCtx = Assert.notNull( builtinCtx );
        this.parser = Assert.notNull( parser );
    }

    /**
     * <p>
     * Compiles flow language source. Uses the {@link Parser} provided in the constructor to
     * generate an AST and then calls {@link #compileFlows(List)}.
     *
     * @param source
     *            The source to be parsed and compiled. Must not be null.
     * @return A compilation result that is either a list of failure messages, or the successfully
     *         built map of exported {@link AppFlow AppFlows}.
     */
    public Try<List<String>, Map<String, AppFlow<?, ?>>> compileFlows( final String source ) {
        final List<Statement> stmts = parser.parse( source );
        return compileFlows( stmts );
    }

    /**
     * <p>
     * Compiles a list of statements (effectively an AST) into a map of exported {@link AppFlow
     * AppFlows}.
     *
     * @param stmts
     *            A list of flow lanugage statements. Must not be null.
     * @return A compilation result that is either a list of failure messages, or the successfully
     *         built map of exported {@link AppFlow AppFlows}.
     */
    public Try<List<String>, Map<String, AppFlow<?, ?>>> compileFlows( final List<Statement> stmts ) {
        final Map<String, AppFlow<?, ?>> exportedFlows = new HashMap<>();
        final Builder ctxBuilder = new CompilationContext.Builder( builtinCtx );
        final List<String> problems = new ArrayList<>();

        final Map<?, List<Statement>> groupedStmts = stmts
        .stream()
        .collect( groupingBy( stmt -> stmt.getClass() ) );

        groupedStmts
        .getOrDefault( ImportIdentifier.class , Collections.emptyList() )
        .stream()
        .map( stmt -> (ImportIdentifier) stmt )
        .forEachOrdered( stmt -> validateImport( ctxBuilder, problems, stmt ) );

        if ( !problems.isEmpty() ) {
            return new Failure<>( problems );
        }

        final Map<String, Function<Set<String>, Try<String, Data>>> ctors = new HashMap<>();
        final List<String> exported = new ArrayList<>();

        stmts
        .stream()
        .filter( stmt -> stmt instanceof ExportFlow || stmt instanceof Assignment )
        .forEachOrdered( stmt -> {
            final Assignment assignment;
            if ( stmt instanceof Assignment ) {
                assignment = (Assignment) stmt;
            }
            else if ( stmt instanceof ExportFlow ) {
                assignment = ((ExportFlow) stmt).assignment;
                exported.add( assignment.assignedId.value );
            }
            else {
                throw new IllegalStateException( "This code path should be impossible since we have filtered for one of the above conditions." );
            }
            ctors.put( assignment.assignedId.value, createAssignmentConstructor( ctxBuilder, ctors, assignment ) );
        } );

        for ( final String id : exported ) {
            final Try<String, Data> res = ctors.get( id ).apply( new HashSet<>() );
            res
            .successFlatMap( data -> validateExportedData( id, data ) )
            .ifSuccess( flowData -> exportedFlows.put( id, flowData.flow ) )
            .ifFailure( problem -> problems.add( problem ) );
        }

        if ( problems.isEmpty() ) {
            return new Success<>( exportedFlows );
        }
        else {
            return new Failure<>( problems );
        }
    }

    private Try<String, ConstantFlowData> validateExportedData( final String id,
                                                     final Data data ) {
        if ( data instanceof ValueData ) {
            return new Failure<>( "Cannot export value [" + id + "]." );
        }
        else if ( data instanceof ConstantFlowData ) {
            final ConstantFlowData flowData = (ConstantFlowData) data;
            return new Success<>( flowData );
        }
        else {
            return new Failure<>( "Cannot export flow that requires external context: [" + data + "]." );
        }
    }

    private void validateImport( final Builder ctxBuilder,
                            final List<String> problems,
                            final ImportIdentifier stmt ) {

        if ( stmt.type instanceof FlowType ) {
            validateFlowImport( ctxBuilder, problems, stmt );
        }
        else {
            validateConstantImport( ctxBuilder, problems, stmt );
        }
    }

    private void validateConstantImport( final Builder ctxBuilder,
                            final List<String> problems,
                            final ImportIdentifier stmt ) {
        final String name = stmt.id.value;
        final String valueType = ((SimpleType) stmt.type).typeId.value;
        final Optional<ValueData> oValueData = ctxBuilder.resolveValue( name );
        if ( oValueData.isPresent() ) {
            final ValueData data = oValueData.get();
            if ( !Objects.equals( data.type, valueType ) ) {
                problems.add( "Value [" + name + " : " + data.type
                              + "] was imported with the wrong type: \"" + stmt + "\"." );
            }
        }
        else {
            problems.add( "No value found for import statement: \"" + stmt + "\"" );
        }
    }

    private void validateFlowImport( final Builder ctxBuilder,
                            final List<String> problems,
                            final ImportIdentifier stmt ) {
        final String name = stmt.id.value;
        final String inputTypeName = ((FlowType) stmt.type).inputId.value;
        final String outputTypeName = ((FlowType) stmt.type).outputId.value;

        final Optional<FlowData> oFlowData = ctxBuilder.resolveFlow( name );
        if ( oFlowData.isPresent() ) {
            final FlowData flowData = oFlowData.get();
            if ( !( Objects.equals( flowData.input, inputTypeName ) && Objects.equals( flowData.output, outputTypeName ) ) ) {
                problems.add( "Flow [" + name + " : " + flowData.input + " -> " + flowData.output
                              + "] was imported with the wrong types: \"" + stmt + "\"." );
            }
        }
        else {
            problems.add( "No flow found for import statement: \"" + stmt + "\"" );
        }
    }

    @SuppressWarnings( { "rawtypes" } )
    private Function<Set<String>, Try<String, Data>> createAssignmentConstructor( final Builder ctxBuilder,
                                                                                  final Map<String, Function<Set<String>, Try<String, Data>>> ctors,
                                                                                  final Assignment assignment ) {
        return cur -> {
            if ( cur.contains( assignment.assignedId.value ) ) {
                final AppFlow deferredFlow = createDeferredBindingFlow( ctxBuilder, assignment );
                // TODO should probably be an error if not a flow type
                final FlowType type = assignment.type.filter( t -> t instanceof FlowType ).map( t -> (FlowType) t ).orElseGet( () -> flowType( "?", "?" ) );

                return new Success<>( new ConstantFlowData( type.inputId.value, type.outputId.value, deferredFlow ) );
            }
            else {
                cur.add( assignment.assignedId.value );
                final Try<String, Data> tValExp = buildExpression( assignment.value, ctors, ctxBuilder, cur );
                cur.remove( assignment.assignedId.value );
                return tValExp
                        .ifSuccess( data -> ctxBuilder.addData( assignment.assignedId.value, data ) )
                        .successFlatMap( data -> validateTypes( assignment, data ) )
                        .successMap( data -> {
                            if ( data instanceof ContextAwareFlowData ) {
                                return wrapContextAwareFlow( data );
                            }
                            else {
                                return data;
                            }
                } );
            }

        };
    }

    private Data wrapContextAwareFlow( final Data data ) {
        final ContextAwareFlowData fd = (ContextAwareFlowData) data;
        @SuppressWarnings( { "rawtypes", "unchecked" } )
        final AppFlow assignableFlow =
                ((AppFlow) factory
                        .buildFromFunction( o -> new RuntimeResult<>( o, new HashMap<>() ) ))
                        .andThen( fd.flow )
                        .andThen( res -> assertRuntimeResult( res ).value );
        return new ConstantFlowData( fd.input, fd.output, assignableFlow );
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private AppFlow createDeferredBindingFlow( final Builder ctxBuilder,
                               final Assignment assignment ) {
        final AppFlow deferredFlow = factory.buildFromTransition( input ->
            ctxBuilder
                .resolveFlow( assignment.assignedId.value )
                .filter( fd -> fd instanceof ConstantFlowData )
                .map( fd -> (ConstantFlowData) fd )
                .map( fd -> {
                    if ( fd instanceof ContextAwareFlowData ) {
                        return ((AppFlow) fd.flow)
                                    .andThen( (Function<RuntimeResult<?>, Object>) r -> r.value )
                                    .withInput( new RuntimeResult<>( input, new HashMap<>() ) );
                    }
                    else {
                        return ((AppFlow) fd.flow).withInput( input );
                    }
                 } )
                .<RuntimeException>orElseThrow( () -> new RuntimeException( "Deferred binding of [" + assignment.assignedId + "] failed." ) ) );
        return deferredFlow;
    }

    private Try<String, Data> validateTypes( final Assignment assignment,
                                             final Data data ) {
        if ( assignment.type.isPresent() ) {
            final Type declaredType = assignment.type.get();
            if ( data instanceof ValueData ) {
                if ( declaredType instanceof SimpleType ) {
                    if ( !((SimpleType) declaredType).typeId.value.equals( ((ValueData) data).type )
                            || "?".equals( ((SimpleType) declaredType).typeId.value )
                            || "?".equals( ((ValueData) data).type ) )  {
                        return wrongAssignedTypeFailure( assignment, data, declaredType );
                    }
                }
                else {
                    return wrongAssignedTypeFailure( assignment, data, declaredType );
                }
            }
            else {
                if ( declaredType instanceof SimpleType ) {
                    return wrongAssignedTypeFailure( assignment, data, declaredType );
                }
                else {
                    final FlowType declaredFlowType = (FlowType) declaredType;
                    final String input = ((FlowData) data).input;
                    final String output = ((FlowData) data).output;
                    if ( !(input.equals( declaredFlowType.inputId.value )
                            || input.equals( "?" )
                            || declaredFlowType.inputId.value.equals( "?" ))
                         || !(output.equals( declaredFlowType.outputId.value )
                                 || output.equals( "?" )
                                 || declaredFlowType.outputId.value.equals( "?" ) ) ) {
                        return wrongAssignedTypeFailure( assignment, data, declaredType );
                    }
                }

            }
        }
        return new Success<>( data );
    }

    private Failure<String, Data> wrongAssignedTypeFailure( final Assignment assignment,
                                             final Data data,
                                             final Type declaredType ) {
        final Failure<String, Data> failure = new Failure<>( "Value of type [" + data.typeString()
        + "] does not match declared type [" + declaredType + "] for ["
        + assignment.assignedId.value + "]." );
        return failure;
    }

    private Try<String, Data> buildExpression( final Expression exp,
                                               final Map<String, Function<Set<String>, Try<String, Data>>> ctors,
                                               final Builder ctxBuilder,
                                               final Set<String> cur ) {
        if ( exp instanceof Literal ) {
            return buildLiteralExpression( (Literal ) exp );
        }
        else if ( exp instanceof Identifier ) {
            return buildIdentifierExpression( (Identifier) exp, ctors, ctxBuilder, cur );
        }
        else if ( exp instanceof MapExpression ) {
            return buildMapExpression( exp, ctors, ctxBuilder, cur, (MapExpression) exp );
        }
        else if ( exp instanceof ConfigExpression ) {
            final ConfigExpression cfgExp = (ConfigExpression) exp;
            return buildConfigExpression( exp, ctxBuilder, cfgExp );
        }
        else if ( exp instanceof FlowExpression ) {
            return buildFlowExpression( ctors, ctxBuilder, cur, (FlowExpression) exp );
        }
        else {
            return Try.failure( "Unsupported expression type [" + exp.getClass().getSimpleName() + "] for expression ["
                                + exp + "]." );
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private Try<String, Data> buildFlowExpression( final Map<String, Function<Set<String>, Try<String, Data>>> ctors,
                                         final Builder ctxBuilder,
                                         final Set<String> cur,
                                         final FlowExpression flowExp ) {
        final List<FlowData> resolvedFlowParts = new ArrayList<>();
        for ( final Expression part : flowExp.flowParts ) {
            final Try<String, Data> builtPart = buildExpression( part, ctors, ctxBuilder, cur );
            if ( builtPart.failure().isPresent() ) {
                return builtPart;
            }
            final Data data = builtPart.success().get();

            if ( data instanceof ValueData ) {
                if ( data instanceof ConstantValueData ) {
                    resolvedFlowParts.add( new ConstantFlowData( "Unit",
                                                                 ((ValueData) data).type,
                                                                 factory.buildFromConstant( ((ConstantValueData) data).value ) ) );
                }
                else {
                    final RuntimeValueData runtimeData = (RuntimeValueData) data;
                    resolvedFlowParts.add( new RuntimeFlowData( "Unit", runtimeData.type, runtimeCtx -> {
                        final Object val = runtimeData.getter.apply( runtimeCtx );
                        return new Success<>( factory.buildFromConstant( val ) );
                    } ) );
                }
            }
            else {
                resolvedFlowParts.add( (FlowData) data );
            }
        }

        final FlowData initFlow = resolvedFlowParts.get( 0 );
        final String input = initFlow.input;
        String output = input;
        AppFlow flow = factory.buildFromFunction( Function.identity() );
        boolean constant = true;
        for ( int i = 0; i < resolvedFlowParts.size(); i++ ) {
            final FlowData next = resolvedFlowParts.get( i );
            if ( next.input.equals( output ) || next.input.equals( "?" ) || output.equals( "?" ) ) {
                if ( next instanceof ContextAwareFlowData ) {
                    flow = flow.andThen( ((ContextAwareFlowData) next).flow );
                }
                else if ( next instanceof ConstantFlowData ) {
                    flow = flow.transitionTo( o -> {
                        final RuntimeResult<Object> res = assertRuntimeResult( o );
                        final AppFlow nextFlow = ((ConstantFlowData) next).flow;
                        return nextFlow
                                .andThen( out -> new RuntimeResult<>( out, res.ctx ) )
                                .withInput( () -> res.value );
                    } );
                }
                else {
                    constant = false;
                    final RuntimeFlowData runtimeData = (RuntimeFlowData) next;
                    flow = flow.transitionTo( o -> {
                        final RuntimeResult<Object> res = assertRuntimeResult( o );
                        final Try<String, AppFlow<?, ?>> tFlow = runtimeData.ctor.apply( res.ctx );
                        if ( tFlow.success().isPresent() ) {
                            return ((AppFlow) tFlow.success().get())
                                    .andThen( val -> new RuntimeResult<>( val, res.ctx ) )
                                    .withInput( () -> res.value );
                        }
                        else {
                            return Try.failure(  "Couldn't construct runtime flow part. Reason: " + tFlow.failure().get()  );
                        }
                    } );
                }
                output = next.output;
            }
            else {
                return new Failure<>( "Incompatible output and input for flow parts ["
                                      + flowExp.flowParts.get( i - 1 ) + "] and [" + flowExp.flowParts.get( i )
                                      + "]." );
            }
        }
        final AppFlow<?, ?> retVal = flow.andThen( o -> assertRuntimeResult( o ).value );
        if ( constant ) {
            return new Success(
                    new ConstantFlowData( input, output, ((AppFlow) factory.buildFromFunction( o -> new RuntimeResult<>( o, new HashMap<>() ) ))
                                          .andThen( retVal ) ) );
        }
        else {
            return new Success<>(
                    new RuntimeFlowData( input, output,
                                         ctx -> new Success<>( factory
                                                 .buildFromFunction( o -> new RuntimeResult<>( o, ctx ) )
                                                 .andThen( (AppFlow) retVal )) ) );
        }
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private Try<String, Data> buildConfigExpression( final Expression exp,
                                         final Builder ctxBuilder,
                                         final ConfigExpression cfgExp ) {
        final String flowName = cfgExp.id.value;
        final Map<String, Object> props = new HashMap<>();
        try {
            cfgExp.config.forEach( (id, val) -> {
                if ( val.isLiteral() ) {
                    final ConstantValueData loadedVal = loadLiteral( (Literal) val );
                    props.put( id.value, loadedVal.value );
                }
                else if ( val.isIdentifier() ) {
                    throw new UnsupportedOperationException( "Found identifier [" + val
                                                             + "] assigned to config property value but currently only literals are supported." );
                }
                else {
                    throw new UnsupportedOperationException( "Found [" + val
                                                             + "] assigned to config property value but currently only literals are supported." );
                }
            } );
        }
        catch ( final UnsupportedOperationException ex ) {
            return Try.failure( ex.getMessage() );
        }

        return ctxBuilder
                .resolveFlow( flowName )
                .map( fd -> {
                    if ( fd instanceof ConstantFlowData ) {
                        return (Try<String, Data>) new Success( fd );
                    }
                    else {
                        final RuntimeFlowData configData = (RuntimeFlowData) fd;
                        return configData.ctor.apply( props ).successMap( flow -> (Data) new ConstantFlowData( fd.input, fd.output, flow ) );
                    }
                } )
                .orElse( new Failure<>( "Unknown identifer in expression: [" + exp + "]." ) );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private Try<String, Data> buildMapExpression( final Expression exp,
                                         final Map<String, Function<Set<String>, Try<String, Data>>> ctors,
                                         final Builder ctxBuilder,
                                         final Set<String> cur,
                                         final MapExpression mapExp ) {
        final List<String> problems = new ArrayList<>();
        final Map<Function<Object, Optional<Map<String, Object>>>, FlowData> buildMap = new LinkedHashMap<>();
        mapExp
            .mapping
            .forEach( ( keyExp, valExp ) -> {
                final Builder subCtx = new CompilationContext.Builder( ctxBuilder );
                final Try<String, Function<Object, Optional<Map<String, Object>>>> tPatternBinder = createPatternBindingFunction( keyExp, subCtx );
                final Try<String, Data> tValData = buildExpression( valExp, ctors, subCtx, cur );
                if ( tValData.success().isPresent() && tPatternBinder.success().isPresent() ) {
                    final Data data = tValData.success().get();
                    if ( data instanceof FlowData ) {
                        buildMap.put( tPatternBinder.success().get(), (FlowData) data );
                    }
                    else {
                        problems.add( "Expression [" + valExp + "] should be a flow." );
                    }
                }
                else {
                    tPatternBinder.failure().ifPresent( s -> problems.add( s ) );
                    tValData.failure().ifPresent( s -> problems.add( s ) );
                }
            } );

        if ( problems.isEmpty() ) {
            // TODO remove this hack. Figure out actual input type
            final String input = "?";
            final String output = buildMap.values().iterator().next().output;
            return new Success<>(
                    new ContextAwareFlowData( input, output,
                       factory.buildFromTransition( res -> {
                           for ( final Entry<Function<Object, Optional<Map<String, Object>>>, FlowData> entry : buildMap.entrySet() ) {
                               final Optional<Map<String, Object>> oProps = entry.getKey().apply( res.value );
                               if ( oProps.isPresent() ) {
                                   AppFlow flow;
                                   if ( entry.getValue() instanceof ConstantFlowData ) {
                                       flow = ((ConstantFlowData) entry.getValue()).flow;
                                   }
                                   else {
                                       res.ctx.putAll( oProps.get() );
                                       final Try<String, AppFlow<?, ?>> tFlow = ((RuntimeFlowData) entry.getValue()).ctor.apply( res.ctx );
                                       if ( tFlow.success().isPresent() ) {
                                           flow = tFlow.success().get();
                                       }
                                       else {
                                           throw new IllegalStateException( "Failed to construct flow ["
                                                                             + exp
                                                                             + "] at runtime because of the following problem: "
                                                                             + tFlow.failure().get() );
                                       }
                                   }
                                   return flow
                                           .andThen( o -> new RuntimeResult<>( o, res.ctx ) );
                               }
                           }
                           throw new IllegalStateException( "The object [" + res
                                                            + "] did not match any of the patterns for the map expression ["
                                                            + exp + "]." );
                       } )
            ) );
        }
        else {
            final StringBuilder sb = new StringBuilder( "Problems evaluating [" ).append( exp ).append( "]" );
            problems.forEach( s -> sb.append( "\n\t" ).append( s ) );
            return new Failure<>( sb.toString() );
        }
    }

    private Try<String, Data> buildLiteralExpression( final Literal literal ) {
        final ConstantValueData literalValue = loadLiteral( literal );
        final Data data = new ConstantFlowData( "Unit", literalValue.type, factory.buildFromConstant( literalValue.value ) );

        return new Success<>( data );
    }

    private Try<String, Data> buildIdentifierExpression( final Identifier exp,
                                         final Map<String, Function<Set<String>, Try<String, Data>>> ctors,
                                         final Builder ctxBuilder,
                                         final Set<String> cur ) {
        return loadIdentifier( ctors, ctxBuilder, cur, exp )
                .successFlatMap( data -> {
                    if ( data instanceof FlowData ) {
                        return new Success<>( data );
                    }
                    else if ( data instanceof ConstantValueData ) {
                        final ConstantValueData constantData = ((ConstantValueData) data);
                        return new Success<>( new ConstantFlowData( "Unit", constantData.type, factory.buildFromConstant( constantData.value ) ) );
                    }
                    else if ( data instanceof RuntimeValueData ) {
                        final RuntimeValueData runtimeData = (RuntimeValueData) data;
                        return new Success<>(
                                new RuntimeFlowData( "Unit", runtimeData.type,
                                                     ctx -> new Success<>(
                                                             factory.buildFromConstant( runtimeData.getter.apply( ctx ) ) ) ) );
                    }
                    else {
                        return new Failure<>( "Unrecognized value type [" + data.getClass().getSimpleName() + "] for identifier [" + exp + "]." );
                    }
                } );
    }

    @SuppressWarnings( "unchecked" )
    private RuntimeResult<Object> assertRuntimeResult( final Object o ) {
        if ( !(o instanceof RuntimeResult) ) {
            throw new IllegalStateException( "Expected a RuntimeResult, but found [" + o + "] of type ["
                                             + o.getClass().getSimpleName() + "]." );
        }
        else {
            return (RuntimeResult<Object>) o;
        }
    }

    private Try<String, Function<Object, Optional<Map<String, Object>>>> createPatternBindingFunction( final MatchableExpression keyExp,
                                                                                                       final Builder ctxBuilder ) {
        if ( keyExp instanceof Literal ) {
            return createLiteralBindingFunction( (Literal) keyExp );
        }
        else if ( keyExp instanceof Identifier ) {
            return createIdentifierBindingFunction( ctxBuilder, (Identifier) keyExp );
        }
        else if ( keyExp instanceof ConstructorPattern ) {
            final ConstructorPattern ctorExp = (ConstructorPattern) keyExp;
            return createConstructorPatternBindingFunction( keyExp, ctxBuilder, ctorExp );
        }
        else {
            return Try.failure( "Unsupported expression kind [" + keyExp + "] for pattern matching expression." );
        }
    }

    private Try<String, Function<Object, Optional<Map<String, Object>>>> createConstructorPatternBindingFunction( final MatchableExpression keyExp,
                                                                                                                  final Builder ctxBuilder,
                                                                                                                  final ConstructorPattern ctorExp ) {
        final Optional<PatternMatcher> oMatcher = ctxBuilder.resolveMatcher( ctorExp.ctor.value );
        if ( oMatcher.isPresent() ) {
            final PatternMatcher matcher = oMatcher.get();
            if ( ctorExp.args.size() != matcher.argLength() ) {
                return new Failure<>( "Expected " + matcher.argLength() + " arguments in [" + keyExp + "]." );
            }
            final List<Try<String, Function<Object, Optional<Map<String, Object>>>>> tArgFunctions =
               ctorExp.args
                .stream()
                .map( exp -> createPatternBindingFunction( exp, ctxBuilder ) )
                .collect( Collectors.toList() );

            final boolean allSuccess = tArgFunctions.stream().allMatch( t -> t.success().isPresent() );
            if ( allSuccess ) {
                final List<Function<Object, Optional<Map<String, Object>>>> argFunctions =
                        tArgFunctions
                            .stream()
                            .map( t -> t.success().get() )
                            .collect( Collectors.toList() );

                return new Success<>( o -> {
                    if ( matcher.matches( o ) ) {
                        final Map<String, Object> ctx = new HashMap<>();
                        for ( int i = 0; i < matcher.argLength(); i++ ) {
                            final Object arg = matcher.get( o, i );
                            final Function<Object, Optional<Map<String, Object>>> argFunc = argFunctions.get( i );
                            final Optional<Map<String, Object>> oArgRes = argFunc.apply( arg );
                            if ( oArgRes.isPresent() ) {
                                ctx.putAll( oArgRes.get() );
                            }
                            else {
                                return Optional.empty();
                            }
                        }

                        return Optional.of( ctx );
                    }
                    else {
                        return Optional.empty();
                    }
                } );
            }
            else {
                final StringBuilder builder = new StringBuilder( "Problems generating pattern matching for [" ).append( keyExp ).append( "]." );
                tArgFunctions
                    .stream()
                    .filter( t -> t.failure().isPresent() )
                    .map( t -> t.failure().get() )
                    .forEach( s -> builder.append( "\n\t" ).append( s ) );

                return new Failure<>( builder.toString() );
            }
        }
        else {
            return new Failure<>( "No pattern matcher found for expression [" + keyExp + "]." );
        }
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private Try<String, Function<Object, Optional<Map<String, Object>>>> createIdentifierBindingFunction( final Builder ctxBuilder,
                                                                                    final Identifier id ) {
        final Optional<ValueData> oValueData = ctxBuilder.resolveValue( id.value );
        return oValueData
                .map( data -> new Success( (Function) o -> {
                    if ( data instanceof ConstantValueData ) {
                        if ( Objects.equals( o, ((ConstantValueData) data).value ) ) {
                            return Optional.of( Collections.emptyMap() );
                        }
                        else {
                            return Optional.empty();
                        }
                    }
                    else {
                        throw new RuntimeException( "Not yet implemented!" );
                    }
                } ) )
                .orElseGet( () -> {
                    ctxBuilder.addVariable( id.value, "?", m -> m.get( id.value ) );
                    return new Success<>( (Function) o -> {
                        final Map<String, Object> props = new HashMap<>();
                        props.put( id.value, o );
                        return Optional.of( props );
                    } );
                } );
    }

    private Try<String, Function<Object, Optional<Map<String, Object>>>> createLiteralBindingFunction( final Literal literal ) {
        final ConstantValueData loadedData = loadLiteral( literal );
        return new Success<>( o -> {
            if ( Objects.equals( o, loadedData.value ) ) {
                return Optional.of( Collections.emptyMap() );
            }
            else {
                return Optional.empty();
            }
        } );
    }

    private ConstantValueData loadLiteral( final Literal val ) {
        if ( "true".equals( val.value ) || "false".equals( val.value ) ) {
            return new ConstantValueData( "Boolean", Boolean.parseBoolean( val.value ) );
        }
        else {
            return new ConstantValueData( "Integer", Integer.valueOf( val.value ) );
        }
    }

    private Try<String, Data> loadIdentifier( final Map<String, Function<Set<String>, Try<String, Data>>> ctors,
                                              final Builder ctxBuilder,
                                              final Set<String> cur,
                                              final Identifier id ) {
        final Optional<ValueData> resolvedValue = ctxBuilder.resolveValue( id.value );
        final Optional<FlowData> resolvedFlow = ctxBuilder.resolveFlow( id.value );
        if ( resolvedFlow.isPresent() || resolvedValue.isPresent() ) {
            if ( resolvedFlow.isPresent() && resolvedValue.isPresent() ) {
                return new Failure<>( "Ambiguous identifier, [" + id.value + "], is assigned as flow and value." );
            }
            else if ( resolvedFlow.isPresent() ) {
                final FlowData fd = resolvedFlow.get();
                if ( fd instanceof ConstantFlowData ) {
                    return new Success<>( fd );
                }
                else {
                    final RuntimeFlowData runtimeData = (RuntimeFlowData) fd;
                    return runtimeData
                        .ctor
                        .apply( Collections.emptyMap() )
                        .successMap( flow -> new ConstantFlowData( runtimeData.input, runtimeData.output, flow ) );
                }
            }
            else {
                return new Success<>( resolvedValue.get() );
            }
        }
        else if ( ctors.containsKey( id.value ) ) {
            final Function<Set<String>, Try<String, Data>> ctor = ctors.get( id.value );
            return ctor.apply( cur );
        }
        else {
            return new Failure<>( "Identifier [" + id.value + "] used but never assigned." );
        }
    }
}
