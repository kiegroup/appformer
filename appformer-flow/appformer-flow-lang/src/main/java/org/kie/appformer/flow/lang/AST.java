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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * <p>
 * Contains parts of the abstract syntax tree of the App Flow Language, and convenience methods for
 * creating parts of the AST.
 */
public class AST {

    public static abstract class Statement {
    }

    public static abstract class Expression {
    }

    public static abstract class Type {
    }

    public static class Empty extends Statement {
        public static Empty INSTANCE = new Empty();
        private Empty() {}
    }

    /**
     * <p>
     * An import statement used to declare values or flows that are externally defined. Takes one of
     * the following forms.
     * <ul>
     * <li>To import a flow:
     *
     * <pre>
     * <code>import $FlOW_NAME : $INPUT_TYPE -> $OUTPUT_TYPE ;</code>
     * </pre>
     *
     * <li>To import a value:
     *
     * <pre>
     * <code>import $VALUE_NAME : $TYPE ;</code>
     * </pre>
     */
    public static class ImportIdentifier extends Statement {
        public final Identifier id;
        public final Type type;
        public ImportIdentifier( final Identifier id, final Type type ) {
            this.id = id;
            this.type = type;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof ImportIdentifier && equalsHelper( this, obj, o -> o.id, o -> o.type );
        }

        @Override
        public int hashCode() {
            return id.hashCode() ^ type.hashCode();
        }

        @Override
        public String toString() {
            return "import " + id + " : " + type + " ;";
        }
    }

    /**
     * <p>
     * An export statement. Normal assignments can only be used by other flow definitions in the
     * same file. Exported flows are accessible as the result of a computation.
     * <p>
     * Takes one of the following forms:
     * <ul>
     * <li>An untyped assignment:
     *
     * <pre>
     * <code>export $FLOW_NAME = $FLOW_EXPRESSION ;</code>
     * </pre>
     *
     * <li>A typed assignment:
     *
     * <pre>
     * <code>export $FLOW_NAME : $INPUT_TYPE -> $OUTPUT_TYPE = $FLOW_EXPRESSION ;</code>
     * </pre>
     */
    public static class ExportFlow extends Statement {
        public final Assignment assignment;
        public ExportFlow( final Assignment assignment ) {
            this.assignment = assignment;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof ExportFlow && equalsHelper( this, obj, o -> o.assignment );
        }

        @Override
        public int hashCode() {
            return assignment.hashCode();
        }

        @Override
        public String toString() {
            return "export " + assignment;
        }
    }

    /**
     * <p>
     * An assignment statement for either a flow or constant value. Takes one of the following
     * forms:
     * <ul>
     * <li>An untyped assignment (for a flow or constant value):
     *
     * <pre>
     * <code>$NAME = $EXPRESSION ;</code>
     * </pre>
     *
     * <li>A typed flow assignment:
     *
     * <pre>
     * <code>$FLOW_NAME : $INPUT_TYPE -> $OUTPUT_TYPE = $FLOW_EXPRESSION ;</code>
     * </pre>
     *
     * <li>A typed value assignment:
     *
     * <pre>
     * <code>$CONSTANT_NAME : $TYPE = $SIMPLE_EXPRESSION ;</code>
     * </pre>
     */
    public static class Assignment extends Statement {
        public final Identifier assignedId;
        public final Optional<Type> type;
        public final Expression value;
        public Assignment( final Identifier assignedId, final Optional<Type> type, final Expression value ) {
            this.assignedId = assignedId;
            this.type = type;
            this.value = value;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof Assignment && equalsHelper( this, obj, o -> assignedId, o -> o.type, o -> o.value );
        }

        @Override
        public int hashCode() {
            return Objects.hash( assignedId, type, value );
        }

        @Override
        public String toString() {
            return assignedId.toString() + type.map( t -> " : " + t ).orElse( "" ) + " = " + value + " ;";
        }
    }

    /**
     * <p>
     * A complex expression chaining together multiple flows. A flow expression matches one of the
     * following cases (with the last being recursive).
     * <ul>
     * <li>
     *
     * <pre>
     * <code>$IDENTIFIER</code>
     * </pre>
     *
     * <li>
     *
     * <pre>
     * <code>$LITERAL</code>
     * </pre>
     *
     * <li>
     *
     * <pre>
     * <code>$CONFIG_EXPRESSION</code>
     * </pre>
     *
     * <li>
     *
     * <pre>
     * <code>$MAP_EXPRESSION</code>
     * </pre>
     *
     * <li>
     *
     * <pre>
     * <code>$EXPRESSION -> $FLOW_EXPRESSION</code>
     * </pre>
     */
    public static class FlowExpression extends Expression {
        public final List<Expression> flowParts;
        public FlowExpression( final List<Expression> flowParts ) {
            this.flowParts = flowParts;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof FlowExpression && equalsHelper( this, obj, o -> o.flowParts );
        }

