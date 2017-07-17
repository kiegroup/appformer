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

package org.kie.appformer.formmodeler.codegen.view.impl.java;

import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.COLUMN_METAS_VAR_NAME;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.COLUMN_META_CLASS_NAME;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_TEMPLATED;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.LIST_VIEW_CLASS;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.appformer.formmodeler.codegen.JavaSourceGenerator;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.view.ListView;
import org.kie.appformer.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGenerator;
import org.kie.appformer.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGeneratorManager;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.EntityRelationField;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.kie.workbench.common.forms.model.JavaFormModel;

@ListView
@ApplicationScoped
public class RoasterListViewSourceGenerator implements JavaSourceGenerator {

    @Inject
    private ColumnMetaGeneratorManager columnMetaGeneratorManager;

    @Override
    public String generateJavaSource( final SourceGenerationContext context ) {
        final JavaClassSource viewClass = Roaster.create( JavaClassSource.class );
        final String packageName = getPackageName( context );

        addTypeSignature( context, viewClass, packageName );
        addAnnotations( context, viewClass );
        addImports( context, viewClass );

        addMethods( viewClass, context );

        return viewClass.toString();
    }

    private void addMethods( final JavaClassSource viewClass,
                             final SourceGenerationContext context ) {
        addGetListTitleImpl( viewClass, context );
        addGetFormTitleImpl( viewClass, context );
        addGetFormIdImpl( viewClass, context );
        addGetColumnsImpl( viewClass, context );
        addGetFormModelImpl( viewClass, context );
        addNewModelImpl( viewClass, context );
    }

    private void addNewModelImpl( final JavaClassSource viewClass,
                                  final SourceGenerationContext context ) {
        final String body = "return new " + context.getEntityName() + "();";
        viewClass
                .addMethod()
                .setName( "newModel" )
                .setPublic()
                .setReturnType( context.getEntityName() )
                .setBody( body );
    }

    private void addGetFormModelImpl( final JavaClassSource viewClass, final SourceGenerationContext context ) {

        final FormDefinition form = context.getFormDefinition();

        checkFormDefinition( form );

        final StringBuffer body = new StringBuffer();

        body.append( context.getFormModelName() )
                .append( " formModel = new " )
                .append( context.getFormModelName() )
                .append( "();" );

        final String modelName = form.getModel().getName();

        body.append( "formModel.set" )
                .append( StringUtils.capitalize( modelName ) )
                .append( "( " )
                .append( modelName )
                .append( " );" );

        body.append( "return formModel;" );

        final MethodSource<JavaClassSource> createFormModel = viewClass.addMethod()
                .setName( "createFormModel" )
                .setReturnType( context.getFormModelName() )
                .setBody( body.toString() )
                .setPublic();

        createFormModel.addParameter( ( (JavaFormModel) form.getModel() ).getType(), modelName );
        createFormModel.addAnnotation( Override.class );
    }

    private void addGetColumnsImpl( final JavaClassSource viewClass,
                                    final SourceGenerationContext context ) {

        viewClass.addImport( List.class.getName() );
        viewClass.addImport( ArrayList.class.getName() );
        viewClass.addImport( COLUMN_META_CLASS_NAME );

        final String returnType = "List<ColumnMeta<" + context.getEntityName() + ">>";
        final StringBuffer body = new StringBuffer();
        body.append( returnType )
                .append( COLUMN_METAS_VAR_NAME )
                .append( " = new ArrayList<>();" );


        final FormDefinition form = context.getFormDefinition();

        for ( final FieldDefinition field : form.getFields() ) {
            if ( !( field instanceof EntityRelationField ) ) {
                final ColumnMetaGenerator generator = columnMetaGeneratorManager.getColumnMetaGeneratorForType( field.getStandaloneClassName() );
                if ( generator != null ) {
                    for ( final String imp : generator.getImports() ) {
                        viewClass.addImport( imp );
                    }
                    body.append( generator.generateColumnMeta( field.getBinding(),
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
                .setReturnType( returnType )
                .setBody( body.toString() )
                .setPublic()
                .addAnnotation( Override.class );

    }

    private void addGetListTitleImpl( final JavaClassSource viewClass,
                                      final SourceGenerationContext context ) {
        viewClass.addMethod()
                .setName( "getListTitle" )
                .setPublic()
                .setReturnType( String.class )
                .setBody( "return \"" + context.getFormDefinition().getName() + "\";" )
                .addAnnotation( Override.class );
    }

    private void addGetFormIdImpl( final JavaClassSource viewClass,
                                   final SourceGenerationContext context ) {
        viewClass.addMethod()
                .setName( "getFormId" )
                .setProtected()
                .setReturnType( String.class )
                .setBody( "return \"" + context.getFormDefinition().getName() + " Form\";" )
                .addAnnotation( Override.class );
    }

    private void addGetFormTitleImpl( final JavaClassSource viewClass,
                                      final SourceGenerationContext context ) {
        viewClass.addMethod()
                .setName( "getFormTitle" )
                .setPublic()
                .setReturnType( String.class )
                .setBody( "return \"" + context.getFormDefinition().getName() + " Form\";" )
                .addAnnotation( Override.class );
    }

    private String getPackageName( final SourceGenerationContext context ) {
        return context.getLocalPackage().getPackageName();
    }

    private void addTypeSignature( final SourceGenerationContext context,
                                   final JavaClassSource viewClass,
                                   final String packageName ) {
        viewClass.setPackage( packageName )
                .setPublic()
                .setName( context.getListViewName() )
                .setSuperType( LIST_VIEW_CLASS + "<" + context.getEntityName() + ", " + context.getFormModelName() + ">" );
    }

    private void addAnnotations( final SourceGenerationContext context, final JavaClassSource viewClass ) {
        viewClass.addAnnotation( ERRAI_TEMPLATED );
    }

    private void addImports( final SourceGenerationContext context,
                             final JavaClassSource viewClass ) {
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getFormModelName() );
        viewClass.addImport( context.getLocalPackage().getPackageName() + "." + context.getFormViewName() );
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getRestServiceName() );
    }

}
