package org.livespark.formmodeler.rendering.client.view;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.livespark.formmodeler.rendering.client.view.ListView.DeleteExecutor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;

public abstract class ListItemView<M extends FormModel> extends BaseView<M> {

    @Inject
    @DataField
    protected Button delete;

    @Inject
    protected DeleteExecutor<M> deleter;

    @EventHandler("delete")
    protected void onClick( ClickEvent e ) {
        deleter.execute( getModel() );
    }
}
