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

package org.uberfire.experimental.client.editor.feature;

import java.util.Comparator;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.soup.commons.validation.PortablePreconditions;
import org.uberfire.experimental.service.definition.ExperimentalFeatureDefRegistry;
import org.uberfire.experimental.service.definition.ExperimentalFeatureDefinition;
import org.uberfire.experimental.service.editor.EditableFeature;

@Dependent
public class ExperimentalFeatureEditor implements ExperimentalFeatureEditorView.Presenter,
                                                  IsElement,
                                                  Comparable<ExperimentalFeatureEditor> {

    private static Comparator<String> comparator = Comparator.nullsFirst(String::compareTo);

    private ExperimentalFeatureDefRegistry registry;
    private TranslationService translationService;
    private ExperimentalFeatureEditorView view;

    private String name;
    private EditableFeature feature;
    private boolean changed = false;

    @Inject
    public ExperimentalFeatureEditor(ExperimentalFeatureDefRegistry registry, TranslationService translationService, ExperimentalFeatureEditorView view) {
        this.registry = registry;
        this.translationService = translationService;
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public void render(EditableFeature feature) {
        PortablePreconditions.checkNotNull("feature", feature);

        ExperimentalFeatureDefinition definition = registry.getFeatureById(feature.getDefinitionId());

        PortablePreconditions.checkNotNull("definition", definition);

        this.feature = feature;

        name = getTranslation(definition.getNameKey(), definition.getId());

        String description = getTranslation(definition.getDescriptionKey(), null);

        view.render(name, description, feature.getEnabled());
    }

    private String getTranslation(String key, String defaultValue) {
        String result = translationService.getTranslation(key);

        if (result == null) {
            return defaultValue;
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public EditableFeature getFeature() {
        return feature;
    }

    @Override
    public void notifyChange(boolean newEnabledValue) {
        if (newEnabledValue != feature.getEnabled()) {
            feature.setEnabled(newEnabledValue);
            changed = !changed;
        }
    }

    public boolean hasChanged() {
        return changed;
    }

    @Override
    public HTMLElement getElement() {
        return view.getElement();
    }

    @Override
    public int compareTo(ExperimentalFeatureEditor other) {
        return comparator.compare(this.getName(), other.getName());
    }
}
