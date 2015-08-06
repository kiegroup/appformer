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
package org.livespark.formmodeler.editor.client.editor;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.guvnor.common.services.shared.metadata.MetadataService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.widgets.metadata.client.KieEditor;
import org.kie.workbench.common.widgets.metadata.client.KieEditorView;
import org.livespark.formmodeler.editor.client.editor.fields.DateBoxLayoutComponent;
import org.livespark.formmodeler.editor.client.editor.fields.TextBoxLayoutComponent;
import org.livespark.formmodeler.editor.client.resources.i18n.Constants;
import org.livespark.formmodeler.editor.client.type.FormDefinitionResourceType;
import org.livespark.formmodeler.editor.model.FormDefinition;
import org.livespark.formmodeler.editor.model.FormModelerContent;
import org.livespark.formmodeler.editor.model.impl.basic.DateBoxFieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.TextBoxFieldDefinition;
import org.livespark.formmodeler.editor.service.FormEditorService;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.ext.layout.editor.client.LayoutEditorPlugin;
import org.uberfire.ext.layout.editor.client.components.LayoutDragComponent;
import org.uberfire.ext.plugin.client.perspective.editor.layout.editor.HTMLLayoutDragComponent;
import org.uberfire.ext.plugin.model.LayoutEditorModel;
import org.uberfire.ext.plugin.model.PluginType;
import org.uberfire.ext.widgets.common.client.common.BusyIndicatorView;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.type.FileNameUtil;

/**
 * Created by pefernan on 7/7/15.
 */
@Dependent
@WorkbenchEditor(identifier = "LSFormEditor", supportedTypes = { FormDefinitionResourceType.class })
public class FormEditorPresenter extends KieEditor {

    public interface FormEditorView extends KieEditorView {

        void initDataHoldersPopup( List<String> availableDataHolders );

        public void setPresenter(FormEditorPresenter presenter);
        public void loadContent(FormDefinition definition);
        public void setupLayoutEditor(LayoutEditorPlugin layoutEditorPluginAPI);
    }

    @Inject
    private LayoutEditorPlugin layoutEditor;

    @Inject
    private HTMLLayoutDragComponent htmlLayoutDragComponent;

    @Inject
    private Caller<FormEditorService> editorService;

    @Inject
    private Caller<MetadataService> metadataService;

    @Inject
    private FormDefinitionResourceType resourceType;

    @Inject
    private BusyIndicatorView busyIndicatorView;

    @Inject
    private FormEditorHelper editorContext;

    private FormEditorView view;


    @Inject
    public FormEditorPresenter( FormEditorView baseView ) {
        super( baseView );
        view = baseView;
    }

    @OnStartup
    public void onStartup( final ObservablePath path,
            final PlaceRequest place ) {

        init( path, place, resourceType );
        view.setPresenter( this );
    }

    @Override
    protected void loadContent() {
        editorService.call( new RemoteCallback<FormModelerContent>() {
            @Override
            public void callback( FormModelerContent content ) {
                doLoadContent( content );
            }
        }, getNoSuchFileExceptionErrorCallback() ).loadContent( versionRecordManager.getCurrentPath() );
    }

    public void doLoadContent( FormModelerContent content ) {
        busyIndicatorView.hideBusyIndicator();

        editorContext.setContent( content );
        this.layoutEditor.init( content.getDefinition().getName(), getLayoutComponents() );
        layoutEditor.load( PluginType.EDITOR, versionRecordManager.getCurrentPath(), new ParameterizedCommand<LayoutEditorModel>() {
            @Override
            public void execute( LayoutEditorModel layoutEditorModel ) {
                //setOriginalHash( layoutEditor.get() );
            }
        } );

        resetEditorPages( content.getOverview() );

        view.setupLayoutEditor( layoutEditor );
        view.loadContent( content.getDefinition() );
    }

    protected List<LayoutDragComponent> getLayoutComponents() {

        final DateBoxFieldDefinition birthDay = new DateBoxFieldDefinition();
        birthDay.setName( "person_birthday" );
        birthDay.setLabel( "Birthday" );
        birthDay.setModelName( "person" );
        birthDay.setBoundPropertyName( "birthday" );
        final TextBoxFieldDefinition textBox = new TextBoxFieldDefinition();
        textBox.setName( "person_name" );
        textBox.setLabel( "Name" );
        textBox.setModelName( "person" );
        textBox.setBoundPropertyName( "name" );
        List<LayoutDragComponent>  list = new ArrayList<LayoutDragComponent>();
        list.add( htmlLayoutDragComponent );

        list.add( new TextBoxLayoutComponent( editorContext.getContent().getPath().toURI(), textBox ));
        list.add( new DateBoxLayoutComponent( editorContext.getContent().getPath().toURI(), birthDay ));

        editorContext.addAvailableField( textBox );
        editorContext.addAvailableField( birthDay );

        return list;
    }

    @WorkbenchPartTitle
    public String getTitleText() {
        String fileName = FileNameUtil.removeExtension( versionRecordManager.getCurrentPath(), resourceType );
        return Constants.INSTANCE.form_modeler_title( fileName );
    }

    @WorkbenchMenu
    public Menus getMenus() {
        if ( menus == null ) {
            makeMenuBar();
        }
        return menus;
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        return super.getWidget();
    }

    protected void makeMenuBar() {
        menus = menuBuilder
                .addSave(versionRecordManager.newSaveMenuItem(new Command() {
                    @Override
                    public void execute() {
                        onSave();
                    }
                }))
                .addCopy(versionRecordManager.getCurrentPath(),
                        fileNameValidator)
                .addRename(versionRecordManager.getPathToLatest(),
                        fileNameValidator)
                .addDelete(versionRecordManager.getPathToLatest())
                .addNewTopLevelMenu(versionRecordManager.buildMenu())
                .build();
    }

    public void getAvailableDataObjectsList() {
        editorService.call( new RemoteCallback<List<String>>() {
            @Override
            public void callback( List<String> availableDataObjects ) {
                view.initDataHoldersPopup( availableDataObjects );
            }
        } ).getAvailableDataObjects( versionRecordManager.getCurrentPath() );
    }
}
