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

package org.uberfire.experimental.client.service.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.experimental.client.service.ClientExperimentalFeaturesRegistryService;
import org.uberfire.experimental.service.backend.BackendExperimentalFeaturesRegistryService;
import org.uberfire.experimental.service.backend.impl.ExperimentalFeaturesSessionImpl;
import org.uberfire.experimental.service.registry.ExperimentalFeaturesRegistry;

@ApplicationScoped
public class ClientExperimentalFeaturesRegistryServiceImpl implements ClientExperimentalFeaturesRegistryService {

    private Caller<BackendExperimentalFeaturesRegistryService> backendService;

    private ExperimentalFeaturesSessionImpl session;

    @Inject
    public ClientExperimentalFeaturesRegistryServiceImpl(Caller<BackendExperimentalFeaturesRegistryService> backendService) {
        this.backendService = backendService;
    }

    @Override
    public void loadRegistry() {
        backendService.call((RemoteCallback<ExperimentalFeaturesSessionImpl>) experimentalFeaturesSession -> session = experimentalFeaturesSession).getExperimentalFeaturesSession();
    }

    @Override
    public ExperimentalFeaturesRegistry getFeaturesRegistry() {
        return session.getFeaturesRegistry();
    }

    @Override
    public boolean isFeatureEnabled(String featureId) {
        return isExperimentalEnabled() && getFeaturesRegistry().isFeatureEnabled(featureId);
    }

    @Override
    public Boolean isExperimentalEnabled() {
        return session.isExperimentalFeaturesEnabled();
    }
}
