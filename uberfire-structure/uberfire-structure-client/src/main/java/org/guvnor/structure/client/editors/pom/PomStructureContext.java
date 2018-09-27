/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.guvnor.structure.client.editors.pom;

import java.util.HashMap;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.common.services.project.client.context.WorkspaceProjectContext;
import org.guvnor.structure.pom.AddPomDependencyEvent;
import org.guvnor.structure.pom.DynamicPomDependency;

@ApplicationScoped
public class PomStructureContext {

    private final HashMap<PomStructureContextChangeHandler.HandlerRegistration, PomStructureContextChangeHandler> handlers = new HashMap<>();
    private WorkspaceProjectContext context;

    public PomStructureContext(){ }

    @Inject
    public PomStructureContext(final WorkspaceProjectContext context) {
        this.context = context;
    }

    public PomStructureContextChangeHandler.HandlerRegistration addPomStructureContextChangeHandler(final PomStructureContextChangeHandler handler) {
        final PomStructureContextChangeHandler.HandlerRegistration handlerRegistration = new PomStructureContextChangeHandler.HandlerRegistration();

        handlers.put(handlerRegistration,
                                 handler);

        return handlerRegistration;
    }

    public void onNewDynamicDependency(final @Observes AddPomDependencyEvent event) {
        for (final PomStructureContextChangeHandler handler : handlers.values()) {
            final Optional<DynamicPomDependency> dependencyOptional = event.getNewPomDependency();
            if(dependencyOptional.isPresent()) {
                handler.onNewDynamicDependencyAdded(dependencyOptional.get());
            }
        }
    }


    public void removeHandler(final PomStructureContextChangeHandler.HandlerRegistration handlerRegistration) {
        handlers.remove(handlerRegistration);
    }

}
