/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl;

import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.*;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.kie.appformer.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGenerator;
import org.kie.appformer.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGeneratorManager;
import org.kie.workbench.common.forms.data.modeller.service.DataObjectFinderService;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.kie.workbench.common.forms.model.impl.relations.MultipleSubFormFieldDefinition;
import org.kie.workbench.common.forms.model.impl.relations.TableColumnMeta;

public class MultipleSubFormHelper extends AbstractNestedModelHelper<MultipleSubFormFieldDefinition> implements RequiresCustomCode<MultipleSubFormFieldDefinition> {

    private final DataObjectFinderService dataObjectFinderService;

    private final ColumnMetaGeneratorManager columnMetaGeneratorManager;

    @Inject
    public MultipleSubFormHelper( final DataObjectFinderService dataObjectFinderService, final ColumnMetaGeneratorManager columnMetaGeneratorManager ) {
        this.dataObjectFinderService = dataObjectFinderService;
        this.columnMetaGeneratorManager = columnMetaGeneratorManager;
    }

    @Override
    public String getSupportedFieldTypeCode() {
        return MultipleSubFormFieldDefinition.CODE;
    }

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getInputWidget( final MultipleSubFormFieldDefinition fieldDefinition ) {
        return "org.kie.appformer.formmodeler.rendering.client.shared.fields.MultipleSubForm";
    }

    @Override
    public String getInputInitLiteral( final SourceGenerationContext context, final MultipleSubFormFieldDefinition fieldDefinition ) {
        return "new MultipleSubForm( new " + WordUtils.capitalize( fieldDefinition.getName() ) + MULTIPLE_SUBFORM_ADAPTER_SUFFIX + "() );";
    }

    @Override
    public String getReadonlyMethod( final String fieldName, final String readonlyParam ) {
        //TODO implement this
        return "";
    }

