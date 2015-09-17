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

import org.gwtbootstrap3.client.ui.Well;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ui.client.widget.HasModel;
import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.livespark.formmodeler.rendering.client.view.FormView;


/**
 * Created by pefernan on 6/18/15.
 */
public class SubForm<M, F extends FormModel> extends Well implements HasModel<M> {

    private SubFormModelAdapter<M, F> subFormModelAdapter;
    private FormView<F> formView;
    private M model;

    public SubForm( SubFormModelAdapter<M, F> adapter ) {
        super();
        if (adapter == null) throw new IllegalArgumentException( "FormModelProvider cannot be null" );
        subFormModelAdapter = adapter;
        initFormView();
    }

    public M getModel() {
        return model;
    }

    public void setModel( M model ) {
        this.model = model;
        formView.setModel( subFormModelAdapter.getFormModelForModel( model ) );
    }

    public void setReadOnly(boolean readonly) {
        if (formView != null) formView.setReadOnly( readonly );
    }

    protected void initFormView() {
        IOCBeanDef<? extends FormView<F>> beanDef = IOC.getBeanManager().lookupBean( subFormModelAdapter.getFormViewType() );
        this.formView = beanDef.getInstance();
        this.add( formView );
    }

}
