package org.livespark.formmodeler.codegen.rest.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.rest.RestApi;


@ApplicationScoped
@RestApi
public class RoasterRestApiJavaTemplateSourceGenerator extends RoasterRestJavaTemplateSourceGenerator<JavaInterfaceSource> {

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaInterfaceSource restIface = Roaster.create( JavaInterfaceSource.class );
        String packageName = getPackageName( context );

        addTypeSignature( context, restIface, packageName );
        addTypeLevelPath( restIface, context );
        addImports( context, restIface, packageName );
        addCrudMethods( context, restIface );

        return restIface.toString();
    }

    private void addCrudMethods( SourceGenerationContext context,
                                 JavaInterfaceSource restIface ) {
        addCreateMethod( context, restIface );
        addLoadMethod( context, restIface );
        addDeleteMethod( context, restIface );
    }

    private void addCreateMethod( SourceGenerationContext context,
                                  JavaInterfaceSource restIface ) {
        MethodSource<JavaInterfaceSource> create = restIface.addMethod(  );

        setCreateMethodSignature( context, create );
        addCreateAnnotations( create );
    }

    private void addCreateAnnotations( MethodSource<JavaInterfaceSource> create ) {
        create.addAnnotation( Path.class ).setStringValue( "create" );
        create.addAnnotation( POST.class );
    }

    private void addLoadMethod( SourceGenerationContext context,
                                JavaInterfaceSource restIface ) {
        MethodSource<JavaInterfaceSource> load = restIface.addMethod(  );
        setLoadMethodSignature( context, load );
        addLoadMethodAnnotations( load );
    }

    private void addLoadMethodAnnotations( MethodSource<JavaInterfaceSource> load ) {
        load.addAnnotation( Path.class ).setStringValue( "load" );
        load.addAnnotation( GET.class );
    }

    private void addDeleteMethod( SourceGenerationContext context,
                                  JavaInterfaceSource restIface ) {
        MethodSource<JavaInterfaceSource> delete = restIface.addMethod(  );
        setDeleteMethodSignature( context, delete );
        addDeleteMethodAnnotations( delete );
    }

    private void addDeleteMethodAnnotations( MethodSource<JavaInterfaceSource> delete ) {
        delete.addAnnotation( Path.class ).setStringValue( "delete" );
        delete.addAnnotation( DELETE.class );
    }

    private void addTypeSignature( SourceGenerationContext context,
                                   JavaInterfaceSource restIface,
                                   String packageName ) {
        restIface.setPackage( packageName )
                 .setPublic()
                 .setName( context.getRestServiceName() );
    }

    private void addTypeLevelPath( JavaInterfaceSource restIface , SourceGenerationContext context  ) {
        restIface.addAnnotation( Path.class ).setStringValue( context.getFormDefinition().getName().toLowerCase() );
    }

}
