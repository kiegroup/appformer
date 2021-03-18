/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dsl.factory.component;

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.external.model.ExternalComponent;

public class ExternalDisplayerBuilder extends DisplayerBuilder {

    ExternalDisplayerBuilder(String componentId, DisplayerSettings settings) {
        super(settings);
        addProperty(ExternalComponent.COMPONENT_ID_KEY, componentId);
        settings.setComponentId(componentId);
    }

    public static ExternalDisplayerBuilder create(String id, DisplayerSettings settings) {
        return new ExternalDisplayerBuilder(id, settings);
    }

    public ExternalDisplayerBuilder componentProperty(String key, String value) {
        settings.setComponentProperty(key, value);
        return this;
    }

}