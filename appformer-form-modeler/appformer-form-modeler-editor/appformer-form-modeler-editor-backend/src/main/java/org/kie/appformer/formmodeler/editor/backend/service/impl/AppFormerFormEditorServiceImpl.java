/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.kie.appformer.formmodeler.editor.backend.service.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.identity.User;
import org.kie.appformer.formmodeler.codegen.FormSourcesGenerator;
import org.kie.workbench.common.forms.editor.backend.service.impl.FormEditorServiceImpl;
import org.kie.workbench.common.forms.editor.model.FormModelerContent;
import org.kie.workbench.common.forms.editor.service.backend.FormModelHandlerManager;
import org.kie.workbench.common.forms.editor.service.shared.VFSFormFinderService;
import org.kie.workbench.common.forms.serialization.FormDefinitionSerializer;
import org.kie.workbench.common.forms.service.shared.FieldManager;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.workbench.events.ResourceOpenedEvent;

@Service
@ApplicationScoped
@Specializes
public class AppFormerFormEditorServiceImpl extends FormEditorServiceImpl {

    protected FormSourcesGenerator formSourcesGenerator;

    @Inject
    public AppFormerFormEditorServiceImpl(@Named("ioStrategy") IOService ioService,
                                          User identity,
                                          SessionInfo sessionInfo,
                                          Event<ResourceOpenedEvent> resourceOpenedEvent,
                                          FieldManager fieldManager,
                                          FormModelHandlerManager modelHandlerManager,
                                          KieProjectService projectService,
                                          FormDefinitionSerializer formDefinitionSerializer,
                                          VFSFormFinderService vfsFormFinderService,
                                          FormSourcesGenerator formSourcesGenerator) {
        super(ioService,
              identity,
              sessionInfo,
              resourceOpenedEvent,
              fieldManager,
              modelHandlerManager,
              projectService,
              formDefinitionSerializer,
              vfsFormFinderService);

        this.formSourcesGenerator = formSourcesGenerator;
    }

    @Override
    public Path save(Path path,
                     FormModelerContent content,
                     Metadata metadata,
                     String comment) {

        Path result = super.save(path,
                                 content,
                                 metadata,
                                 comment);

        formSourcesGenerator.generateFormSources(content.getDefinition(),
                                                 path);

        return result;
    }

    /*
    @Override
    protected boolean isDataObjectBanned( DataObject dataObject ) {
        return dataObject.getSuperClassName().equals( SourceGenerationUtil.LIST_VIEW_CLASS )
                || dataObject.getSuperClassName().equals( SourceGenerationUtil.FORM_VIEW_CLASS )
                || dataObject.getSuperClassName().equals( SourceGenerationUtil.FORM_MODEL_CLASS )
                || dataObject.getSuperClassName().equals( SourceGenerationUtil.ENTITY_SERVICE_CLASS )
                || dataObject.getSuperClassName().equals( SourceGenerationUtil.BASE_REST_SERVICE )
                || dataObject.getClassName().endsWith( SourceGenerationContext.REST_IMPL_SUFFIX );
    }*/
}
