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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.kie.appformer.flow.api.AppFlow;
import org.kie.appformer.flow.lang.AST.ConfigExpression;
import org.kie.appformer.flow.lang.AST.ConstructorPattern;
import org.kie.appformer.flow.lang.AST.ImportIdentifier;
import org.kie.appformer.flow.lang.AST.MapExpression;

/**
 * <p>
 * Context use to compile a flow language script into an executable {@link AppFlow}.
 * <p>
 * The context contains flows and constant values that can be imported, and pattern matchers for
 * evaluating {@link ConstructorPattern constructor patterns}.
 * <p>
 * Contexts can optionally have a parent. If a context has a parent, values will be resolved from
 * the child first, and if not present will then be resolved from the parent recursively.
 */
public class CompilationContext {

    protected final Optional<CompilationContext> parent;
    protected final Map<String, FlowData> flows;
    protected final Map<String, ValueData> values;
    protected final Map<String, PatternMatcher> matchers;

    static abstract class Data {
        public abstract String typeString();
    }

    static abstract class FlowData extends Data {
        public final String input;
        public final String output;
        protected FlowData( final String input, final String output ) {
            this.input = input;
            this.output = output;
        }

        @Override
        public String typeString() {
            return input + " -> " + output;
        }
    }

    static class ConstantFlowData extends FlowData {
        public final AppFlow<?, ?> flow;

        ConstantFlowData( final String input, final String output, final AppFlow<?, ?> flow ) {
            super( input, output );
            this.flow = flow;
        }

        @Override
        public String toString() {
            return "ConstantFlow[" + typeString() + "]";
        }
    }

    static class ContextAwareFlowData extends ConstantFlowData {
        public ContextAwareFlowData( final String input, final String output, final AppFlow<RuntimeResult<?>, RuntimeResult<?>> flow ) {
            super( input, output, flow );
        }

        @Override
        public String toString() {
            return "ContextAwareFlow[" + typeString() + "]";
        }
    }

    static class RuntimeFlowData extends FlowData {
        public final Function<Map<String, Object>, Try<String, AppFlow<?, ?>>> ctor;

        RuntimeFlowData( final String input, final String output, final Function<Map<String, Object>, Try<String, AppFlow<?, ?>>> ctor ) {
            super( input, output );
            this.ctor = ctor;
        }

        @Override
        public String toString() {
            return "RuntimeFlow[" + typeString() + "]";
        }
    }

    static abstract class ValueData extends Data {
        public final String type;
        public ValueData( final String type ) {
            this.type = type;
        }

        @Override
        public String typeString() {
            return type;
        }
    }

    static class ConstantValueData extends ValueData {
        public final Object value;
        public ConstantValueData( final String type, final Object value ) {
            super( type );
            this.value = value;
        }
    }

    static class RuntimeValueData extends ValueData {
        public final Function<Map<String, Object>, Object> getter;
        public RuntimeValueData( final String type, final Function<Map<String, Object>, Object> getter ) {
            super( type );
            this.getter = getter;
        }
    }

    /**
     * Used to build an immutable {@link CompilationContext}.
     */
    public static class Builder extends CompilationContext {

        public Builder( final CompilationContext parent ) {
            super( parent, new HashMap<>(), new HashMap<>(), new HashMap<>() );
        }

        /**
         * <p>
         * Add an {@link AppFlow} to the context (used in {@link ImportIdentifier import
         * statements}).
         *
         * @param name
         *            Name of the flow (used as an identifier when imported).
         * @param inputType
         *            Name of the input type of this flow (used as input type identifier when
         *            imported).
         * @param outputType
         *            Name of the output type of this flow (used as output type identifier when
         *            imported).
         * @param flow
         *            The flow that will be used for the given name when compiled.
         * @return This builder for chaining method calls.
         */
        public Builder addFlow( final String name, final String inputType, final String outputType, final AppFlow<?, ?> flow ) {
            flows.put( name, new ConstantFlowData( inputType, outputType, flow ) );
            return this;
        }

        /**
         * <p>
         * Add an constructor for an {@link AppFlow} to the context (used in {@link ImportIdentifier
         * import statements}). The constructor is used to create a flow based on a particular
         * {@link ConfigExpression configuration}.
         *
         * @param name
         *            Name of the flow (used as an identifier when imported).
         * @param inputType
         *            Name of the input type of this flow (used as input type identifier when
         *            imported).
         * @param outputType
         *            Name of the output type of this flow (used as output type identifier when
         *            imported).
         * @param ctor
         *            The constructor function used to create an {@link AppFlow} during compilation.
         *            Returns a {@link Try} so that failure is explicitly handled through the return
         *            value. Should not throw exceptions.
         * @return This builder for chaining method calls.
         */
        public Builder addFlowConstructor( final String name,
                                           final String inputType,
                                           final String outputType,
                                           final Function<Map<String, Object>, Try<String, AppFlow<?, ?>>> ctor ) {
            flows.put( name, new RuntimeFlowData( inputType, outputType, ctor ) );
            return this;
        }

