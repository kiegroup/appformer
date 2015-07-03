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

import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.livespark.formmodeler.rendering.client.view.ListItemView;
import org.livespark.formmodeler.rendering.client.view.ListView;
import org.livespark.formmodeler.rendering.client.view.util.ListViewActionsHelper;

/**
 * Created by pefernan on 6/18/15.
 */

public class MultipleSubForm<L extends List<M>, M, F extends FormModel> extends SimplePanel implements HasValue<L> {

    private MultipleSubFormModelAdapter<L, F> multipleSubFormModelAdapter;
    private ListView<F, ? extends ListItemView<F>> listView;
    private L model;

    public MultipleSubForm( MultipleSubFormModelAdapter<L, F> adapter ) {
        super();
        if (adapter == null) throw new IllegalArgumentException( "FormModelProvider cannot be null" );
        multipleSubFormModelAdapter = adapter;
    }


    public L getValue() {
        return model;
    }

    public void setValue( L model ) {
        doSetValue( model );
    }

    public void setValue( L model, boolean b ) {
        doSetValue( model );
    }

    public void setModel( L model ) {
        this.model = model;
    }

    protected void doSetValue(L model) {
        this.model = model;
        if (listView == null) {
            initView();
        }
        if (model != null) {
            listView.loadItems( multipleSubFormModelAdapter.getListModelsForModel( model ) );
        }
    }

    protected void initView() {
        IOCBeanDef<? extends ListView<F, ? extends ListItemView<F>>> beanDef = IOC.getBeanManager().lookupBean( multipleSubFormModelAdapter.getListViewType() );
        listView = beanDef.getInstance();
        add( listView );
        listView.setActionsHelper( new ListViewActionsHelper<F>() {
            @Override
            public void create( F formModel ) {
                model.add( ( M ) formModel.getDataModels().get( 0 ) );
                listView.syncListWidget( formModel );
            }

            @Override
            public void update( FormModel formModel ) {
            }

            @Override
            public void delete( FormModel formModel ) {
                model.remove( formModel.getDataModels().get( 0 ) );
            }
        } );
    }

    public HandlerRegistration addValueChangeHandler( ValueChangeHandler<L> valueChangeHandler ) {
        return this.addHandler(valueChangeHandler, ValueChangeEvent.getType());
    }
}