        @Override
        public int hashCode() {
            return flowParts.hashCode();
        }

        @Override
        public String toString() {
            return flowParts.stream().map( part -> part.toString() ).reduce( (s1, s2 ) -> s1 + " -> " + s2 ).orElse( "" );
        }
    }

    /**
     * <p>
     * An expression for passing in named values to a flow. Takes the following form.
     *
     * <pre>
     * <code>$IDENTIFIER ( $PARAM1 = $ARG1 , $PARAM2 = $ARG2 [, ... ] )</code>
     * </pre>
     *
     * (Where <code>[ , ... ]</code> indicates an arbitrary number of parameters).
     */
    public static class ConfigExpression extends Expression {
        public final Identifier id;
        public final Map<Identifier, SimpleExpression> config;
        public ConfigExpression( final Identifier id, final Map<Identifier, SimpleExpression> config ) {
            this.id = id;
            this.config = config;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof ConfigExpression && equalsHelper( this, obj, o -> id, o -> o.config );
        }

        @Override
        public int hashCode() {
            return Objects.hash( id, config );
        }

        @Override
        public String toString() {
            return id + " ( " + config.entrySet().stream().map( e -> e.getKey() + " = " + e.getValue() ).reduce( (s1, s2) -> s1 + " , " + s2 ).orElse( "" ) + " )";
        }
    }

    /**
     * <p>
     * An expression for choosing a flow at runtime based on the output value of the previous flow
     * in the chain. Takes the form:
     *
     * <pre>
     * <code>{
     *   $PATTERN1 : $FLOW_EXPRESSION1 ,
     *   $PATTERN2 : $FLOW_EXPRESSION2 [, ... ]
     * }
     * </code>
     * </pre>
     *
     * (Where <code>[, ... ]</code> indicates that there may be an arbitrary number of pattern-value
     * pairs.)
     */
    public static class MapExpression extends Expression {
        public final LinkedHashMap<MatchableExpression, Expression> mapping;
        public MapExpression( final LinkedHashMap<MatchableExpression, Expression> mapping ) {
            this.mapping = mapping;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof MapExpression && equalsHelper( this, obj, o -> mapping );
        }

        @Override
        public int hashCode() {
            return mapping.hashCode();
        }

        @Override
        public String toString() {
            return "{\n" + mapping.entrySet().stream().map( e -> "\t" + e.getKey() + " : " + e.getValue() ).reduce( ( s1,
                                                                                                                     s2 ) -> s1
                                                                                                                             + " ,\n"
                                                                                                                             + s2 ).orElse( "" )
                   + "\n}";
        }
    }

    public static abstract class MatchableExpression extends Expression {
    }

    /**
     * <p>
     * An expression for pattern matching constructors, of the following form:
     *
     * <pre>
     * <code>$CONSTRUCTOR_NAME ( $VALUE1 , $VALUE2 [, ... ] )</code>
     * </pre>
     *
     * (Where <code>[, ... ]</code> indicates that there may be an arbitrary number of values.)
     */
    public static class ConstructorPattern extends MatchableExpression {
        public final Identifier ctor;
        public final List<MatchableExpression> args;

        public ConstructorPattern( final Identifier ctor, final List<MatchableExpression> args ) {
            this.ctor = ctor;
            this.args = args;
        }

        @Override
        public int hashCode() {
            return Objects.hash( ctor, args );
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof ConstructorPattern && equalsHelper( this, obj, o -> o.ctor, o -> o.args );
        }

        @Override
        public String toString() {
            return ctor + " ( " + args.stream().map( Object::toString ).reduce( (s1, s2) -> s1 + " , " + s2 ).orElse( "" ) + " )";
        }
    }

    /**
     * <p>
     * A common type for literals and identifiers.
     */
    public static abstract class SimpleExpression extends MatchableExpression {
        public final String value;
        public SimpleExpression( final String value ) {
            this.value = value;
        }
        public abstract boolean isLiteral();
        public abstract boolean isIdentifier();

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * <p>
     * An expression type for literals such as the boolean constants <code>true</code> and
     * <code>false</code> or integer literals (i.e. 1, 2, 3, etc.).
     */
    public static class Literal extends SimpleExpression {
        public Literal( final String value ) {
            super( value );
        }
        @Override
        public boolean isLiteral() {
            return true;
        }
        @Override
        public boolean isIdentifier() {
            return false;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof Literal && equalsHelper( this, obj, o -> o.value );
        }
    }

