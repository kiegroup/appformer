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

import org.kie.appformer.flowset.api.definition.MultiStep;
import org.kie.appformer.flowset.api.shape.def.FlowPictures;
import org.kie.appformer.flowset.client.resources.FlowSVGViewFactory;
import org.kie.workbench.common.stunner.core.client.shape.view.HasTitle;
import org.kie.workbench.common.stunner.core.definition.shape.AbstractShapeDef;
import org.kie.workbench.common.stunner.core.definition.shape.GlyphDef;
import org.kie.workbench.common.stunner.shapes.def.picture.PictureGlyphDef;
import org.kie.workbench.common.stunner.svg.client.shape.def.SVGMutableShapeDef;
import org.kie.workbench.common.stunner.svg.client.shape.view.SVGShapeView;

public class MultiStepShapeDef
        extends AbstractShapeDef<MultiStep>
        implements SVGMutableShapeDef<MultiStep, FlowSVGViewFactory> {

    @Override
    public double getAlpha(final MultiStep element) {
        return 1d;
    }

    @Override
    public String getBackgroundColor(final MultiStep element) {
        return element.getBackgroundSet().getBgColor().getValue();
    }

    @Override
    public double getBackgroundAlpha(final MultiStep element) {
        return 0.1d;
    }

    @Override
    public String getBorderColor(final MultiStep element) {
        return element.getBackgroundSet().getBorderColor().getValue();
    }

    @Override
    public double getBorderSize(final MultiStep element) {
        return element.getBackgroundSet().getBorderSize().getValue();
    }

    @Override
    public double getBorderAlpha(final MultiStep element) {
        return 1;
    }

    @Override
    public String getFontFamily(final MultiStep element) {
        return element.getFontSet().getFontFamily().getValue();
    }

    @Override
    public String getFontColor(final MultiStep element) {
        return element.getFontSet().getFontColor().getValue();
    }

    @Override
    public double getFontSize(final MultiStep element) {
        return element.getFontSet().getFontSize().getValue();
    }

    @Override
    public double getFontBorderSize(final MultiStep element) {
        return element.getFontSet().getFontBorderSize().getValue();
    }

    @Override
    public HasTitle.Position getFontPosition(final MultiStep element) {
        return HasTitle.Position.LEFT;
    }

    @Override
    public double getFontRotation(final MultiStep element) {
        return 270;
    }

    @Override
    public double getWidth(final MultiStep element) {
        return element.getDimensionsSet().getWidth().getValue();
    }

    @Override
    public double getHeight(final MultiStep element) {
        return element.getDimensionsSet().getHeight().getValue();
    }

    @Override
    public boolean isSVGViewVisible(final String viewName,
                                    final MultiStep element) {
        return false;
    }

    @Override
    public SVGShapeView<?> newViewInstance(final FlowSVGViewFactory factory,
                                           final MultiStep lane) {
        return factory.lane(getWidth(lane),
                            getHeight(lane),
                            true);
    }

    @Override
    public Class<FlowSVGViewFactory> getViewFactoryType() {
        return FlowSVGViewFactory.class;
    }

    @Override
    public GlyphDef<MultiStep> getGlyphDef() {
        return GLYPH_DEF;
    }

    private static final PictureGlyphDef<MultiStep, FlowPictures> GLYPH_DEF = new PictureGlyphDef<MultiStep, FlowPictures>() {
        @Override
        public FlowPictures getSource(final Class<?> type) {
            return FlowPictures.LANE;
        }

        @Override
        public String getGlyphDescription(final MultiStep element) {
            return element.getDescription();
        }
    };
}