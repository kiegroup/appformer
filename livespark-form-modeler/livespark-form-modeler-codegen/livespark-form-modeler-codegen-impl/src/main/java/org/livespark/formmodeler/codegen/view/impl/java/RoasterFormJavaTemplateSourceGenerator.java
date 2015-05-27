package org.livespark.formmodeler.codegen.view.impl.java;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_TEMPLATED;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.FORM_VIEW_CLASS;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.INJECT_NAMED;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;

/**
 * Created by pefernan on 4/28/15.
 */
@ApplicationScoped
public class RoasterFormJavaTemplateSourceGenerator extends RoasterClientFormTemplateSourceGenerator {

    @Override
    protected void addAdditional( SourceGenerationContext context,
                                  JavaClassSource viewClass ) {
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
    }
}
