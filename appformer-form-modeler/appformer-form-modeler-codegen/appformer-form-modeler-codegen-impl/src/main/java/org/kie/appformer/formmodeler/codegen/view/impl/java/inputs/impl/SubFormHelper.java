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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.subForm.definition.SubFormFieldDefinition;
import org.kie.workbench.common.forms.model.FormDefinition;

public class SubFormHelper extends AbstractNestedModelHelper<SubFormFieldDefinition> implements RequiresCustomCode<SubFormFieldDefinition> {

    @Override
    public String getSupportedFieldTypeCode() {
        return SubFormFieldDefinition.FIELD_TYPE.getTypeName();
    }

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getInputWidget( final SubFormFieldDefinition fieldDefinition ) {
        return "org.kie.appformer.formmodeler.rendering.client.shared.fields.SubForm";
    }

    @Override
    public String getInputInitLiteral( final SourceGenerationContext context, final SubFormFieldDefinition fieldDefinition ) {
        return "new SubForm( new " + WordUtils.capitalize( fieldDefinition.getName() ) + SUBFORM_ADAPTER_SUFFIX + "() );";
    }

    @Override
    public void addCustomCode( final SubFormFieldDefinition fieldDefinition, final SourceGenerationContext context, final JavaClassSource viewClass ) {

        final JavaClassSource subformAdapter = Roaster.create( JavaClassSource.class );

        viewClass.addImport( fieldDefinition.getStandaloneClassName() );

        final FormDefinition nestedForm = getContextFormById( context, fieldDefinition.getNestedForm() );

        if ( nestedForm == null ) {
            throw new RuntimeException( "Unable to find form '" + fieldDefinition.getNestedForm() + "'." );
        }

        final String formModelClassName = getFormModelClassName( nestedForm, context );
        final String formViewClassName = getFormViewClassName( nestedForm, context );

        viewClass.addImport( formModelClassName );
        viewClass.addImport( formViewClassName );
        viewClass.addImport( SUBFORM_ClASSNAME );

        final String standaloneName = cleanClassName( fieldDefinition.getStandaloneClassName() );
        final String modelName = cleanClassName( formModelClassName );
        final String viewName = cleanClassName( formViewClassName );

        final String superType = SUBFORM_ClASSNAME + "<" + standaloneName + ", " + modelName + ">";
        subformAdapter.setPublic()
                .addInterface( superType )
                .setName( WordUtils.capitalize( fieldDefinition.getName() ) + SUBFORM_ADAPTER_SUFFIX );

        subformAdapter.addMethod()
                .setPublic()
                .setReturnType( "Class<" + viewName + ">" )
                .setName("getFormViewType")
                .setBody("return " + viewName + ".class;")
                .addAnnotation(Override.class);

        final MethodSource<JavaClassSource> modelMethod = subformAdapter.addMethod();
        modelMethod.setPublic()
                .setName("getFormModelForModel")
                .addParameter( fieldDefinition.getStandaloneClassName(), "model");

        modelMethod.setReturnType( modelName )
                .setBody( " return new " + modelName + "( model );" )
                .addAnnotation( Override.class );

        viewClass.addNestedType(subformAdapter);

        amendUpdateNestedModels(fieldDefinition, context, viewClass);

        amendValidateNestedModels( fieldDefinition, context, viewClass );
    }

    private void amendValidateNestedModels(final SubFormFieldDefinition fieldDefinition, final SourceGenerationContext context, final JavaClassSource viewClass) {

        final MethodSource<JavaClassSource> updateNestedModelsMethod = viewClass.getMethod( DO_EXTRA_VALIDATIONS_METHOD,
                boolean.class );
        if ( updateNestedModelsMethod != null ) {
            String body = updateNestedModelsMethod.getBody();

            final StringBuffer validText = new StringBuffer();
            validText.append(";")
                    .append( "if ( !" )
                    .append( fieldDefinition.getName())
                    .append(".validate() && valid) {valid=false;}");

            body = body.replaceFirst(";", validText.toString());
            updateNestedModelsMethod.setBody( body );
        }
    }

    private void amendUpdateNestedModels(final SubFormFieldDefinition fieldDefinition, final SourceGenerationContext context, final JavaClassSource viewClass) {
        final MethodSource<JavaClassSource> updateNestedModelsMethod = getUpdateNestedModelsMethod( context, viewClass );
        if ( updateNestedModelsMethod != null ) {
            String body = updateNestedModelsMethod.getBody();

            final String pName = fieldDefinition.getBinding();
            final String pType = fieldDefinition.getStandaloneClassName();
            final String modelName = StringUtils.capitalize( context.getFormDefinition().getModel().getName() );

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
