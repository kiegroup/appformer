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

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

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
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.settings.FieldPolicy;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.Description;
import org.kie.workbench.common.stunner.core.definition.annotation.PropertySet;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Category;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Labels;
import org.kie.workbench.common.stunner.core.definition.annotation.definition.Title;
import org.kie.workbench.common.stunner.core.definition.builder.Builder;
import org.kie.workbench.common.stunner.core.factory.graph.NodeFactory;

@Portable
@Bindable
@Definition(graphFactory = NodeFactory.class, builder = StartNoneEvent.StartNoneEventBuilder.class)
@FormDefinition(
        startElement = "general",
        policy = FieldPolicy.ONLY_MARKED
)
public class StartNoneEvent implements FlowDefinition {

    @Title
    public static final transient String title = "Start Event";

    @Description
    public static final transient String description = "Untyped start event";

    @Category
    public static final transient String category = Categories.EVENTS;

    @NonPortable
    public static class StartNoneEventBuilder implements Builder<StartNoneEvent> {

        public static final String BG_COLOR = "#FFFFFF";
        public static final Double BORDER_SIZE = 1.5d;
        public static final String BORDER_COLOR = "#000000";
        public static final Double RADIUS = 15d;

        @Override
        public StartNoneEvent build() {
            return new StartNoneEvent(new FlowGeneralSet("Start"),
                                      new BackgroundSet(BG_COLOR,
                                                        BORDER_COLOR,
                                                        BORDER_SIZE),
                                      new FontSet(),
                                      new CircleDimensionSet(new Radius(RADIUS)));
        }
    }

    @PropertySet
    @FormField
    @Valid
    protected FlowGeneralSet general;

    @PropertySet
    @FormField( afterElement = "dataIOSet" )
    @Valid
    protected BackgroundSet backgroundSet;

    @PropertySet
    protected FontSet fontSet;

    @PropertySet
    private CircleDimensionSet dimensionsSet;

    @Labels
    protected final Set<String> labels = new HashSet<String>() {{
            add("all");
            add("Startevents_all");
            add("sequence_start");
            add("choreography_sequence_start");
            add("to_task_event");
            add("from_task_event");
            add("fromtoall");
            add("StartEventsMorph");
        }};

    public StartNoneEvent() {
    }

    public StartNoneEvent(final @MapsTo("general") FlowGeneralSet general,
                          final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                          final @MapsTo("fontSet") FontSet fontSet,
                          final @MapsTo("dimensionsSet") CircleDimensionSet dimensionsSet) {
        this.general = general;
        this.backgroundSet = backgroundSet;
        this.fontSet = fontSet;
        this.dimensionsSet = dimensionsSet;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public FlowGeneralSet getGeneral() {
        return general;
    }

    public BackgroundSet getBackgroundSet() {
        return backgroundSet;
    }

    public FontSet getFontSet() {
        return fontSet;
    }

    public void setGeneral( final FlowGeneralSet general ) {
        this.general = general;
    }

    public void setBackgroundSet( final BackgroundSet backgroundSet ) {
        this.backgroundSet = backgroundSet;
    }

    public void setFontSet( final FontSet fontSet ) {
        this.fontSet = fontSet;
    }

    public CircleDimensionSet getDimensionsSet() {
        return dimensionsSet;
    }

    public void setDimensionsSet( final CircleDimensionSet dimensionsSet ) {
        this.dimensionsSet = dimensionsSet;
    }
}
