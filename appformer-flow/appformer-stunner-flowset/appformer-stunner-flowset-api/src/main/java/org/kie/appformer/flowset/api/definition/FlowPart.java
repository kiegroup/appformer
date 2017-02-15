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

import javax.validation.Valid;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.appformer.flowset.api.definition.property.background.BackgroundSet;
import org.kie.appformer.flowset.api.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.appformer.flowset.api.definition.property.font.FontSet;
import org.kie.appformer.flowset.api.definition.property.general.Name;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.annotations.field.selector.SelectorDataProvider;
import org.kie.workbench.common.forms.adf.definitions.annotations.field.selector.SelectorDataProvider.ProviderType;
import org.kie.workbench.common.forms.adf.definitions.settings.FieldPolicy;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.listBox.type.ListBoxFieldType;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.Property;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Title;
import org.kie.workbench.common.stunner.core.factory.graph.NodeFactory;

@Portable
@Bindable
@Definition(graphFactory = NodeFactory.class, builder = FlowPart.FlowPartBuilder.class)
@FormDefinition(
        startElement = "general",
        policy = FieldPolicy.ONLY_MARKED
)
public class FlowPart extends BasePart {

    @Title
    public static final transient String title = "Flow Part";

    @NonPortable
    public static class FlowPartBuilder extends BasePartBuilder<FlowPart> {

        @Override
        public FlowPart build() {
            return new FlowPart(new Name("Flow Part"),
                                        new BackgroundSet(COLOR,
                                                          BORDER_COLOR,
                                                          BORDER_SIZE),
                                        new FontSet(),
                                        new RectangleDimensionsSet(WIDTH,
                                                                   HEIGHT)
            );
        }
    }

    @Property
    @FormField(type = ListBoxFieldType.class)
    @SelectorDataProvider(
                          className = "org.kie.appformer.flowset.backend.FlowPartProvider",
                          type = ProviderType.REMOTE
                         )
    @Valid
    private Name name;

    public FlowPart() {
        super();
    }

    public FlowPart(final @MapsTo("name") Name name,
                            final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                            final @MapsTo("fontSet") FontSet fontSet,
                            final @MapsTo("dimensionsSet") RectangleDimensionsSet dimensionsSet) {
        super(backgroundSet,
              fontSet,
              dimensionsSet);
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void setName( final Name name ) {
        this.name = name;
    }

    @Override
    public Name getName() {
        return name;
    }
}