        /**
         * <p>
         * Add an constructor for an {@link AppFlow} to the context. The constructor is used to
         * create a flow based on a particular {@link ConfigExpression configuration}.
         *
         * @param name
         *            Name of the flow (used as an identifier when imported).
         * @param inputType
         *            Name of the input type of this flow (used as input type identifier when
         *            imported).
         * @param outputType
         *            Name of the output type of this flow (used as output type identifier when
         *            imported).
         * @param ctor
         *            The constructor function used to create an {@link AppFlow} during compilation.
         *            Returns a {@link Try} so that failure is explicitly handled through the return
         *            value. Should not throw exceptions.
         * @return This builder for chaining method calls.
         */
        public Builder addValue( final String name, final String type, final Object value ) {
            values.put( name, new ConstantValueData( type, value ) );
            return this;
        }

        /**
         * <p>
         * Add an variable that's value is known at runtime. Used to bind values that are
         * pattern-matched in {@link MapExpression map expressions}.
         *
         * @param name
         *            Name of the variable.
         * @param type
         *            Name of the type of the variable.
         * @param getter
         *            The getter function that extracts the value of the variable from the runtime
         *            context.
         * @return This builder for chaining method calls.
         */
        public Builder addVariable( final String name, final String type, final Function<Map<String, Object>, Object> getter ) {
            values.put( name, new RuntimeValueData( type, getter ) );
            return this;
        }

        Builder addData( final String name, final Data data ) {
            if ( data instanceof FlowData ) {
                flows.put( name, (FlowData) data );
            }
            else {
                values.put( name, (ValueData) data );
            }
            return this;
        }

        /**
         * <p>
         * Add a {@link PatternMatcher} used to match {@link ConstructorPattern constructor
         * patterns} in {@link MapExpression} map expressions.
         *
         * @param ctorName
         *            Name of the constructor (i.e. for <code>Foo(value)</code> this parmeter value
         *            would be <code>"Foo"</code>).
         * @param matcher
         *            The {@link PatternMatcher} used to match expressions for the given
         *            constructor.
         * @return This builder for chaining method calls.
         */
        public Builder addMatcher( final String ctorName, final PatternMatcher matcher ) {
            matchers.put( ctorName, matcher );
            return this;
        }

        /**
         * @return An immutable {@link CompilationContext} containing all the data added to this
         *         builder.
         */
        public CompilationContext build() {
            return new CompilationContext( null, new HashMap<>( flows ), new HashMap<>( values ), new HashMap<>( matchers ) );
        }

    }

    private CompilationContext( final CompilationContext parent,
                               final Map<String, FlowData> flows,
                               final Map<String, ValueData> values,
                               final Map<String, PatternMatcher> matchers ) {
        this.matchers = matchers;
        this.parent = Optional.ofNullable( parent );
        this.flows = flows;
        this.values = values;
    }

    private <T> Optional<T> childFirstAction( final Function<CompilationContext, Optional<T>> action ) {
        return action.apply( this ).map( Optional::of ).orElseGet( () -> parent.flatMap( p -> p.childFirstAction( action ) ) );
    }

    /**
     * <p>
     * Resolves a flow recursively starting at this context, and then falling back to parent
     * context.
     *
     * @param name
     *            The name of the flow to be resolved.
     *
     * @return An optional containing a flow if this context or any of its ancestors contains a flow
     *         by the given name. Otherwise an empty optional.
     */
    Optional<FlowData> resolveFlow( final String name ) {
        return childFirstAction( ctx -> Optional.ofNullable( ctx.flows.get( name ) ) );
    }

    /**
     * <p>
     * Resolves a constant value recursively starting at this context, and then falling back to
     * parent context.
     *
     * @param name
     *            The name of the constant value to be resolved.
     *
     * @return An optional containing data of a constant value if this context or any of its
     *         ancestors contains a value by the given name. Otherwise an empty optional.
     */
    Optional<ValueData> resolveValue( final String name ) {
        return childFirstAction( ctx -> Optional.ofNullable( ctx.values.get( name ) ) );
    }

    /**
     * <p>
     * Resolves a pattern matcher recursively starting at this context, and then falling back to
     * parent context.
     *
     * @param ctorName
     *            The name of the constructor for which the pattern matcher is to be resolved.
     *
     * @return An optional containing a pattern matcher if this context or any of its ancestors
     *         contains a matcher by the given name. Otherwise an empty optional.
     */
    Optional<PatternMatcher> resolveMatcher( final String ctorName ) {
        return childFirstAction( ctx -> Optional.ofNullable( ctx.matchers.get( ctorName ) ) );
    }
}
