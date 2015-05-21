package org.livespark.formmodeler.rendering.client.view;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;

import com.google.gwt.user.client.ui.Composite;


public abstract class ListItemView<M> extends Composite implements HasModel<M> {

    @Inject
    @AutoBound
    protected DataBinder<M> binder;

    protected List<String> inputNames = new ArrayList<String>(  );

    @Override
    public M getModel() {
        return binder.getModel();
    }

    @Override
    public void setModel( M model ) {
        binder.setModel( model );
    }

    protected abstract void initInputNames();

}
