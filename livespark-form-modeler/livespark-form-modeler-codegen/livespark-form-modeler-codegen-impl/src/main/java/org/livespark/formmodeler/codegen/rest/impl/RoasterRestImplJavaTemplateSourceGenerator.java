package org.livespark.formmodeler.codegen.rest.impl;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.EJB_STATELESS;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.model.impl.ModelConstructorGenerator;
import org.livespark.formmodeler.codegen.rest.RestImpl;


@ApplicationScoped
@RestImpl
public class RoasterRestImplJavaTemplateSourceGenerator extends RoasterRestJavaTemplateSourceGenerator<JavaClassSource> {

    @Inject
    ModelConstructorGenerator constructorGenerator;

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaClassSource restImpl = Roaster.create( JavaClassSource.class );
        String packageName = getPackageName( context );

        addImports( context, restImpl, packageName );
        addFields( context, restImpl );
        addTypeSignature( context, restImpl, packageName );
        addTypeAnnotations( context, restImpl );
        addCrudMethodImpls( context, restImpl );

        return restImpl.toString();
    }

    private void addFields( SourceGenerationContext context,
                            JavaClassSource restImpl ) {
        restImpl.addField()
                .setPrivate()
                .setType( context.getEntityServiceName() )
                .addAnnotation( Inject.class );
    }

    private void addTypeAnnotations( SourceGenerationContext context,
                                     JavaClassSource restImpl ) {
        restImpl.addAnnotation( EJB_STATELESS );
    }

    @Override
    protected void addImports( SourceGenerationContext context,
                               JavaClassSource restIface,
                               String packageName ) {
        super.addImports( context, restIface, packageName );
        restIface.addImport( packageName + "." + context.getRestServiceName() );
    }

    private void addCrudMethodImpls( SourceGenerationContext context,
                                     JavaClassSource restImpl ) {
        addCreateMethodImpl( context, restImpl );
        addLoadMethodImpl( context, restImpl );
        addDeleteMethodImpl( context, restImpl );
    }

    private void addDeleteMethodImpl( SourceGenerationContext context,
                                      JavaClassSource restImpl ) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented.");
    }

    private void addLoadMethodImpl( SourceGenerationContext context,
                                    JavaClassSource restImpl ) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented.");
    }

    private void addCreateMethodImpl( SourceGenerationContext context,
                                      JavaClassSource restImpl ) {
        MethodSource<JavaClassSource> create = restImpl.addMethod();
        setCreateMethodSignature( context, create );

    }

    @Override
    protected void setCreateMethodSignature( SourceGenerationContext context,
                                             MethodSource<JavaClassSource> create ) {
        super.setCreateMethodSignature( context, create );
        create.addAnnotation( Override.class );
        addCreateMethodBody( context, create );
    }

    private void addCreateMethodBody( SourceGenerationContext context,
                                      MethodSource<JavaClassSource> create ) {
        String invocation = constructorGenerator.getConstructorInvocation( context );
        create.setBody( "return " + invocation + ";" );
    }

    private void addTypeSignature( SourceGenerationContext context,
                                   JavaClassSource restImpl,
                                   String packageName ) {
        restImpl.setPackage( packageName )
                .setPublic()
                .setName( context.getRestServiceName() + "Impl" )
                .addInterface( context.getRestServiceName() );
    }

}
