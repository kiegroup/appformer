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

package org.livespark.formmodeler.rendering.client.view;

import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.client.widget.Table;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.client.view.util.ListViewActionsHelper;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;

public abstract class ListView<M extends FormModel, W extends ListItemView<M>> extends Composite {

    @Inject
    @DataField
    protected Button create;

    @Inject
    @DataField
    @Table(root = "tbody")
    protected ListWidget<M, W> items;

    @Inject
    protected SyncBeanManager beanManager;

    protected FormViewModal modal;

    protected ListViewActionsHelper<M> helper = new ListViewActionsHelper<M>() {
        @Override
        public void create( M model ) {
            org.jboss.errai.enterprise.client.jaxrs.api.RestClient.create(
                    getRemoteServiceClass(), new RemoteCallback<M>() {
                        @Override
                        public void callback( M response ) {
                            items.getValue().add( response );
                            items.getWidget( response ).setParentView( ListView.this );
                        }
                    } ).create( model );
        }

        @Override
        public void update( M model ) {
            org.jboss.errai.enterprise.client.jaxrs.api.RestClient.create(
                    getRemoteServiceClass(), new RemoteCallback<Boolean>() {
                        @Override
                        public void callback( Boolean response ) {
                        }
                    } ).update( model );
        }

        @Override
        public void delete( M model ) {
            ListView.this.delete( model );
        }
    };

    public void init() {
        loadData( new RemoteCallback<List<M>>() {

            @Override
            public void callback( List<M> response ) {
                loadItems( response );
            }
        } );
    }

    public void setActionsHelper( ListViewActionsHelper<M> helper ) {
        this.helper = helper;
    }

    protected abstract void loadData( RemoteCallback<List<M>> callback );

    protected abstract void remoteDelete( M model, RemoteCallback<Boolean> callback );

    public void loadItems(List<M> itemsToLoad) {
        items.setItems( itemsToLoad );
        syncListWidgets();
    }

    public void syncListWidgets() {
        for (M model : items.getValue()) {
            syncListWidget( model );
        }
    }

    public  void syncListWidget (M model) {
        ListItemView<M> widget = items.getWidget( model );
        widget.setParentView( this );
    }

    protected FormView<M> getForm() {
        IOCBeanDef<? extends FormView<M>> beanDef = beanManager.lookupBean( getFormType() );
        return beanDef.getInstance();
    }

    protected abstract Class<? extends FormView<M>> getFormType();

    public abstract String getListTitle();

    protected abstract String getFormTitle();

    protected abstract String getFormId();

    protected abstract Class<? extends LiveSparkRestService<M>> getRemoteServiceClass();

    public void delete( final M model ) {
        remoteDelete( model,
                new RemoteCallback<Boolean>() {

                    @Override
                    public void callback( Boolean response ) {
                        if ( response ) {
                            items.getValue().remove( model );
                        }
                    }
                } );
    }

    @EventHandler( "create" )
    public void onCreateClick( ClickEvent event ) {
        final FormView<M> form = getForm();

        modal = new FormViewModal( form, getFormTitle(), getFormId() );
        modal.addSubmitClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent clickEvent ) {
                if (form.validate()) {
                    doCreate( form.getModel() );
                }
            }
        } );
        modal.addCancelClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent clickEvent ) {
                modal.hide();
            }
        } );
        modal.show();
    }

    protected void doCreate(M model) {
        if (modal != null) modal.hide();
        helper.create( model );
    }

    protected void doUpdate(M model) {
        if (modal != null) modal.hide();
        helper.update( model );
    }

    protected void doDelete(M model) {
        helper.delete( model );
    }

    public void onDelete( M model ) {
        doDelete( model );
    }

    public void onEdit( M model ) {
        final FormView<M> form = getForm();
        form.setModel( model );

        modal = new FormViewModal( form, getFormTitle(), getFormId() );
        modal.addSubmitClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent clickEvent ) {
                if (form.validate()) {
                    doUpdate( form.getModel() );
                }
            }
        } );
        modal.addCancelClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent clickEvent ) {
                modal.hide();
            }
        } );
        modal.show();
    }
}
