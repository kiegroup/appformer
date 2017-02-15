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

package org.kie.appformer.flowset.api.definition;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.appformer.flowset.api.definition.property.background.BackgroundSet;
import org.kie.appformer.flowset.api.definition.property.dimensions.CircleDimensionSet;
import org.kie.appformer.flowset.api.definition.property.dimensions.Radius;
import org.kie.appformer.flowset.api.definition.property.font.FontSet;
import org.kie.appformer.flowset.api.definition.property.general.FlowGeneralSet;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.settings.FieldPolicy;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.Description;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Title;
import org.kie.workbench.common.stunner.core.factory.graph.NodeFactory;

@Portable
@Bindable
@Definition(graphFactory = NodeFactory.class, builder = DecisionGateway.DecisionGatewayBuilder.class)
@FormDefinition(
        startElement = "general",
        policy = FieldPolicy.ONLY_MARKED
)
public class DecisionGateway extends BaseGateway {

    @Title
    public static final transient String title = "Decision Gateway";

    @Description
    public static final transient String description = "Decision Gateway";

    @NonPortable
    public static class DecisionGatewayBuilder extends BaseGatewayBuilder<DecisionGateway> {

        @Override
        public DecisionGateway build() {
            return new DecisionGateway(new FlowGeneralSet("Decision"),
                                       new BackgroundSet(COLOR,
                                                         BORDER_COLOR,
                                                         BORDER_SIZE),
                                       new FontSet(),
                                       new CircleDimensionSet(new Radius(RADIUS)));
        }
    }

    {
        labels.add( "initial_gateway" );
    }

    public DecisionGateway() {
    }

    public DecisionGateway(final @MapsTo("general") FlowGeneralSet general,
                           final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                           final @MapsTo("fontSet") FontSet fontSet,
                           final @MapsTo("dimensionsSet") CircleDimensionSet dimensionsSet) {
        super(general,
              backgroundSet,
              fontSet,
              dimensionsSet);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
