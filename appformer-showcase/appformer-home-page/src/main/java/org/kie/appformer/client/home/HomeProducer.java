/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.appformer.client.home;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.appformer.client.resources.i18n.HomeConstants;
import org.kie.workbench.common.screens.home.model.HomeModel;
import org.kie.workbench.common.screens.home.model.HomeModelProvider;
import org.kie.workbench.common.screens.home.model.ModelUtils;
import org.uberfire.client.mvp.PlaceManager;

import static org.kie.workbench.common.workbench.client.PerspectiveIds.BUSINESS_DASHBOARDS;
import static org.kie.workbench.common.workbench.client.PerspectiveIds.LIBRARY;
import static org.kie.workbench.common.workbench.client.PerspectiveIds.SERVER_MANAGEMENT;
import static org.uberfire.workbench.model.ActivityResourceType.PERSPECTIVE;

/**
 * Producer method for the Home Page content
 */
@ApplicationScoped
public class HomeProducer implements HomeModelProvider {

    @Inject
    private PlaceManager placeManager;

    @Inject
    private TranslationService translationService;

    public HomeModel get() {
        final HomeModel model = new HomeModel(translationService.format(HomeConstants.Heading),
                                              translationService.format(HomeConstants.SubHeading),
                                              "images/home_bg.jpg");

        model.addShortcut(ModelUtils.makeShortcut("pficon-blueprint",
                                                  translationService.format(HomeConstants.Design),
                                                  translationService.format(HomeConstants.DesignDescription),
                                                  () -> placeManager.goTo(LIBRARY),
                                                  LIBRARY,
                                                  PERSPECTIVE));
        model.addShortcut(ModelUtils.makeShortcut("pficon-build",
                                                  translationService.format(HomeConstants.DevOps),
                                                  translationService.format(HomeConstants.DevOpsDescription),
                                                  () -> placeManager.goTo(SERVER_MANAGEMENT),
                                                  SERVER_MANAGEMENT,
                                                  PERSPECTIVE));
        model.addShortcut(ModelUtils.makeShortcut("pficon-trend-up",
                                                  translationService.format(HomeConstants.Track),
                                                  translationService.format(HomeConstants.TrackDescription),
                                                  () -> placeManager.goTo(BUSINESS_DASHBOARDS),
                                                  BUSINESS_DASHBOARDS,
                                                  PERSPECTIVE));

        return model;
    }
}
