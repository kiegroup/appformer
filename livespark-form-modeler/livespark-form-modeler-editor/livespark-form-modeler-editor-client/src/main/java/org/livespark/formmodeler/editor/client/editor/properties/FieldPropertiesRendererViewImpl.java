/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.livespark.formmodeler.editor.client.editor.properties;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.livespark.formmodeler.editor.client.resources.i18n.Constants;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.editor.service.FormEditorRenderingContext;
import org.livespark.formmodeler.renderer.client.DynamicFormRenderer;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;

@Dependent
public class FieldPropertiesRendererViewImpl extends BaseModal implements FieldPropertiesRenderer.FieldPropertiesRendererView {

    interface FieldPropertiesRendererViewImplBinder
            extends UiBinder<Widget, FieldPropertiesRendererViewImpl> {

    }

    private static FieldPropertiesRendererViewImplBinder uiBinder = GWT.create(FieldPropertiesRendererViewImplBinder.class);

    @UiField
    FlowPanel formContent;

    @UiField
    ListBox fieldType;

    @UiField
    ListBox fieldBinding;

    @Inject
    private DynamicFormRenderer formRenderer;

    private FieldPropertiesRenderer presenter;

    private FieldPropertiesRendererHelper helper;

    public FieldPropertiesRendererViewImpl() {
        this.setClosable( false );
        this.setTitle( FieldProperties.INSTANCE.title() );
        add( new ModalBody() {{
            add( uiBinder.createAndBindUi( FieldPropertiesRendererViewImpl.this ) );
        }} );
        final Button acceptButton = new Button( Constants.INSTANCE.accept());
        acceptButton.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                closeModal();
            }
        } );
        acceptButton.setType( ButtonType.PRIMARY );
        this.add( new ModalFooter() {{
            add( acceptButton );
        }} );
    }

    protected void closeModal() {
        if ( formRenderer.isValid() ) {
            helper.onClose();
            hide();
        }
    }

    @PostConstruct
    protected void init() {
        formContent.add( formRenderer );
    }

    @Override
    public void setPresenter( FieldPropertiesRenderer presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void render( FieldPropertiesRendererHelper helper, FormEditorRenderingContext renderingContext ) {
        this.helper = helper;
        formRenderer.render( renderingContext );
        initFieldTypeList();
        initFieldBindings();
        show();
    }

    protected void initFieldTypeList() {
        fieldType.clear();
        List<String> types = helper.getCompatibleFieldTypes();
        for ( int i = 0; i < types.size(); i++ ) {
            String type = types.get( i );
            fieldType.addItem( type );
            if ( type.equals( helper.getCurrentField().getCode() )) {
                fieldType.setSelectedIndex( i );
            }
        }
    }

    @UiHandler( "fieldType" )
    public void onTypeChange( ChangeEvent event ) {
        helper.onFieldTypeChange( fieldType.getSelectedValue() );
    }

    @UiHandler( "fieldBinding" )
    public void onBindingChange( ChangeEvent event ) {
        helper.onFieldBindingChange( fieldBinding.getSelectedValue() );
        initFieldBindings();
    }

    protected void initFieldBindings() {
        fieldBinding.clear();
        List<String> fields = helper.getAvailableFields();
        for ( int i = 0; i < fields.size(); i++ ) {
            String field = fields.get( i );
            fieldBinding.addItem( field );
            if ( field.equals( helper.getCurrentField().getBindingExpression() )) {
                fieldBinding.setSelectedIndex( i );
            }
        }
    }
}
