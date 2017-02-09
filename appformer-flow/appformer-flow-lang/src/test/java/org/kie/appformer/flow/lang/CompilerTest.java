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

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kie.appformer.flow.api.AppFlow;
import org.kie.appformer.flow.api.AppFlowExecutor;
import org.kie.appformer.flow.api.AppFlowFactory;
import org.kie.appformer.flow.api.Unit;
import org.kie.appformer.flow.impl.RuntimeAppFlowExecutor;
import org.kie.appformer.flow.impl.RuntimeAppFlowFactory;
import org.kie.appformer.flow.lang.CompilationContext.Builder;

@RunWith( JUnit4.class )
@SuppressWarnings( "unchecked" )
public class CompilerTest {

    Compiler compiler;
    Parser parser;
    AppFlowFactory factory = new RuntimeAppFlowFactory();
    AppFlowExecutor executor = new RuntimeAppFlowExecutor();

    @Before
    public void setup() {
        parser = new Parser();
        final Builder builder = new CompilationContext.Builder( null );
        builder.addValue( "Num", "Integer", 1 );
        builder.addFlow( "One", "Unit", "Integer", factory.buildFromConstant( 1 ) );
        builder.addFlow( "Double", "Integer", "Integer", factory.buildFromFunction( (final Integer x) -> 2*x ) );
        builder.addFlow( "Same", "Integer", "Integer", factory.buildFromFunction( (final Integer x) -> x ) );
        builder.addFlow( "Decrement", "Integer", "Integer", factory.buildFromFunction( (final Integer x) -> x-1 ) );
        builder.addFlow( "toUnit", "?", "Unit", factory.buildFromFunction( o -> Unit.INSTANCE ) );
        builder.addFlowConstructor( "Int", "Unit", "Integer", props -> {
            final Object val = props.getOrDefault( "num", 0 );
            if ( val instanceof Integer ) {
                return new Success<>( factory.buildFromConstant( (Integer) val ) );
            }
            else {
                return new Failure<>( "Must have Integer property \"num\"." );
            }
        } );
        builder.addMatcher( "IntVal", new IntValMatcher() );
        builder.addMatcher( "Complex", new ComplexMatcher() );
        compiler = new Compiler( parser, builder.build(), factory );
    }

