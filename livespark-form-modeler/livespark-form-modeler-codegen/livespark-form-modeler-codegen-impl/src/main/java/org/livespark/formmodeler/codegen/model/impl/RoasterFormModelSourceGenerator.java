package org.livespark.formmodeler.codegen.model.impl;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BINDABLE;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_PORTABLE;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.FORM_MODEL_CLASS;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.INJECT_NAMED;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.model.FormModelSourceGenerator;

/**
 * Created by pefernan on 4/27/15.
 */
@ApplicationScoped
public class RoasterFormModelSourceGenerator implements FormModelSourceGenerator {

    @Inject
    private ModelConstructorGenerator constructorGenerator;

    @Override
    public String generateFormModelSource( SourceGenerationContext context ) {

        JavaClassSource modelClass = Roaster.create( JavaClassSource.class );
        modelClass.setPackage( context.getPackage().getPackageName() )
                .setPublic()
                .setName( context.getModelName() );

        modelClass.setSuperType( FORM_MODEL_CLASS );

        modelClass.addAnnotation( ERRAI_PORTABLE );
        modelClass.addAnnotation( ERRAI_BINDABLE );
        modelClass.addAnnotation( INJECT_NAMED ).setStringValue( context.getModelName() );

        constructorGenerator.addNoArgConstructor( modelClass );
        constructorGenerator.addAllFieldsConstructor( context, modelClass );

        return modelClass.toString();
    }
}
