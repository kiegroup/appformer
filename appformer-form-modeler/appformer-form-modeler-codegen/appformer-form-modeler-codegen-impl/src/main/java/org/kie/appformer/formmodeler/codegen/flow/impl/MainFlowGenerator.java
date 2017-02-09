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


package org.kie.appformer.formmodeler.codegen.flow.impl;

import static org.kie.appformer.flow.lang.AST.assignment;
import static org.kie.appformer.flow.lang.AST.constructorPattern;
import static org.kie.appformer.flow.lang.AST.export;
import static org.kie.appformer.flow.lang.AST.flowExpression;
import static org.kie.appformer.flow.lang.AST.flowType;
import static org.kie.appformer.flow.lang.AST.identifier;
import static org.kie.appformer.flow.lang.AST.importFlow;
import static org.kie.appformer.flow.lang.AST.mapping;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import org.kie.appformer.flow.lang.AST.Assignment;
import org.kie.appformer.flow.lang.AST.ExportFlow;
import org.kie.appformer.flow.lang.AST.Expression;
import org.kie.appformer.flow.lang.AST.Identifier;
import org.kie.appformer.flow.lang.AST.ImportIdentifier;
import org.kie.appformer.flow.lang.AST.MatchableExpression;
import org.kie.appformer.flow.lang.AST.Statement;
import org.kie.appformer.flow.lang.Parser;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.flow.FlowLangSourceGenerator;

/**
 * <p>
 * Generatess a flow-lang source file for defining application flows form AppFormer generated
 * components.
 *
 * <p>
 * The generated flow is meant to be executed on the client of a generated AppFormer application.
 */
@ApplicationScoped
public class MainFlowGenerator implements FlowLangSourceGenerator {

    private final Parser parser = new Parser();

    /**
     * <p>
     * Generates imports for flows and values not belonging to any particular entity.
     */
    @Override
    public String generateInitialFlowSource( final SourceGenerationContext context ) {
        return    "\n"
                + "import CREATE : CrudOperation ;\n"
                + "import UPDATE : CrudOperation ;\n"
                + "import DELETE : CrudOperation ;\n"
                + "\n"
                + "import toUnit : ? -> Unit ;\n"
                + "import unit : Unit ;\n"
                + "\n";
    }

    /**
     * <p>
     * Update source adding imports and some sample flow definitions for a generated entity. ,
     * <p>
     * If no {@code Main} flow is exported, the updated source will contain an exported definition
     * for {@code Main} that is a simple CRUD application flow (listView with options to create,
     * update, and delete).
     */
    @Override
    public Optional<String> updateSource( final SourceGenerationContext context, final String original ) {
        final List<Statement> parsed = parser.parse( original );

        final StringBuilder sourceBuilder = new StringBuilder();

        final Set<String> existingImports = parsed
                .stream()
                .filter( stmt -> stmt instanceof ImportIdentifier )
                .map( stmt -> (ImportIdentifier) stmt )
                .map( stmt -> stmt.id.value )
                .collect( Collectors.toSet() );

        final Set<String> existingAssignments = parsed
            .stream()
            .filter( stmt -> stmt instanceof Assignment || stmt instanceof ExportFlow )
            .map( stmt -> (stmt instanceof ExportFlow ? ((ExportFlow) stmt).assignment : (Assignment) stmt) )
            .map( stmt -> stmt.assignedId.value )
            .collect( Collectors.toSet() );

        final Stream<ImportIdentifier> newImports = generateImportsFromContext( context );
        newImports
            .filter( stmt -> !existingImports.contains( stmt.id.value ) )
            .forEach( stmt -> sourceBuilder.append( stmt.toString() ).append( '\n' ) );

        sourceBuilder.append( original );

        final Stream<Assignment> newAssignments = generateAssignmentsFromContext( context );
        newAssignments
            .filter( stmt -> !existingAssignments.contains( stmt.assignedId.value ) )
            .forEach( stmt -> sourceBuilder.append( stmt.toString() ).append( '\n' ) );

        if ( !existingAssignments.contains( "Main" ) ) {
            sourceBuilder.append( createMainExport( context ) ).append( '\n' );
        }

        if ( sourceBuilder.length() > original.length() ) {
            return Optional.of( sourceBuilder.toString() );
        }
        else {
            return Optional.empty();
        }
    }

    private ExportFlow createMainExport( final SourceGenerationContext context ) {
        return export( assignment( "Main",
                            flowType( "Unit", "Unit" ),
                            flowExpression( identifier( context.getEntityName() + "Crud" ), identifier( "Main" ) ) ) );
    }

