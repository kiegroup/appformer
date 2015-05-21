package org.livespark.formmodeler.codegen.view.impl.java;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.LIST_ITEM_VIEW_CLASS;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListItemView;


@ListItemView
@ApplicationScoped
public class RoasterListItemJavaSourceGenerator implements FormJavaTemplateSourceGenerator {

    @Inject
    private KieProjectService projectService;

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaClassSource viewClass = Roaster.create( JavaClassSource.class );

        String packageName = projectService.resolvePackage( context.getPath() ).getPackageName();

        viewClass.setPackage( packageName )
                 .setPublic()
                 .setName( context.getListItemViewName() )
                 .setSuperType( LIST_ITEM_VIEW_CLASS + "<" + context.getModelName() + ">" );


        viewClass.addImport( packageName + "." + context.getModelName() );

        // TODO Generate templated file
//        viewClass.addAnnotation( ERRAI_TEMPLATED ).setStringValue( LIST_VIEW_HTML_PATH );

        // TODO Implement callback
        MethodSource<JavaClassSource> initInputNames = viewClass.addMethod();
        initInputNames.setProtected()
                .setName( "initInputNames" )
                .setReturnType( void.class )
                .setBody( "throw new RuntimeException(\"Not yet implemented.\");" )
                .addAnnotation( Override.class );

        return viewClass.toString();
    }

}
