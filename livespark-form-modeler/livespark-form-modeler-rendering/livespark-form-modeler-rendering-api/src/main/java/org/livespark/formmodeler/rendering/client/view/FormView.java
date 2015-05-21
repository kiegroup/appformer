package org.livespark.formmodeler.rendering.client.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.livespark.formmodeler.rendering.client.shared.FormModel;

/**
 * Created by pefernan on 4/17/15.
 */
public abstract class FormView<M extends FormModel> extends Composite implements HasModel<M> {

    protected List<String> inputNames = new ArrayList<String>(  );

    @Inject
    protected Validator validator;

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
        clearFieldErrors();
    }

    public List<String> getInputNames() {
        return inputNames;
    }

    public abstract void setReadOnly( boolean readOnly );

    protected abstract void initInputNames();

    protected void clearFieldErrors() {
        for (String field : getInputNames()) {
            Element group = Document.get().getElementById( field + "_form_group" );
            Element helpBlock = Document.get().getElementById( field + "_help_block" );
            if ( group != null ) group.removeClassName( "error" );
            if ( helpBlock != null ) helpBlock.setInnerHTML( "" );
        }
    }

    public boolean validate() {

        boolean isValid = true;

        clearFieldErrors();

        Set<ConstraintViolation<M>> result = validator.validate( binder.getModel() );
        for (ConstraintViolation<M> validation : result) {
            String property = validation.getPropertyPath().toString().replace( ".", "_" );
            if (!getInputNames().contains( property )) continue;
            isValid = false;
            Element group = Document.get().getElementById( property + "_form_group" );
            Element helpBlock = Document.get().getElementById( property + "_help_block" );
            if ( group != null ) group.addClassName( "error" );
            if ( helpBlock != null ) helpBlock.setInnerHTML( validation.getMessage() );
        }
        return isValid;
    }
}
