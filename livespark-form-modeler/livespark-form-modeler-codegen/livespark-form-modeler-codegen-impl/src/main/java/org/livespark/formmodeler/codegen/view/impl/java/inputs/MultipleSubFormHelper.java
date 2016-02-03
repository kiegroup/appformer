/*
 * Copyright 2015 JBoss Inc
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

package org.livespark.formmodeler.codegen.view.impl.java.inputs;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.livespark.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGenerator;
import org.livespark.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGeneratorManager;
import org.livespark.formmodeler.editor.service.DataObjectFinderService;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.impl.relations.MultipleSubFormFieldDefinition;
import org.livespark.formmodeler.model.impl.relations.TableColumnMeta;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

/**
 * Created by pefernan on 4/28/15.
 */
public class MultipleSubFormHelper extends AbstractNestedModelHelper implements RequiresCustomCode<MultipleSubFormFieldDefinition> {

    @Inject
    private DataObjectFinderService dataObjectFinderService;

    @Inject
    private ColumnMetaGeneratorManager columnMetaGeneratorManager;

    @Override
    public String getSupportedFieldTypeCode() {
        return MultipleSubFormFieldDefinition._CODE;
    }

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getInputWidget( FieldDefinition fieldDefinition ) {
        return "org.livespark.formmodeler.rendering.client.shared.fields.MultipleSubForm";
    }

    @Override
    public String getInputInitLiteral( SourceGenerationContext context, FieldDefinition fieldDefinition ) {
        return "new MultipleSubForm( new " + WordUtils.capitalize( fieldDefinition.getName() ) + MULTIPLE_SUBFORM_ADAPTER_SUFFIX + "() );";
    }

    @Override
    public String getReadonlyMethod( String fieldName, String readonlyParam ) {
        //TODO implement this
        return "";
    }

    @Override
    public void addCustomCode( MultipleSubFormFieldDefinition field, SourceGenerationContext context, JavaClassSource viewClass ) {

        JavaClassSource multipleSubformAdapter = Roaster.create( JavaClassSource.class );
        multipleSubformAdapter.addImport( List.class );

        FormDefinition creationForm = getContextFormById( context, field.getCreationForm() );

        if ( creationForm == null ) {
            throw new RuntimeException( "Unable to find Creation form '" + field.getCreationForm() + "'." );
        }

        FormDefinition editionForm = getContextFormById( context, field.getEditionForm() );

        if ( editionForm == null ) {
            throw new RuntimeException( "Unable to find Edition form '" + field.getEditionForm() + "'." );
        }

        String creationFormModelClassName = getFormModelClassName( creationForm, context );
        String creationFormViewClassName = getFormViewClassName( creationForm, context );

        String editionFormModelClassName = getFormModelClassName( editionForm, context );
        String editionFormViewClassName = getFormViewClassName( editionForm, context );

        viewClass.addImport( field.getStandaloneClassName() );
        viewClass.addImport( creationFormModelClassName );
        viewClass.addImport( creationFormViewClassName );
        viewClass.addImport( editionFormModelClassName );
        viewClass.addImport( editionFormViewClassName );
        viewClass.addImport( List.class );
        viewClass.addImport( ArrayList.class );
        viewClass.addImport( MULTIPLE_SUBFORM_ClASSNAME );
        viewClass.addImport( COLUMN_META_CLASS_NAME );

        String standaloneName = cleanClassName( field.getStandaloneClassName() );
        String creationModelName = cleanClassName( creationFormModelClassName );
        String creationViewName = cleanClassName( creationFormViewClassName );

        String editionModelName = cleanClassName( editionFormModelClassName );
        String editionViewName = cleanClassName( editionFormViewClassName );

        multipleSubformAdapter.addImport( List.class );
        multipleSubformAdapter.addImport( ArrayList.class );

        String superType = MULTIPLE_SUBFORM_ClASSNAME + "<List<" + standaloneName + ">, "
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

        MethodSource<JavaClassSource> getEditionModelMethod = multipleSubformAdapter.addMethod()
                .setName( "getEditionFormModel" )
                .setPublic()
                .setReturnType( editionModelName )
                .setBody( "return new " + editionModelName + "( model );" );
        getEditionModelMethod.addParameter( field.getStandaloneClassName(), "model" );
        getEditionModelMethod.addAnnotation( Override.class );


        StringBuffer getCrudColumnsBody = new StringBuffer();
        getCrudColumnsBody.append( "List<ColumnMeta> " )
                .append( COLUMN_METAS_VAR_NAME )
                .append( " = new ArrayList<ColumnMeta>();" );

        DataObject dataObject = dataObjectFinderService.getDataObject( field.getStandaloneClassName(),
                context.getPath() );

        for ( TableColumnMeta meta : field.getColumnMetas() ) {
            ObjectProperty property = dataObject.getProperty( meta.getProperty() );
            if ( property != null ) {
                ColumnMetaGenerator generator = columnMetaGeneratorManager.getColumnMetaGeneratorForType( field.getStandaloneClassName() );
                if ( generator != null ) {
                    for ( String imp : generator.getImports() ) {
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
                .setReturnType( "List<ColumnMeta>" )
                .setBody( getCrudColumnsBody.toString() )
                .setPublic()
                .addAnnotation( Override.class );

        viewClass.addNestedType( multipleSubformAdapter );
        amendUpdateNestedModels(field, context, viewClass);
    }

    private void amendUpdateNestedModels(MultipleSubFormFieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass) {
        MethodSource<JavaClassSource> updateNestedModelsMethod = getUpdateNestedModelsMethod( context, viewClass );
        if ( updateNestedModelsMethod != null ) {

            viewClass.addImport( JAVA_UTIL_LIST_CLASSNAME );
            viewClass.addImport( JAVA_UTIL_ARRAYLIST_CLASSNAME );

            String body = updateNestedModelsMethod.getBody();

            String pName = fieldDefinition.getBoundPropertyName();
            String pType = fieldDefinition.getStandaloneClassName();

            String modelName = StringUtils.capitalize( fieldDefinition.getModelName() );

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