    @Test
    public void compileSimpleExport() throws Exception {
        final String source =
                  "import One : Unit -> Integer ;\n"
                + "import Double : Integer -> Integer ;\n"
                + "export Two : Unit -> Integer = One -> Double ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().orElse( emptyList() ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Two" ) );

        final AppFlow<Unit, Integer> two = (AppFlow<Unit, Integer>) exported.get( "Two" );
        final Integer obs = syncExecute( two );
        assertEquals( 2, obs.intValue() );
    }

    @Test
    public void compileMultipleSimpleExports() throws Exception {
        final String source =
                  "import One : Unit -> Integer ;\n"
                + "import Double : Integer -> Integer ;\n"
                + "export Two : Unit -> Integer = One -> Double ;\n"
                + "export Four : Unit -> Integer = Two -> Double ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().orElse( emptyList() ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 2, exported.size() );
        assertTrue( exported.containsKey( "Two" ) );
        assertTrue( exported.containsKey( "Four" ) );

        final AppFlow<Unit, Integer> two = (AppFlow<Unit, Integer>) exported.get( "Two" );
        final AppFlow<Unit, Integer> four = (AppFlow<Unit, Integer>) exported.get( "Four" );
        final Integer twoObs = syncExecute( two );
        final Integer fourObs = syncExecute( four );
        assertEquals( 2, twoObs.intValue() );
        assertEquals( 4, fourObs.intValue() );
    }

    @Test
    public void compileExportUsingNonExportAssignment() throws Exception {
        final String source =
                  "import One : Unit -> Integer ;\n"
                + "import Double : Integer -> Integer ;\n"
                + "Two : Unit -> Integer = One -> Double ;\n"
                + "export Four : Unit -> Integer = Two -> Double ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().orElse( emptyList() ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Four" ) );

        final AppFlow<Unit, Integer> four = (AppFlow<Unit, Integer>) exported.get( "Four" );
        final Integer fourObs = syncExecute( four );
        assertEquals( 4, fourObs.intValue() );
    }

    @Test
    public void compileExportWithoutExplicitType() throws Exception {
        final String source =
                  "import One : Unit -> Integer ;\n"
                + "import Double : Integer -> Integer ;\n"
                + "export Two = One -> Double ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().orElse( emptyList() ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Two" ) );

        final AppFlow<Unit, Integer> two = (AppFlow<Unit, Integer>) exported.get( "Two" );
        final Integer obs = syncExecute( two );
        assertEquals( 2, obs.intValue() );
    }

    @Test
    public void exportWithWrongTypeFails() throws Exception {
        final String source =
                  "import One : Unit -> Integer ;\n"
                + "import Double : Integer -> Integer ;\n"
                + "export Two : Unit -> String = One -> Double ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( res.failure().isPresent() );
    }

    @Test
    public void nonExportAssignmentWithWrongTypeFails() throws Exception {
        final String source =
                  "import One : Unit -> Integer ;\n"
                + "import Double : Integer -> Integer ;\n"
                + "Two : Unit -> String = One -> Double ;\n"
                + "export Whatever : Unit -> String = Two ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( res.failure().isPresent() );
    }

    @Test
    public void exportWithConfigExpression() throws Exception {
        final String source =
                  "import Int : Unit -> Integer ;\n"
                + "export Two : Unit -> Integer = Int ( num = 2 ) ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().orElse( Collections.emptyList() ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Two" ) );

        final AppFlow<Unit, Integer> flow = (AppFlow<Unit, Integer>) exported.get( "Two" );
        final Integer obs = syncExecute( flow );
        assertEquals( 2, obs.intValue() );
    }

    @Test
    public void exportWithConfigurableFlowWithDefaultValues() throws Exception {
        final String source =
                  "import Int : Unit -> Integer ;\n"
                + "export Zero : Unit -> Integer = Int ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().orElse( Collections.emptyList() ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Zero" ) );

        final AppFlow<Unit, Integer> flow = (AppFlow<Unit, Integer>) exported.get( "Zero" );
        final Integer obs = syncExecute( flow );
        assertEquals( 0, obs.intValue() );
    }

    @Ignore @Test
    public void exportConfigExpressionWithIdentifierValue() throws Exception {
        final String source =
                  "import Int : Unit -> Integer ;\n"
                + "val = 2 ;\n"
                + "export Two : Unit -> Integer = Int ( num = val ) ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Two" ) );

        final AppFlow<Unit, Integer> flow = (AppFlow<Unit, Integer>) exported.get( "Two" );
        final Integer obs = syncExecute( flow );
        assertEquals( 2, obs.intValue() );
    }

    @Test
    public void exportSimpleMapExpression() throws Exception {
        final String source =
                  "export DoubleOne : Integer -> Integer = {\n"
                + "    1 : 1 -> Double ,\n"
                + "    2 : 2 -> Double\n"
                + "} ;";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().map( l -> l.toString() ).orElse( "" ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "DoubleOne" ) );

        final AppFlow<Integer, Integer> flow = (AppFlow<Integer, Integer>) exported.get( "DoubleOne" );
        assertEquals( 2, syncExecute( flow, 1 ).intValue() );
        assertEquals( 4, syncExecute( flow, 2 ).intValue() );
    }

    @Test
    public void exportMapExpressionIdentifierPatternMatching() throws Exception {
        final String source =
                  "import Double : Integer -> Integer ;\n"
                + "export DoubleOne : Integer -> Integer = {\n"
                + "    1 : 1 -> Double ,\n"
                + "    n : n\n"
                + "} ;";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().map( l -> l.toString() ).orElse( "" ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "DoubleOne" ) );

        final AppFlow<Integer, Integer> flow = (AppFlow<Integer, Integer>) exported.get( "DoubleOne" );
        assertEquals( 2, syncExecute( flow, 1 ).intValue() );
        assertEquals( 2, syncExecute( flow, 2 ).intValue() );
        assertEquals( 3, syncExecute( flow, 3 ).intValue() );
    }

    @Test
    public void exportMapExpressionConstructorPatternMatching() throws Exception {
        final String source =
                  "import Double : Integer -> Integer ;\n"
                + "export DoubleOne : IntVal -> Integer = {\n"
                + "    IntVal ( 1 ) : 1 ,\n"
                + "    IntVal ( n ) : n -> Double\n"
                + "} ;";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().map( l -> l.toString() ).orElse( "" ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "DoubleOne" ) );

        final AppFlow<IntVal, Integer> flow = (AppFlow<IntVal, Integer>) exported.get( "DoubleOne" );
        assertEquals( 1, syncExecute( flow, new IntVal( 1 ) ).intValue() );
        assertEquals( 4, syncExecute( flow, new IntVal( 2 ) ).intValue() );
    }

    @Test
    public void mapExpressionWithComplexConstructorPattern() throws Exception {
        final String source =
                  "import Double : Integer -> Integer ;\n"
                + "export Thing : Complex -> Integer = {\n"
                + "    Complex ( IntVal ( 1 ) , true ) : 1 ,\n"
                + "    Complex ( IntVal ( n ) , true ) : n ,"
                + "    Complex ( IntVal ( 1 ) , false ) : 0 ,"
                + "    Complex ( IntVal ( n ) , false ) : n -> Double\n"
                + "} ;";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().map( l -> l.toString() ).orElse( "" ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Thing" ) );

        final AppFlow<Complex, Integer> flow = (AppFlow<Complex, Integer>) exported.get( "Thing" );
        assertEquals( 1, syncExecute( flow, new Complex( new IntVal( 1 ), true ) ).intValue() );

        assertEquals( 2, syncExecute( flow, new Complex( new IntVal( 2 ), true ) ).intValue() );
        assertEquals( 3, syncExecute( flow, new Complex( new IntVal( 3 ), true ) ).intValue() );

        assertEquals( 0, syncExecute( flow, new Complex( new IntVal( 1 ), false ) ).intValue() );

        assertEquals( 4, syncExecute( flow, new Complex( new IntVal( 2 ), false ) ).intValue() );
        assertEquals( 6, syncExecute( flow, new Complex( new IntVal( 3 ), false ) ).intValue() );
    }

    @Test
    public void exportExpressionWithInnerMapExpression() throws Exception {
        final String source =
                  "import Double : Integer -> Integer ;\n"
                + "import Int : Unit -> Integer ;\n"
                + "DoubleOne : Integer -> Integer = {\n"
                + "    1 : 1 -> Double ,\n"
                + "    2 : 0\n"
                + "} ;\n"
                + "export OneTwo : Unit -> Integer = 1 -> DoubleOne ;\n"
                + "export TwoZero : Unit -> Integer = 2 -> DoubleOne ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( "Problems: " + res.failure().map( l -> l.toString() ).orElse( "" ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 2, exported.size() );
        assertTrue( exported.containsKey( "OneTwo" ) );
        assertTrue( exported.containsKey( "TwoZero" ) );

        assertEquals( 2, syncExecute( (AppFlow<Unit, Integer>) exported.get( "OneTwo" ) ).intValue() );
        assertEquals( 0, syncExecute( (AppFlow<Unit, Integer>) exported.get( "TwoZero" ) ).intValue() );
    }

    @Test
    public void compileFlowWithConstantPart() throws Exception {
        final String source =
                  "import Double : Integer -> Integer ;\n"
                + "export Two : Unit -> Integer = 1 -> Double ;\n";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( res.failure().map( o -> o.toString() ).orElse( "" ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Two" ) );

        assertEquals( 2, syncExecute( (AppFlow<Unit, Integer>) exported.get( "Two" ) ).intValue() );
    }

    @Test
    public void compileFlowWithImportedConstantPart() throws Exception {
        final String source =
                  "import Num : Integer ;\n"
                + "export Nothing : Unit -> Unit = 1 -> toUnit ;";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( res.failure().map( o -> o.toString() ).orElse( "" ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Nothing" ) );

        assertEquals( Unit.INSTANCE, syncExecute( (AppFlow<Unit, Unit>) exported.get( "Nothing" ) ) );
    }

    @Test
    public void importWithGenericInput() throws Exception {
        final String source =
                  "import toUnit : ? -> Unit ;\n"
                + "export Two : Unit -> Integer = Num -> Double ;";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( res.failure().map( o -> o.toString() ).orElse( "" ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Two" ) );

        assertEquals( 2, syncExecute( (AppFlow<Unit, Integer>) exported.get( "Two" ) ).intValue() );
    }

    @Test
    public void tailRecursiveFlow() throws Exception {
        final String source =
                    "import Decrement : Integer -> Integer ;\n"
                  + "export Countdown : Integer -> Integer = {\n"
                  + "  0 : 0 ,\n"
                  + "  n : n -> Decrement -> Countdown\n"
                  + "} ;";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( res.failure().map( o -> o.toString() ).orElse( "" ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Countdown" ) );

        assertEquals( 0, syncExecute( (AppFlow<Integer, Integer>) exported.get( "Countdown" ), 1 ).intValue() );
        assertEquals( 0, syncExecute( (AppFlow<Integer, Integer>) exported.get( "Countdown" ), 2 ).intValue() );
        assertEquals( 0, syncExecute( (AppFlow<Integer, Integer>) exported.get( "Countdown" ), 3 ).intValue() );
    }


    @Test
    public void useIdentifierInNestedMapExpression() throws Exception {
        final String source =
                    "export Nested : Integer -> Integer = {\n"
                  + "  n : n -> {\n"
                  + "    m : n"
                  + "  }\n"
                  + "} ;";

        final Try<List<String>, Map<String, AppFlow<?, ?>>> res = compiler.compileFlows( source );
        assertTrue( res.failure().map( o -> o.toString() ).orElse( "" ), res.success().isPresent() );

        final Map<String, AppFlow<?, ?>> exported = res.success().get();
        assertEquals( 1, exported.size() );
        assertTrue( exported.containsKey( "Nested" ) );

        assertEquals( 1, syncExecute( (AppFlow<Integer, Integer>) exported.get( "Nested" ), 1 ).intValue() );
        assertEquals( 2, syncExecute( (AppFlow<Integer, Integer>) exported.get( "Nested" ), 2 ).intValue() );
        assertEquals( 3, syncExecute( (AppFlow<Integer, Integer>) exported.get( "Nested" ), 3 ).intValue() );
    }

    private <O> O syncExecute( final AppFlow<Unit, O> flow ) {
        return syncExecute( flow, Unit.INSTANCE );
    }

    private <I, O> O syncExecute( final AppFlow<I, O> flow, final I input ) {
        class Ref {
            O o;
        }
        final Ref ref = new Ref();
        executor.execute( input, flow, o -> {
            ref.o = o;
        } );

        assertNotNull( "Flow was not synchronous!", ref.o );

        return ref.o;
    }

    public static class IntVal {
        public final int n;
        public IntVal( final int n ) {
            this.n = n;
        }
    }

    public static class IntValMatcher implements PatternMatcher {

        @Override
        public int argLength() {
            return 1;
        }

        @Override
        public Object get( final Object o, final int index ) {
            if ( o instanceof IntVal ) {
                if ( index == 0 ) {
                    return ((IntVal) o).n;
                }
                else {
                    throw new IndexOutOfBoundsException();
                }
            }
            else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public boolean matches( final Object candidate ) {
            return candidate instanceof IntVal;
        }

    }

    public static class Complex {
        public final IntVal val;
        public final boolean bool;

        public Complex( final IntVal val, final boolean bool ) {
            this.val = val;
            this.bool = bool;
        }
    }

    public static class ComplexMatcher implements PatternMatcher {

        @Override
        public int argLength() {
            return 2;
        }

        @Override
        public Object get( final Object o,
                           final int index ) {
            if ( o instanceof Complex ) {
                final Complex c = (Complex) o;
                if ( index >= 0 && index < 2 ) {
                    if ( index == 0 ) {
                        return c.val;
                    }
                    else {
                        return c.bool;
                    }
                }
                else {
                    throw new IndexOutOfBoundsException();
                }
            }
            else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public boolean matches( final Object candidate ) {
            return candidate instanceof Complex;
        }

    }

}
