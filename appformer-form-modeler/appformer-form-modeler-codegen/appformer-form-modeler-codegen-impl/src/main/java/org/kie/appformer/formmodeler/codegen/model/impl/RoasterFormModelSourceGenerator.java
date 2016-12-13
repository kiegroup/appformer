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

package org.kie.appformer.formmodeler.codegen.model.impl;

import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BINDABLE;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_PORTABLE;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.FORM_MODEL_CLASS;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INJECT_NAMED;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.VALIDATION_VALID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.kie.appformer.formmodeler.codegen.JavaSourceGenerator;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.model.FormModel;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.kie.workbench.common.forms.model.JavaModel;

@FormModel
@ApplicationScoped
public class RoasterFormModelSourceGenerator implements JavaSourceGenerator {

    @Inject
    public RoasterFormModelSourceGenerator( final ConstructorGenerator constructorGenerator ) {
        this.constructorGenerator = constructorGenerator;
    }

    private final ConstructorGenerator constructorGenerator;

    @Override
    public String generateJavaSource( final SourceGenerationContext context ) {

        final JavaClassSource modelClass = Roaster.create( JavaClassSource.class );

        addTypeSignature( context, modelClass );
        addImports( context, modelClass );
        addTypeAnnotations( context, modelClass );
        addProperties( context, modelClass );
        addConstructors( context, modelClass );

        return modelClass.toString();
    }

    private void addImports( final SourceGenerationContext context,
                             final JavaClassSource modelClass ) {
    }


    private void addConstructors( final SourceGenerationContext context,
                                  final JavaClassSource modelClass ) {
        constructorGenerator.addNoArgConstructor( modelClass );
        constructorGenerator.addFormModelConstructor( context, modelClass );
    }

    private void addTypeAnnotations( final SourceGenerationContext context,
                                     final JavaClassSource modelClass ) {
        modelClass.addAnnotation( ERRAI_PORTABLE );
        modelClass.addAnnotation( ERRAI_BINDABLE );
        modelClass.addAnnotation( INJECT_NAMED ).setStringValue( context.getFormModelName() );
    }

    private void addTypeSignature( final SourceGenerationContext context,
                                   final JavaClassSource modelClass ) {
        modelClass.setPackage( context.getSharedPackage().getPackageName() )
                .setPublic()
                .setName( context.getFormModelName() );

        modelClass.setSuperType( FORM_MODEL_CLASS + "<" + context.getEntityName() + ">" );
    }

    private void addProperties( final SourceGenerationContext context, final JavaClassSource modelClass ) {

        final FormDefinition form = context.getFormDefinition();

        checkFormDefinition( form );

        final JavaModel model = (JavaModel) context.getFormDefinition().getModel();

        final FieldSource<JavaClassSource> modelField = modelClass.addProperty( model.getType(),
                                                                                form.getModel().getName() ).getField();

        modelField.addAnnotation( VALIDATION_VALID );

        modelClass.addMethod()
                .setName( "getModel" )
                .setReturnType( context.getEntityName() )
                .setPublic()
                .setBody( "return " + form.getModel().getName() + ";" )
                .addAnnotation( Override.class );

        modelClass.addMethod()
                .setName( "initModel" )
                .setReturnTypeVoid()
                .setPublic()
                .setBody( form.getModel().getName() + "= new " + context.getEntityName() + "();" )
                .addAnnotation( Override.class );
    }
}
