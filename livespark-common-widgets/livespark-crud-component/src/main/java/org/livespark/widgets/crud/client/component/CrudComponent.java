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

import static org.livespark.widgets.crud.client.resources.i18n.CrudComponentConstants.CrudComponentViewImplEditInstanceTitle;
import static org.livespark.widgets.crud.client.resources.i18n.CrudComponentConstants.CrudComponentViewImplNewInstanceTitle;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.livespark.widgets.crud.client.component.formDisplay.FormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.livespark.widgets.crud.client.component.formDisplay.embedded.EmbeddedFormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.modal.ModalFormDisplayer;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

@Dependent
public class CrudComponent implements IsWidget{

    public interface CrudComponentView extends IsWidget{
        void setPresenter( CrudComponent presenter );

        int getCurrentPage();

        void addDisplayer( FormDisplayer displayer );

        void removeDisplayer( FormDisplayer displayer );

        void initTableView( List<ColumnMeta> dataColumns, int pageSize );

        void showCreateButton();

        void setDataProvider( final AsyncDataProvider dataProvider );

        void showDeleteButtons();

        void showEditButtons();
    }

    private final CrudComponentView view;

    private final EmbeddedFormDisplayer embeddedFormDisplayer;

    private final ModalFormDisplayer modalFormDisplayer;

    protected boolean embedded = true;

    protected CrudActionsHelper helper;

    private final TranslationService translationService;

    @Inject
    public CrudComponent( final CrudComponentView view,
                          final EmbeddedFormDisplayer embeddedFormDisplayer,
                          final ModalFormDisplayer modalFormDisplayer,
                          final TranslationService translationService ) {
        this.view = view;
        this.embeddedFormDisplayer = embeddedFormDisplayer;
        this.modalFormDisplayer = modalFormDisplayer;
        this.translationService = translationService;
        view.setPresenter( this );
    }

    public void init( final CrudActionsHelper helper ) {
        this.helper = helper;
        view.initTableView( helper.getGridColumns(), helper.getPageSize() );
        if ( helper.isAllowCreate() ) {
            view.showCreateButton();
        }
        if ( helper.isAllowEdit() ) {
            view.showEditButtons();
        }
        if ( helper.isAllowDelete() ) {
            view.showDeleteButtons();
        }

        view.setDataProvider( helper.getDataProvider() );
        refresh();
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

    public IsFormView getEditForm( final int index ) {
        return helper.getEditInstanceForm( index );
    }

    public void createInstance() {
        helper.createInstance();
    }

    public void editInstance() {
        helper.editInstance();
    }

    public void deleteInstance( final int index ) {
        helper.deleteInstance( index );
    }

    public int getCurrentPage() {
        return view.getCurrentPage();
    }

    public void refresh() {
        final HasData next = (HasData) helper.getDataProvider().getDataDisplays().iterator().next();
        next.setVisibleRangeAndClearData( next.getVisibleRange(), true );
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded( final boolean embedded ) {
        this.embedded = embedded;
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void renderNestedForm( final String title, final IsFormView formView, final FormDisplayer.FormDisplayerCallback callback ) {
        final FormDisplayer displayer = getFormDisplayer();

        if ( displayer.isEmbeddable() ) {
            view.addDisplayer( displayer );
        }

        displayer.display( title, formView, callback );
    }

    public void restoreTable() {
        final FormDisplayer displayer = getFormDisplayer();
        if ( displayer.isEmbeddable() ) {
            view.removeDisplayer( displayer );
        }
    }

    public void doCreate() {
        restoreTable();
        createInstance();
    }

    public void doEdit() {
        restoreTable();
        editInstance();
    }

    public void doCancel() {
        restoreTable();
    }

    public void showCreateForm() {
        renderNestedForm( translationService.getTranslation( CrudComponentViewImplNewInstanceTitle ),
                          getCreateForm(),
                          new FormDisplayer.FormDisplayerCallback() {
                              @Override
                              public void onAccept() {
                                  doCreate();
                              }

                              @Override
                              public void onCancel() {
                                  doCancel();
                              }
                          } );
    }

    public void showEditForm( final int index ) {
        renderNestedForm( translationService.getTranslation( CrudComponentViewImplEditInstanceTitle ),
                          getEditForm( index ),
                          new FormDisplayer.FormDisplayerCallback() {
                              @Override
                              public void onAccept() {
                                  doEdit();
                              }

                              @Override
                              public void onCancel() {
                                  doCancel();
                              }
                          } );
    }
}
