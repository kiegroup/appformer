package org.livespark.formmodeler.codegen;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.security.shared.api.identity.User;
import org.livespark.formmodeler.codegen.model.FormModelSourceGenerator;
import org.livespark.formmodeler.codegen.rest.RestApi;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListItemView;
import org.livespark.formmodeler.codegen.view.ListView;
import org.livespark.formmodeler.model.FormDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
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

    @Override
    public void generateFormSources( FormDefinition form, Path resourcePath ) {
        SourceGenerationContext context = new SourceGenerationContext( form, resourcePath );

        String modelSource = modelSourceGenerator.generateFormModelSource( context );

        String javaTemplate = javaTemplateSourceGenerator.generateJavaTemplateSource( context );
        String htmlTemplate = htmlTemplateSourceGenerator.generateHTMLTemplateSource( context );

        String listJavaTemplate = javaListTemplateSourceGenerator.generateJavaTemplateSource( context );
        String listItemJavaTemplate = javaListItemTemplateSourceGenerator.generateJavaTemplateSource( context );
        String htmlListItemTemplate = htmlListItemTemplateSourceGenerator.generateHTMLTemplateSource( context );

        String restServiceTemplate = javaRestTemplateSourceGenerator.generateJavaTemplateSource( context );

        if ( StringUtils.isEmpty( modelSource )
                || StringUtils.isEmpty( javaTemplate )
                || StringUtils.isEmpty( htmlTemplate )
                || StringUtils.isEmpty( listJavaTemplate )
                || StringUtils.isEmpty( listItemJavaTemplate )
                || StringUtils.isEmpty( htmlListItemTemplate )
                || StringUtils.isEmpty( restServiceTemplate ) ) {
            log.warn( "Unable to generate the required form assets for Data Object: {}",
                      resourcePath );
            return;
        }

        org.uberfire.java.nio.file.Path parent = Paths.convert( resourcePath ).getParent();

        ioService.startBatch( parent.getFileSystem() );
        try {
            writeJavaSource( resourcePath, context.getModelName(), modelSource, parent );
            writeJavaSource( resourcePath, context.getFormViewName(), javaTemplate, parent );
            writeJavaSource( resourcePath, context.getListViewName(), listJavaTemplate, parent );
            writeJavaSource( resourcePath, context.getListItemViewName(), listItemJavaTemplate, parent );
            writeJavaSource( resourcePath, context.getRestServiceName(), restServiceTemplate, parent );

            writeHTMLSource( resourcePath, context.getFormViewName(), htmlTemplate, parent );
            writeHTMLSource( resourcePath, context.getListItemViewName(), htmlListItemTemplate, parent );
        } catch ( Exception e ) {
            log.error( "It was not possible to generate form sources for file: " + resourcePath + " due to the following errors.", e );
        } finally {
            ioService.endBatch();
        }
    }

    private void writeHTMLSource( Path resourcePath,
                                  String name,
                                  String htmlTemplate,
                                  org.uberfire.java.nio.file.Path parent ) {
        org.uberfire.java.nio.file.Path htmlPath = parent.resolve( name + ".html" );

        ioService.write( htmlPath,
                         htmlTemplate,
                         makeCommentedOption( "Added HTML Source for Form Template '" + resourcePath + "'" ) );
    }

    private void writeJavaSource( Path resourcePath,
                                  String name,
                                  String javaSource,
                                  org.uberfire.java.nio.file.Path parent ) {
        org.uberfire.java.nio.file.Path filePath = parent.resolve( name + ".java" );
        ioService.write( filePath,
                         javaSource,
                         makeCommentedOption( "Added Java Source for Form Model '" + resourcePath + "'" ) );
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
