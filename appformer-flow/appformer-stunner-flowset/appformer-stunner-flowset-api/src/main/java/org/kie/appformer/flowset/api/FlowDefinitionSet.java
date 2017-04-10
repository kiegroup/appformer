/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.flowset.api;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.appformer.flowset.api.definition.DecisionGateway;
import org.kie.appformer.flowset.api.definition.FlowPart;
import org.kie.appformer.flowset.api.definition.FormPart;
import org.kie.appformer.flowset.api.definition.JoinGateway;
import org.kie.appformer.flowset.api.definition.MatcherGateway;
import org.kie.appformer.flowset.api.definition.MultiStep;
import org.kie.appformer.flowset.api.definition.SequenceFlow;
import org.kie.appformer.flowset.api.definition.StartNoneEvent;
import org.kie.appformer.flowset.api.factory.FlowGraphFactory;
import org.kie.workbench.common.stunner.core.definition.annotation.DefinitionSet;
import org.kie.workbench.common.stunner.core.definition.annotation.Description;
import org.kie.workbench.common.stunner.core.definition.builder.Builder;
import org.kie.workbench.common.stunner.core.rule.annotation.CanContain;
import org.kie.workbench.common.stunner.core.rule.annotation.Occurrences;

@ApplicationScoped
@Bindable
@DefinitionSet(
        graphFactory = FlowGraphFactory.class,
        definitions = {
                FlowPart.class,
                FormPart.class,
                MultiStep.class,
                StartNoneEvent.class,
                DecisionGateway.class,
                JoinGateway.class,
                MatcherGateway.class,
                SequenceFlow.class
        },
        builder = FlowDefinitionSet.FlowDefinitionSetBuilder.class
)
@CanContain(roles = {"all"})
@Occurrences(
        role = "Startevents_all",
        min = 0
)
@Occurrences(
        role = "Endevents_all",
        min = 0
)
public class FlowDefinitionSet {

    @Description
    public static final transient String description = "Application Flow";

    @NonPortable
    public static class FlowDefinitionSetBuilder implements Builder<FlowDefinitionSet> {

        @Override
        public FlowDefinitionSet build() {
            return new FlowDefinitionSet();
        }
    }

    public FlowDefinitionSet() {
    }

    public String getDescription() {
        return description;
    }
}
