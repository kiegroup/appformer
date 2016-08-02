/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.formmodeler.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.project.model.Package;
import org.kie.workbench.common.forms.commons.layout.FormLayoutTemplateGenerator;
import org.kie.workbench.common.forms.commons.layout.Static;
import org.kie.workbench.common.forms.editor.service.FormDefinitionSerializer;
import org.kie.workbench.common.forms.editor.service.VFSFormFinderService;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.model.FormModelSourceGenerator;
import org.livespark.formmodeler.codegen.rest.EntityService;
import org.livespark.formmodeler.codegen.rest.RestApi;
import org.livespark.formmodeler.codegen.rest.RestImpl;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListView;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.base.options.CommentedOption;

@ApplicationScoped
public class FormSourcesGeneratorImpl implements FormSourcesGenerator {
    private static transient Logger log = LoggerFactory.getLogger( FormSourcesGeneratorImpl.class );

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private KieProjectService projectService;

    @Inject
    private DataModelerService dataModelerService;

    @Inject
    private FormModelSourceGenerator modelSourceGenerator;

    @Inject
    private FormJavaTemplateSourceGenerator javaTemplateSourceGenerator;

    @Inject
    private FormHTMLTemplateSourceGenerator htmlTemplateSourceGenerator;

    @Inject
    private FormDefinitionSerializer formDefinitionSerializer;

    @Inject
    @Static
    private FormLayoutTemplateGenerator formLayoutTemplateGenerator;

    @Inject
    @ListView
    private FormJavaTemplateSourceGenerator javaListTemplateSourceGenerator;

    @Inject
    @ListView
    private FormHTMLTemplateSourceGenerator htmlListTemplateSourceGenerator;

    @Inject
    @RestApi
    private FormJavaTemplateSourceGenerator javaRestTemplateSourceGenerator;

    @Inject
    @EntityService
    private FormJavaTemplateSourceGenerator javaEntityServiceTemplateSourceGenerator;

    @Inject
    @RestImpl
    private FormJavaTemplateSourceGenerator javaRestImplTemplateSourceGenerator;

    @Inject
    private ErraiAppPropertiesGenerator serializableTypesGenerator;

    @Inject
    private VFSFormFinderService vfsFormFinderService;

    @Inject
    private CommentedOptionFactory commentedOptionFactory;

    @Override
    public void generateEntityFormSources(FormDefinition form, Path resourcePath) {
        Package resPackage = projectService.resolvePackage(resourcePath);
        KieProject project = projectService.resolveProject(resourcePath);

        Package root = getRootPackage(resPackage);

        Package client = getOrCreateClientPackage(root);
        Package local = getOrCreateLocalPackage(client);
        Package shared = getOrCreateSharedPackage(client);
        Package server = getOrCreateServerPackage(root);

        SourceGenerationContext context = new SourceGenerationContext( form,
                resourcePath, root, local, shared, server,
                vfsFormFinderService.findAllForms( resourcePath ) );

        String modelSource = modelSourceGenerator.generateFormModelSource( context );

        if ( form.getLayoutTemplate() == null ) {
            form.setLayoutTemplate( formLayoutTemplateGenerator.generateLayoutTemplate( form ) );
        }

        String formTemplateLayout = formDefinitionSerializer.serialize( form );

        String javaTemplate = javaTemplateSourceGenerator.generateJavaTemplateSource( context );
        String htmlTemplate = htmlTemplateSourceGenerator.generateHTMLTemplateSource( context );

        String listJavaTemplate = javaListTemplateSourceGenerator.generateJavaTemplateSource( context );
        String listHtmlTemplate = htmlListTemplateSourceGenerator.generateHTMLTemplateSource( context );

        String restServiceTemplate = javaRestTemplateSourceGenerator.generateJavaTemplateSource( context );
        String restImplTemplate = javaRestImplTemplateSourceGenerator.generateJavaTemplateSource( context );
        String entityServiceTemplate = javaEntityServiceTemplateSourceGenerator.generateJavaTemplateSource( context );

        String serializableTypesDeclaration = serializableTypesGenerator.generate( getSerializableTypeClassNames( project ) );

        if ( !allNonEmpty( resourcePath,
                modelSource,
                formTemplateLayout,
                javaTemplate,
                htmlTemplate,
                listJavaTemplate,
                listHtmlTemplate,
                restServiceTemplate,
                restImplTemplate,
                entityServiceTemplate,
                serializableTypesDeclaration ) ) {
            log.warn( "Unable to generate the required form assets for Data Object: {}", resourcePath );
            return;
        }

        org.uberfire.java.nio.file.Path parent = Paths.convert( resourcePath ).getParent();

        ioService.startBatch( parent.getFileSystem() );
        try {
            writeJavaSource( resourcePath, context.getFormModelName(), modelSource, shared );
            writeFormTemplate( resourcePath, form.getName(), formTemplateLayout, shared );

            writeJavaSource( resourcePath, context.getFormViewName(), javaTemplate, local );
            writeJavaSource( resourcePath, context.getListViewName(), listJavaTemplate, local );
            writeJavaSource( resourcePath, context.getRestServiceName(), restServiceTemplate, shared );
            writeJavaSource( resourcePath, context.getRestServiceImplName(), restImplTemplate, server );
            writeJavaSource( resourcePath, context.getEntityServiceName(), entityServiceTemplate, server );

            writeHTMLSource( resourcePath, context.getFormViewName(), htmlTemplate, local );
            writeHTMLSource( resourcePath, context.getListViewName(), listHtmlTemplate, local );

            writeErraiAppProperties( serializableTypesDeclaration, project );
        } catch ( Exception e ) {
            log.error( "It was not possible to generate form sources for file: " + resourcePath + " due to the following errors.", e );
        } finally {
            ioService.endBatch();
        }
    }

