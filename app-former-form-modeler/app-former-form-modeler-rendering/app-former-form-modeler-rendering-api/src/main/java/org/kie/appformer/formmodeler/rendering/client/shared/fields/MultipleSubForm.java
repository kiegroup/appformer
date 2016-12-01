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
package org.kie.appformer.formmodeler.rendering.client.shared.fields;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.client.widget.HasModel;
import org.kie.appformer.formmodeler.rendering.client.shared.FormModel;
import org.kie.appformer.formmodeler.rendering.client.view.FormView;
import org.kie.workbench.common.forms.crud.client.component.CrudActionsHelper;
import org.kie.workbench.common.forms.crud.client.component.CrudComponent;
import org.kie.workbench.common.forms.crud.client.component.formDisplay.FormDisplayer;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

public class MultipleSubForm<L extends List<M>, M, C extends FormModel, E extends FormModel> extends SimplePanel implements HasModel<L> {

    private CrudComponent<M, FormModel> crudComponent;

    private MultipleSubFormModelAdapter<L, M, C, E> multipleSubFormModelAdapter;

    private L model;

    private AsyncDataProvider<M> dataProvider;

    public MultipleSubForm( MultipleSubFormModelAdapter<L, M, C, E> adapter ) {
        super();
        if (adapter == null) throw new IllegalArgumentException( "FormModelProvider cannot be null" );

        // FIXME This bean is never destroyed
        crudComponent = IOC.getBeanManager().lookupBean( CrudComponent.class ).getInstance();

        add( crudComponent );

        multipleSubFormModelAdapter = adapter;
    }

    @Override
    public L getModel() {
        return model;
    }

    @Override
    public void setModel( L model ) {
        this.model = model;
        initView();
    }

    protected void initView() {
        dataProvider = new AsyncDataProvider<M>() {
            @Override
            protected void onRangeChanged( HasData<M> hasData ) {
                if ( model != null ) {
                    updateRowCount( model.size(), true );
                    updateRowData( 0, model );
                } else {
                    updateRowCount( 0, true );
                    updateRowData( 0, new ArrayList<M>() );
                }
            }
        };

        crudComponent.init( new CrudActionsHelper<M>() {

            @Override
            public boolean showEmbeddedForms() {
                return true;
            }

            @Override
            public int getPageSize() {
                return 5;
            }

            @Override
            public boolean isAllowCreate() {
                return true;
            }

            @Override
            public boolean isAllowEdit() {
                return true;
            }

            @Override
            public boolean isAllowDelete() {
                return true;
            }

            @Override
            public List<ColumnMeta<M>> getGridColumns() {
                return multipleSubFormModelAdapter.getCrudColumns();
            }

            @Override
            public AsyncDataProvider<M> getDataProvider() {
                return dataProvider;
            }

            @SuppressWarnings( "unchecked" )
            private FormView<M, FormModel> getCreateInstanceForm() {
                // FIXME This bean is never destroyed
                return (FormView<M, FormModel>) IOC.getBeanManager().lookupBean( multipleSubFormModelAdapter.getCreationForm() ).newInstance();
            }

            @Override
            public void createInstance() {
                FormView<M, FormModel> form = getCreateInstanceForm();
                crudComponent.displayForm( form, new FormDisplayer.FormDisplayerCallback() {

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onAccept() {
                        model.add( (M) form.getModel().getModel() );
                        crudComponent.refresh();
                    }
                } );
            }

            private FormView<M, FormModel> getEditInstanceForm( int index ) {
                // FIXME This bean is never destroyed
                @SuppressWarnings( "unchecked" )
                FormView<M, FormModel> form = (FormView<M, FormModel>) IOC.getBeanManager().lookupBean( multipleSubFormModelAdapter.getEditionForm() ).newInstance();

                M model = MultipleSubForm.this.model.get( index );
                form.setModel( multipleSubFormModelAdapter.getEditionFormModel( model ) );
                form.pauseBinding();

                return form;
            }

            @Override
            public void editInstance( int index ) {
                FormView<M, FormModel> form = getEditInstanceForm( index );
                crudComponent.displayForm( form, new FormDisplayer.FormDisplayerCallback() {

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onAccept() {
                        form.resumeBinding( true );
                        crudComponent.refresh();
                    }
                } );
            }

            @Override
            public void deleteInstance( int index ) {
                model.remove( index );
                crudComponent.refresh();
            }
        } );
    }

    public HandlerRegistration addValueChangeHandler( ValueChangeHandler<L> valueChangeHandler ) {
        return this.addHandler(valueChangeHandler, ValueChangeEvent.getType());
    }
}
