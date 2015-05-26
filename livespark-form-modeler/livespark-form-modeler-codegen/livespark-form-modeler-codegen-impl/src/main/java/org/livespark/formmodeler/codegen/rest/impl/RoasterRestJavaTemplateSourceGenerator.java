package org.livespark.formmodeler.codegen.rest.impl;

import java.util.List;

import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.SourceGenerationContext;

public abstract class RoasterRestJavaTemplateSourceGenerator<O extends JavaSource<O>> implements FormJavaTemplateSourceGenerator {

    protected void setCreateMethodSignature( SourceGenerationContext context,
                                             MethodSource<O> create ) {
        create.setName( "create" )
              .setPublic()
              .setReturnType( context.getModelName() )
              .addParameter( context.getModelName(), "model" );
    }

    protected void setLoadMethodSignature( SourceGenerationContext context,
                                           MethodSource<O> load ) {
        load.setName( "load" )
            .setPublic()
            .setReturnType( "List<" + context.getModelName() + ">" );
    }

    protected void setUpdateMethodSignature( SourceGenerationContext context,
                                             MethodSource<O> update ) {
        update.setName( "update" )
              .setPublic()
              .setReturnType( Boolean.class )
              .addParameter( context.getModelName(), "model" );
    }

    protected void setDeleteMethodSignature( SourceGenerationContext context,
                                             MethodSource<O> delete ) {
        delete.setName( "delete" )
              .setPublic()
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
