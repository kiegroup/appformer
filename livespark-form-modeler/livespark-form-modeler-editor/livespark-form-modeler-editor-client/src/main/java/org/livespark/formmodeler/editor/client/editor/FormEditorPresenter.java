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

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Controls;
import com.github.gwtbootstrap.client.ui.FormLabel;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;
import org.guvnor.common.services.shared.metadata.MetadataService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.widgets.metadata.client.KieEditor;
import org.kie.workbench.common.widgets.metadata.client.KieEditorView;
import org.livespark.formmodeler.editor.client.resources.i18n.Constants;
import org.livespark.formmodeler.editor.client.type.FormDefinitionResourceType;
import org.livespark.formmodeler.editor.model.FieldDefinition;
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
import org.uberfire.ext.editor.commons.client.history.VersionRecordManager;
import org.uberfire.ext.layout.editor.client.LayoutEditorPluginAPI;
import org.uberfire.ext.layout.editor.client.structure.EditorWidget;
import org.uberfire.ext.layout.editor.client.util.LayoutDragComponent;
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
        public void setupLayoutEditor(LayoutEditorPluginAPI layoutEditorPluginAPI);
    }

    @Inject
    private LayoutEditorPluginAPI layoutEditorPluginAPI;

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

    private FormEditorView view;

    private FormModelerContent content;

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

        this.content = content;
        this.layoutEditorPluginAPI.init( PluginType.PERSPECTIVE_LAYOUT, content.getDefinition().getName(), getLayoutComponents() );
        layoutEditorPluginAPI.load( PluginType.EDITOR, versionRecordManager.getCurrentPath(), new ParameterizedCommand<LayoutEditorModel>() {
            @Override
            public void execute( LayoutEditorModel layoutEditorModel ) {
                setOriginalHash( layoutEditorPluginAPI.getCurrentModelHash() );
            }
        }  );
        resetEditorPages( content.getOverview() );

        view.setupLayoutEditor( layoutEditorPluginAPI );
        view.loadContent( content.getDefinition() );
    }

    protected LayoutDragComponent[] getLayoutComponents() {
        final TextBoxFieldDefinition textBox = new TextBoxFieldDefinition();
        textBox.setName( "person_name" );
        textBox.setLabel( "Name" );
        textBox.setModelName( "person" );
        textBox.setBoundPropertyName( "name" );
        final DateBoxFieldDefinition birthDay = new DateBoxFieldDefinition();
        birthDay.setName( "person_birthday" );
        birthDay.setLabel( "Birthday" );
        birthDay.setModelName( "person" );
        birthDay.setBoundPropertyName( "birthday" );
        LayoutDragComponent[] list = new LayoutDragComponent[] {
                htmlLayoutDragComponent,
                new LayoutDragComponent() {
                    private FieldDefinition field = textBox;
                    @Override
                    public String label() {
                        return textBox.getBindingExpression();
                    }

                    @Override
                    public Widget getDragWidget() {
                        return new Label(field.getBindingExpression());
                    }

                    @Override
                    public IsWidget getComponentPreview() {
                        ControlGroup group = new ControlGroup(  );
                        Controls controls = new Controls();
                        FormLabel label = new FormLabel( field.getLabel() );
                        TextBox box = new TextBox();
                        label.setFor( box.getId() );
                        controls.add( label );
                        controls.add( box );
                        group.add( controls );
                        group.add( new HelpBlock(  ) );
                        return group;
                    }

                    @Override
                    public boolean hasConfigureModal() {
                        return false;
                    }

                    @Override
                    public Modal getConfigureModal( EditorWidget editorWidget ) {
                        return new Modal(  );
                    }
                },
                new LayoutDragComponent() {
                    private FieldDefinition field = birthDay;
                    @Override
                    public String label() {
                        return textBox.getBindingExpression();
                    }

                    @Override
                    public Widget getDragWidget() {
                        return new Label(field.getBindingExpression());
                    }

                    @Override
                    public IsWidget getComponentPreview() {
                        ControlGroup group = new ControlGroup(  );
                        Controls controls = new Controls();
                        FormLabel label = new FormLabel( field.getLabel() );
                        DatePicker box = new DatePicker();
                        label.setFor( box.getElement().getId() );
                        controls.add( label );
                        controls.add( box );
                        group.add( controls );
                        group.add( new HelpBlock(  ) );
                        return group;
                    }

                    @Override
                    public boolean hasConfigureModal() {
                        return false;
                    }

                    @Override
                    public Modal getConfigureModal( EditorWidget editorWidget ) {
                        return new Modal();
                    }
                }
        };

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
