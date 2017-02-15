/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.flowset.client.shape;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.safehtml.shared.SafeUri;

import org.kie.appformer.flowset.api.shape.def.FlowPictures;
import org.kie.appformer.flowset.client.resources.FlowImageResources;
import org.kie.workbench.common.stunner.shapes.def.picture.PictureProvider;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@ApplicationScoped
public class FlowPictureProvider implements PictureProvider<FlowPictures> {

    private static final Map<FlowPictures, SafeUri> PICTURE_URIS =
            new HashMap<FlowPictures, SafeUri>() {{
                put(FlowPictures.TASK_USER,
                    FlowImageResources.INSTANCE.taskUser().getSafeUri());
                put(FlowPictures.TASK_SCRIPT,
                    FlowImageResources.INSTANCE.taskScript().getSafeUri());
                put(FlowPictures.FLOW_PART,
                    FlowImageResources.INSTANCE.taskBusinessRule().getSafeUri());
                put(FlowPictures.CANCEL,
                    FlowImageResources.INSTANCE.cancel().getSafeUri());
                put(FlowPictures.CIRCLE,
                    FlowImageResources.INSTANCE.circle().getSafeUri());
                put(FlowPictures.CLOCK_O,
                    FlowImageResources.INSTANCE.clockO().getSafeUri());
                put(FlowPictures.EVENT_END,
                    FlowImageResources.INSTANCE.eventEnd().getSafeUri());
                put(FlowPictures.EVENT_INTERMEDIATE,
                    FlowImageResources.INSTANCE.eventIntermediate().getSafeUri());
                put(FlowPictures.EVENT_START,
                    FlowImageResources.INSTANCE.eventStart().getSafeUri());
                put(FlowPictures.LANE,
                    FlowImageResources.INSTANCE.lane().getSafeUri());
                put(FlowPictures.PLUS_QUARE,
                    FlowImageResources.INSTANCE.plusSquare().getSafeUri());
                put(FlowPictures.SUB_PROCESS,
                    FlowImageResources.INSTANCE.subProcess().getSafeUri());
                put(FlowPictures.PARALLEL_EVENT,
                    FlowImageResources.INSTANCE.gatewayParallelEvent().getSafeUri());
                put(FlowPictures.PARALLEL_MULTIPLE,
                    FlowImageResources.INSTANCE.gatewayParallelMultiple().getSafeUri());
                put(FlowPictures.EXCLUSIVE,
                    FlowImageResources.INSTANCE.gatewayExclusive().getSafeUri());
            }};

    @Override
    public Class<FlowPictures> getSourceType() {
        return FlowPictures.class;
    }

    @Override
    public boolean thumbFor(final FlowPictures source) {
        return null != get(source);
    }

    @Override
    public SafeUri getThumbnailUri(final FlowPictures source) {
        return get(source);
    }

    private SafeUri get(final FlowPictures source) {
        checkNotNull("source",
                     source);
        return PICTURE_URIS.get(source);
    }
}
