package org.livespark.formmodeler.codegen.rest.impl;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.model.impl.ModelConstructorGenerator;

public abstract class RoasterRestJavaTemplateSourceGenerator<O extends JavaSource<O>> implements FormJavaTemplateSourceGenerator {

    @Inject
    private ModelConstructorGenerator constructorGenerator;

    protected void setCreateMethodSignature( SourceGenerationContext context,
                                             MethodSource<O> create ) {
        create.setName( "create" )
              .setReturnType( context.getModelName() );

        constructorGenerator.addModelFieldsAsParameters( context, create );
    }

    protected void setLoadMethodSignature( SourceGenerationContext context,
                                           MethodSource<O> load ) {
        load.setName( "load" )
            .setReturnType( "List<" + context.getModelName() + ">" );
    }

    protected void setDeleteMethodSignature( SourceGenerationContext context,
                                             MethodSource<O> delete ) {
        delete.setName( "delete" )
              .setReturnType( Boolean.class );
        // TODO The parameter should be a unique identifier, not the entire model.
        delete.addParameter( context.getModelName(), "model" );
    }

    protected String getPackageName( SourceGenerationContext context ) {
        return context.getPackage().getPackageName();
    }

    protected void addImports( SourceGenerationContext context,
                                  O restIface,
                                  String packageName ) {
                                    restIface.addImport( packageName + "." + context.getModelName() );
                                    restIface.addImport( List.class );
                                }

}
