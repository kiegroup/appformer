/*
 * Copyright 2012 JBoss Inc
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
package org.livespark.formmodeler.codegen.services.datamodeller;

import static org.livespark.formmodeler.codegen.SourceGenerationContext.ENTITY_SERVICE_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_MODEL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.LIST_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_IMPL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_SERVICE_SUFFIX;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.Project;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.util.SourceGenerationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;
import org.uberfire.workbench.events.ResourceAddedEvent;
import org.uberfire.workbench.events.ResourceCopiedEvent;
import org.uberfire.workbench.events.ResourceDeletedEvent;
import org.uberfire.workbench.events.ResourceRenamedEvent;
import org.uberfire.workbench.events.ResourceUpdatedEvent;

/**
 * Server side component that observes for the Data Objects add/delete/update events
 * and generates the associated form.
 */
@Dependent
public class DataObjectChangeObserver {

    private static final Logger logger = LoggerFactory.getLogger( DataObjectChangeObserver.class );

    @Inject
    private KieProjectService projectService;

    @Inject
    private DataModelerService dataModelerService;

    @Inject
    private DataModellerFormGenerator formGenerator;


    public void processResourceAdd( @Observes final ResourceAddedEvent resourceAddedEvent ) {
        if ( isFormAware( resourceAddedEvent.getPath() ) ) {
            generateSources( resourceAddedEvent.getPath() );
        }
    }

    public void processResourceDelete( @Observes final ResourceDeletedEvent resourceDeletedEvent ) {

    }

    public void processResourceUpdate( @Observes final ResourceUpdatedEvent resourceUpdatedEvent ) {
        if ( isFormAware( resourceUpdatedEvent.getPath() ) ) {
            generateSources( resourceUpdatedEvent.getPath() );
        }
    }

    protected void generateSources (Path path) {
        DataObject dataObject = getDataObjectForPath( path );

        if ( dataObject != null ) formGenerator.generateFormForDataObject( dataObject, path );
    }

    public void processResourceCopied( @Observes final ResourceCopiedEvent resourceCopiedEvent ) {

    }

    public void processResourceRenamed( @Observes final ResourceRenamedEvent resourceRenamedEvent ) {


    }

    protected DataObject getDataObjectForPath(Path path) {
        if (!path.getFileName().endsWith( ".java" )) return null;

        try {
            KieProject project = projectService.resolveProject( path );
            DataModel dataModel = dataModelerService.loadModel( project );
            String className = calculateClassName( project, path );
            DataObject dataObject = dataModel != null ? dataModel.getDataObject( className ) : null;

            if ( dataObject != null ) {
                if ( !dataObject.getSuperClassName().equals( SourceGenerationUtil.FORM_MODEL_CLASS )
                        && !dataObject.getSuperClassName().equals( SourceGenerationUtil.FORM_VIEW_CLASS ) )
                    return dataObject;
            }

        } catch ( Exception e ) {
            logger.warn( "Error loading Data Object for path '{}': {}", path.toURI(), e );
        }
        return null;
    }

    protected boolean isFormAware( final Path path ) {
        return path != null &&
                //TODO review this filtering.
                path.getFileName().endsWith( ".java" ) &&
                !path.getFileName().endsWith( ENTITY_SERVICE_SUFFIX ) &&
                !path.getFileName().endsWith( FORM_MODEL_SUFFIX ) &&
                !path.getFileName().endsWith( FORM_VIEW_SUFFIX ) &&
                !path.getFileName().endsWith( LIST_VIEW_SUFFIX ) &&
                !path.getFileName().endsWith( REST_IMPL_SUFFIX ) &&
                !path.getFileName().endsWith( REST_SERVICE_SUFFIX );
    }

    private String calculateClassName(Project project,
            Path path) {

        Path rootPath = project.getRootPath();
        if (!path.toURI().startsWith(rootPath.toURI())) {
            return null;
        }

        org.guvnor.common.services.project.model.Package defaultPackage = projectService.resolveDefaultPackage(project);
        Path srcPath = null;

        if (path.toURI().startsWith(defaultPackage.getPackageMainSrcPath().toURI())) {
            srcPath = defaultPackage.getPackageMainSrcPath();
        } else if (path.toURI().startsWith(defaultPackage.getPackageTestSrcPath().toURI())) {
            srcPath = defaultPackage.getPackageTestSrcPath();
        }

        //project: default://master@uf-playground/mortgages/main/src/Pojo.java
        if (srcPath == null) {
            return null;
        }

        String strPath = path.toURI().substring(srcPath.toURI().length() + 1, path.toURI().length());
        strPath = strPath.replace("/", ".");
        strPath = strPath.substring(0, strPath.indexOf(".java"));

        return strPath;
    }
}
