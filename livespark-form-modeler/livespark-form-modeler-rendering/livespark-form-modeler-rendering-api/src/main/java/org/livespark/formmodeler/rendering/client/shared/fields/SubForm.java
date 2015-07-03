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
package org.livespark.formmodeler.rendering.client.shared.fields;

import com.github.gwtbootstrap.client.ui.Well;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.livespark.formmodeler.rendering.client.view.FormView;

/**
 * Created by pefernan on 6/18/15.
 */
public class SubForm<M, F extends FormModel> extends Well implements HasValue<M> {

    private SubFormModelAdapter<M, F> subFormModelAdapter;
    private FormView<F> formView;
    private M model;

    public SubForm( SubFormModelAdapter<M, F> adapter ) {
        super();
        if (adapter == null) throw new IllegalArgumentException( "FormModelProvider cannot be null" );
        subFormModelAdapter = adapter;
    }


    public M getValue() {
        return model;
    }

    public void setValue( M model ) {
        doSetValue( model );
    }

    public void setValue( M model, boolean b ) {
        doSetValue( model );
    }

    public void setModel( M model ) {
        this.model = model;
    }

    public void setReadOnly(boolean readonly) {
        if (formView != null) formView.setReadOnly( readonly );
    }

    protected void doSetValue(M model) {
        this.model = model;
        if (formView == null) {
            initFormView();
        }
        formView.setModel( subFormModelAdapter.getFormModelForModel( model ) );
    }

    protected void initFormView() {
        IOCBeanDef<? extends FormView<F>> beanDef = IOC.getBeanManager().lookupBean( subFormModelAdapter.getFormViewType() );
        this.formView = beanDef.getInstance();
        this.add( formView );
    }



    public HandlerRegistration addValueChangeHandler( ValueChangeHandler<M> valueChangeHandler ) {
        return this.addHandler(valueChangeHandler, ValueChangeEvent.getType());
    }
}