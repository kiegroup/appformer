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

package org.uberfire.ext.experimental.client.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.experimental.service.registry.impl.ExperimentalFeatureImpl;
import org.uberfire.ext.experimental.service.registry.impl.ExperimentalFeaturesRegistryImpl;
import org.uberfire.ext.experimental.service.backend.impl.ExperimentalFeaturesSessionImpl;
import org.uberfire.ext.experimental.service.backend.BackendExperimentalFeaturesRegistryService;
import org.uberfire.mocks.CallerMock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.uberfire.ext.experimental.client.editor.test.TestExperimentalFeatureDefRegistry.FEATURE_1;
import static org.uberfire.ext.experimental.client.editor.test.TestExperimentalFeatureDefRegistry.FEATURE_2;
import static org.uberfire.ext.experimental.client.editor.test.TestExperimentalFeatureDefRegistry.FEATURE_3;

@RunWith(MockitoJUnitRunner.class)
public class ClientExperimentalFeaturesRegistryServiceImplTest {

    @Mock
    private BackendExperimentalFeaturesRegistryService backendService;

    private CallerMock<BackendExperimentalFeaturesRegistryService> callerMock;

    private ExperimentalFeaturesRegistryImpl registry;

    private ClientExperimentalFeaturesRegistryServiceImpl service;

    @Before
    public void init() {
        callerMock = new CallerMock<>(backendService);

        service = new ClientExperimentalFeaturesRegistryServiceImpl(callerMock);
    }

    @Test
    public void tesBasicTestExperimentalEnabled() {

        doBasicTest(true);
    }

    @Test
    public void tesBasicTestExperimentalDisabled() {

        doBasicTest(false);

    }

    private void doBasicTest(boolean experimentalEnabled) {

        initService(experimentalEnabled);

        assertEquals(experimentalEnabled, service.isExperimentalEnabled());

        /*
         FEATURE_1 & FEATURE_2 are enabled by default BUT when experimental is disabled
         the experimental service makes them disabled
        */
        assertEquals(experimentalEnabled, service.isFeatureEnabled(FEATURE_1));
        assertEquals(experimentalEnabled, service.isFeatureEnabled(FEATURE_2));

        assertEquals(false, service.isFeatureEnabled(FEATURE_3));

        assertEquals(registry, service.getFeaturesRegistry());
    }

    private void initService(boolean experimentalEnabled) {

        List<ExperimentalFeatureImpl> features = new ArrayList<>();

        features.add(new ExperimentalFeatureImpl(FEATURE_1, true));
        features.add(new ExperimentalFeatureImpl(FEATURE_2, true));
        features.add(new ExperimentalFeatureImpl(FEATURE_3, false));

        registry = new ExperimentalFeaturesRegistryImpl(features);

        when(backendService.getExperimentalFeaturesSession()).thenReturn(new ExperimentalFeaturesSessionImpl(experimentalEnabled, registry));

        service.loadRegistry();
    }
}
