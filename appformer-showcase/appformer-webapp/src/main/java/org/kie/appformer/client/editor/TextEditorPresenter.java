package org.kie.appformer.client.editor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.editor.commons.client.menu.BasicFileMenuBuilder;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.PathPlaceRequest;
import org.uberfire.workbench.model.menu.Menus;

import com.google.gwt.user.client.ui.IsWidget;

@WorkbenchEditor(identifier = "EclipseTextEditor", supportedTypes = { FlowLangResource.class })
public class TextEditorPresenter {

    public interface View extends UberView<TextEditorPresenter> {
        IsWidget getTitleWidget();

        void setContent(String content);

        String getContent();

        boolean isDirty();

        void setDirty(boolean dirty);
    }

    protected Menus menus;

    @Inject
    protected BasicFileMenuBuilder menuBuilder;

    @Inject
    private View view;

    @Inject
    protected Caller<VFSService> vfsServices;

    @Inject
    private PlaceManager placeManager;

    private PathPlaceRequest place;
    private ObservablePath path;

    @WorkbenchPartTitle
    public String getScreenTitle() {
        return "Text Editor";
    }

    @PostConstruct
    public void setup() {
        view.init(this);
    }

    @AfterInitialization
    public void init() {
        buildMenus();
    }

    @OnStartup
    public void onStartup(final ObservablePath path, final PlaceRequest place) {
        this.path = path;
        this.place = (PathPlaceRequest) place;
    }

    @OnOpen
    public void onOpen() {
        load();
    }

    @OnMayClose
    public boolean onMayClose() {
        if (view.isDirty()) {
            return false;
        }
        return true;
    }

    @WorkbenchPartTitleDecoration
    public IsWidget getTitle() {
        return view.getTitleWidget();
    }

    @WorkbenchPartView
    public IsWidget asWidget() {
        return view;
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    protected void buildMenus() {
        menus = menuBuilder.addSave(new Command() {
            @Override
            public void execute() {
                onSave();
            }
        }).build();
    }

    public void onSave() {
        if (view.isDirty()) {
            save();
        }
    }

    public void onClose() {
        if (onMayClose()) {
            close();
        }
    }

    private void load() {
        vfsServices.call(new RemoteCallback<String>() {
            @Override
            public void callback(String response) {
                if (response == null)
                    response = "empty";
                view.setContent(response);
            }
        }).readAllString(path);
    }

    private void save() {
        final String content = view.getContent();
        vfsServices.call(new RemoteCallback<Path>() {
            @Override
            public void callback(final Path response) {
                view.setDirty(false);
            }
        }).write(path, content);
    }

    private void close() {
        placeManager.closePlace(place);
    }
}