    /**
     * <p>
     * An expression type for identifiers of constants or flows.
     */
    public static class Identifier extends SimpleExpression {
        public Identifier( final String value ) {
            super( value );
        }
        @Override
        public boolean isLiteral() {
            return false;
        }
        @Override
        public boolean isIdentifier() {
            return true;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof Identifier && equalsHelper( this, obj, o -> o.value );
        }
    }

    /**
     * <p>
     * A type of a flow (has two identifiers, one for input and one for output). Takes the form:
     *
     * <pre>
     * <code>$INPUT_TYPE_ID -> $OUTPUT_TYPE_ID<code>
     * </pre>
     */
    public static class FlowType extends Type {
        public final Identifier inputId;
        public final Identifier outputId;
        public FlowType(final Identifier inputId, final Identifier outputId) {
            this.inputId = inputId;
            this.outputId = outputId;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof FlowType && equalsHelper( this, obj, o -> o.inputId, o -> o.outputId );
        }

        @Override
        public int hashCode() {
            return Objects.hash( inputId, outputId );
        }

        @Override
        public String toString() {
            return inputId + " -> " + outputId;
        }
    }

    /**
     * <p>
     * A type for a constant value. Has only a single identifier for the name of the type of the
     * constant.
     */
    public static class SimpleType extends Type {
        public final Identifier typeId;
        public SimpleType( final Identifier typeId ) {
            this.typeId = typeId;
        }

        @Override
        public boolean equals( final Object obj ) {
            return obj instanceof SimpleType && equalsHelper( this, obj, o -> typeId );
        }

        @Override
        public int hashCode() {
            return typeId.hashCode();
        }

        @Override
        public String toString() {
            return typeId.toString();
        }
    }

    /**
     * @param typeName
     *            The name for an identifier. Must not be null.
     * @return An identifier with the given name.
     */
    public static Identifier identifier( final String typeName ) {
        return new Identifier( typeName );
    }

    /**
     * @param typeName
     *            The name of a value type. Must not be null.
     * @return A value type with the given name.
     */
    public static SimpleType simpleType( final String typeName ) {
        return new SimpleType( identifier( typeName ) );
    }

    /**
     * @param inputType
     *            The name of a value type for a flow input. Must not be null.
     * @param outputType
     *            Th ename of a value type for a flow output. Must not be null.
     * @return A flow type consisting of input and output types of the given names.
     */
    public static FlowType flowType( final String inputType, final String outputType ) {
        return new FlowType( identifier( inputType ), identifier( outputType ) );
    }

    /**
     * @param flowName
     *            The name of a flow being imported. Must not be null.
     * @param inputType
     *            The name of the input type of the imported flow. Must not be null.
     * @param outputType
     *            The name of the output type of the imported flow. Must not be null.
     * @return An import statement for a flow with the given name and input/output types.
     */
    public static ImportIdentifier importFlow( final String flowName,
                                               final String inputType,
                                               final String outputType ) {
        return new ImportIdentifier( identifier( flowName ), new FlowType( identifier( inputType ), identifier( outputType ) ) );
    }

    /**
     * @param name
     *            The name of a value being imported. Must not be null.
     * @param type
     *            The name of the type of the value being imported. Must not be null.
     * @return An import statement for a value with the given name and type.
     */
    public static ImportIdentifier importValue( final String name, final String type ) {
        return new ImportIdentifier( identifier( name ), new SimpleType( identifier( type ) ) );
    }

    /**
     * @param lhs
     *            The name of the identifier being exported (i.e. the left-hand side of the
     *            assignment). Must not be null.
     * @param rhs
     *            The name of the identifier that is the value being assigned (i.e. the right-hand
     *            side of the assignment). Must not be null.
     * @return An export statement for a simple assignment (one identifier assigned as a value to
     *         another).
     */
    public static ExportFlow simpleExport( final String lhs, final String rhs ) {
        return new ExportFlow( simpleAssignment( lhs, rhs ) );
    }

    /**
     * @param assignment
     *            The assignment to be exported. Must not be null.
     * @return An export statement for the given assignment.
     */
    public static ExportFlow export( final Assignment assignment ) {
        return new ExportFlow( assignment );
    }

