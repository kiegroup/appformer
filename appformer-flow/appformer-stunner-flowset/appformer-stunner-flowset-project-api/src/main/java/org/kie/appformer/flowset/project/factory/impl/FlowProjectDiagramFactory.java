/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.flowset.project.factory.impl;

import javax.enterprise.context.ApplicationScoped;

import org.kie.appformer.flowset.api.FlowDefinitionSet;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.factory.impl.BindableDiagramFactory;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.project.diagram.ProjectDiagram;
import org.kie.workbench.common.stunner.project.diagram.ProjectMetadata;
import org.kie.workbench.common.stunner.project.diagram.impl.ProjectDiagramImpl;

@ApplicationScoped
public class FlowProjectDiagramFactory
        extends BindableDiagramFactory<ProjectMetadata, ProjectDiagram> {

    @Override
    public ProjectDiagram build(final String name,
                                final ProjectMetadata metadata,
                                final Graph<DefinitionSet, ?> graph) {
        return new ProjectDiagramImpl(name, graph, metadata);
    }

    @Override
    public Class<? extends Metadata> getMetadataType() {
        return ProjectMetadata.class;
    }

    @Override
    protected Class<?> getDefinitionSetType() {
        return FlowDefinitionSet.class;
    }
}
