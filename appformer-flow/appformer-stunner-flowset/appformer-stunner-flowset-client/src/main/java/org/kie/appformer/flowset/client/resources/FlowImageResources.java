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

package org.kie.appformer.flowset.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.DataResource;

public interface FlowImageResources extends ClientBundleWithLookup {

    public static final FlowImageResources INSTANCE = GWT.create(FlowImageResources.class);

    // ****** BPMN ShapeSet Thumbnail. *******
    @Source("images/bpmn_thumb.png")
    DataResource bpmnSetThumb();

    // ******* BPMN Pictures/Icons *******

    // ******* Categories *******
    @ClientBundle.Source("images/categories/activity.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource categoryActivity();

    @ClientBundle.Source("images/categories/container.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource categoryContainer();

    @ClientBundle.Source("images/categories/gateway.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource categoryGateway();

    @ClientBundle.Source("images/categories/library.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource categoryLibrary();

    @ClientBundle.Source("images/categories/sequence.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource categorySequence();

    // ******* Task *******
    @ClientBundle.Source(FlowSVGViewFactory.TASK_USER)
    @DataResource.MimeType("image/svg+xml")
    DataResource taskUser();

    @ClientBundle.Source(FlowSVGViewFactory.TASK_SCRIPT)
    @DataResource.MimeType("image/svg+xml")
    DataResource taskScript();

    @ClientBundle.Source(FlowSVGViewFactory.TASK_BUSINESS_RULE)
    @DataResource.MimeType("image/svg+xml")
    DataResource taskBusinessRule();

    @ClientBundle.Source("images/task/task-manual.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource taskManual();

    @ClientBundle.Source("images/task/task-service.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource taskService();

    // ******* Event *******
    @ClientBundle.Source(FlowSVGViewFactory.EVENT_END)
    @DataResource.MimeType("image/svg+xml")
    DataResource eventEnd();

    @ClientBundle.Source("images/event/event-terminating-end.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource eventTerminatingEnd();

    @ClientBundle.Source(FlowSVGViewFactory.EVENT_INTERMEDIATE)
    @DataResource.MimeType("image/svg+xml")
    DataResource eventIntermediate();

    @ClientBundle.Source("images/event/event-intermediate-non-interrupting.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource eventIntermediateNonInterrupting();

    @ClientBundle.Source(FlowSVGViewFactory.EVENT_START)
    @DataResource.MimeType("image/svg+xml")
    DataResource eventStart();

    @ClientBundle.Source("images/event/event-start-non-interrupting.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource eventStartNonInterrupting();

    // ******* Gateway *******
    @ClientBundle.Source("images/gateway/parallel-event.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource gatewayParallelEvent();

    @ClientBundle.Source(FlowSVGViewFactory.GATEWAY_PARALLEL_MULTIPLE)
    @DataResource.MimeType("image/svg+xml")
    DataResource gatewayParallelMultiple();

    @ClientBundle.Source(FlowSVGViewFactory.GATEWAY_EXCLUSIVE)
    @DataResource.MimeType("image/svg+xml")
    DataResource gatewayExclusive();

    @ClientBundle.Source("images/gateway/complex.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource gatewayComplex();

    @ClientBundle.Source("images/gateway/event.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource gatewayEvent();

    @ClientBundle.Source("images/gateway/inclusive.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource gatewayInclusive();

    @ClientBundle.Source(FlowSVGViewFactory.LANE)
    @DataResource.MimeType("image/svg+xml")
    DataResource lane();

    @ClientBundle.Source(FlowSVGViewFactory.LANE_ICON)
    @DataResource.MimeType("image/svg+xml")
    DataResource laneIcon();

    // ******* Misc *******

    @ClientBundle.Source(FlowSVGViewFactory.RECTANGLE)
    @DataResource.MimeType("image/svg+xml")
    DataResource rectangle();

    @ClientBundle.Source(FlowSVGViewFactory.CIRCLE)
    @DataResource.MimeType("image/svg+xml")
    DataResource circle();

    @ClientBundle.Source("images/cancel.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource cancel();

    @ClientBundle.Source("images/clock-o.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource clockO();

    @ClientBundle.Source("images/plus-square.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource plusSquare();

    @ClientBundle.Source("images/sub-process.svg")
    @DataResource.MimeType("image/svg+xml")
    DataResource subProcess();
}