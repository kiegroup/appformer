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
import org.kie.appformer.flowset.api.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.appformer.flowset.api.definition.property.font.FontBorderSize;
import org.kie.appformer.flowset.api.definition.property.font.FontColor;
import org.kie.appformer.flowset.api.definition.property.font.FontFamily;
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
import org.kie.workbench.common.stunner.core.rule.annotation.CanContain;

@Portable
@Bindable
@Definition(graphFactory = NodeFactory.class, builder = MultiStep.MultiStepBuilder.class)
@CanContain(roles = {"all"})
@FormDefinition(
        startElement = "general",
        policy = FieldPolicy.ONLY_MARKED
)
public class MultiStep implements FlowDefinition {

    @Category
    public static final transient String category = Categories.MULTISTEP;

    @Title
    public static final transient String title = "MultiStep";

    @Description
    public static final transient String description = "For containing multiple forms to be combined as steps of a larger form.";

    @PropertySet
    @FormField
    @Valid
    protected FlowGeneralSet general;

    @PropertySet
    @Valid
    protected BackgroundSet backgroundSet;

    @PropertySet
    private FontSet fontSet;

    @PropertySet
    protected RectangleDimensionsSet dimensionsSet;

    @Labels
    private final Set<String> labels = new HashSet<String>() {{
        add("all");
        add("multistep");
    }};

    @NonPortable
    public static class MultiStepBuilder implements Builder<MultiStep> {

        public static final transient String COLOR = "#FFFFFF";
        public static final Double WIDTH = 450d;
        public static final Double HEIGHT = 250d;
        public static final Double BORDER_SIZE = 1.5d;
        public static final String BORDER_COLOR = "#000000";

        @Override
        public MultiStep build() {
            return new MultiStep(new FlowGeneralSet("Lane"),
                            new BackgroundSet(COLOR,
                                              BORDER_COLOR,
                                              BORDER_SIZE),
                            new FontSet(FontFamily.defaultValue,
                                        FontColor.defaultValue,
                                        14d,
                                        FontBorderSize.defaultValue),
                            new RectangleDimensionsSet(WIDTH,
                                                       HEIGHT));
        }
    }

    public MultiStep() {
    }

    public MultiStep(final @MapsTo("general") FlowGeneralSet general,
                final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                final @MapsTo("fontSet") FontSet fontSet,
                final @MapsTo("dimensionsSet") RectangleDimensionsSet dimensionsSet) {
        this.general = general;
        this.backgroundSet = backgroundSet;
        this.fontSet = fontSet;
        this.dimensionsSet = dimensionsSet;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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

    public void setFontSet(final FontSet fontSet) {
        this.fontSet = fontSet;
    }

    public void setBackgroundSet(final BackgroundSet backgroundSet) {
        this.backgroundSet = backgroundSet;
    }

    public void setGeneral(final FlowGeneralSet general) {
        this.general = general;
    }

    public RectangleDimensionsSet getDimensionsSet() {
        return dimensionsSet;
    }

    public void setDimensionsSet(final RectangleDimensionsSet dimensionsSet) {
        this.dimensionsSet = dimensionsSet;
    }
}
