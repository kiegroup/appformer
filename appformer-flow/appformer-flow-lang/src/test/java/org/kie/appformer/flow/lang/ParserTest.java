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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.kie.appformer.flow.lang.AST.assignment;
import static org.kie.appformer.flow.lang.AST.config;
import static org.kie.appformer.flow.lang.AST.constructorPattern;
import static org.kie.appformer.flow.lang.AST.export;
import static org.kie.appformer.flow.lang.AST.flowExpression;
import static org.kie.appformer.flow.lang.AST.flowType;
import static org.kie.appformer.flow.lang.AST.identifier;
import static org.kie.appformer.flow.lang.AST.importFlow;
import static org.kie.appformer.flow.lang.AST.importValue;
import static org.kie.appformer.flow.lang.AST.literal;
import static org.kie.appformer.flow.lang.AST.mapping;
import static org.kie.appformer.flow.lang.AST.simpleAssignment;
import static org.kie.appformer.flow.lang.AST.simpleExport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kie.appformer.flow.lang.AST.ConfigExpression;
import org.kie.appformer.flow.lang.AST.Expression;
import org.kie.appformer.flow.lang.AST.Identifier;
import org.kie.appformer.flow.lang.AST.MatchableExpression;
import org.kie.appformer.flow.lang.AST.SimpleExpression;
import org.kie.appformer.flow.lang.AST.Statement;

@RunWith( JUnit4.class )
public class ParserTest {

    Parser parser;

    @Before
    public void setup() {
        parser = new Parser();
    }

    @Test
    public void parseImportFlowStmt() throws Exception {
        final String source = "import Flow : Input -> Output ;";
        final List<Statement> stmts = parser.parse( source );

        assertEquals( asList( importFlow( "Flow", "Input", "Output" ) ), stmts );
    }

    @Test
    public void parseSimpleAssignment() throws Exception {
        final String source = "Flow1 = Flow2 ;";
        final List<Statement> stmts = parser.parse( source );

        assertEquals( asList( simpleAssignment( "Flow1", "Flow2" ) ), stmts );
    }

    @Test
    public void parseSimpleExport() throws Exception {
        final String source = "export Flow1 = Flow2 ;";
        final List<Statement> stmts = parser.parse( source );

        assertEquals( asList( simpleExport( "Flow1", "Flow2" ) ), stmts );
    }

    @Test
    public void parseSimpleExportWithType() throws Exception {
        final String source = "export Flow1 : Input -> Output = Flow2 ;";
        final List<Statement> stmts = parser.parse( source );

        assertEquals( asList( export( assignment( "Flow1", flowType( "Input", "Output" ), identifier( "Flow2" ) ) ) ), stmts );
    }

    @Test
    public void parseSimpleAssignmentWithType() throws Exception {
        final String source = "Flow1 : Input -> Output = Flow2 ;";
        final List<Statement> stmts = parser.parse( source );

        assertEquals( asList( simpleAssignment( "Flow1", "Input", "Output", "Flow2" ) ), stmts );
    }

    @Test
    public void parseConfigAssignment() throws Exception {
        final String source = "Flow1 = Config ( prop1 = true , prop2 = 2 , prop3 = id ) ;";
        final List<Statement> stmts = parser.parse( source );

        final Map<Identifier, SimpleExpression> configMap = new LinkedHashMap<>();
        configMap.put( identifier( "prop1" ), literal( "true" ) );
        configMap.put( identifier( "prop2" ), literal( "2" ) );
        configMap.put( identifier( "prop3" ), identifier( "id" ) );

        assertEquals( asList( assignment( "Flow1", config( "Config", configMap ) ) ), stmts );
    }

    @Test
    public void parseConfigAssignmentWithType() throws Exception {
        final String source = "Flow1 : Input -> Output = Config ( prop1 = true , prop2 = 2 , prop3 = id ) ;";
        final List<Statement> stmts = parser.parse( source );

        final Map<Identifier, SimpleExpression> configMap = new LinkedHashMap<>();
        configMap.put( identifier( "prop1" ), literal( "true" ) );
        configMap.put( identifier( "prop2" ), literal( "2" ) );
        configMap.put( identifier( "prop3" ), identifier( "id" ) );

        assertEquals( asList( assignment( "Flow1",
                                                 flowType( "Input",
                                                                   "Output" ),
                                                 config( "Config",
                                                         configMap ) ) ),
                      stmts );
    }

    @Test
    public void parseMappingAssignment() throws Exception {
        final String source = "Flow = { 1 : Flow1 , 2 : Flow2 } ;";
        final List<Statement> stmts = parser.parse( source );

        final LinkedHashMap<MatchableExpression, Expression> mappings = new LinkedHashMap<>();
        mappings.put( literal( "1" ), identifier( "Flow1" ) );
        mappings.put( literal( "2" ), identifier( "Flow2" ) );

        assertEquals( asList( assignment( "Flow", mapping( mappings ) ) ), stmts );
    }

