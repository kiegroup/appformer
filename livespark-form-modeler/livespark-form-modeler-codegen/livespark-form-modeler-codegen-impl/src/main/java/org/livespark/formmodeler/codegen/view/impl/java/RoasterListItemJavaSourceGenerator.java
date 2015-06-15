package org.livespark.formmodeler.codegen.view.impl.java;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_TEMPLATED;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.LIST_ITEM_VIEW_CLASS;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.ListItemView;

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

}
