package org.livespark.formmodeler.codegen.view.impl.java;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.RestService;
import org.livespark.formmodeler.model.FieldDefinition;


@ApplicationScoped
@RestService
public class RoasterRestServiceJavaTemplateSourceGenerator implements FormJavaTemplateSourceGenerator {

    @Inject
    private KieProjectService projectService;

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
        MethodSource<JavaInterfaceSource> create = restIface.addMethod(  )
                 .setName( "create" )
                 .setReturnType( context.getModelName() );
        create.addAnnotation( Path.class ).setLiteralValue( "\"create\"" );

        for (FieldDefinition<?> field : context.getFormDefinition().getFields()) {
            create.addParameter( field.getStandaloneClassName(), field.getName() );
        }
    }

    private void addLoadMethod( SourceGenerationContext context,
                                JavaInterfaceSource restIface ) {
        MethodSource<JavaInterfaceSource> load = restIface.addMethod(  )
                 .setName( "load" )
                 .setReturnType( "List<" + context.getModelName() + ">" );
        load.addAnnotation( Path.class ).setLiteralValue( "\"load\"" );
    }

    private void addDeleteMethod( SourceGenerationContext context,
                                  JavaInterfaceSource restIface ) {
        MethodSource<JavaInterfaceSource> delete = restIface.addMethod(  )
                 .setName( "delete" )
                 .setReturnType( Boolean.class );
        delete.addAnnotation( Path.class ).setLiteralValue( "\"delete\"" );
        // TODO The parameter should be a unique identifier, not the entire model.
        delete.addParameter( context.getModelName(), "model" );
    }

    private String getPackageName( SourceGenerationContext context ) {
        return projectService.resolvePackage( context.getPath() ).getPackageName();
    }

    private void addTypeSignature( SourceGenerationContext context,
                                   JavaInterfaceSource restIface,
                                   String packageName ) {
        restIface.setPackage( packageName )
                 .setPublic()
                 .setName( context.getRestServiceName() );
    }

    private void addTypeLevelPath( JavaInterfaceSource restIface , SourceGenerationContext context  ) {
        restIface.addAnnotation( Path.class ).setLiteralValue( "\"" + context.getModelName().toLowerCase() + "\"" );
    }

    private void addImports( SourceGenerationContext context,
                            JavaInterfaceSource restIface,
                            String packageName ) {
        restIface.addImport( packageName + "." + context.getModelName() );
        restIface.addImport( List.class );
    }

}
