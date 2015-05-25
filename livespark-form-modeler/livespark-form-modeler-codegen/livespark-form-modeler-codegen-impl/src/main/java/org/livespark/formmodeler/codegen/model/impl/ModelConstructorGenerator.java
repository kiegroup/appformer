package org.livespark.formmodeler.codegen.model.impl;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_MAPS_TO;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.VALIDATION_NOT_NULL;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.VALIDATION_VALID;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.model.DataHolder;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.service.FieldManager;

public class ModelConstructorGenerator {

    public void addNoArgConstructor( JavaClassSource modelClass ) {
        modelClass.addMethod()
                  .setConstructor( true )
                  .setPublic()
                  .setBody( "" );
    }

    public void addAllFieldsConstructor( SourceGenerationContext context,
                                         JavaClassSource modelClass ) {
        MethodSource<JavaClassSource> constructor = modelClass.addMethod()
                                                              .setConstructor( true )
                                                              .setPublic();
        StringBuffer source = new StringBuffer();

        for ( DataHolder dataHolder : context.getFormDefinition().getDataHolders() ) {
            FieldSource<JavaClassSource> modelField = modelClass.addProperty( dataHolder.getType(),
                                                                              dataHolder.getName() ).getField();

            if ( ArrayUtils.contains( FieldManager.BASIC_TYPES,
                                      dataHolder.getType() ) ) {
                modelField.addAnnotation( VALIDATION_NOT_NULL );
            } else {
                modelField.addAnnotation( VALIDATION_VALID );
            }

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

    public <O extends JavaSource<O>> void addModelFieldsAsParameters( SourceGenerationContext context,
                                                                      MethodSource<O> method ) {
        for ( FieldDefinition<?> field : context.getFormDefinition().getFields() ) {
            method.addParameter( field.getStandaloneClassName(),
                                 field.getName() );
        }
    }

    public <O extends JavaSource<O>> String getConstructorInvocation( SourceGenerationContext context ) {
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
