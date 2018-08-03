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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.experimental.service.backend.BackendExperimentalFeaturesRegistryService;
import org.uberfire.experimental.service.backend.ExperimentalFeaturesSession;
import org.uberfire.experimental.service.backend.impl.ExperimentalFeaturesSessionImpl;
import org.uberfire.experimental.service.definition.ExperimentalFeatureDefRegistry;
import org.uberfire.experimental.service.editor.FeatureEditorModel;
import org.uberfire.experimental.service.editor.FeaturesEditorService;
import org.uberfire.experimental.service.registry.ExperimentalFeaturesRegistry;
import org.uberfire.experimental.service.registry.impl.ExperimentalFeatureImpl;
import org.uberfire.experimental.service.registry.impl.ExperimentalFeaturesRegistryImpl;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.FileSystemAlreadyExistsException;
import org.uberfire.java.nio.file.Path;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.spaces.SpacesAPI;

@Service
@ApplicationScoped
public class BackendExperimentalFeaturesRegistryServiceImpl implements ExperimentalFeaturesRegistryService,
                                                                       BackendExperimentalFeaturesRegistryService,
                                                                       FeaturesEditorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackendExperimentalFeaturesRegistryServiceImpl.class);

    public static final String COMMENTS = "Experimental features registry";

    public static final String EXPERIMENTAL_FEATURES_PROPERTY_NAME = "appformer.experimental.features";

    private static final String EXPERIMENTAL = "experimental";

    public static final String EXPERIMENTAL_FILE_NAME = "." + EXPERIMENTAL;

    public static final String SEPARATOR = "/";

    public static final String EXPERIMENTAL_STORAGE_FOLDER = SEPARATOR + EXPERIMENTAL;

    private final SessionInfo sessionInfo;

    private final SpacesAPI spaces;

    private IOService ioService;

    private final ExperimentalFeatureDefRegistry defRegistry;

    private FileSystem fileSystem;

    @Inject
    public BackendExperimentalFeaturesRegistryServiceImpl(final SessionInfo sessionInfo, final SpacesAPI spaces, @Named("configIO") final IOService ioService, final ExperimentalFeatureDefRegistry defRegistry) {
        this.sessionInfo = sessionInfo;
        this.spaces = spaces;
        this.ioService = ioService;
        this.defRegistry = defRegistry;
    }

    @PostConstruct
    public void init() {
        initializeFileSystem();
    }

    @Override
    public ExperimentalFeaturesRegistryImpl getFeaturesRegistry() {

        ExperimentalFeaturesRegistryImpl registry = loadRegistry();

        if (registry == null) {
            registry = newRegistry();
            updateRegistry(registry);
        }

        return registry;
    }

    private ExperimentalFeaturesRegistryImpl newRegistry() {
        List<ExperimentalFeatureImpl> features = defRegistry.getAllFeatures().stream()
                .map(featureDef -> new ExperimentalFeatureImpl(featureDef.getId(), false))
                .collect(Collectors.toList());
        return new ExperimentalFeaturesRegistryImpl(features);
    }

    @Override
    public Boolean isExperimentalEnabled() {
        return Boolean.parseBoolean(System.getProperty(EXPERIMENTAL_FEATURES_PROPERTY_NAME, "false"));
    }

    @Override
    public boolean isFeatureEnabled(String featureId) {
        return isExperimentalEnabled() && getFeaturesRegistry().isFeatureEnabled(featureId);
    }

    @Override
    public void save(FeatureEditorModel model) {
        List<ExperimentalFeatureImpl> features = model.getFeatures().stream()
                .filter(editableFeature -> defRegistry.getFeatureById(editableFeature.getDefinitionId()) != null)
                .map(editableFeature -> new ExperimentalFeatureImpl(editableFeature.getDefinitionId(), editableFeature.getEnabled()))
                .collect(Collectors.toList());

        ExperimentalFeaturesRegistryImpl registry = new ExperimentalFeaturesRegistryImpl(features);

        updateRegistry(registry);
    }

    public void updateRegistry(ExperimentalFeaturesRegistry registry) {
        String path = getStoragePath();

        Path fsPath = fileSystem.getPath(path);

        Properties properties = new Properties();

        registry.getFeaturesList()
                .stream()
                .forEach(feature -> properties.put(feature.getFeatureId(), String.valueOf(feature.isEnabled())));

        try (OutputStream out = ioService.newOutputStream(fsPath)) {
            ioService.startBatch(fileSystem);
            properties.store(out, COMMENTS);
        } catch (Exception ex) {
            LOGGER.warn("Impossible to write registry for user {}: {}", sessionInfo.getIdentity().getIdentifier(), ex);
        } finally {
            ioService.endBatch();
        }
    }

    private ExperimentalFeaturesRegistryImpl loadRegistry() {
        String path = getStoragePath();

        Path fsPath = fileSystem.getPath(path);

        if (ioService.exists(fsPath)) {

            try (InputStream in = ioService.newInputStream(fsPath)) {
                Properties properties = new Properties();

                properties.load(in);

                List<ExperimentalFeatureImpl> features = properties.entrySet().stream()
                        .filter(entry -> defRegistry.getFeatureById((String) entry.getKey()) != null)
                        .map(entry -> new ExperimentalFeatureImpl((String) entry.getKey(), Boolean.valueOf((String) entry.getValue())))
                        .collect(Collectors.toList());

                return new ExperimentalFeaturesRegistryImpl(features);
            } catch (Exception ex) {
                LOGGER.warn("Impossible to load registry", ex);
            }
        }

        return null;
    }

    private String getStoragePath() {
        return EXPERIMENTAL_STORAGE_FOLDER + SEPARATOR + sessionInfo.getIdentity().getIdentifier() + SEPARATOR + EXPERIMENTAL_FILE_NAME;
    }

    @Override
    public ExperimentalFeaturesSession getExperimentalFeaturesSession() {
        return new ExperimentalFeaturesSessionImpl(isExperimentalEnabled(), getFeaturesRegistry());
    }

    protected void initializeFileSystem() {

        final URI fileSystemURI = spaces.resolveFileSystemURI(SpacesAPI.Scheme.GIT, SpacesAPI.DEFAULT_SPACE, "preferences");

        try {
            Map<String, Object> options = new HashMap<>();

            options.put("init", Boolean.TRUE);
            options.put("internal", Boolean.TRUE);

            fileSystem = ioService.newFileSystem(fileSystemURI, options);
        } catch (FileSystemAlreadyExistsException e) {
            fileSystem = ioService.getFileSystem(fileSystemURI);
        }
    }
}
