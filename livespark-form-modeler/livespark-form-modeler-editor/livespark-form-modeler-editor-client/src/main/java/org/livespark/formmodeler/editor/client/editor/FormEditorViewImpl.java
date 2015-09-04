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

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Tab;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.widgets.metadata.client.KieEditorViewImpl;
import org.livespark.formmodeler.editor.client.editor.dataHolder.DataHolderModal;
import org.livespark.formmodeler.editor.client.editor.dataHolder.DataHolderPanel;
import org.livespark.formmodeler.editor.model.FormDefinition;
import org.uberfire.ext.layout.editor.client.LayoutEditor;
import org.uberfire.ext.layout.editor.client.generator.LayoutGenerator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by pefernan on 7/7/15.
 */
public class FormEditorViewImpl extends KieEditorViewImpl implements FormEditorPresenter.FormEditorView {

    interface FormEditorViewBinder
            extends
            UiBinder<Widget, FormEditorViewImpl> {

    }

    private static FormEditorViewBinder uiBinder = GWT.create(FormEditorViewBinder.class);

    @UiField
    Button createHolder;

    @UiField
    FlowPanel editorContent;

    @UiField
    FlowPanel previewContent;

    @UiField
    Tab previewTab;

    @Inject
    private DataHolderPanel dataHolderPanel;

    @Inject
    private LayoutGenerator layoutGenerator;

    private FormEditorPresenter presenter;

    public FormEditorViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @PostConstruct
    protected void init() {
    }

    @Override
    public void loadContent( FormDefinition definition ) {

    }

    @Override
    public void setupLayoutEditor( LayoutEditor layoutEditor ) {
        editorContent.clear();
        editorContent.add(layoutEditor.asWidget());
    }

    @UiHandler("createHolder")
    void handleClick(ClickEvent event) {
        presenter.getAvailableDataObjectsList();
    }

    @UiHandler("previewTab")
    public void onPreview( ClickEvent event ) {
        previewContent.clear();
        previewContent.add( layoutGenerator.build( presenter.getFormTemplate() ) );
    }

    @Override
    public void initDataHoldersPopup( List<String> availableDataHolders ) {
        dataHolderPanel.init( availableDataHolders );
        final DataHolderModal modal = new DataHolderModal( dataHolderPanel );
        modal.addSubmitClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent clickEvent ) {
                if (dataHolderPanel.validate()) {
                    modal.hide();
                    presenter.addDataHolder( dataHolderPanel.getDataHolderName(), dataHolderPanel.getDataHolderclass() );
                }
            }
        } );
    }

    @Override
    public void setPresenter( FormEditorPresenter presenter ) {
        this.presenter = presenter;
    }
}
