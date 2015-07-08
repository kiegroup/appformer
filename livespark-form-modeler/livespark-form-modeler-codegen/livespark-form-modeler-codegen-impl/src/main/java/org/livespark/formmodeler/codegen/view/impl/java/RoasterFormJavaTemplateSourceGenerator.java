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

package org.livespark.formmodeler.codegen.view.impl.java;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_TEMPLATED;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.FORM_MODEL_ANNOTATION;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.FORM_VIEW_CLASS;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.INJECT_INJECT;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.INJECT_NAMED;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.JAVA_LANG_OVERRIDE;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.model.FieldDefinition;

/**
 * Created by pefernan on 4/28/15.
 */
@ApplicationScoped
public class RoasterFormJavaTemplateSourceGenerator extends RoasterClientFormTemplateSourceGenerator {

    @Override
    protected void addAdditional( SourceGenerationContext context,
            JavaClassSource viewClass ) {
        
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getEntityName() );
        
        viewClass.addMethod()
                 .setName( "getEntity" )
                 .setBody( "return getModel().get" + context.getEntityName() + "();" )
                 .setReturnType( Object.class )
                 .setProtected()
                 .addAnnotation( JAVA_LANG_OVERRIDE );
        
        viewClass.addMethod()
                 .setName( "setNewEntity" )
                 .setBody( "getModel().set" + context.getEntityName() + "(new " + context.getEntityName() + "());" )
                 .setReturnTypeVoid()
                 .setProtected()
                 .addAnnotation( JAVA_LANG_OVERRIDE );
        
        viewClass.addMethod()
                 .setName( "updateNestedModels" )
                 .setBody( "" )
                 .setParameters( "boolean init" )
                 .setReturnTypeVoid()
                 .setProtected()
                 .addAnnotation( JAVA_LANG_OVERRIDE );
    }

    @Override
    protected void addTypeSignature( SourceGenerationContext context,
            JavaClassSource viewClass,
            String packageName ) {
        viewClass.setPackage( packageName )
                .setPublic()
                .setName( context.getFormViewName() )
                .setSuperType( FORM_VIEW_CLASS + "<" + context.getModelName() + ">" );
    }

    @Override
    protected void addImports( SourceGenerationContext context,
            JavaClassSource viewClass ) {
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getModelName() );
    }

    @Override
    protected void addAnnotations( SourceGenerationContext context,
            JavaClassSource viewClass ) {
        viewClass.addAnnotation( ERRAI_TEMPLATED );
        viewClass.addAnnotation( INJECT_NAMED ).setStringValue( context.getFormViewName() );
        viewClass.addAnnotation( FORM_MODEL_ANNOTATION ).setStringValue( context.getSharedPackage().getPackageName() + "." + context.getModelName() );
    }

    @Override
    protected String getWidgetFromHelper( InputCreatorHelper helper ) {
        return helper.getInputWidget();
    }

    @Override
    protected boolean isEditable() {
        return true;
    }

    @Override
    protected void initializeProperty( InputCreatorHelper helper, SourceGenerationContext context, FieldDefinition fieldDefinition, FieldSource<JavaClassSource> field ) {
        if (helper.isInputInjectable()) field.addAnnotation( INJECT_INJECT );
        else field.setLiteralInitializer( helper.getInputInitLiteral( context, fieldDefinition) );
    }

    @Override
    protected boolean displaysId() {
        return false;
    }

    @Override
    protected boolean isBanned( FieldDefinition definition ) {
        return false;
    }
}
