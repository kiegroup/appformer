/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.model.FormDefinition;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

public abstract class AbstractNestedModelHelper extends AbstractInputCreatorHelper {
    public static final String UPDATE_NESTED_MODELS_METHOD = "updateNestedModels";

    protected MethodSource<JavaClassSource> getUpdateNestedModelsMethod( SourceGenerationContext context,
                                                                         JavaClassSource viewClass ) {
        MethodSource<JavaClassSource> method = viewClass.getMethod( UPDATE_NESTED_MODELS_METHOD, boolean.class );

        if ( method == null ) {
            method = viewClass.addMethod()
                    .setName( UPDATE_NESTED_MODELS_METHOD )
                    .setBody( "" )
                    .setParameters( "boolean init" )
                    .setReturnTypeVoid()
                    .setProtected();

            MethodSource<JavaClassSource> initFormMethod = viewClass.getMethod( INIT_FORM_METHOD );
            StringBuffer body = new StringBuffer( initFormMethod.getBody() );
            if ( body.indexOf( UPDATE_NESTED_MODELS_METHOD ) == -1 ) {
                body.append( UPDATE_NESTED_MODELS_METHOD ).append( "(true);" );
                initFormMethod.setBody( body.toString() );
            }

            body = new StringBuffer( "super.setModel( model );" );
            body.append( UPDATE_NESTED_MODELS_METHOD ).append( "( false );" );

            MethodSource<JavaClassSource> setModelMethod = viewClass.addMethod()
                    .setName( SET_MODEL_METHOD )
                    .setReturnTypeVoid()
                    .setPublic()
                    .setBody( body.toString() );
            setModelMethod.addAnnotation( JAVA_LANG_OVERRIDE );
            setModelMethod.addParameter( context.getFormModelName(), "model" );
        }

        return method;
    }

    protected FormDefinition getContextFormById( SourceGenerationContext context, String formId ) {
        for ( FormDefinition form : context.getProjectForms() ) {
            if ( form.getId().equals( formId ) ) {
                return form;
            }
        }
        return null;
    }

    protected String getFormModelClassName( FormDefinition form, SourceGenerationContext context ) {
        return context.getSharedPackage().getPackageName() + "." + form.getName() + SourceGenerationContext.FORM_MODEL_SUFFIX;
    }

    protected String getFormViewClassName( FormDefinition form, SourceGenerationContext context ) {
        return context.getLocalPackage().getPackageName() + "." + form.getName() + SourceGenerationContext.FORM_VIEW_SUFFIX;
    }

}
