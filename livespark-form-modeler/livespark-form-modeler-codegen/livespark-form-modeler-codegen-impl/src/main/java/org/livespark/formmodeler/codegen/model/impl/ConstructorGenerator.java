package org.livespark.formmodeler.codegen.model.impl;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_MAPS_TO;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.model.DataHolder;
import org.livespark.formmodeler.model.FieldDefinition;

public class ConstructorGenerator {

    public void addNoArgConstructor( JavaClassSource modelClass ) {
        modelClass.addMethod()
                  .setConstructor( true )
                  .setPublic()
                  .setBody( "" );
    }

    public void addFormModelConstructor( SourceGenerationContext context,
                                         JavaClassSource modelClass ) {
        MethodSource<JavaClassSource> constructor = modelClass.addMethod()
                                                              .setConstructor( true )
                                                              .setPublic();
        StringBuffer source = new StringBuffer();

        for ( DataHolder dataHolder : context.getFormDefinition().getDataHolders() ) {
            constructor.addParameter( dataHolder.getType(),
                                      dataHolder.getName() )
                       .addAnnotation( ERRAI_MAPS_TO )
                       .setStringValue( dataHolder.getName() );
            source.append( "this." )
                  .append( dataHolder.getName() )
                  .append( " = " )
                  .append( dataHolder.getName() )
                  .append( ";" );
        }

        constructor.setBody( source.toString() );
    }

    public <O extends JavaSource<O>> void addFormFieldsAsParameters( SourceGenerationContext context,
                                                                     MethodSource<O> method ) {
        for ( FieldDefinition<?> field : context.getFormDefinition().getFields() ) {
            method.addParameter( field.getStandaloneClassName(),
                                 field.getName() );
        }
    }

    public <O extends JavaSource<O>> String getDataObjectConstructorInvocation( SourceGenerationContext context ) {
        StringBuilder call = new StringBuilder();
        call.append( "new " )
                       .append( context.getDataObjectName() )
                       .append( "(" );

        for ( FieldDefinition<?> field : context.getFormDefinition().getFields() ) {
            call.append( field.getName() )
                .append( ", " );
        }
        if ( context.getFormDefinition().getFields().size() > 0 ) {
            call.delete( call.length() - ", ".length(),
                         call.length() );
        }

        call.append( ")" );

        return call.toString();
    }

}
