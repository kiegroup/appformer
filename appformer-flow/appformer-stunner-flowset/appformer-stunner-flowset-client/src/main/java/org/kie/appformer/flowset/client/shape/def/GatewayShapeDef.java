/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.flowset.client.shape.def;

import java.util.HashMap;
import java.util.Map;

import org.kie.appformer.flowset.api.definition.BaseGateway;
import org.kie.appformer.flowset.api.definition.DecisionGateway;
import org.kie.appformer.flowset.api.definition.JoinGateway;
import org.kie.appformer.flowset.api.definition.MatcherGateway;
import org.kie.appformer.flowset.api.shape.def.FlowPictures;
import org.kie.appformer.flowset.client.resources.FlowSVGViewFactory;
import org.kie.workbench.common.stunner.core.client.shape.view.HasTitle;
import org.kie.workbench.common.stunner.core.definition.shape.AbstractShapeDef;
import org.kie.workbench.common.stunner.core.definition.shape.GlyphDef;
import org.kie.workbench.common.stunner.shapes.def.picture.PictureGlyphDef;
import org.kie.workbench.common.stunner.svg.client.shape.def.SVGMutableShapeDef;
import org.kie.workbench.common.stunner.svg.client.shape.view.SVGShapeView;

public class GatewayShapeDef
        extends AbstractShapeDef<BaseGateway>
        implements SVGMutableShapeDef<BaseGateway, FlowSVGViewFactory> {

    private static final String GW_MULTIPLE = "gwParallelMultiple";

    @Override
    public double getAlpha(final BaseGateway element) {
        return 1d;
    }

    @Override
    public String getBackgroundColor(final BaseGateway element) {
        return element.getBackgroundSet().getBgColor().getValue();
    }

    @Override
    public double getBackgroundAlpha(final BaseGateway element) {
        return 1;
    }

    @Override
    public String getBorderColor(final BaseGateway element) {
        return element.getBackgroundSet().getBorderColor().getValue();
    }

    @Override
    public double getBorderSize(final BaseGateway element) {
        return element.getBackgroundSet().getBorderSize().getValue();
    }

    @Override
    public double getBorderAlpha(final BaseGateway element) {
        return 1;
    }

    @Override
    public String getFontFamily(final BaseGateway element) {
        return element.getFontSet().getFontFamily().getValue();
    }

    @Override
    public String getFontColor(final BaseGateway element) {
        return element.getFontSet().getFontColor().getValue();
    }

    @Override
    public double getFontSize(final BaseGateway element) {
        return element.getFontSet().getFontSize().getValue();
    }

    @Override
    public double getFontBorderSize(final BaseGateway element) {
        return element.getFontSet().getFontBorderSize().getValue();
    }

    @Override
    public HasTitle.Position getFontPosition(final BaseGateway element) {
        return HasTitle.Position.BOTTOM;
    }

    @Override
    public double getFontRotation(final BaseGateway element) {
        return 0;
    }

    @Override
    public double getWidth(final BaseGateway element) {
        return element.getDimensionsSet().getRadius().getValue() * 2;
    }

    @Override
    public double getHeight(final BaseGateway element) {
        return element.getDimensionsSet().getRadius().getValue() * 2;
    }

    @Override
    public boolean isSVGViewVisible(final String viewName,
                                    final BaseGateway element) {
        switch (viewName) {
            case GW_MULTIPLE:
                return element instanceof BaseGateway;
        }
        return false;
    }

    @Override
    public SVGShapeView<?> newViewInstance(final FlowSVGViewFactory factory,
                                           final BaseGateway gateway) {
        return factory.gateway(getWidth(gateway),
                               getHeight(gateway),
                               false);
    }

    @Override
    public Class<FlowSVGViewFactory> getViewFactoryType() {
        return FlowSVGViewFactory.class;
    }

    private static final PictureGlyphDef<BaseGateway, FlowPictures> TASK_GLYPH_DEF = new PictureGlyphDef<BaseGateway, FlowPictures>() {

        private final Map<Class<?>, FlowPictures> PICTURES = new HashMap<Class<?>, FlowPictures>() {{
            put(DecisionGateway.class,
                FlowPictures.PARALLEL_MULTIPLE);
            put(JoinGateway.class,
                FlowPictures.PARALLEL_MULTIPLE);
            put(MatcherGateway.class,
                FlowPictures.PARALLEL_MULTIPLE);
        }};

        @Override
        public String getGlyphDescription(final BaseGateway element) {
            return element.getGeneral().getName().getValue();
        }

        @Override
        public FlowPictures getSource(final Class<?> type) {
            return PICTURES.get(type);
        }
    };

    @Override
    public GlyphDef<BaseGateway> getGlyphDef() {
        return TASK_GLYPH_DEF;
    }
}