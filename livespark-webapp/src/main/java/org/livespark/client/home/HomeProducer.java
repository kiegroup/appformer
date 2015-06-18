/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.client.home;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.guvnor.common.services.shared.security.AppRoles;
import org.kie.workbench.common.screens.home.model.HomeModel;
import org.kie.workbench.common.screens.home.model.ModelUtils;
import org.kie.workbench.common.screens.home.model.Section;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.Command;

import com.google.gwt.core.client.GWT;

/**
 * Producer method for the Home Page content
 */
@ApplicationScoped
public class HomeProducer {

    private static String[] PERMISSIONS_ADMIN = new String[]{ AppRoles.ADMIN.getName() };

    private HomeModel model;

    @Inject
    private PlaceManager placeManager;

    @PostConstruct
    public void init() {
        final String url = GWT.getModuleBaseURL();
        model = new HomeModel( "Welcome to the LiveSpark platform" );
        model.addCarouselEntry( ModelUtils.makeCarouselEntry( "Model",
                                                              "Create your persistable models",
                                                              url + "/images/HandHome.jpg" ) );
        model.addCarouselEntry( ModelUtils.makeCarouselEntry( "Desing",
                                                              "Design your forms",
                                                              url + "/images/HandHome.jpg" ) );
        model.addCarouselEntry( ModelUtils.makeCarouselEntry( "Develop",
                "Create your KieApplications",
                url + "/images/HandHome.jpg" ) );

        final Section s1 = new Section( "Discover and Author:" );
        s1.addEntry( ModelUtils.makeSectionEntry( "Author",
                                                  new Command() {

                                                      @Override
                                                      public void execute() {
                                                          placeManager.goTo( "AuthoringPerspective" );
                                                      }
                                                  } ) );
        model.addSection( s1 );
    }

    @Produces
    public HomeModel getModel() {
        return model;
    }

}
