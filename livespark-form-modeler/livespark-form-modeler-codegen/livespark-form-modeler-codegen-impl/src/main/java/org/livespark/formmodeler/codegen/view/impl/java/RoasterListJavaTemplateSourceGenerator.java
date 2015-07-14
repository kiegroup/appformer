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
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.LIST_VIEW_CLASS;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.ListView;

@ListView
@ApplicationScoped
public class RoasterListJavaTemplateSourceGenerator implements FormJavaTemplateSourceGenerator {

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaClassSource viewClass = Roaster.create( JavaClassSource.class );
        String packageName = getPackageName( context );

        addTypeSignature( context, viewClass, packageName );
        addAnnotations( context, viewClass );
        addImports( context, viewClass );

        addMethods( viewClass, context );

        return viewClass.toString();
    }

    private void addMethods( JavaClassSource viewClass,
            SourceGenerationContext context ) {
        addGetFormTypeImpl( viewClass, context );
        addGetListTitleImpl( viewClass, context );
        addGetFormTitleImpl( viewClass, context );
        addGetFormIdImpl( viewClass, context );
        addGetRemoteServiceClassImpl( viewClass, context );
    }

    private void addGetFormTypeImpl( JavaClassSource viewClass,
            SourceGenerationContext context ) {
        viewClass.addMethod()
                .setProtected()
                .setName( "getFormType" )
                .setReturnType( "Class<" + context.getFormViewName() + ">" )
                .setBody( "return " + context.getFormViewName() + ".class;" )
                .addAnnotation( Override.class );
    }

    private void addGetListTitleImpl( JavaClassSource viewClass,
            SourceGenerationContext context ) {
        viewClass.addMethod()
                .setName( "getListTitle" )
                .setPublic()
                .setReturnType( String.class )
                .setBody( "return \"" + context.getFormDefinition().getName() + "\";" )
                .addAnnotation( Override.class );
    }

    private void addGetFormIdImpl( JavaClassSource viewClass,
            SourceGenerationContext context ) {
        viewClass.addMethod()
                .setName( "getFormId" )
                .setProtected()
                .setReturnType( String.class )
                .setBody( "return \"" + context.getFormDefinition().getName() + " Form\";" )
                .addAnnotation( Override.class );
    }

    private void addGetFormTitleImpl( JavaClassSource viewClass,
            SourceGenerationContext context ) {
        viewClass.addMethod()
                .setName( "getFormTitle" )
                .setProtected()
                .setReturnType( String.class )
                .setBody( "return \"" + context.getFormDefinition().getName() + " Form\";" )
                .addAnnotation( Override.class );
    }

    private void addGetRemoteServiceClassImpl( JavaClassSource viewClass,
            SourceGenerationContext context ) {
        viewClass.addMethod()
                .setName( "getRemoteServiceClass" )
                .setProtected()
                .setReturnType( "Class<" + context.getRestServiceName() + ">" )
                .setBody( "return " + context.getRestServiceName() + ".class;" )
                .addAnnotation( Override.class );
    }

    private String getPackageName( SourceGenerationContext context ) {
        return context.getLocalPackage().getPackageName();
    }

    private void addTypeSignature( SourceGenerationContext context,
            JavaClassSource viewClass,
            String packageName ) {
        viewClass.setPackage( packageName )
                .setPublic()
                .setName( context.getListViewName() )
                .setSuperType( LIST_VIEW_CLASS + "<" + context.getModelName() + ", " + context.getListItemViewName() + ">" );
    }

    private void addAnnotations( SourceGenerationContext context, JavaClassSource viewClass ) {
        viewClass.addAnnotation( ERRAI_TEMPLATED );
        viewClass.addAnnotation( FORM_MODEL_ANNOTATION ).setStringValue( context.getSharedPackage().getPackageName() + "." + context.getModelName() );
    }

    private void addImports( SourceGenerationContext context,
            JavaClassSource viewClass ) {
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getModelName() );
        viewClass.addImport( context.getLocalPackage().getPackageName() + "." + context.getFormViewName() );
        viewClass.addImport( context.getLocalPackage().getPackageName() + "." + context.getListItemViewName() );
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getRestServiceName() );
    }

}
