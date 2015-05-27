package org.livespark.formmodeler.codegen.model.impl;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BINDABLE;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_PORTABLE;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.FORM_MODEL_CLASS;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.INJECT_NAMED;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.VALIDATION_NOT_NULL;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.VALIDATION_VALID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.model.FormModelSourceGenerator;
import org.livespark.formmodeler.model.DataHolder;
import org.livespark.formmodeler.service.FieldManager;

/**
 * Created by pefernan on 4/27/15.
 */
@ApplicationScoped
public class RoasterFormModelSourceGenerator implements FormModelSourceGenerator {

    @Inject
    private ConstructorGenerator constructorGenerator;

    @Override
    public String generateFormModelSource( SourceGenerationContext context ) {

        JavaClassSource modelClass = Roaster.create( JavaClassSource.class );
        modelClass.setPackage( context.getSharedPackage().getPackageName() )
                .setPublic()
                .setName( context.getModelName() );

        modelClass.setSuperType( FORM_MODEL_CLASS );

        modelClass.addAnnotation( ERRAI_PORTABLE );
        modelClass.addAnnotation( ERRAI_BINDABLE );
        modelClass.addAnnotation( INJECT_NAMED ).setStringValue( context.getModelName() );

        addProperties( context, modelClass );
        constructorGenerator.addNoArgConstructor( modelClass );
        constructorGenerator.addFormModelConstructor( context, modelClass );

        return modelClass.toString();
    }

    private void addProperties( SourceGenerationContext context, JavaClassSource modelClass ) {
        for ( DataHolder dataHolder : context.getFormDefinition().getDataHolders() ) {
            FieldSource<JavaClassSource> modelField = modelClass.addProperty( dataHolder.getType(),
                                                                              dataHolder.getName() ).getField();

            if ( ArrayUtils.contains( FieldManager.BASIC_TYPES,
                                      dataHolder.getType() ) ) {
                modelField.addAnnotation( VALIDATION_NOT_NULL );
            } else {
                modelField.addAnnotation( VALIDATION_VALID );
            }

        }
    }
}
