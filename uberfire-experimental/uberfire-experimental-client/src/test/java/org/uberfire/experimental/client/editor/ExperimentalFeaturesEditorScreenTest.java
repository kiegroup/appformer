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

package org.uberfire.experimental.client.editor;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.uberfire.experimental.client.editor.feature.ExperimentalFeatureEditor;
import org.uberfire.experimental.client.resources.i18n.UberfireExperimentalConstants;
import org.uberfire.experimental.client.service.ClientExperimentalFeaturesRegistryService;
import org.uberfire.experimental.service.editor.EditableExperimentalFeature;
import org.uberfire.experimental.service.editor.FeaturesEditorService;
import org.uberfire.experimental.service.registry.impl.ExperimentalFeatureImpl;
import org.uberfire.experimental.service.registry.impl.ExperimentalFeaturesRegistryImpl;
import org.uberfire.mocks.CallerMock;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.uberfire.experimental.client.editor.test.TestExperimentalFeatureDefRegistry.FEATURE_1;
import static org.uberfire.experimental.client.editor.test.TestExperimentalFeatureDefRegistry.FEATURE_2;
import static org.uberfire.experimental.client.editor.test.TestExperimentalFeatureDefRegistry.FEATURE_3;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentalFeaturesEditorScreenTest {

    @Mock
    private TranslationService translationService;

    @Mock
    private ClientExperimentalFeaturesRegistryService registryService;

    private ExperimentalFeaturesRegistryImpl registry;

    private List<ExperimentalFeatureImpl> features = new ArrayList<>();

    @Mock
    private ExperimentalFeaturesEditorScreenView view;

    @Mock
    private ManagedInstance<ExperimentalFeatureEditor> instance;

    @Mock
    private FeaturesEditorService featuresEditorService;

    private CallerMock<FeaturesEditorService> editorServiceCaller;

    private ExperimentalFeaturesEditorScreen presenter;

    @Before
    public void init() {
        features.add(new ExperimentalFeatureImpl(FEATURE_1, false));
        features.add(new ExperimentalFeatureImpl(FEATURE_2, false));
        features.add(new ExperimentalFeatureImpl(FEATURE_3, false));

        registry = new ExperimentalFeaturesRegistryImpl(features);

        when(registryService.getFeaturesRegistry()).thenReturn(registry);

        when(instance.get()).thenAnswer((Answer<ExperimentalFeatureEditor>) invocationOnMock -> mock(ExperimentalFeatureEditor.class));

        editorServiceCaller = new CallerMock<>(featuresEditorService);

        presenter = new ExperimentalFeaturesEditorScreen(translationService, registryService, view, instance, editorServiceCaller);
    }

    @Test
    public void testBasicFunctions() {
        presenter.init();

        verify(view).init(presenter);

        presenter.getTitle();

        verify(translationService).getTranslation(UberfireExperimentalConstants.experimentalFeaturesTitle);

        presenter.clear();

        verifyClear();

        assertSame(view, presenter.getView());
    }

    @Test
    public void testShow() {

        presenter.show();

        verifyClear();

        verify(instance, times(features.size())).get();
    }

    @Test
    public void testModificationCallback() {
        EditableExperimentalFeature feature = new EditableExperimentalFeature(FEATURE_1, true);

        presenter.doSave(feature);

        verify(featuresEditorService, times(1)).save(feature);
        verify(registryService, times(1)).updateExperimentalFeature(feature.getFeatureId(), feature.isEnabled());
    }

    private void verifyClear() {
        verify(view).clear();
        verify(instance).destroyAll();
    }
}
