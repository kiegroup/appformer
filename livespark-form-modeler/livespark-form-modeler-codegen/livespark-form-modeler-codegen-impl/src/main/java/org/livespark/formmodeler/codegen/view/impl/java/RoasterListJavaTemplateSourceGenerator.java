package org.livespark.formmodeler.codegen.view.impl.java;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_TEMPLATED;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.LIST_VIEW_CLASS;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.LIST_VIEW_HTML_PATH;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListView;


@ListView
@ApplicationScoped
public class RoasterListJavaTemplateSourceGenerator implements FormJavaTemplateSourceGenerator {

    @Inject
    private KieProjectService projectService;

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaClassSource viewClass = Roaster.create( JavaClassSource.class );

        String packageName = projectService.resolvePackage( context.getPath() ).getPackageName();

        viewClass.setPackage( packageName )
                 .setPublic()
                 .setName( context.getListViewName() )
                 .setSuperType( LIST_VIEW_CLASS + "<" + context.getModelName() + ", " + context.getListItemViewName() + ">" );


        viewClass.addImport( packageName + "." + context.getModelName() );
        viewClass.addImport( packageName + "." + context.getListItemViewName() );

        viewClass.addAnnotation( ERRAI_TEMPLATED ).setStringValue( LIST_VIEW_HTML_PATH );

        // TODO Implement callback
        MethodSource<JavaClassSource> loadData = viewClass.addMethod();
        loadData.setProtected()
                .setName( "loadData" )
                .setReturnType( void.class )
                .setBody( "throw new RuntimeException(\"Not yet implemented.\");" )
                .addParameter( RemoteCallback.class, "callback" );
        loadData.addAnnotation( Override.class );

        // TODO Implement callback
        MethodSource<JavaClassSource> remoteDelete = viewClass.addMethod();
        remoteDelete.setProtected()
                    .setName( "remoteDelete" )
                    .setReturnType( void.class )
                    .setBody( "throw new RuntimeException(\"Not yet implemented.\");" )
                    .addParameter( RemoteCallback.class, "callback" );
        remoteDelete.addAnnotation( Override.class );

        return viewClass.toString();
    }

}
