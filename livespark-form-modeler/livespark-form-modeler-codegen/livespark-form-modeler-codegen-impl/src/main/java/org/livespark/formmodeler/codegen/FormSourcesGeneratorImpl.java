package org.livespark.formmodeler.codegen;

import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.security.shared.api.identity.User;
import org.livespark.formmodeler.codegen.model.FormModelSourceGenerator;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.FormJavaTemplateSourceGenerator;
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


    @Override
    public void generateFormSources( FormDefinition form, Path resourcePath ) {
        SourceGenerationContext context = new SourceGenerationContext( form, resourcePath );

        String modelSource = modelSourceGenerator.generateFormModelSource( context );
        String javaTemplate = javaTemplateSourceGenerator.generateJavaTemplateSource( context );
        String htmlTemplate = htmlTemplateSourceGenerator.generateHTMLTemplateSource( context );

        if ( StringUtils.isEmpty( modelSource ) || StringUtils.isEmpty( javaTemplate ) || StringUtils.isEmpty( htmlTemplate )) {
            log.warn( "Unable to generate the required form assets for Data Object: {}", resourcePath );
            return;
        }

        org.uberfire.java.nio.file.Path path = Paths.convert( resourcePath );
        org.uberfire.java.nio.file.Path parent = path.getParent();

        org.uberfire.java.nio.file.Path modelPath = parent.resolve( context.getModelName() + ".java" );

        ioService.write( modelPath, modelSource, makeCommentedOption( "Added Java Source for Form Model '" + resourcePath + "'" ) );

        org.uberfire.java.nio.file.Path javaPath = parent.resolve( context.getViewName() + ".java" );

        ioService.write( javaPath, javaTemplate, makeCommentedOption( "Added Java Source for Form Template '" + resourcePath + "'" ) );

        org.uberfire.java.nio.file.Path htmlPath = parent.resolve( context.getViewName() + ".html" );

        ioService.write( htmlPath, htmlTemplate, makeCommentedOption( "Added HTML Source for Form Template '" + resourcePath + "'" ) );
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
