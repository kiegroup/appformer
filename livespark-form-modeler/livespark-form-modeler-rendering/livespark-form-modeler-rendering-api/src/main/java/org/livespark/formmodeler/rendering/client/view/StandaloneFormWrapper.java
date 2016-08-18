/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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


package org.livespark.formmodeler.rendering.client.view;

import static org.jboss.errai.common.client.dom.DOMUtil.removeAllElementChildren;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.TemplateWidgetMapper;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.livespark.flow.cdi.api.FlowInput;
import org.livespark.flow.cdi.api.FlowOutput;
import org.livespark.flow.cdi.api.FlowComponent;
import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.kie.workbench.common.forms.crud.client.component.formDisplay.IsFormView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Widget;

@FlowComponent
@Dependent
@Templated
public class StandaloneFormWrapper<MODEL, FORM_MODEL extends FormModel, FORM_VIEW extends FormView<MODEL, FORM_MODEL>> implements IsElement, IsFormView<FORM_MODEL> {
    /*
     * FIXME This should not be in this module, because it fails Errai IOC generation without the livespark-app-flow-client module.
     */

    private FORM_VIEW formView;

    @Inject
    @DataField
    private Div container;

    @Inject
    @DataField
    private Button accept;

    @Inject
    @DataField
    private Button cancel;

    @Inject
    private FlowInput<FORM_MODEL> input;

    @Inject
    private FlowOutput<Optional<FORM_MODEL>> output;

    public void init() {
        setModel( input.get() );
        formView.pauseBinding();
    }

    public void setFormView( final FORM_VIEW formView ) {
        this.formView = formView;
        removeAllElementChildren( container );
        container.appendChild( formView.getElement() );
    }

    @EventHandler("accept")
    public void onAccept( final ClickEvent event ) {
        if ( formView.isValid() ) {
            formView.resumeBinding( true );
            output.submit( Optional.of( getModel() ) );
        }
    }

    @EventHandler("cancel")
    public void onCancel( final ClickEvent event ) {
        formView.resumeBinding( false );
        output.submit( Optional.empty() );
    }

    @Override
    public void setModel( final FORM_MODEL model ) {
        formView.setModel( model );
    }

    public FORM_VIEW getFormView() {
        return formView;
    }

    @Override
    public FORM_MODEL getModel() {
        return formView.getModel();
    }

    @Override
    public boolean isValid() {
        return formView.isValid();
    }

    @Override
    public Widget asWidget() {
        return TemplateWidgetMapper.get( this );
    }

}
