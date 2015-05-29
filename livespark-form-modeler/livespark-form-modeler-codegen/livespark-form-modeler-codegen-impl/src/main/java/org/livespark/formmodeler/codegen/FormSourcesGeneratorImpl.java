package org.livespark.formmodeler.codegen;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.guvnor.common.services.project.model.Package;
import org.jboss.errai.security.shared.api.identity.User;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.model.FormModelSourceGenerator;
import org.livespark.formmodeler.codegen.rest.EntityService;
import org.livespark.formmodeler.codegen.rest.RestApi;
import org.livespark.formmodeler.codegen.rest.RestImpl;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListItemView;
import org.livespark.formmodeler.codegen.view.ListView;
import org.livespark.formmodeler.model.FormDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.base.options.CommentedOption;

/**
 * Created by pefernan on 5/5/15.
 */
@ApplicationScoped
public class FormSourcesGeneratorImpl implements FormSourcesGenerator {
    private static transient Logger log = LoggerFactory.getLogger( FormSourcesGeneratorImpl.class );

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private User identity;

    @Inject
    private KieProjectService projectService;

    @Inject
    private FormModelSourceGenerator modelSourceGenerator;

    @Inject
    private FormJavaTemplateSourceGenerator javaTemplateSourceGenerator;

    @Inject
    private FormHTMLTemplateSourceGenerator htmlTemplateSourceGenerator;

    @Inject
    @ListView
    private FormJavaTemplateSourceGenerator javaListTemplateSourceGenerator;

    @Inject
    @ListItemView
    private FormJavaTemplateSourceGenerator javaListItemTemplateSourceGenerator;

    @Inject
    @ListItemView
    private FormHTMLTemplateSourceGenerator htmlListItemTemplateSourceGenerator;

    @Inject
    @RestApi
    private FormJavaTemplateSourceGenerator javaRestTemplateSourceGenerator;

    @Inject
    @EntityService
    private FormJavaTemplateSourceGenerator javaEntityServiceTemplateSourceGenerator;

    @Inject
    @RestImpl
    private FormJavaTemplateSourceGenerator javaRestImplTemplateSourceGenerator;

    @Override
    public void generateFormSources( FormDefinition form, Path resourcePath ) {
        Package resPackage = projectService.resolvePackage( resourcePath );

        Package root = getRootPackage( resPackage );

        Package client = getOrCreateClientPackage( root );
        Package local = getOrCreateLocalPackage( client );
        Package shared = getOrCreateSharedPackage( client );
        Package server = getOrCreateServerPackage( root );

        SourceGenerationContext context = new SourceGenerationContext( form, resourcePath, root, local, shared, server );

        String modelSource = modelSourceGenerator.generateFormModelSource( context );

        String javaTemplate = javaTemplateSourceGenerator.generateJavaTemplateSource( context );
        String htmlTemplate = htmlTemplateSourceGenerator.generateHTMLTemplateSource( context );

        String listJavaTemplate = javaListTemplateSourceGenerator.generateJavaTemplateSource( context );
        String listItemJavaTemplate = javaListItemTemplateSourceGenerator.generateJavaTemplateSource( context );
        String htmlListItemTemplate = htmlListItemTemplateSourceGenerator.generateHTMLTemplateSource( context );

        String restServiceTemplate = javaRestTemplateSourceGenerator.generateJavaTemplateSource( context );
        String restImplTemplate = javaRestImplTemplateSourceGenerator.generateJavaTemplateSource( context );
        String entityServiceTemplate = javaEntityServiceTemplateSourceGenerator.generateJavaTemplateSource( context );

        if ( !allNonEmpty( resourcePath,
                             modelSource,
                             javaTemplate,
                             htmlTemplate,
                             listJavaTemplate,
                             listItemJavaTemplate,
                             htmlListItemTemplate,
                             restServiceTemplate,
                             restImplTemplate,
                             entityServiceTemplate ) ) {
            log.warn( "Unable to generate the required form assets for Data Object: {}", resourcePath );
            return;
        }

        org.uberfire.java.nio.file.Path parent = Paths.convert( resourcePath ).getParent();

        ioService.startBatch( parent.getFileSystem() );
        try {
            writeJavaSource( resourcePath, context.getModelName(), modelSource, shared );
            writeJavaSource( resourcePath, context.getFormViewName(), javaTemplate, local );
            writeJavaSource( resourcePath, context.getListViewName(), listJavaTemplate, local );
            writeJavaSource( resourcePath, context.getListItemViewName(), listItemJavaTemplate, local );
            writeJavaSource( resourcePath, context.getRestServiceName(), restServiceTemplate, shared );
            writeJavaSource( resourcePath, context.getRestServiceImplName(), restImplTemplate, server );
            writeJavaSource( resourcePath, context.getEntityServiceName(), entityServiceTemplate, server );

            writeHTMLSource( resourcePath, context.getFormViewName(), htmlTemplate, local );
            writeHTMLSource( resourcePath, context.getListItemViewName(), htmlListItemTemplate, local );
        } catch ( Exception e ) {
            log.error( "It was not possible to generate form sources for file: " + resourcePath + " due to the following errors.", e );
        } finally {
            ioService.endBatch();
        }
    }

