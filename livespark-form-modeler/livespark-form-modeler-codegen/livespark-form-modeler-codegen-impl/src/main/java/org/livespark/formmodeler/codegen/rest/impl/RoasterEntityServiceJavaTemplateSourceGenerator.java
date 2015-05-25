package org.livespark.formmodeler.codegen.rest.impl;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.EJB_REQUIRES_NEW;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.EJB_STATELESS;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.EJB_TRANSACTION_ATTR;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ENTITY_SERVICE_CLASS;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.rest.EntityService;


@ApplicationScoped
@EntityService
public class RoasterEntityServiceJavaTemplateSourceGenerator implements FormJavaTemplateSourceGenerator {

    @Inject
    private KieProjectService projectService;

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaClassSource entityService = Roaster.create( JavaClassSource.class );
        String packageName = getPackageName( context );

        addTypeSignature( context, entityService, packageName );
        addMethods( context, entityService );

        return entityService.toString();
    }

    private void addMethods( SourceGenerationContext context,
                             JavaClassSource entityService ) {
        entityService.addMethod()
                     .setProtected()
                     .setName( "getEntityServiceType" )
                     .setReturnType( "Class<" + context.getDataObjectName() + ">" )
                     .setBody( "return " + context.getDataObjectName() + ".class;" )
                     .addAnnotation( Override.class );
    }

    private void addTypeSignature( SourceGenerationContext context,
                                   JavaClassSource entityService,
                                   String packageName ) {
        entityService.setPackage( packageName )
                     .setPublic()
                     .setName( context.getEntityServiceName() )
                     .setSuperType( ENTITY_SERVICE_CLASS + "<" + context.getDataObjectName() + ">" );
        entityService.addAnnotation( EJB_STATELESS );
        entityService.addAnnotation( EJB_TRANSACTION_ATTR )
                     .setLiteralValue( EJB_REQUIRES_NEW );
    }

    private String getPackageName( SourceGenerationContext context ) {
        return projectService.resolvePackage( context.getPath() ).getPackageName();
    }

}
