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
package org.dashbuilder.client.services.dummy;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.uberfire.experimental.client.service.ClientExperimentalFeaturesRegistryService;
import org.uberfire.experimental.service.registry.ExperimentalFeature;
import org.uberfire.experimental.service.registry.ExperimentalFeaturesRegistry;

@Alternative
@ApplicationScoped
public class RuntimeClientExperimentalFeaturesRegistryService implements ClientExperimentalFeaturesRegistryService {

    ExperimentalFeaturesRegistry registry = new ExperimentalFeaturesRegistry() {

        @Override
        public boolean isFeatureEnabled(String featureId) {
            return true;
        }

        @Override
        public Optional<ExperimentalFeature> getFeature(String featureId) {
            return Optional.empty();
        }

        @Override
        public Collection<ExperimentalFeature> getAllFeatures() {
            return Collections.emptyList();
        }
    };

    @Override
    public ExperimentalFeaturesRegistry getFeaturesRegistry() {
        return registry;
    }

    @Override
    public boolean isFeatureEnabled(String featureId) {
        return true;
    }

    @Override
    public Boolean isExperimentalEnabled() {
        return false;
    }

    @Override
    public void loadRegistry() {
        // empty
    }

    @Override
    public void updateExperimentalFeature(String featureId, boolean enabled) {
        // empty
    }

}
