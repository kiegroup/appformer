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

package org.uberfire.client.experimental.editor;

import java.util.function.Supplier;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.experimental.definition.annotations.ExperimentalFeature;
import org.uberfire.ext.editor.commons.client.BaseEditor;
import org.uberfire.ext.editor.commons.file.DefaultMetadata;
import org.uberfire.ext.editor.commons.service.support.SupportsDelete;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.shared.experimental.ExperimentalAssetRemoved;
import org.uberfire.shared.experimental.ExperimentalEditorService;
import org.uberfire.workbench.model.menu.Menus;

import static org.uberfire.ext.editor.commons.client.menu.MenuItems.DELETE;
import static org.uberfire.ext.editor.commons.client.menu.MenuItems.SAVE;

@Dependent
@WorkbenchEditor(identifier = "ExperimentalAsset", supportedTypes = ExperimentalAssetResourceType.class)
@ExperimentalFeature(nameI18nKey = "experimental_asset_editor", descriptionI18nKey = "experimental_asset_editor_description")
public class ExperimentalAssetEditor extends BaseEditor<String, DefaultMetadata> {

    private ExperimentalAssetResourceType resourceType;
    private AssetEditor editor;
    private Caller<ExperimentalEditorService> service;

    @Inject
    public ExperimentalAssetEditor(ExperimentalAssetResourceType resourceType, AssetEditor editor, Caller<ExperimentalEditorService> service) {
        super(editor.getView());
        this.resourceType = resourceType;
        this.editor = editor;
        this.service = service;
    }

    @Override
    protected void loadContent() {
        service.call((RemoteCallback<String>) response -> {
            editor.showContent(response);
        }).load(versionRecordManager.getCurrentPath());
        baseView.hideBusyIndicator();
    }

    @OnStartup
    public void onStartup(final ObservablePath path,
                          final PlaceRequest place) {
        init(path,
             place,
             resourceType,
             SAVE,
             DELETE);
    }

    @Override
    protected Supplier<String> getContentSupplier() {
        return editor::getContent;
    }

    @Override
    protected Caller<? extends SupportsDelete> getDeleteServiceCaller() {
        return service;
    }

    @Override
    protected void save() {
        final String content = editor.getContent();
        service.call(getSaveSuccessCallback(content.hashCode())).save(versionRecordManager.getCurrentPath(), content);
        concurrentUpdateSessionInfo = null;
    }

    @Override
    @WorkbenchPartTitleDecoration
    public IsWidget getTitle() {
        return super.getTitle();
    }

    @Override
    @WorkbenchPartTitle
    public String getTitleText() {
        return "Experimental Editor [" + versionRecordManager.getCurrentPath().getFileName() + "]";
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return editor.getView();
    }

    public void onDelete(@Observes ExperimentalAssetRemoved event) {
        if (event.getPath().equals(versionRecordManager.getCurrentPath())) {
            placeManager.closePlace(place);
        }
    }
}
