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

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.impl.relations.SubFormFieldDefinition;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

/**
 * Created by pefernan on 4/28/15.
 */
public class SubFormHelper extends AbstractInputCreatorHelper implements RequiresCustomCode {

    @Override
    public String getSupportedFieldType() {
        return SubFormFieldDefinition.class.getName();
    }

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getInputWidget() {
        return "org.livespark.formmodeler.rendering.client.shared.fields.SubForm";
    }

    @Override
    public String getInputInitLiteral( SourceGenerationContext context, FieldDefinition fieldDefinition ) {
        return "new SubForm( new " + WordUtils.capitalize( fieldDefinition.getName() ) + SUBFORM_ADAPTER_SUFFIX + "() );";
    }

    @Override
    public void addCustomCode( FieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass ) {
        SubFormFieldDefinition subformField = ( SubFormFieldDefinition ) fieldDefinition;

        JavaClassSource subformAdapter = Roaster.create( JavaClassSource.class );

        viewClass.addImport( subformField.getStandaloneType() );
        viewClass.addImport( subformField.getEmbeddedModel() );
        viewClass.addImport( subformField.getEmbeddedFormView() );
        viewClass.addImport( SUBFORM_ClASSNAME );

        String standaloneName = cleanClassName( subformField.getStandaloneType() );
        String modelName = cleanClassName( subformField.getEmbeddedModel() );
        String viewName = cleanClassName( subformField.getEmbeddedFormView() );

        String superType = SUBFORM_ClASSNAME + "<" + standaloneName + ", " + modelName + ">";
        subformAdapter.setPublic()
                .addInterface( superType )
                .setName( WordUtils.capitalize( fieldDefinition.getName() ) + SUBFORM_ADAPTER_SUFFIX );

        subformAdapter.addMethod()
                .setPublic()
                .setReturnType( "Class<" + viewName + ">" )
                .setName( "getFormViewType" )
                .setBody( "return " + viewName + ".class;" )
                .addAnnotation( Override.class );

        MethodSource<JavaClassSource> modelMethod = subformAdapter.addMethod();
        modelMethod.setPublic()
                .setName( "getFormModelForModel" )
                .addParameter( subformField.getStandaloneType(), "model" );
        modelMethod.setReturnType( subformField.getEmbeddedModel() )
                .setBody( " return new " + modelName + "( model );")
                .addAnnotation( Override.class );

        viewClass.addNestedType( subformAdapter );
        
        amendUpdateNestedModels(subformField, context, viewClass);
    }
    
    private void amendUpdateNestedModels(SubFormFieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass) {
        MethodSource<JavaClassSource> updateNestedModelsMethod = viewClass.getMethod( "updateNestedModels",
                                                                                      boolean.class );
        if ( updateNestedModelsMethod != null ) {
            String body = updateNestedModelsMethod.getBody();

            String pName = fieldDefinition.getBoundPropertyName();
            String pType = StringUtils.capitalize( pName );

            body += pType + " " + pName + " = getModel().get" + context.getEntityName() + "().get" + pType + "();\n";
            body += "if (" + pName + " == null && init) {\n";
            body += "  " + pName + " = new " + pType + "();\n";
            body += "  getModel().get" + context.getEntityName() + "().set" + pType + "(" + pName + ");\n";
            body += "}\n";
            body += fieldDefinition.getName() + ".setValue(" + pName + ");\n";
            updateNestedModelsMethod.setBody( body );
        }
    }
   
}