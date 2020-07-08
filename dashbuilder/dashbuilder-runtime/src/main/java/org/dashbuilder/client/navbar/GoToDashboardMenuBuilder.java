/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.dashbuilder.client.navbar;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import elemental2.dom.DomGlobal;
import org.dashbuilder.client.RuntimeEntryPoint;
import org.dashbuilder.client.resources.i18n.AppConstants;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.workbench.events.PerspectiveChange;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuFactory.CustomMenuBuilder;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.MenuPosition;
import org.uberfire.workbench.model.menu.impl.BaseMenuCustom;

@ApplicationScoped
public class GoToDashboardMenuBuilder implements MenuFactory.CustomMenuBuilder {

    private AnchorListItem link = new AnchorListItem();

    @Inject
    private PerspectiveManager perspectiveManager;

    @PostConstruct
    public void buildLink() {
        link.setIcon(IconType.EXTERNAL_LINK);

        link.getWidget(0).setStyleName("nav-item-iconic"); // Fix for IE11
        link.setTitle(AppConstants.INSTANCE.dashboardOpenTooltip());

        link.addClickHandler(e -> this.openDashboardInNewWindow());
    }

    @Override
    public void push(CustomMenuBuilder element) {
        // do nothing
    }

    @Override
    public MenuItem build() {
        return new BaseMenuCustom<IsWidget>() {

            @Override
            public IsWidget build() {
                return link;
            }

            @Override
            public MenuPosition getPosition() {
                return MenuPosition.RIGHT;
            }
        };

    }

    private void openDashboardInNewWindow() {
        String currentPlace = perspectiveManager.getCurrentPerspective().getIdentifier();
        String standaloneUrl = Window.Location.createUrlBuilder()
                                              .setParameter(RuntimeEntryPoint.DASHBOARD_PARAM, currentPlace)
                                              .setParameter("standalone", "true")
                                              .buildString();
        DomGlobal.window.open(standaloneUrl);
    }
    
}