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
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;

import org.kie.appformer.client.resources.i18n.HomePageCommunityConstants;
import org.kie.workbench.common.screens.home.model.HomeModel;
import org.kie.workbench.common.screens.home.model.ModelUtils;
import org.kie.workbench.common.screens.home.model.SectionEntry;
import org.uberfire.client.mvp.PlaceManager;

import static org.uberfire.workbench.model.ActivityResourceType.*;
import static org.kie.workbench.common.workbench.client.PerspectiveIds.*;

/**
 * Producer method for the Home Page content
 */
@ApplicationScoped
public class HomeProducer {

    private HomePageCommunityConstants constants = HomePageCommunityConstants.INSTANCE;

    private HomeModel model;

    @Inject
    private PlaceManager placeManager;

    public void init() {
        final String url = GWT.getModuleBaseURL();
        model = new HomeModel( constants.homeTheKnowledgeLifeCycle() );
        model.addCarouselEntry( ModelUtils.makeCarouselEntry( constants.homeAuthor(),
                                                              constants.homeAuthorCaption(),
                                                              url + "/images/HandHome.jpg" ) );
        model.addCarouselEntry( ModelUtils.makeCarouselEntry( constants.homeDeploy(),
                                                              constants.homeDeployCaption(),
                                                              url + "/images/HandHome.jpg" ) );

        final SectionEntry s1 = ModelUtils.makeSectionEntry( constants.authoring() );

        s1.addChild( ModelUtils.makeSectionEntry( constants.project_authoring(),
                () -> placeManager.goTo( AUTHORING ),
                AUTHORING, PERSPECTIVE ) );

        s1.addChild( ModelUtils.makeSectionEntry( constants.contributors(),
                () -> placeManager.goTo( CONTRIBUTORS ),
                CONTRIBUTORS, PERSPECTIVE ) );

        s1.addChild( ModelUtils.makeSectionEntry( constants.artifactRepository(),
                () -> placeManager.goTo( GUVNOR_M2REPO ),
                GUVNOR_M2REPO, PERSPECTIVE ) );

        s1.addChild( ModelUtils.makeSectionEntry( constants.administration(),
                () -> placeManager.goTo( ADMINISTRATION ),
                ADMINISTRATION, PERSPECTIVE ) );

        final SectionEntry s2 = ModelUtils.makeSectionEntry( constants.deploy() );

        s2.addChild( ModelUtils.makeSectionEntry( constants.ruleDeployments(),
                () -> placeManager.goTo( SERVER_MANAGEMENT ),
                SERVER_MANAGEMENT, PERSPECTIVE ) );

        final SectionEntry s3 = ModelUtils.makeSectionEntry( constants.tasks() );

        s3.addChild( ModelUtils.makeSectionEntry( constants.Tasks_List(),
                () -> placeManager.goTo( DATASET_TASKS ),
                DATASET_TASKS, PERSPECTIVE ) );

        model.addSection( s1 );
        model.addSection( s2 );
        model.addSection( s3 );
    }

    @Produces
    public HomeModel getModel() {
        return model;
    }

}