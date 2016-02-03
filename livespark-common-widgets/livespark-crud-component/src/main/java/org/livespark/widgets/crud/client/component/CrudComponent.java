/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.livespark.widgets.crud.client.component;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.livespark.widgets.crud.client.component.formDisplay.FormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.livespark.widgets.crud.client.component.formDisplay.embedded.EmbeddedFormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.modal.ModalFormDisplayer;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

@Dependent
public class CrudComponent implements IsWidget{

    public interface CrudComponentView extends IsWidget{
        void setPresenter( CrudComponent presenter );

        void init ( CrudMetaDefinition definition );

        int getCurrentPage();

        void showCreateForm();

        void showEditionForm( int index );

        void renderNestedForm( String title, IsFormView formView, FormDisplayer.FormDisplayerCallback callback );

        void deleteInstance( int index );

        void doCreate();

        void doEdit();

        void doCancel();

        void restoreTable();
    }

    private CrudComponentView view;

    private EmbeddedFormDisplayer embeddedFormDisplayer;

    private ModalFormDisplayer modalFormDisplayer;

    protected boolean embedded = true;

    private CrudActionsHelper helper;

    @Inject
    public CrudComponent( CrudComponentView view,
                          EmbeddedFormDisplayer embeddedFormDisplayer,
                          ModalFormDisplayer modalFormDisplayer ) {
        this.view = view;
        this.embeddedFormDisplayer = embeddedFormDisplayer;
        this.modalFormDisplayer = modalFormDisplayer;
        view.setPresenter( this );
    }

    public void init( final CrudActionsHelper helper ) {
        this.helper = helper;
        view.init( new CrudMetaDefinition() );
    }

    public FormDisplayer getFormDisplayer() {
        if ( helper.showEmbeddedForms() ) {
            return embeddedFormDisplayer;
        }
        return modalFormDisplayer;
    }

    public IsFormView getCreateForm() {
        return helper.getCreateInstanceForm();
    }

    public IsFormView getEditForm( int index ) {
        return helper.getEditInstanceForm( index );
    }

    public void createInstance() {
        helper.createInstance();
    }

    public void editInstance() {
        helper.editInstance();
    }

    public void deleteInstance( int index ) {
        helper.deleteInstance( index );
    }

    public int getCurrentPage() {
        return view.getCurrentPage();
    }

    public void refresh() {
        HasData next = (HasData) helper.getDataProvider().getDataDisplays().iterator().next();
        next.setVisibleRangeAndClearData( next.getVisibleRange(), true );
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded( boolean embedded ) {
        this.embedded = embedded;
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public class CrudMetaDefinition {
        public boolean isAllowCreate() {
            return helper.isAllowCreate();
        }

        public boolean isAllowDelete() {
            return helper.isAllowDelete();
        }

        public boolean isAllowEdit() {
            return helper.isAllowEdit();
        }

        public int getPageSize() {
            return helper.getPageSize();
        }

        public List<ColumnMeta<?>> getGridColumns() {
            return helper.getGridColumns();
        }

        public AsyncDataProvider getDataProvider() {
            return helper.getDataProvider();
        }
    }
}