    @Override
    public void generateFormSources( FormDefinition form, Path resourcePath ) {
        Package resPackage = projectService.resolvePackage( resourcePath );

        Package root = getRootPackage( resPackage );

        Package client = getOrCreateClientPackage( root );
        Package local = getOrCreateLocalPackage( client );
        Package shared = getOrCreateSharedPackage( client );

        SourceGenerationContext context = new SourceGenerationContext( form, resourcePath, root, local, shared, null,
                vfsFormFinderService.findAllForms( resourcePath ) );

        String modelSource = modelSourceGenerator.generateFormModelSource( context );

        String javaTemplate = javaTemplateSourceGenerator.generateJavaTemplateSource( context );
        String htmlTemplate = htmlTemplateSourceGenerator.generateHTMLTemplateSource( context );

        if ( !allNonEmpty( resourcePath,
                             modelSource,
                             javaTemplate,
                             htmlTemplate ) ) {
            log.warn( "Unable to generate the required form assets for Data Object: {}", resourcePath );
            return;
        }

        org.uberfire.java.nio.file.Path parent = Paths.convert( resourcePath ).getParent();

        ioService.startBatch( parent.getFileSystem() );
        try {
            writeJavaSource( resourcePath, context.getFormModelName(), modelSource, shared );
            writeJavaSource( resourcePath, context.getFormViewName(), javaTemplate, local );
            writeHTMLSource( resourcePath, context.getFormViewName(), htmlTemplate, local );
        } catch ( Exception e ) {
            log.error( "It was not possible to generate form sources for file: " + resourcePath + " due to the following errors.", e );
        } finally {
            ioService.endBatch();
        }
    }

    private Collection<String> getSerializableTypeClassNames( KieProject project ) {
        Set<DataObject> dataObjects = dataModelerService.loadModel( project ).getDataObjects();
        Collection<String> retVal = new ArrayList<String>( dataObjects.size() );

        for ( DataObject dataObject : dataObjects ) {
            String className = dataObject.getClassName();
            if ( isNotDerivedObject( className ) ) {
                retVal.add( className );
            }
        }

        return retVal;
    }

    private boolean isNotDerivedObject( String className ) {
        // TODO figure out a less hacky implementation of this method
        return !( className.endsWith( SourceGenerationContext.ENTITY_SERVICE_SUFFIX )
        || className.endsWith( SourceGenerationContext.FORM_MODEL_SUFFIX )
        || className.endsWith( SourceGenerationContext.FORM_VIEW_SUFFIX )
        || className.endsWith( SourceGenerationContext.LIST_VIEW_SUFFIX )
        || className.endsWith( SourceGenerationContext.REST_IMPL_SUFFIX )
        || className.endsWith( SourceGenerationContext.REST_SERVICE_SUFFIX )
        || className.contains( ".builtin." )
        || className.contains( ".server." )
        || className.contains( ".backend." ) );
    }

    private void writeErraiAppProperties( String serializableTypesDeclaration, KieProject project  ) {
        Package defaultPackage = projectService.resolveDefaultPackage( project );
        Path resourceRoot = defaultPackage.getPackageMainResourcesPath();

        org.uberfire.java.nio.file.Path parentPath = Paths.convert( resourceRoot );
        org.uberfire.java.nio.file.Path filePath = parentPath.resolve( "ErraiApp.properties" );
        ioService.write( filePath,
                         serializableTypesDeclaration,
                         makeCommentedOption( "Updated ErraiApp.properties." ) );
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

    private void writeFormTemplate( Path dataObjectPath,
                                  String name,
                                  String formTemplate,
                                  Package sourcePackage ) {
        org.uberfire.java.nio.file.Path parentPath = Paths.convert( sourcePackage.getPackageMainResourcesPath() );
        org.uberfire.java.nio.file.Path htmlPath = parentPath.resolve( name + ".frm" );

        ioService.write( htmlPath,
                formTemplate,
                makeCommentedOption( "Added HTML Source for Form Template '" + dataObjectPath + "'" ) );
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
        return commentedOptionFactory.makeCommentedOption( commitMessage );
    }
}