    @Override
    public void addCustomCode( final MultipleSubFormFieldDefinition field, final SourceGenerationContext context, final JavaClassSource viewClass ) {

        final JavaClassSource multipleSubformAdapter = Roaster.create( JavaClassSource.class );
        multipleSubformAdapter.addImport( List.class );

        final FormDefinition creationForm = getContextFormById( context, field.getCreationForm() );

        if ( creationForm == null ) {
            throw new RuntimeException( "Unable to find Creation form '" + field.getCreationForm() + "'." );
        }

        final FormDefinition editionForm = getContextFormById( context, field.getEditionForm() );

        if ( editionForm == null ) {
            throw new RuntimeException( "Unable to find Edition form '" + field.getEditionForm() + "'." );
        }

        final String creationFormModelClassName = getFormModelClassName( creationForm, context );
        final String creationFormViewClassName = getFormViewClassName( creationForm, context );

        final String editionFormModelClassName = getFormModelClassName( editionForm, context );
        final String editionFormViewClassName = getFormViewClassName( editionForm, context );

        viewClass.addImport( field.getStandaloneClassName() );
        viewClass.addImport( creationFormModelClassName );
        viewClass.addImport( creationFormViewClassName );
        viewClass.addImport( editionFormModelClassName );
        viewClass.addImport( editionFormViewClassName );
        viewClass.addImport( List.class );
        viewClass.addImport( ArrayList.class );
        viewClass.addImport( MULTIPLE_SUBFORM_ClASSNAME );
        viewClass.addImport( COLUMN_META_CLASS_NAME );

        final String standaloneName = cleanClassName( field.getStandaloneClassName() );
        final String creationModelName = cleanClassName( creationFormModelClassName );
        final String creationViewName = cleanClassName( creationFormViewClassName );

        final String editionModelName = cleanClassName( editionFormModelClassName );
        final String editionViewName = cleanClassName( editionFormViewClassName );

        multipleSubformAdapter.addImport( List.class );
        multipleSubformAdapter.addImport( ArrayList.class );

        final String superType = MULTIPLE_SUBFORM_ClASSNAME + "<List<" + standaloneName + ">, "
                + standaloneName + " , " + creationModelName + " , " + editionModelName + ">";

        multipleSubformAdapter.setPublic()
                .addInterface( superType )
                .setName( WordUtils.capitalize( field.getName() ) + MULTIPLE_SUBFORM_ADAPTER_SUFFIX );

        multipleSubformAdapter.addMethod()
                .setName( "getCreationForm" )
                .setPublic()
                .setReturnType( "Class<" + creationViewName + ">" )
                .setBody( "return " + creationViewName + ".class;" )
                .addAnnotation( Override.class );

        multipleSubformAdapter.addMethod()
                .setName( "getEditionForm" )
                .setPublic()
                .setReturnType( "Class<" + editionViewName + ">" )
                .setBody( "return " + editionViewName + ".class;" )
                .addAnnotation( Override.class );

        final MethodSource<JavaClassSource> getEditionModelMethod = multipleSubformAdapter.addMethod()
                .setName( "getEditionFormModel" )
                .setPublic()
                .setReturnType( editionModelName )
                .setBody( "return new " + editionModelName + "( model );" );
        getEditionModelMethod.addParameter( field.getStandaloneClassName(), "model" );
        getEditionModelMethod.addAnnotation( Override.class );


        final String returnType = "List<ColumnMeta<" + standaloneName + ">>";
        final StringBuffer getCrudColumnsBody = new StringBuffer();
        getCrudColumnsBody
            .append( returnType )
            .append( " " )
            .append( COLUMN_METAS_VAR_NAME )
            .append( " = new ArrayList<>();" );

        final DataObject dataObject = dataObjectFinderService.getDataObject( field.getStandaloneClassName(),
                context.getPath() );

        for ( final TableColumnMeta meta : field.getColumnMetas() ) {
            final ObjectProperty property = dataObject.getProperty( meta.getProperty() );
            if ( property != null ) {
                final ColumnMetaGenerator generator = columnMetaGeneratorManager.getColumnMetaGeneratorForType( field.getStandaloneClassName() );
                if ( generator != null ) {
                    for ( final String imp : generator.getImports() ) {
                        viewClass.addImport( imp );
                    }
                    getCrudColumnsBody.append( generator.generateColumnMeta( meta.getProperty(),
                            meta.getLabel(), cleanClassName( field.getStandaloneClassName() ), context ) );
                }
            }
        }

        getCrudColumnsBody.append( "return " )
                .append( COLUMN_METAS_VAR_NAME )
                .append( ";" );

        multipleSubformAdapter.addMethod()
                .setName( "getCrudColumns" )
                .setReturnType( returnType )
                .setBody( getCrudColumnsBody.toString() )
                .setPublic()
                .addAnnotation( Override.class );

        viewClass.addNestedType( multipleSubformAdapter );
        amendUpdateNestedModels(field, context, viewClass);
    }

    private void amendUpdateNestedModels(final MultipleSubFormFieldDefinition fieldDefinition, final SourceGenerationContext context, final JavaClassSource viewClass) {
        final MethodSource<JavaClassSource> updateNestedModelsMethod = getUpdateNestedModelsMethod( context, viewClass );
        if ( updateNestedModelsMethod != null ) {

            viewClass.addImport( JAVA_UTIL_LIST_CLASSNAME );
            viewClass.addImport( JAVA_UTIL_ARRAYLIST_CLASSNAME );

            String body = updateNestedModelsMethod.getBody();

            final String pName = fieldDefinition.getBinding();
            final String pType = fieldDefinition.getStandaloneClassName();

            final String modelName = StringUtils.capitalize( context.getFormDefinition().getModel().getName() );

            body += "List " + pName + " = getModel().get" + modelName + "().get" + StringUtils.capitalize( pName ) + "();\n";
            body += "if (" + pName + " == null && init) {\n";
            body += "  " + pName + " = new ArrayList<" + pType + ">();\n";
            body += "  getModel().get" + modelName + "().set" + StringUtils.capitalize( pName ) + "(" + pName + ");\n";
            body += "}\n";
            body += fieldDefinition.getName() + ".setModel(" + pName + ");\n";
            updateNestedModelsMethod.setBody( body );
        }
    }
}
