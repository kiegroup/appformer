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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.widgets.metadata.client.KieEditorViewImpl;
import org.livespark.formmodeler.editor.client.resources.i18n.FormEditorConstants;
import org.livespark.formmodeler.renderer.client.DynamicFormRenderer;
import org.uberfire.ext.layout.editor.client.LayoutEditor;

@Dependent
@Templated
public class FormEditorViewImpl extends KieEditorViewImpl implements FormEditorPresenter.FormEditorView, RequiresResize {

    @DataField
    private Element container = DOM.createDiv();

    @Inject
    @DataField
    private Button createHolder;

    @Inject
    @DataField
    private FlowPanel editorContent;

    @Inject
    @DataField
    private FlowPanel previewContent;

    @Inject
    @DataField
    private Anchor previewTab;

    @Inject
    private DynamicFormRenderer formRenderer;

    private TranslationService translationService;

    private FormEditorPresenter presenter;

    @Inject
    public FormEditorViewImpl( TranslationService translationService ) {
        this.translationService = translationService;
    }

    @PostConstruct
    protected void initView() {
        previewContent.clear();
        previewContent.add( formRenderer );
    }

    @Override
    public void init( FormEditorPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setupLayoutEditor( LayoutEditor layoutEditor ) {
        editorContent.clear();
        editorContent.add( layoutEditor.asWidget() );
    }

    @EventHandler( "createHolder" )
    void handleClick( ClickEvent event ) {
        presenter.initDataObjectsTab();
    }

    @EventHandler( "previewTab" )
    public void onPreview( ClickEvent event ) {
        formRenderer.render( presenter.getRenderingContext() );
    }

    @Override
    public void onResize() {
        if ( getParent() == null ) {
            return;
        }
        int height = getParent().getOffsetHeight();
        int width = getParent().getOffsetWidth();

        container.getStyle().setWidth( width, Style.Unit.PX );
        container.getStyle().setHeight( height, Style.Unit.PX );
    }
}
