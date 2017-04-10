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

package org.kie.appformer.flowset.client.components.palette.factory;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.appformer.flowset.api.FlowDefinitionSet;
import org.kie.appformer.flowset.api.definition.BaseGateway;
import org.kie.appformer.flowset.api.definition.BasePart;
import org.kie.appformer.flowset.api.definition.Categories;
import org.kie.appformer.flowset.api.definition.DecisionGateway;
import org.kie.appformer.flowset.api.definition.MultiStep;
import org.kie.appformer.flowset.api.definition.SequenceFlow;
import org.kie.appformer.flowset.api.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.core.client.api.ShapeManager;
import org.kie.workbench.common.stunner.core.client.components.palette.factory.BindableDefSetPaletteDefinitionFactory;
import org.kie.workbench.common.stunner.core.client.components.palette.model.definition.DefinitionSetPaletteBuilder;

// TODO: i18n.
@Dependent
public class FlowPaletteDefinitionFactory extends BindableDefSetPaletteDefinitionFactory {

    private static final Map<String, String> CAT_TITLES = new HashMap<String, String>() {{
        put(Categories.ACTIVITIES,
            "Activities");
        put(Categories.CONNECTING_OBJECTS,
            "Connecting objects");
        put(Categories.EVENTS,
            "Events");
        put(Categories.GATEWAYS,
            "Gateways");
        put(Categories.MULTISTEP,
            "MultiStep");
    }};

    private static final Map<String, Class<?>> CAT_DEF_IDS = new HashMap<String, Class<?>>() {{
        put(Categories.CONNECTING_OBJECTS,
            SequenceFlow.class);
        put(Categories.EVENTS,
            StartNoneEvent.class);
        put(Categories.GATEWAYS,
            DecisionGateway.class);
        put(Categories.MULTISTEP,
            MultiStep.class);
    }};

    private static final Map<String, String> MORPH_GROUP_TITLES = new HashMap<String, String>() {{
        put(BasePart.class.getName(),
            "Tasks");
        put(BaseGateway.class.getName(),
            "Gateways");
    }};

    @Inject
    public FlowPaletteDefinitionFactory(final ShapeManager shapeManager,
                                        final DefinitionSetPaletteBuilder paletteBuilder) {
        super(shapeManager,
              paletteBuilder);
    }

    @Override
    protected void configureBuilder() {
        super.configureBuilder();
        // TODO: Exclude connectors category from being present on the palette model - Dropping connectors from palette produces an error right now, must fix it on lienzo side.
        excludeCategory(Categories.CONNECTING_OBJECTS);
    }

    @Override
    protected String getCategoryTitle(final String id) {
        return CAT_TITLES.get(id);
    }

    @Override
    protected Class<?> getCategoryTargetDefinitionId(final String id) {
        return CAT_DEF_IDS.get(id);
    }

    @Override
    protected String getCategoryDescription(final String id) {
        return CAT_TITLES.get(id);
    }

    @Override
    protected String getMorphGroupTitle(final String morphBaseId,
                                        final Object definition) {
        return MORPH_GROUP_TITLES.get(morphBaseId);
    }

    @Override
    protected String getMorphGroupDescription(final String morphBaseId,
                                              final Object definition) {
        return MORPH_GROUP_TITLES.get(morphBaseId);
    }

    @Override
    protected Class<?> getDefinitionSetType() {
        return FlowDefinitionSet.class;
    }
}