    /**
     * @param lhs
     *            The name of the identifier being assigned (i.e. the left-hand side of the
     *            assignment). Must not be null.
     * @param rhs
     *            The name of the identifier that is the value being assigned (i.e. the right-hand
     *            side of the assignment). Must not be null.
     * @return Asimple assignment (one identifier assigned as a value to another) without explicit
     *         type declaration.
     */
    public static Assignment simpleAssignment( final String lhs, final String rhs ) {
        return new Assignment( identifier( lhs ), Optional.empty(), identifier( rhs ) );
    }

    /**
     * @param lhs
     *            The name of the identifier being assigned (i.e. the left-hand side of the
     *            assignment). Must not be null.
     * @param inputType
     *            The name of the input type for the assigned flow. Must not be null.
     * @param outputType
     *            The name of the output type for the assigned flow. Must not be null.
     * @param rhs
     *            The name of the identifier that is the value being assigned (i.e. the right-hand
     *            side of the assignment). Must not be null.
     * @return Asimple assignment for a flow (one identifier assigned as a value to another) with
     *         explicit type declaration.
     */
    public static Assignment simpleAssignment( final String lhs, final String inputType, final String outputType, final String rhs ) {
        return new Assignment( identifier( lhs ),
                               Optional.of( new FlowType( identifier( inputType ),
                                                          identifier( outputType ) ) ),
                               identifier( rhs ) );
    }

    /**
     * @param lhs
     *            The name of the identifier being assigned to. Must not be null.
     * @param rhs
     *            The value expression of the assignemnt. Must not be null.
     * @return An assignment without an explicit type.
     */
    public static Assignment assignment( final String lhs, final Expression rhs ) {
        return new Assignment( identifier( lhs ), Optional.empty(), rhs );
    }

    /**
     * @param lhs
     *            The name of the identifier being assigned to. Must not be null.
     * @param type
     *            The explicit type of the assignment. Must not be null.
     * @param rhs
     *            The value expression of the assignemnt. Must not be null.
     * @return An assignment of a flow or value with an explicit type.
     */
    public static Assignment assignment( final String lhs, final Type type, final Expression rhs ) {
        return new Assignment( identifier( lhs ), Optional.of( type ), rhs );
    }

    /**
     * @param expressions
     *            An sequence of expressions that are each flows. Must not be null.
     * @return An expression that combines the given sequence into a single flow using the
     *         {@code ->} combinator.
     */
    public static FlowExpression flowExpression( final Expression... expressions ) {
        return new FlowExpression( Arrays.asList( expressions ) );
    }

    /**
     * @param name
     *            name The name of the identifier for a config expression. Must not be null.
     * @param config
     *            A map of identifiers to simple values for the config expression. Must not be null.
     * @return A configuration expression for the given identifier name with the given arguments.
     */
    public static ConfigExpression config( final String name, final Map<Identifier, SimpleExpression> config ) {
        return new ConfigExpression( identifier( name ), config );
    }

    /**
     * @param map
     *            An insertion-ordered map of pattern-matchable expressions to flow expressions.
     *            Must not be null.
     * @return A map expression for binding the given patterns to their respecitve flow expressions
     *         at runtime.
     */
    public static MapExpression mapping( final LinkedHashMap<MatchableExpression, Expression> map ) {
        return new MapExpression( map );
    }

    /**
     * @param ctorName
     *            The identifier name of the constructor for the returned pattern-matching
     *            expression. Must not be null.
     * @param args
     *            The arguments of the constructor pattern-matching expression. Must not be null.
     * @return A constructor pattern for a constructor of the given name with the given argument
     *         expressions.
     */
    public static ConstructorPattern constructorPattern( final String ctorName, final MatchableExpression... args ) {
        return new ConstructorPattern( identifier( ctorName ), Arrays.asList( args ) );
    }

    /**
     * @param value
     *            A string representation of a literal value (i.e. an integer, {@code true}, or
     *            {@code false}). Must not be null.
     * @return A representation of a literal value such as an integer or a boolean.
     */
    public static Literal literal( final String value ) {
        return new Literal( value );
    }

    @SuppressWarnings( "unchecked" )
    @SafeVarargs
    private static <T> boolean equalsHelper( final T obj1, final Object obj2, final Function<T, Object>... getters ) {
        return Arrays.stream( getters ).allMatch( getter -> Objects.equals( getter.apply( obj1 ), getter.apply( (T) obj2 ) ) );
    }

}
