package org.livespark.formmodeler.rendering.client.view;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.livespark.formmodeler.rendering.client.shared.FormModel;

import com.google.gwt.user.client.ui.Composite;

public abstract class BaseView<M extends FormModel> extends Composite implements HasModel<M> {

    protected List<String> inputNames = new ArrayList<String>();

    @Inject
    @AutoBound
    protected DataBinder<M> binder;

    @Override
    public M getModel() {
        return binder.getModel();
    }

    @PostConstruct
    protected void initFormView() {
        initInputNames();
    }

    @Override
    public void setModel( M model ) {
        binder.setModel( model );
    }

    public List<String> getInputNames() {
        return inputNames;
    }

    public abstract void setReadOnly( boolean readOnly );

    protected abstract void initInputNames();

}
