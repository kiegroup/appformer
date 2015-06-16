package org.livespark.formmodeler.codegen.view.impl.java;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_TEMPLATED;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.LIST_VIEW_CLASS;
import static org.livespark.formmodeler.codegen.view.impl.java.RestCodegenUtil.generateRestCall;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.ListView;


@ListView
@ApplicationScoped
public class RoasterListJavaTemplateSourceGenerator implements FormJavaTemplateSourceGenerator {

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaClassSource viewClass = Roaster.create( JavaClassSource.class );
        String packageName = getPackageName( context );

        addTypeSignature( context, viewClass, packageName );
        addTemplatedAnnotation( viewClass );
        addImports( context, viewClass );

        addMethods( viewClass, context );

        return viewClass.toString();
    }

    private void addMethods( JavaClassSource viewClass,
                             SourceGenerationContext context ) {
        addLoadDataImpl( viewClass, context );
        addRemoteDeleteImpl( viewClass, context );
        addGetFormTypeImpl( viewClass, context );
        addGetFormTitleImpl( viewClass, context );
        addGetFormIdImpl( viewClass, context );
    }

    private void addGetFormTypeImpl( JavaClassSource viewClass,
                                     SourceGenerationContext context ) {
        viewClass.addMethod()
                 .setProtected()
                 .setName( "getFormType" )
                 .setReturnType( "Class<" + context.getFormViewName() + ">" )
                 .setBody( "return " + context.getFormViewName() + ".class;" )
                 .addAnnotation( Override.class );
    }

    private void addGetFormIdImpl( JavaClassSource viewClass,
                                   SourceGenerationContext context ) {
        viewClass.addMethod()
                 .setName( "getFormId" )
                 .setProtected()
                 .setReturnType( String.class )
                 .setBody( "return \"" + context.getFormDefinition().getName() + " Form\";" )
                 .addAnnotation( Override.class );
    }

    private void addGetFormTitleImpl( JavaClassSource viewClass,
                                      SourceGenerationContext context ) {
        viewClass.addMethod()
                 .setName( "getFormTitle" )
                 .setProtected()
                 .setReturnType( String.class )
                 .setBody( "return \"" + context.getFormDefinition().getName() + "Form\";" )
                 .addAnnotation( Override.class );
    }

    private String getPackageName( SourceGenerationContext context ) {
        return context.getLocalPackage().getPackageName();
    }

    private void addTypeSignature( SourceGenerationContext context,
                            JavaClassSource viewClass,
                            String packageName ) {
        viewClass.setPackage( packageName )
                 .setPublic()
                 .setName( context.getListViewName() )
                 .setSuperType( LIST_VIEW_CLASS + "<" + context.getModelName() + ", " + context.getListItemViewName() + ">" );
    }

    private void addTemplatedAnnotation( JavaClassSource viewClass ) {
        viewClass.addAnnotation( ERRAI_TEMPLATED );
    }

    private void addImports( SourceGenerationContext context,
                            JavaClassSource viewClass ) {
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getModelName() );
        viewClass.addImport( context.getLocalPackage().getPackageName() + "." + context.getFormViewName() );
        viewClass.addImport( context.getLocalPackage().getPackageName() + "." + context.getListItemViewName() );
        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getRestServiceName() );
    }

    private void addRemoteDeleteImpl( JavaClassSource viewClass , SourceGenerationContext context  ) {
        MethodSource<JavaClassSource> remoteDelete = viewClass.addMethod();
        remoteDelete.setProtected()
                    .setName( "remoteDelete" )
                    .setReturnType( void.class )
                    .addParameter( context.getModelName(), "model" );
        remoteDelete.addParameter( RemoteCallback.class,
                                   "callback" );
        remoteDelete.addAnnotation( Override.class );

        remoteDelete.setBody( generateRestCall( "delete",
                                                "callback",
                                                context,
                                                "model") );
    }

    private void addLoadDataImpl( JavaClassSource viewClass , SourceGenerationContext context  ) {
        MethodSource<JavaClassSource> loadData = viewClass.addMethod();
        loadData.setProtected()
                .setName( "loadData" )
                .setReturnType( void.class )
                .addParameter( RemoteCallback.class,
                               "callback" );
        loadData.addAnnotation( Override.class );

        loadData.setBody( generateRestCall( "load",
                                            "callback",
                                            context ) );
    }

}
