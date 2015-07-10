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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.impl.relations.MultipleSubFormFieldDefinition;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

/**
 * Created by pefernan on 4/28/15.
 */
public class MultipleSubFormHelper extends AbstractInputCreatorHelper implements RequiresCustomCode {

    @Override
    public String getSupportedFieldType() {
        return MultipleSubFormFieldDefinition.class.getName();
    }

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getInputWidget() {
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
    public void addCustomCode( FieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass ) {
        MultipleSubFormFieldDefinition subformField = ( MultipleSubFormFieldDefinition ) fieldDefinition;

        JavaClassSource multipleSubformAdapter = Roaster.create( JavaClassSource.class );
        multipleSubformAdapter.addImport( List.class );

        viewClass.addImport( subformField.getStandaloneType() );
        viewClass.addImport( subformField.getEmbeddedModel() );
        viewClass.addImport( subformField.getEmbeddedFormView() );
        viewClass.addImport( List.class );
        viewClass.addImport( ArrayList.class );
        viewClass.addImport( MULTIPLE_SUBFORM_ClASSNAME );

        String standaloneName = cleanClassName( subformField.getStandaloneType() );
        String modelName = cleanClassName( subformField.getEmbeddedModel() );
        String viewName = cleanClassName( subformField.getEmbeddedFormView() );


        multipleSubformAdapter.addImport( List.class );
        multipleSubformAdapter.addImport( ArrayList.class );

        String superType = MULTIPLE_SUBFORM_ClASSNAME + "<List<" + standaloneName + ">, " + modelName + ">";
        multipleSubformAdapter.setPublic()
                .addInterface( superType )
                .setName( WordUtils.capitalize( fieldDefinition.getName() ) + MULTIPLE_SUBFORM_ADAPTER_SUFFIX );

        multipleSubformAdapter.addMethod()
                .setPublic()
                .setReturnType( "Class<" + viewName + ">" )
                .setName( "getListViewType" )
                .setBody( "return " + viewName + ".class;" )
                .addAnnotation( Override.class );

        StringBuffer modelMethodBody = new StringBuffer(  );
        modelMethodBody.append( "List<" ).append( modelName ).append( "> result = new ArrayList<" ).append( modelName ).append( ">();" );
        modelMethodBody.append( "if ( models != null ) {" );
        modelMethodBody.append( "for( " ).append( standaloneName ).append( " model : models ) {" );
        modelMethodBody.append( "result.add( new " ).append( modelName ).append( "( model ) );" );
        modelMethodBody.append( "}" );
        modelMethodBody.append( "}" );
        modelMethodBody.append( "return result;" );

        MethodSource<JavaClassSource> modelMethod = multipleSubformAdapter.addMethod();
        modelMethod.setPublic()
                .setName( "getListModelsForModel" )
                .addParameter( "List<" + subformField.getStandaloneType() + ">", "models" );
        modelMethod.setReturnType( "List<" + subformField.getEmbeddedModel() + ">")
                .setBody( modelMethodBody.toString() )
                .addAnnotation( Override.class );

        viewClass.addNestedType( multipleSubformAdapter );
        amendUpdateNestedModels(subformField, context, viewClass);
    }

    private void amendUpdateNestedModels(MultipleSubFormFieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass) {
        MethodSource<JavaClassSource> updateNestedModelsMethod = viewClass.getMethod( "updateNestedModels", boolean.class );
        if ( updateNestedModelsMethod != null ) {

            viewClass.addImport( JAVA_UTIL_LIST );
            viewClass.addImport( JAVA_UTIL_ARRAYLIST );

            String body = updateNestedModelsMethod.getBody();

            String pName = fieldDefinition.getBoundPropertyName();
            String pType = fieldDefinition.getStandaloneType();

            body += "List " + pName + " = getModel().get" + StringUtils.capitalize( fieldDefinition.getModelName() ) + "().get" + StringUtils.capitalize( pName ) + "();\n";
            body += "if (" + pName + " == null && init) {\n";
            body += "  " + pName + " = new ArrayList<" + pType + ">();\n";
            body += "  getModel().get" + context.getEntityName() + "().set" + StringUtils.capitalize( pName ) + "(" + pName + ");\n";
            body += "}\n";
            body += fieldDefinition.getName() + ".setModel(" + pName + ");\n";
            updateNestedModelsMethod.setBody( body );
        }
    }
}