    private Stream<Assignment> generateAssignmentsFromContext( final SourceGenerationContext context ) {
        final String entity = context.getEntityName();
        final String optionalOfFormModel = "Optional<" + entity + "FormModel>";
        final String entityCrud = entity + "Crud";

        final Identifier createOp = identifier( "CREATE" );
        final Identifier updateOp = identifier( "UPDATE" );
        final Identifier deleteOp = identifier( "DELETE" );

        final Identifier entityToFormModel = identifier( entity + "To" + entity + "FormModel" );
        final Identifier formModelToEntity = identifier( entity + "FormModelTo" + entity );

        final Identifier loadEntities = identifier( "Load" + entity );
        final Identifier saveEntity = identifier( "Save" + entity );
        final Identifier updateEntity = identifier( "Update" + entity );
        final Identifier deleteEntity = identifier( "Delete" + entity );
        final Identifier listView = identifier( entity + "ListView" );
        final Identifier formView = identifier( entity + "FormView" );

        final Identifier unboundEntity = identifier( entity.toLowerCase() );
        final Identifier unboundFormModel = identifier( entity.toLowerCase() + "FormModel" );
        final Identifier maybeSaveId = identifier( "MaybeSave" + entity );
        final Identifier maybeUpdateId = identifier( "MaybeUpdate" + entity );

        final Identifier toUnit = identifier( "toUnit" );
        return Stream.of(
                         assignment( entityCrud,
                                     flowType( "Unit", "Unit" ),
                                     flowExpression( loadEntities,
                                                     listView,
                                                     mapping( pairs(
                                                                    pair( constructorPattern( "Command", createOp, unboundEntity ),
                                                                          flowExpression( unboundEntity, entityToFormModel, formView, maybeSaveId ) ),
                                                                    pair( constructorPattern( "Command", updateOp, unboundEntity ),
                                                                          flowExpression( unboundEntity, entityToFormModel, formView, maybeUpdateId ) ),
                                                                    pair( constructorPattern( "Command", deleteOp, unboundEntity ),
                                                                          flowExpression( unboundEntity, deleteEntity, toUnit ) ) ) ) ) ),

                         assignment( maybeSaveId.value,
                                     flowType( optionalOfFormModel, "Unit" ),
                                     mapping( pairs(
                                                    pair( constructorPattern( "Some", unboundFormModel ),
                                                          flowExpression( unboundFormModel, formModelToEntity, saveEntity, toUnit ) ),
                                                    pair( identifier( "None" ),
                                                          identifier( "unit" ) ) ) ) ),

                         assignment( maybeUpdateId.value,
                                     flowType( optionalOfFormModel, "Unit" ),
                                     mapping( pairs(
                                                    pair( constructorPattern( "Some", unboundFormModel ),
                                                          flowExpression( unboundFormModel, formModelToEntity, updateEntity, toUnit ) ),
                                                    pair( identifier( "None" ),
                                                          identifier( "unit" ) ) ) ) )

                         );
    }

    private static LinkedHashMap<MatchableExpression, Expression> pairs( final Mapping... pairs ) {
        final LinkedHashMap<MatchableExpression, Expression> retVal = new LinkedHashMap<>();
        Arrays.stream( pairs ).forEach( pair -> retVal.put( pair.key, pair.value ) );

        return retVal;
    }

    private static Mapping pair( final MatchableExpression key, final Expression exp ) {
        return new Mapping( key, exp );
    }

    private Stream<ImportIdentifier> generateImportsFromContext(final SourceGenerationContext context) {
        final String entity = context.getEntityName();
        final String formModel = context.getFormModelName();
        final String flowDataProvider = "FlowDataProvider<" + entity + ">";
        final String unit = "Unit";

        return Stream.of(
                          importFlow( "Save" + entity, entity, entity ),
                          importFlow( "Update" + entity, entity, entity ),
                          importFlow( "Delete" + entity, entity, entity ),
                          importFlow( "Load" + entity, unit, flowDataProvider ),

                          importFlow( entity + "To" + formModel, entity, formModel ),
                          importFlow( formModel + "To" + entity, formModel, entity ),
                          importFlow( "New" + entity, "Unit", entity ),
                          importFlow( "New" + formModel, "Unit", formModel ),

                          importFlow( entity + "ListView", flowDataProvider, "Command<CrudOperation," + entity + ">" ),
                          importFlow( entity + "FormView", formModel, "Optional<" + formModel + ">" )
                          );
    }

    private static class Mapping {
        final MatchableExpression key;
        final Expression value;
        Mapping( final MatchableExpression key, final Expression value ) {
            this.key = key;
            this.value = value;
        }
    }
}
