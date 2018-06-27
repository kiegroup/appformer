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

package org.uberfire.ext.experimental.client.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.ext.experimental.client.editor.feature.ExperimentalFeatureEditor;
import org.uberfire.ext.experimental.client.resources.i18n.UberfireExperimentalConstants;
import org.uberfire.ext.experimental.client.service.ClientExperimentalFeaturesRegistryService;
import org.uberfire.ext.experimental.service.registry.ExperimentalFeature;
import org.uberfire.ext.experimental.service.registry.ExperimentalFeaturesRegistry;
import org.uberfire.ext.experimental.service.editor.EditableFeature;
import org.uberfire.ext.experimental.service.editor.FeatureEditorModel;
import org.uberfire.ext.experimental.service.editor.FeaturesEditorService;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnOpen;

@ApplicationScoped
@WorkbenchScreen(identifier = ExperimentalFeaturesEditorScreen.SCREEN_ID)
public class ExperimentalFeaturesEditorScreen implements ExperimentalFeaturesEditorScreenView.Presenter {

    public static final String SCREEN_ID = "ExperimentalFeaturesEditor";

    private final TranslationService translationService;
    private final ClientExperimentalFeaturesRegistryService registryService;
    private final ExperimentalFeaturesEditorScreenView view;
    private final ManagedInstance<ExperimentalFeatureEditor> instance;
    private final Caller<FeaturesEditorService> editorService;

    protected final List<ExperimentalFeatureEditor> featureEditors = new ArrayList<>();

    @Inject
    public ExperimentalFeaturesEditorScreen(final TranslationService translationService, final ClientExperimentalFeaturesRegistryService registryService, final ExperimentalFeaturesEditorScreenView view, final ManagedInstance<ExperimentalFeatureEditor> instance, Caller<FeaturesEditorService> editorService) {
        this.translationService = translationService;
        this.registryService = registryService;
        this.view = view;
        this.instance = instance;
        this.editorService = editorService;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @OnOpen
    public void show() {
        clear();

        ExperimentalFeaturesRegistry registry = registryService.getFeaturesRegistry();

        if(registry != null) {
            registry.getFeaturesList()
                    .stream()
                    .map(this::getFeatureEditor)
                    .sorted()
                    .collect(Collectors.toCollection(() -> featureEditors))
                    .forEach(view::add);
        }
    }

    private ExperimentalFeatureEditor getFeatureEditor(ExperimentalFeature feature) {
        ExperimentalFeatureEditor element = instance.get();
        element.render(new EditableFeature(feature.getFeatureId(), feature.isEnabled()));
        return element;
    }

    @OnClose
    public void close() {
        Optional<ExperimentalFeatureEditor> optional = featureEditors.stream()
                .filter(ExperimentalFeatureEditor::hasChanged)
                .findAny();

        if (optional.isPresent()) {
            doSave();
        }
    }

    private void doSave() {
        List<EditableFeature> modelElements = featureEditors.stream().map(ExperimentalFeatureEditor::getFeature).collect(Collectors.toList());

        FeatureEditorModel model = new FeatureEditorModel(modelElements);

        editorService.call((RemoteCallback<Void>) aVoid -> registryService.loadRegistry()).save(model);
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return translationService.getTranslation(UberfireExperimentalConstants.experimentalFeaturesTitle);
    }

    @WorkbenchPartView
    public ExperimentalFeaturesEditorScreenView getView() {
        return view;
    }

    @PreDestroy
    public void clear() {
        featureEditors.clear();
        view.clear();
        instance.destroyAll();
    }
}
