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

package org.uberfire.ext.experimental.client.editor.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.experimental.client.editor.test.TestExperimentalFeatureDefRegistry;
import org.uberfire.ext.experimental.service.editor.EditableFeature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentalFeatureEditorTest {

    private TestExperimentalFeatureDefRegistry defRegistry;

    @Mock
    private ExperimentalFeatureEditorView view;

    @Mock
    private TranslationService translationService;

    private ExperimentalFeatureEditor editor;

    @Before
    public void init() {

        defRegistry = new TestExperimentalFeatureDefRegistry();

        editor = new ExperimentalFeatureEditor(defRegistry, translationService, view);
    }

    @Test
    public void testBasicFunctionallity() {
        editor.init();

        verify(view).init(editor);

        editor.getElement();

        verify(view).getElement();

        EditableFeature feature = spy(new EditableFeature(TestExperimentalFeatureDefRegistry.FEATURE_1, false));

        editor.render(feature);

        verify(translationService, times(2)).getTranslation(TestExperimentalFeatureDefRegistry.FEATURE_1);

        verify(view).render(eq(TestExperimentalFeatureDefRegistry.FEATURE_1), eq(null), eq(false));

        assertSame(feature, editor.getFeature());
        assertEquals(TestExperimentalFeatureDefRegistry.FEATURE_1, editor.getName());

        editor.notifyChange(false);

        verify(feature, never()).setEnabled(false);
        assertFalse(editor.hasChanged());

        editor.notifyChange(true);

        verify(feature).setEnabled(true);
        assertTrue(editor.hasChanged());

        editor.notifyChange(false);

        verify(feature).setEnabled(false);
        assertFalse(editor.hasChanged());
    }

    @Test
    public void testCompareTo() {

        new ArrayList<>();

        ExperimentalFeatureEditor editor4 = new ExperimentalFeatureEditor(defRegistry, translationService, mock(ExperimentalFeatureEditorView.class));

        EditableFeature feature3 = new EditableFeature(TestExperimentalFeatureDefRegistry.FEATURE_3, false);
        ExperimentalFeatureEditor editor3 = new ExperimentalFeatureEditor(defRegistry, translationService, mock(ExperimentalFeatureEditorView.class));
        editor3.render(feature3);

        EditableFeature feature1 = new EditableFeature(TestExperimentalFeatureDefRegistry.FEATURE_1, false);
        ExperimentalFeatureEditor editor1 = new ExperimentalFeatureEditor(defRegistry, translationService, mock(ExperimentalFeatureEditorView.class));
        editor1.render(feature1);

        ExperimentalFeatureEditor editor5 = new ExperimentalFeatureEditor(defRegistry, translationService, mock(ExperimentalFeatureEditorView.class));

        EditableFeature feature2 = new EditableFeature(TestExperimentalFeatureDefRegistry.FEATURE_2, false);
        ExperimentalFeatureEditor editor2 = new ExperimentalFeatureEditor(defRegistry, translationService, mock(ExperimentalFeatureEditorView.class));
        editor2.render(feature2);

        List<ExperimentalFeatureEditor> editors = Arrays.asList(editor1, editor2, editor3, editor4, editor5);

        List<ExperimentalFeatureEditor> sortedEditors = editors.stream().sorted().collect(Collectors.toList());

        Assertions.assertThat(sortedEditors)
                .hasSize(editors.size())
                .containsExactly(editor4, editor5, editor1, editor2, editor3);
    }
}
