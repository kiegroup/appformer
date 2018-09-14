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

package org.uberfire.experimental.service;

import java.io.IOException;
import java.net.URI;

import org.assertj.core.api.Assertions;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.server.spaces.SpacesAPIImpl;
import org.uberfire.experimental.service.definition.ExperimentalFeatureDefinition;
import org.uberfire.experimental.service.registry.ExperimentalFeaturesRegistry;
import org.uberfire.experimental.service.registry.impl.ExperimentalFeaturesRegistryImpl;
import org.uberfire.experimental.service.definition.impl.ExperimentalFeatureDefRegistryImpl;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.Path;
import org.uberfire.mocks.FileSystemTestingUtils;
import org.uberfire.mocks.SessionInfoMock;
import org.uberfire.rpc.SessionInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BackendExperimentalFeaturesRegistryServiceTest {

    private static final String FEATURE_1 = "feature_1";
    private static final String FEATURE_2 = "feature_2";
    private static final String FEATURE_3 = "feature_3";

    private static final String EXPECTED_STORAGE_PATH = "/experimental/my-user/.experimental";

    private static final String USER_NAME = "my-user";

    private static FileSystemTestingUtils fileSystemTestingUtils = new FileSystemTestingUtils();

    private SessionInfo sessionInfo;
    private FileSystem fileSystem;
    private IOService ioService;

    private ExperimentalFeatureDefRegistryImpl defRegistry;

    private BackendExperimentalFeaturesRegistryServiceImpl service;

    @Before
    public void setup() throws IOException {
        MappingContextSingleton.get();
        fileSystemTestingUtils.setup();

        sessionInfo = new SessionInfoMock(USER_NAME);
        fileSystem = fileSystemTestingUtils.getFileSystem();
        ioService = spy(fileSystemTestingUtils.getIoService());

        doNothing().when(ioService).startBatch(any(FileSystem.class));
        doNothing().when(ioService).endBatch();
        doReturn(fileSystem).when(ioService).newFileSystem(any(URI.class), anyMap());

        defRegistry = new ExperimentalFeatureDefRegistryImpl();
        defRegistry.register(new ExperimentalFeatureDefinition(FEATURE_1, false, "", FEATURE_1, FEATURE_1));
        defRegistry.register(new ExperimentalFeatureDefinition(FEATURE_2, false, "", FEATURE_2, FEATURE_2));
        defRegistry.register(new ExperimentalFeatureDefinition(FEATURE_3, false, "", FEATURE_3, FEATURE_3));
    }

    @After
    public void clean() {
        ioService.delete(fileSystem.getPath(EXPECTED_STORAGE_PATH));
    }

    @Test
    public void testLoadFirstTimeWithExperimentals() {
        testLoadFirstTime(true);
    }

    @Test
    public void testLoadFirstTimeWithoutExperimentals() {
        testLoadFirstTime(false);
    }

    @Test
    public void testLoadSecondTimeWithExperimentals() {
        testLoadFirstTime(true);
        testLoadSecondTime();
    }

    @Test
    public void testLoadSecondTimeWithoutExperimentals() {
        testLoadFirstTime(true);
        testLoadSecondTime();
    }

    @Test
    public void testUpdateExistingRegistry() {
        init(true);

        assertTrue(service.isExperimentalEnabled());

        ExperimentalFeaturesRegistryImpl registry = service.getFeaturesRegistry();

        Assertions.assertThat(registry)
                .isNotNull()
                .isInstanceOf(ExperimentalFeaturesRegistryImpl.class);

        Assertions.assertThat(registry.getAllFeatures())
                .isNotNull()
                .hasSize(defRegistry.getAllFeatures().size());

        checkFeature(registry, FEATURE_1, false);
        checkFeature(registry, FEATURE_2, false);
        checkFeature(registry, FEATURE_3, false);

        registry.getFeature(FEATURE_1).setEnabled(true);
        registry.getFeature(FEATURE_2).setEnabled(true);
        registry.getFeature(FEATURE_3).setEnabled(true);

        assertFalse(service.isFeatureEnabled(FEATURE_1));
        assertFalse(service.isFeatureEnabled(FEATURE_2));
        assertFalse(service.isFeatureEnabled(FEATURE_3));

        service.updateRegistry(registry);

        verify(service, times(2)).updateRegistry(any());
        verify(ioService, times(2)).startBatch(any());

        registry = service.getFeaturesRegistry();

        checkFeature(registry, FEATURE_1, true);
        checkFeature(registry, FEATURE_2, true);
        checkFeature(registry, FEATURE_3, true);

        assertTrue(service.isFeatureEnabled(FEATURE_1));
        assertTrue(service.isFeatureEnabled(FEATURE_2));
        assertTrue(service.isFeatureEnabled(FEATURE_3));
    }

    private void checkFeature(ExperimentalFeaturesRegistryImpl registry, String feature, boolean enabled) {
        Assertions.assertThat(registry.getFeature(feature))
                .isNotNull()
                .hasFieldOrPropertyWithValue("featureId", feature)
                .hasFieldOrPropertyWithValue("enabled", enabled);
    }

    private void testLoadFirstTime(boolean enableExperimental) {

        init(enableExperimental);

        assertEquals(enableExperimental, service.isExperimentalEnabled());

        ExperimentalFeaturesRegistry registry = service.getFeaturesRegistry();

        Assertions.assertThat(registry)
                .isNotNull()
                .isInstanceOf(ExperimentalFeaturesRegistryImpl.class);

        Assertions.assertThat(registry.getAllFeatures())
                .isNotNull()
                .hasSize(defRegistry.getAllFeatures().size());

        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);

        verify(ioService).exists(pathCaptor.capture());

        assertEquals(EXPECTED_STORAGE_PATH, pathCaptor.getValue().toString());

        verify(service).updateRegistry(any());

        verify(ioService).startBatch(fileSystem);
        verify(ioService).endBatch();
    }

    private void testLoadSecondTime() {
        ExperimentalFeaturesRegistry registry = service.getFeaturesRegistry();

        Assertions.assertThat(registry)
                .isNotNull()
                .isInstanceOf(ExperimentalFeaturesRegistryImpl.class);

        Assertions.assertThat(registry.getAllFeatures())
                .isNotNull()
                .hasSize(defRegistry.getAllFeatures().size());

        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);

        verify(ioService, times(2)).exists(pathCaptor.capture());

        assertEquals(EXPECTED_STORAGE_PATH, pathCaptor.getValue().toString());

        verify(ioService, times(1)).newInputStream(any());
    }

    private void init(Boolean enableExperimental) {

        System.setProperty(BackendExperimentalFeaturesRegistryServiceImpl.EXPERIMENTAL_FEATURES_PROPERTY_NAME, enableExperimental.toString());

        service = spy(new BackendExperimentalFeaturesRegistryServiceImpl(sessionInfo, new SpacesAPIImpl(), ioService, defRegistry));

        service.init();
    }
}
