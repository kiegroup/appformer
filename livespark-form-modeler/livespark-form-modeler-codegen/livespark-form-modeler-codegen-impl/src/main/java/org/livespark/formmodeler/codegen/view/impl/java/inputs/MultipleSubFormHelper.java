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
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.impl.relations.MultipleSubFormFieldDefinition;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

/**
 * Created by pefernan on 4/28/15.
 */
public class MultipleSubFormHelper extends AbstractNestedModelHelper implements RequiresCustomCode<MultipleSubFormFieldDefinition> {

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
    public void addCustomCode( MultipleSubFormFieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass ) {

        JavaClassSource multipleSubformAdapter = Roaster.create( JavaClassSource.class );
        multipleSubformAdapter.addImport( List.class );

        viewClass.addImport( fieldDefinition.getStandaloneClassName() );
        viewClass.addImport( fieldDefinition.getEmbeddedModel() );
        viewClass.addImport( fieldDefinition.getEmbeddedFormView() );
        viewClass.addImport( List.class );
        viewClass.addImport( ArrayList.class );
        viewClass.addImport( MULTIPLE_SUBFORM_ClASSNAME );

        String standaloneName = cleanClassName( fieldDefinition.getStandaloneClassName() );
        String modelName = cleanClassName( fieldDefinition.getEmbeddedModel() );
        String viewName = cleanClassName( fieldDefinition.getEmbeddedFormView() );


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
                .addParameter( "List<" + fieldDefinition.getStandaloneClassName() + ">", "models" );
        modelMethod.setReturnType( "List<" + fieldDefinition.getEmbeddedModel() + ">")
                .setBody( modelMethodBody.toString() )
                .addAnnotation( Override.class );

        viewClass.addNestedType( multipleSubformAdapter );
        amendUpdateNestedModels(fieldDefinition, context, viewClass);
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
