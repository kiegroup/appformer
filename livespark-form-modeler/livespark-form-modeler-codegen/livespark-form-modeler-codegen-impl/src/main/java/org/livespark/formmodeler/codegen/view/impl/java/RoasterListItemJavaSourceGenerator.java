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
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.INJECT_INJECT;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.LIST_ITEM_VIEW_CLASS;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.ListItemView;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.MultipleSubFormHelper;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.SubFormHelper;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.impl.relations.EmbeddedFormField;

@ListItemView
@ApplicationScoped
public class RoasterListItemJavaSourceGenerator extends RoasterClientFormTemplateSourceGenerator {

    @Override
    protected void addAdditional( SourceGenerationContext context,
            JavaClassSource viewClass ) {
    }

    @Override
    protected void addAnnotations( SourceGenerationContext context,
            JavaClassSource viewClass ) {
        viewClass.addAnnotation( ERRAI_TEMPLATED ).setStringValue( context.getListViewName() + ".html#" + context.getListItemRowId() );
    }

    @Override
    protected void addImports( SourceGenerationContext context,
            JavaClassSource viewClass ) {
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getModelName() );
    }

    @Override
    protected void addTypeSignature( SourceGenerationContext context,
            JavaClassSource viewClass,
            String packageName ) {
        viewClass.setPackage( packageName )
                .setPublic()
                .setName( context.getListItemViewName() )
                .setSuperType( LIST_ITEM_VIEW_CLASS + "<" + context.getModelName() + ">" );
    }

    @Override
    protected String getWidgetFromHelper( InputCreatorHelper helper ) {
        return helper.getDisplayWidget();
    }

    @Override
    protected boolean isEditable() {
        return false;
    }

    @Override
    protected void initializeProperty( InputCreatorHelper helper, SourceGenerationContext context, FieldDefinition fieldDefinition, FieldSource<JavaClassSource> field ) {
        if (helper.isDisplayInjectable()) field.addAnnotation( INJECT_INJECT );
        else field.setLiteralInitializer( helper.getDisplayInitLiteral() );
    }

    @Override
    protected boolean displaysId() {
        return true;
    }

    @Override
    protected boolean isBanned( FieldDefinition definition ) {
        return definition instanceof EmbeddedFormField;
    }
}