    @Test
    public void parseMappingAssignmentWithType() throws Exception {
        final String source = "Flow : Input -> Output = { 1 : Flow1 , 2 : Flow2 } ;";
        final List<Statement> stmts = parser.parse( source );

        final LinkedHashMap<MatchableExpression, Expression> mappings = new LinkedHashMap<>();
        mappings.put( literal( "1" ), identifier( "Flow1" ) );
        mappings.put( literal( "2" ), identifier( "Flow2" ) );

        assertEquals( asList( assignment( "Flow", flowType( "Input", "Output" ), mapping( mappings ) ) ), stmts );
    }

    @Test
    public void parseComplexAssignment() throws Exception {
        final String source = "Flow1 = Flow2 -> Config ( prop1 = true , prop2 = 2 , prop3 = id ) -> { 1 : Flow1 , 2 : Flow2 } ;";
        final List<Statement> stmts = parser.parse( source );

        final Map<Identifier, SimpleExpression> configMap = new LinkedHashMap<>();
        configMap.put( identifier( "prop1" ), literal( "true" ) );
        configMap.put( identifier( "prop2" ), literal( "2" ) );
        configMap.put( identifier( "prop3" ), identifier( "id" ) );
        final ConfigExpression config = config( "Config", configMap );

        final LinkedHashMap<MatchableExpression, Expression> mappings = new LinkedHashMap<>();
        mappings.put( literal( "1" ), identifier( "Flow1" ) );
        mappings.put( literal( "2" ), identifier( "Flow2" ) );

        assertEquals( asList( assignment( "Flow1", flowExpression( identifier( "Flow2" ), config, mapping( mappings ) ) ) ), stmts );
    }

    @Test
    public void parseMultipleStatements() throws Exception {
        final String source =
                  "import ConfigFlow : String -> Input ;\n"
                + "import PlainFlow : Input -> Output ;\n"
                + "\n"
                + "export Main : String -> Output = ConfigFlow ( awesome = true ) -> PlainFlow ;\n";
        final List<Statement> stmts = parser.parse( source );

        final List<Statement> expected = new ArrayList<>();
        expected.add( importFlow( "ConfigFlow", "String", "Input" ) );
        expected.add( importFlow( "PlainFlow", "Input", "Output" ) );
        final Map<Identifier, SimpleExpression> config = new LinkedHashMap<>();
        config.put( identifier( "awesome" ), literal( "true" ) );
        expected.add( export( assignment( "Main",
                                          flowType( "String",
                                                    "Output" ),
                                          flowExpression( config( "ConfigFlow",
                                                                  config ),
                                                          identifier( "PlainFlow" ) ) ) ) );
        assertEquals( expected, stmts );
    }

    @Test
    public void parseLiteralAsFlowPart() throws Exception {
        final String source =
                  "import Double : Integer -> Integer ;\n"
                + "export Two : Unit -> Integer = 1 -> Double ;";

        final List<Statement> stmts = parser.parse( source );

        final List<Statement> expected = new ArrayList<>();
        expected.add( importFlow( "Double", "Integer", "Integer" ) );
        expected.add( export( assignment( "Two", flowType( "Unit", "Integer" ), flowExpression( literal( "1" ), identifier( "Double" ) ) ) ) );

        assertEquals( expected, stmts );
    }

    @Test
    public void parseImportOfConstant() throws Exception {
        final String source = "import CREATE : CrudOperation ;\n";
        final List<Statement> stmts = parser.parse( source );

        assertEquals( singletonList( importValue( "CREATE", "CrudOperation" ) ), stmts );
    }

    @Test
    public void parsePatternMatching() throws Exception {
        final String source =
                  "flow : IntVal -> Integer = {\n"
                + "  IntVal ( 1 ) : 1 ,\n"
                + "  IntVal ( n ) : n\n"
                + "} ;\n";
        final List<Statement> stmts = parser.parse( source );

        final LinkedHashMap<MatchableExpression, Expression> mappings = new LinkedHashMap<>();
        mappings.put( constructorPattern( "IntVal", literal( "1" ) ), literal( "1" ) );
        mappings.put( constructorPattern( "IntVal", identifier( "n" ) ), identifier( "n" ) );

        assertEquals( singletonList( assignment( "flow", flowType( "IntVal", "Integer" ), mapping( mappings ) ) ), stmts );
    }

    @Test
    public void parseExportWithLeadingWhiteSpace() throws Exception {
        final String source =
                  "\n"
                + "export Main : Unit -> Unit = ??? ;\n";

        final List<Statement> stmts = parser.parse( source );

        assertEquals( singletonList( export( assignment( "Main", flowType( "Unit", "Unit" ), identifier( "???" ) ) ) ), stmts );
    }
}
