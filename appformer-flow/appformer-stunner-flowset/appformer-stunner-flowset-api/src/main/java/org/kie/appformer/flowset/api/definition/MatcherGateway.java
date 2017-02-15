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

import static org.kie.workbench.common.forms.adf.definitions.annotations.field.selector.SelectorDataProvider.ProviderType.CLIENT;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.appformer.flowset.api.definition.property.background.BackgroundSet;
import org.kie.appformer.flowset.api.definition.property.dimensions.CircleDimensionSet;
import org.kie.appformer.flowset.api.definition.property.dimensions.Radius;
import org.kie.appformer.flowset.api.definition.property.font.FontSet;
import org.kie.appformer.flowset.api.definition.property.gateway.MatchedOperation;
import org.kie.appformer.flowset.api.definition.property.general.FlowGeneralSet;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.annotations.field.selector.SelectorDataProvider;
import org.kie.workbench.common.forms.adf.definitions.settings.FieldPolicy;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.listBox.type.ListBoxFieldType;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textBox.type.TextBoxFieldType;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.Description;
import org.kie.workbench.common.stunner.core.definition.annotation.Property;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Title;
import org.kie.workbench.common.stunner.core.factory.graph.NodeFactory;

@Portable
@Bindable
@Definition(graphFactory = NodeFactory.class, builder = MatcherGateway.JoinGatewayBuilder.class)
@FormDefinition(
        startElement = "general",
        policy = FieldPolicy.ONLY_MARKED
)
public class MatcherGateway extends BaseGateway {

    @Title
    public static final transient String title = "Matcher Gateway";

    @Description
    public static final transient String description = "Matcher Gateway";

    @FormField(type = TextBoxFieldType.class)
    @Property
    private MatchedOperation operation;

    @NonPortable
    public static class JoinGatewayBuilder extends BaseGatewayBuilder<MatcherGateway> {

        @Override
        public MatcherGateway build() {
            return new MatcherGateway(new FlowGeneralSet("Matcher"),
                                       new BackgroundSet(COLOR,
                                                         BORDER_COLOR,
                                                         BORDER_SIZE),
                                       new FontSet(),
                                       new CircleDimensionSet(new Radius(RADIUS)),
                                       new MatchedOperation());
        }
    }

    {
        labels.add( "intermediate_gateway" );
    }

    public MatcherGateway() {
    }

    public MatcherGateway( final @MapsTo( "general" ) FlowGeneralSet general,
                           final @MapsTo( "backgroundSet" ) BackgroundSet backgroundSet,
                           final @MapsTo( "fontSet" ) FontSet fontSet,
                           final @MapsTo( "dimensionsSet" ) CircleDimensionSet dimensionsSet,
                           final @MapsTo( "operation" ) MatchedOperation operation) {
        super(general,
              backgroundSet,
              fontSet,
              dimensionsSet);
        this.operation = operation;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MatchedOperation getOperation() {
        return operation;
    }

    public void setOperation( final MatchedOperation operation ) {
        this.operation = operation;
    }
}
