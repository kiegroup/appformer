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

import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INIT_FORM_METHOD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.JAVA_LANG_OVERRIDE;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.SET_MODEL_METHOD;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.FormDefinition;

public abstract class AbstractNestedModelHelper<F extends FieldDefinition> extends AbstractInputCreatorHelper<F> {
    public static final String UPDATE_NESTED_MODELS_METHOD = "updateNestedModels";

    protected MethodSource<JavaClassSource> getUpdateNestedModelsMethod( final SourceGenerationContext context,
                                                                         final JavaClassSource viewClass ) {
        MethodSource<JavaClassSource> method = viewClass.getMethod( UPDATE_NESTED_MODELS_METHOD, boolean.class.getName() );

        if ( method == null ) {
            method = viewClass.addMethod()
                    .setName( UPDATE_NESTED_MODELS_METHOD )
                    .setBody( "" )
                    .setParameters( "boolean init" )
                    .setReturnTypeVoid()
                    .setProtected();

            method.addAnnotation( JAVA_LANG_OVERRIDE );

            final MethodSource<JavaClassSource> initFormMethod = viewClass.getMethod( INIT_FORM_METHOD );
            StringBuffer body = new StringBuffer( initFormMethod.getBody() );
            if ( body.indexOf( UPDATE_NESTED_MODELS_METHOD ) == -1 ) {
                body.append( UPDATE_NESTED_MODELS_METHOD ).append( "(true);" );
                initFormMethod.setBody( body.toString() );
            }

            body = new StringBuffer( "super.setModel( model );" );
            body.append( UPDATE_NESTED_MODELS_METHOD ).append( "( false );" );

            final MethodSource<JavaClassSource> setModelMethod = viewClass.addMethod()
                    .setName( SET_MODEL_METHOD )
                    .setReturnTypeVoid()
                    .setPublic()
                    .setBody( body.toString() );
            setModelMethod.addAnnotation( JAVA_LANG_OVERRIDE );
            setModelMethod.addParameter( context.getFormModelName(), "model" );
        }

        return method;
    }

    protected FormDefinition getContextFormById( final SourceGenerationContext context, final String formId ) {
        for ( final FormDefinition form : context.getProjectForms() ) {
            if ( form.getId().equals( formId ) ) {
                return form;
            }
        }
        return null;
    }

    protected String getFormModelClassName( final FormDefinition form, final SourceGenerationContext context ) {
        return context.getSharedPackage().getPackageName() + "." + form.getName() + SourceGenerationContext.FORM_MODEL_SUFFIX;
    }

    protected String getFormViewClassName( final FormDefinition form, final SourceGenerationContext context ) {
        return context.getLocalPackage().getPackageName() + "." + form.getName() + SourceGenerationContext.FORM_VIEW_SUFFIX;
    }

}
