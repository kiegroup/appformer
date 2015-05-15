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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.ProjectService;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.livespark.formmodeler.codegen.util.SourceGenerationUtil;
import org.kie.workbench.common.screens.datamodeller.model.EditorModelContent;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
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
    private ProjectService<? extends Project> projectService;

    @Inject
    private DataModelerService dataModelerService;

    @Inject
    private DataModellerFormGenerator formGenerator;


    public void processResourceAdd( @Observes final ResourceAddedEvent resourceAddedEvent ) {
        generateSources( resourceAddedEvent.getPath() );
    }

    public void processResourceDelete( @Observes final ResourceDeletedEvent resourceDeletedEvent ) {

    }

    public void processResourceUpdate( @Observes final ResourceUpdatedEvent resourceUpdatedEvent ) {
        generateSources( resourceUpdatedEvent.getPath() );
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
            EditorModelContent content = dataModelerService.loadContent( path );
            if ( content != null && content.getDataObject() != null ) {
                DataObject dataObject = content.getDataObject();

                if ( !dataObject.getSuperClassName().equals( SourceGenerationUtil.FORM_MODEL_CLASS )
                        && !dataObject.getSuperClassName().equals( SourceGenerationUtil.FORM_VIEW_CLASS ) )
                    return dataObject;
            }

        } catch ( Exception e ) {
            logger.warn( "Error loading Data Object for path '{}': {}", path.toURI(), e );
        }
        return null;
    }
}
