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
package org.guvnor.structure.backend.pom;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.guvnor.structure.pom.AddPomDependencyEvent;
import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;

@ApplicationScoped
public class PomStructureEditor {

    private final Logger logger = LoggerFactory.getLogger(PomStructureEditor.class);
    private PomEditor pomEditor;
    private MapperDependencyTypes mapper;

    public PomStructureEditor() {
        pomEditor = new PomEditorDefault();
        mapper = new MapperDependencyTypes();
    }

    public void onNewDynamicDependency(final @Observes AddPomDependencyEvent event) {
        final Optional<DynamicPomDependency> dependencyOptional = event.getNewPomDependency();
        final Optional<Path> projectPathOptional = event.getProjectPath();
        final Optional<DependencyType> dependencyTypeOptional = event.getDependencyType();

        if (dependencyOptional.isPresent() && projectPathOptional.isPresent()) {

            addDependencyToPom(projectPathOptional.get(),
                               dependencyOptional.get());
        } else if (dependencyTypeOptional.isPresent() && projectPathOptional.isPresent()) {

            addDependencyToPom(projectPathOptional.get(),
                               mapper.getDependency(dependencyTypeOptional.get()));
        } else {
            logger.error("Invalid event received {}",
                         event);
        }
    }

    private void addDependencyToPom(Path projectPath,
                                    DynamicPomDependency dep) {
        if (!pomEditor.addDependency(dep,
                                     projectPath)) {
            logger.warn("Failed to add dependency {} to pom.xml located in {}",
                        dep,
                        projectPath);
        }
    }
}
