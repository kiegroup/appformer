package org.livespark.formmodeler.rendering.client.view;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.livespark.formmodeler.rendering.client.shared.FormModel;

import com.google.gwt.user.client.ui.Composite;

public abstract class ListView<M extends FormModel, W extends ListItemView<M>> extends Composite {

    @Inject
    @DataField
    protected ListWidget<M, W> items;

    protected final DeleteExecutor<M> deleteCommand = new DeleteExecutor<M>() {

        @Override
        public void execute( M model ) {
            delete(model);
        }
    };

    @PostConstruct
    protected void init() {
        loadData( new RemoteCallback<List<M>>() {

            @Override
            public void callback( List<M> response ) {
                items.setItems( response );
            }
        } );
    }

    protected abstract void loadData( RemoteCallback<List<M>> callback );

    protected abstract void remoteDelete( M model,
                                          RemoteCallback<Boolean> callback );

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

    public interface DeleteExecutor<T> {
        public void execute(T model);
    }

}
