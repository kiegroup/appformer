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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.impl.relations.SubFormFieldDefinition;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

/**
 * Created by pefernan on 4/28/15.
 */
public class SubFormHelper extends AbstractNestedModelHelper implements RequiresCustomCode {

    @Override
    public String getSupportedFieldTypeCode() {
        return SubFormFieldDefinition._CODE;
    }

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getInputWidget( FieldDefinition fieldDefinition ) {
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

        viewClass.addImport( subformField.getStandaloneClassName() );

        FormDefinition nestedForm = getContextFormById( context, subformField.getNestedForm() );

        if ( nestedForm == null ) {
            throw new RuntimeException( "Unable to find form '" + subformField.getNestedForm() + "'." );
        }

        String formModelClassName = getFormModelClassName( nestedForm, context );
        String formViewClassName = getFormViewClassName( nestedForm, context );

        viewClass.addImport( formModelClassName );
        viewClass.addImport( formViewClassName );
        viewClass.addImport( SUBFORM_ClASSNAME );

        String standaloneName = cleanClassName( subformField.getStandaloneClassName() );
        String modelName = cleanClassName( formModelClassName );
        String viewName = cleanClassName( formViewClassName );

        String superType = SUBFORM_ClASSNAME + "<" + standaloneName + ", " + modelName + ">";
        subformAdapter.setPublic()
                .addInterface( superType )
                .setName( WordUtils.capitalize( fieldDefinition.getName() ) + SUBFORM_ADAPTER_SUFFIX );

        subformAdapter.addMethod()
                .setPublic()
                .setReturnType( "Class<" + viewName + ">" )
                .setName("getFormViewType")
                .setBody("return " + viewName + ".class;")
                .addAnnotation(Override.class);

        MethodSource<JavaClassSource> modelMethod = subformAdapter.addMethod();
        modelMethod.setPublic()
                .setName("getFormModelForModel")
                .addParameter( subformField.getStandaloneClassName(), "model");

        modelMethod.setReturnType( modelName )
                .setBody( " return new " + modelName + "( model );" )
                .addAnnotation( Override.class );

        viewClass.addNestedType(subformAdapter);

        amendUpdateNestedModels(subformField, context, viewClass);

        amendValidateNestedModels( subformField, context, viewClass );
    }

    private void amendValidateNestedModels(SubFormFieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass) {

        MethodSource<JavaClassSource> updateNestedModelsMethod = viewClass.getMethod( DO_EXTRA_VALIDATIONS_METHOD,
                boolean.class );
        if ( updateNestedModelsMethod != null ) {
            String body = updateNestedModelsMethod.getBody();

            StringBuffer validText = new StringBuffer();
            validText.append(";")
                    .append( "if ( !" )
                    .append( fieldDefinition.getName())
                    .append(".validate() && valid) {valid=false;}");

            body = body.replaceFirst(";", validText.toString());
            updateNestedModelsMethod.setBody( body );
        }
    }

    private void amendUpdateNestedModels(SubFormFieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass) {
        MethodSource<JavaClassSource> updateNestedModelsMethod = getUpdateNestedModelsMethod( context, viewClass );
        if ( updateNestedModelsMethod != null ) {
            String body = updateNestedModelsMethod.getBody();

            String pName = fieldDefinition.getBoundPropertyName();
            String pType = fieldDefinition.getStandaloneClassName();
            String modelName = StringUtils.capitalize( fieldDefinition.getModelName() );

            body += pType + " " + pName + " = getModel().get" + modelName + "().get" + StringUtils.capitalize( pName ) + "();\n";
            body += "if (" + pName + " == null && init) {\n";
            body += "  " + pName + " = new " + pType + "();\n";
            body += "  getModel().get" + modelName + "().set" + StringUtils.capitalize( pName ) + "(" + pName + ");\n";
            body += "}\n";
            body += fieldDefinition.getName() + ".setModel(" + pName + ");\n";
            updateNestedModelsMethod.setBody( body );
        }
    }

}
