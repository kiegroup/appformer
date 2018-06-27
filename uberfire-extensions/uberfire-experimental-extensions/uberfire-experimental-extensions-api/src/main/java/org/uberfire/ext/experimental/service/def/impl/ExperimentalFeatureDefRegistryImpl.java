/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.ext.experimental.service.def.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.uberfire.ext.experimental.service.def.ExperimentalFeatureDefRegistry;
import org.uberfire.ext.experimental.service.def.ExperimentalFeatureDefinition;

public class ExperimentalFeatureDefRegistryImpl implements ExperimentalFeatureDefRegistry {

    protected Map<String, ExperimentalFeatureDefinition> features = new HashMap<>();

    public void register(ExperimentalFeatureDefinition featureDefinition) {
        features.put(featureDefinition.getId(), featureDefinition);
    }

    @Override
    public ExperimentalFeatureDefinition getFeatureById(String definitionId) {
        return features.get(definitionId);
    }

    @Override
    public Collection<ExperimentalFeatureDefinition> getAllFeatures() {
        return features.values();
    }
}
