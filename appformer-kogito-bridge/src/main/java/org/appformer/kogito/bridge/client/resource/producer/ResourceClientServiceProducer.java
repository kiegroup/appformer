/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.appformer.kogito.bridge.client.resource.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.appformer.kogito.bridge.client.resource.ResourceContentService;
import org.appformer.kogito.bridge.client.resource.impl.MockResourceContentService;
import org.appformer.kogito.bridge.client.resource.impl.ResourceContentServiceImpl;

public class ResourceClientServiceProducer {
    
    @Produces
    @ApplicationScoped
    public ResourceContentService produce() {
        boolean isResourceContentServiceAvailable = isResourceContentServiceAvailable();
        if (isResourceContentServiceAvailable) {
            return new ResourceContentServiceImpl();
        } else {
            return new MockResourceContentService();
        }
    }

    private native boolean isResourceContentServiceAvailable()/*-{
        return typeof $wnd.envelope !== "undefined";
    }-*/;

}
