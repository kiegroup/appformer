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

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.ListView;
import org.livespark.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGenerator;
import org.livespark.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGeneratorManager;
import org.livespark.formmodeler.model.DataHolder;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.impl.relations.EntityRelationField;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

@ListView
@ApplicationScoped
public class RoasterListJavaTemplateSourceGenerator implements FormJavaTemplateSourceGenerator {

    @Inject
    private ColumnMetaGeneratorManager columnMetaGeneratorManager;

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
        addGetColumnsImpl( viewClass, context );
        addGetModelImpl( viewClass, context );
        addGetFormModelImpl( viewClass, context );
    }

    private void addGetFormModelImpl( JavaClassSource viewClass, SourceGenerationContext context ) {

        StringBuffer body = new StringBuffer();

        body.append( context.getFormModelName() )
                .append( " formModel = new " )
                .append( context.getFormModelName() )
                .append( "();" );

        // TODO: improve this, is there a real need to have more than one model?
        DataHolder holder = context.getFormDefinition().getDataHolders().get( 0 );
        String modelName = holder.getName();

        body.append( "formModel.set" )
                .append( StringUtils.capitalize( modelName ) )
                .append( "( ")
                .append( modelName )
                .append( " );" );

        body.append( "return formModel;" );

        MethodSource<JavaClassSource> createFormModel = viewClass.addMethod()
                .setName( "createFormModel" )
                .setReturnType( context.getFormModelName() )
                .setBody( body.toString() )
                .setPublic();

        createFormModel.addParameter( holder.getType(), modelName );
        createFormModel.addAnnotation( Override.class );
    }

    private void addGetModelImpl( JavaClassSource viewClass, SourceGenerationContext context ) {
        StringBuffer body = new StringBuffer();

        // TODO: improve this, is there a real need to have more than one model?
        body.append( "return formModel.get" )
                .append( StringUtils.capitalize( context.getFormDefinition().getDataHolders().get( 0 ).getName() ) )
                .append( "();" );

        MethodSource<JavaClassSource> getModel = viewClass.addMethod()
                .setName( "getModel" )
                .setReturnType( context.getEntityName() )
                .setBody( body.toString() )
                .setPublic();
        getModel.addParameter( context.getFormModelName(), "formModel" );
        getModel.addAnnotation( Override.class );
    }

    private void addGetColumnsImpl( JavaClassSource viewClass,
                                    SourceGenerationContext context ) {

        viewClass.addImport( List.class.getName() );
        viewClass.addImport( ArrayList.class.getName() );
        viewClass.addImport( COLUMN_META_CLASS_NAME );

        StringBuffer body = new StringBuffer();
        body.append( "List<ColumnMeta> " )
                .append( COLUMN_METAS_VAR_NAME )
                .append( " = new ArrayList<ColumnMeta>();" );


        FormDefinition form = context.getFormDefinition();

        for ( FieldDefinition field : form.getFields() ) {
            if ( !(field instanceof EntityRelationField) ) {
                ColumnMetaGenerator generator = columnMetaGeneratorManager.getColumnMetaGeneratorForType( field.getStandaloneClassName() );
                if ( generator != null ) {
                    for ( String imp : generator.getImports() ) {
                        viewClass.addImport( imp );
                    }
                    body.append( generator.generateColumnMeta( field.getBoundPropertyName(),
                            field.getLabel(),
                            context.getEntityName(),
                            context ) );
                }
            }
        }

        body.append( "return " )
                .append( COLUMN_METAS_VAR_NAME )
                .append( ";" );

        viewClass.addMethod()
                .setName( "getCrudColumns" )
                .setReturnType( "List<ColumnMeta>" )
                .setBody( body.toString() )
                .setPublic()
                .addAnnotation( Override.class );

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
                .setPublic()
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
                .setSuperType( LIST_VIEW_CLASS + "<" + context.getEntityName() + ", " + context.getFormModelName() + ">" );
    }

    private void addAnnotations( SourceGenerationContext context, JavaClassSource viewClass ) {
        viewClass.addAnnotation( ERRAI_TEMPLATED );
    }

    private void addImports( SourceGenerationContext context,
            JavaClassSource viewClass ) {
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getFormModelName() );
        viewClass.addImport( context.getLocalPackage().getPackageName() + "." + context.getFormViewName() );
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getRestServiceName() );
    }

}
