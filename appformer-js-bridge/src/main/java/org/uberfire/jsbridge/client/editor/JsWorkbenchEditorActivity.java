/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.jsbridge.client.editor;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.mvp.AbstractWorkbenchEditorActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.WorkbenchEditorActivity;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

public class JsWorkbenchEditorActivity extends AbstractWorkbenchEditorActivity implements WorkbenchEditorActivity {

    private final JsNativeEditor editor;
    private PlaceRequest placeRequest;

    public JsWorkbenchEditorActivity(final JsNativeEditor editor,
                                     final PlaceManager placeManager) {
        super(placeManager);
        this.editor = editor;
    }

    //
    //Lifecycle

    @Override
    public void onStartup(final ObservablePath path, final PlaceRequest place) {
        this.placeRequest = place;
        editor.af_onEditorStartup(path, place);
    }

    @Override
    public void onOpen() {
        editor.af_onOpen();
    }

    @Override
    public void onSave() {
        editor.af_onSave();
    }

    @Override
    public void onFocus() {
        editor.af_onFocus();
    }

    @Override
    public void onLostFocus() {
        editor.af_onLostFocus();
    }

    @Override
    public boolean onMayClose() {
        return editor.af_onMayClose();
    }

    @Override
    public void onClose() {
        editor.af_onClose();
    }

    @Override
    public void onShutdown() {
        editor.af_onShutdown();
    }

    //
    //Properties

    @Override
    public boolean isDirty() {
        return editor.af_isDirty();
    }

    @Override
    public Position getDefaultPosition() {
        return CompassPosition.ROOT;
    }

    @Override
    public PlaceRequest getOwningPlace() {
        return null;
    }

    @Override
    public String getTitle() {
        return editor.getTitle();
    }

    @Override
    public IsWidget getTitleDecoration() {
        return null;
    }

    @Override
    public IsWidget getWidget() {
        return ElementWrapperWidget.getWidget(editor.getElement());
    }

    @Override
    public Menus getMenus() {
        return null; //FIXME: Implement
    }

    @Override
    public ToolBar getToolBar() {
        return null; //FIXME: Implement
    }

    @Override
    public String contextId() {
        return null;
    }

    @Override
    public PlaceRequest getPlace() {
        return placeRequest;
    }

    @Override
    public String getIdentifier() {
        return editor.getComponentId();
    }
}
