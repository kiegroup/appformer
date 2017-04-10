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

import org.kie.appformer.flowset.api.definition.BasePart;
import org.kie.appformer.flowset.api.definition.FlowPart;
import org.kie.appformer.flowset.api.definition.FormPart;
import org.kie.appformer.flowset.api.shape.def.FlowPictures;
import org.kie.appformer.flowset.client.resources.FlowSVGViewFactory;
import org.kie.workbench.common.stunner.core.client.shape.view.HasTitle;
import org.kie.workbench.common.stunner.core.definition.shape.AbstractShapeDef;
import org.kie.workbench.common.stunner.core.definition.shape.GlyphDef;
import org.kie.workbench.common.stunner.shapes.def.picture.PictureGlyphDef;
import org.kie.workbench.common.stunner.svg.client.shape.def.SVGMutableShapeDef;
import org.kie.workbench.common.stunner.svg.client.shape.view.SVGShapeView;

public class FlowPartShapeDef
        extends AbstractShapeDef<BasePart>
        implements SVGMutableShapeDef<BasePart, FlowSVGViewFactory> {

    @Override
    public double getAlpha(final BasePart element) {
        return 1d;
    }

    @Override
    public String getBackgroundColor(final BasePart element) {
        return element.getBackgroundSet().getBgColor().getValue();
    }

    @Override
    public double getBackgroundAlpha(final BasePart element) {
        return 1;
    }

    @Override
    public String getBorderColor(final BasePart element) {
        return element.getBackgroundSet().getBorderColor().getValue();
    }

    @Override
    public double getBorderSize(final BasePart element) {
        return element.getBackgroundSet().getBorderSize().getValue();
    }

    @Override
    public double getBorderAlpha(final BasePart element) {
        return 1;
    }

    @Override
    public String getFontFamily(final BasePart element) {
        return element.getFontSet().getFontFamily().getValue();
    }

    @Override
    public String getFontColor(final BasePart element) {
        return element.getFontSet().getFontColor().getValue();
    }

    @Override
    public double getFontSize(final BasePart element) {
        return element.getFontSet().getFontSize().getValue();
    }

    @Override
    public double getFontBorderSize(final BasePart element) {
        return element.getFontSet().getFontBorderSize().getValue();
    }

    @Override
    public HasTitle.Position getFontPosition(final BasePart element) {
        return HasTitle.Position.CENTER;
    }

    @Override
    public double getFontRotation(final BasePart element) {
        return 0;
    }

    private static final PictureGlyphDef<BasePart, FlowPictures> TASK_GLYPH_DEF = new PictureGlyphDef<BasePart, FlowPictures>() {

        private final Map<Class<?>, FlowPictures> PICTURES = new HashMap<Class<?>, FlowPictures>() {{
            put(FlowPart.class,
                FlowPictures.FLOW_PART);
            put(FormPart.class,
                FlowPictures.FLOW_PART);
        }};

        @Override
        public String getGlyphDescription(final BasePart element) {
            return element.getDescription();
        }

        @Override
        public FlowPictures getSource(final Class<?> type) {
            return PICTURES.get(type);
        }
    };

    @Override
    public GlyphDef<BasePart> getGlyphDef() {
        return TASK_GLYPH_DEF;
    }

    @Override
    public double getWidth(final BasePart element) {
        return element.getDimensionsSet().getWidth().getValue();
    }

    @Override
    public double getHeight(final BasePart element) {
        return element.getDimensionsSet().getHeight().getValue();
    }

    @Override
    public boolean isSVGViewVisible(final String viewName,
                                    final BasePart element) {
        return true;
    }

    @Override
    public SVGShapeView<?> newViewInstance(final FlowSVGViewFactory factory,
                                           final BasePart task) {
        return factory.task(getWidth(task),
                            getHeight(task),
                            true);
    }

    @Override
    public Class<FlowSVGViewFactory> getViewFactoryType() {
        return FlowSVGViewFactory.class;
    }
}