    private Package getOrCreateServerPackage( Package root ) {
        return getOrCreateSubpackage( root, "server" );
    }

    private Package getOrCreateSharedPackage( Package client ) {
        return getOrCreateSubpackage( client, "shared" );
    }

    private Package getOrCreateLocalPackage( Package client ) {
        return getOrCreateSubpackage( client, "local" );
    }

    private Package getOrCreateClientPackage( Package root ) {
        return getOrCreateSubpackage( root, "client" );
    }

    private Package getOrCreateSubpackage( Package root, String subPackage ) {
        Path fullPath = PathFactory.newPath( "/", root.getPackageMainSrcPath().toURI() + "/" + subPackage );
        Package resolved = projectService.resolvePackage( fullPath );

        if ( resolved == null ) resolved = projectService.newPackage( root, subPackage );

        return resolved;
    }

    private Package getRootPackage( Package resPackage ) {
        if ( !resPackage.getPackageName().endsWith( "client.shared" ) ) return resPackage;

        Package cur = resPackage;
        while ( cur.getPackageName().matches( ".*\\.client(\\..*)?" ) )
            cur = projectService.resolveParentPackage( cur );

        return cur;
    }

    private boolean allNonEmpty(Path resourcePath, String... templates) {
        for ( String template : templates ) {
            if ( StringUtils.isEmpty( template ) ) {
                return false;
            }
        }

        return true;
    }

    private void writeHTMLSource( Path dataObjectPath,
                                  String name,
                                  String htmlTemplate,
                                  Package sourcePackage ) {
        org.uberfire.java.nio.file.Path parentPath = Paths.convert( sourcePackage.getPackageMainResourcesPath() );
        org.uberfire.java.nio.file.Path htmlPath = parentPath.resolve( name + ".html" );

        ioService.write( htmlPath,
                         htmlTemplate,
                         makeCommentedOption( "Added HTML Source for Form Template '" + dataObjectPath + "'" ) );
    }

    private void writeJavaSource( Path dataObjectPath,
                                  String name,
                                  String javaSource,
                                  Package sourcePackage ) {
        org.uberfire.java.nio.file.Path parentPath = Paths.convert( sourcePackage.getPackageMainSrcPath() );
        org.uberfire.java.nio.file.Path filePath = parentPath.resolve( name + ".java" );
        ioService.write( filePath,
                         javaSource,
                         makeCommentedOption( "Added Java Source for Form Model '" + dataObjectPath + "'" ) );
    }

    public CommentedOption makeCommentedOption( String commitMessage ) {
        final String name = identity.getIdentifier();
        final Date when = new Date();

        final CommentedOption option = new CommentedOption( name,
                null,
                commitMessage,
                when );
        return option;
    }
}
