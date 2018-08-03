/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.client.screens.react;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.uberfire.client.jsapi.JSPlaceRequest;
import org.uberfire.client.mvp.AbstractWorkbenchScreenActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

public class JsWorkbenchScreenActivity extends AbstractWorkbenchScreenActivity {

    private PlaceRequest place;
    private JsNativeScreen screen;

    public JsWorkbenchScreenActivity(final JsNativeScreen screen,
                                     final PlaceManager placeManager) {

        super(placeManager);
        this.screen = screen;
    }

    //
    //
    //LIFECYCLE

    @Override
    public void onStartup(final PlaceRequest place) {
        this.place = place;
        screen.run("on_startup", JSPlaceRequest.fromPlaceRequest(place));
    }

    @Override
    public void onOpen() {
        Scheduler.get().scheduleDeferred(() -> {
            screen.init();
            screen.run("on_open");
            placeManager.executeOnOpenCallbacks(place);
        });
    }

    @Override
    public void onClose() {
        screen.run("on_close");
        placeManager.executeOnCloseCallbacks(place);
    }

    @Override
    public boolean onMayClose() {
        return (boolean) screen.run("on_may_close");
    }

    @Override
    public void onShutdown() {
        screen.run("on_shutdown");
    }

    @Override
    public void onFocus() {
        screen.run("on_focus");
    }

    @Override
    public void onLostFocus() {
        screen.run("on_lost_focus");
    }

    //
    //
    // PROPERTIES

    @Override
    public String getTitle() {
        return screen.get("title");
    }

    @Override
    public Position getDefaultPosition() {
        return CompassPosition.ROOT;
    }

    @Override
    public PlaceRequest getPlace() {
        return place;
    }

    @Override
    public String getIdentifier() {
        return screen.get("id");
    }

    @Override
    public IsWidget getTitleDecoration() {
        return null;
    }

    @Override
    public Menus getMenus() {
        return null;
    }

    @Override
    public ToolBar getToolBar() {
        return null;
    }

    @Override
    public PlaceRequest getOwningPlace() {
        return null;
    }

    @Override
    public IsWidget getWidget() {
        return ElementWrapperWidget.getWidget(screen.getElement());
    }

    @Override
    public String contextId() {
        return screen.get("contextId");
    }

    @Override
    public int preferredHeight() {
        return -1;
    }

    @Override
    public int preferredWidth() {
        return -1;
    }
}
