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

package org.uberfire.experimental.client.mvp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.dom.client.Document;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.mvp.experimental.ExperimentalActivitiesAuthorizationManager;
import org.uberfire.experimental.client.disabled.DisabledFeatureActivity;
import org.uberfire.experimental.client.service.ClientExperimentalFeaturesRegistryService;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.ConditionalPlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.mvp.impl.PathPlaceRequest;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PartDefinition;

@Singleton
public class ExperimentalActivitiesAuthorizationManagerImpl implements ExperimentalActivitiesAuthorizationManager {

    private ClientExperimentalFeaturesRegistryService experimentalFeaturesRegistryService;
    private SyncBeanManager iocManager;
    private Supplier<String> uniqueIdSupplier;

    protected Map<String, String> activityIdToExperimentalFeatureId = new HashMap<>();
    protected Map<String, String> activityToExperimentalFeatureId = new HashMap<>();

    @Inject
    public ExperimentalActivitiesAuthorizationManagerImpl(SyncBeanManager iocManager, ClientExperimentalFeaturesRegistryService experimentalFeaturesRegistryService) {
        this(iocManager, experimentalFeaturesRegistryService, () -> createUniqueId());
    }

    ExperimentalActivitiesAuthorizationManagerImpl(SyncBeanManager iocManager, ClientExperimentalFeaturesRegistryService experimentalFeaturesRegistryService, Supplier<String> uniqueIdSupplier) {
        this.iocManager = iocManager;
        this.uniqueIdSupplier = uniqueIdSupplier;
        this.experimentalFeaturesRegistryService = experimentalFeaturesRegistryService;
    }

    public void init() {
        Collection<SyncBeanDef<ExperimentalActivityReference>> activities = iocManager.lookupBeans(ExperimentalActivityReference.class);

        activities.stream()
                .map(SyncBeanDef::getInstance)
                .forEach(activity -> {
                    activityIdToExperimentalFeatureId.put(activity.getActivityId(), activity.getExperimentalFeatureId());
                    activityToExperimentalFeatureId.put(activity.getActivityTypeName(), activity.getExperimentalFeatureId());
                });
    }

    @Override
    public boolean authorize(Object activity) {
        return authorizeByClassName(activity.getClass().getName());
    }

    @Override
    public void securePart(PanelDefinition panel, PartDefinition part) {

        final PlaceRequest request = part.getPlace();
        final String identifier = request.getIdentifier();

        if (request instanceof PathPlaceRequest) {
            return;
        }

        if (activityIdToExperimentalFeatureId.containsKey(identifier)) {
            panel.removePart(part);

            DefaultPlaceRequest disabledRequest = new DefaultPlaceRequest(getDisabledActivityId());

            disabledRequest.addParameter("id", uniqueIdSupplier.get());

            part.setPlace(new ConditionalPlaceRequest(identifier, request.getParameters()).when(placeRequest -> authorizeByActivityId(identifier)).orElse(disabledRequest));
        }
    }

    protected boolean authorizeByClassName(final String activityClassName) {
        return doAuthorize(() -> activityToExperimentalFeatureId.get(activityClassName));
    }

    protected boolean authorizeByActivityId(final String activityId) {
        return doAuthorize(() -> activityIdToExperimentalFeatureId.get(activityId));
    }

    protected boolean doAuthorize(final Supplier<String> keySupplier) {
        Optional<String> optional = Optional.ofNullable(keySupplier.get());

        return optional.map(this::authorize).orElse(true);
    }

    protected boolean authorize(final String experimentalFeatureId) {
        return experimentalFeaturesRegistryService.isFeatureEnabled(experimentalFeatureId);
    }

    protected String getDisabledActivityId() {
        return DisabledFeatureActivity.ID;
    }

    private static String createUniqueId() {
        return Document.get().createUniqueId();
    }
}
