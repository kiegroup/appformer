package org.livespark.formmodeler.codegen.rest.impl;

import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.model.FieldDefinition;

public abstract class RoasterRestJavaTemplateSourceGenerator<O extends JavaSource<O>> implements FormJavaTemplateSourceGenerator {

    protected void setCreateMethodSignature( SourceGenerationContext context,
                                             MethodSource<O> create ) {
        create.setName( "create" )
              .setReturnType( context.getModelName() );

        for ( FieldDefinition<?> field : context.getFormDefinition().getFields() ) {
            create.addParameter( field.getStandaloneClassName(),
                                 field.getName() );
        }
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

}
