/*
 * Copyright 2016 JBoss Inc
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

package org.livespark.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * This uses GWT to provide client side compile time resolving of locales. See:
 * http://code.google.com/docreader/#p=google-web-toolkit-doc-1-5&s=google-web-
 * toolkit-doc-1-5&t=DevGuideInternationalization (for more information).
 * <p/>
 * Each method name matches up with a key in Constants.properties (the
 * properties file can still be used on the server). To use this, use
 * <code>GWT.create(Constants.class)</code>.
 */
public interface AppConstants
        extends
        Messages {

    AppConstants INSTANCE = GWT.create( AppConstants.class );

    String home();

    String homePage();

    String authoring();

    String project_authoring();

    String administration();

    String contributors();

    String timeline();

    String people();
    
    String userManagement();
    
    String groupManagement();

    String deploy();

    String ruleDeployments();

    String artifactRepository();

    String newItem();

    String find();

    String plugins();

    String extensions();

    String search();

    String missingDefaultPerspective();

    String explore();

    String repositories();

    String listRepositories();

    String cloneRepository();

    String newRepository();

    String inboxIncomingChanges();

    String inboxRecentlyEdited();

    String inboxRecentlyOpened();

    String tools();

    String Role();

    String User();

    String LogOut();

    String MenuOrganizationalUnits();

    String MenuManageOrganizationalUnits();

    String Repository();

    String Upload();

    String Refresh();

    String Asset_Management();

    String tasks();

    String Tasks_List();

    String Apps();

    String DataSets();

    String logoBannerError();

    String assetSearch();

    String Examples();

    String Messages();

}
