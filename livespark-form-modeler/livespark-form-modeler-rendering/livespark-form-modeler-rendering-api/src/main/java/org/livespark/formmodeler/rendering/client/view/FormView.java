package org.livespark.formmodeler.rendering.client.view;

import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.livespark.formmodeler.rendering.client.shared.FormModel;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;

/**
 * Created by pefernan on 4/17/15.
 */
public abstract class FormView<M extends FormModel> extends BaseView<M> {

    private class NoOpCallback implements RemoteCallback<M> {

        @Override
        public void callback( M response ) {
        }
    }

    private RemoteCallback<M> callback = new NoOpCallback();

    @Inject
    @DataField
    private Button submit;

    @Inject
    protected Validator validator;

    public void setCallback( RemoteCallback<M> callback ) {
        if ( callback == null ) {
            this.callback = new NoOpCallback();
        } else {
            this.callback = callback;
        }
    }

    @Override
    public void setModel( M model ) {
        super.setModel( model );
        clearFieldErrors();
    }

    @EventHandler( "submit" )
    private void onSubmit( ClickEvent event ) {
        M model = binder.getModel();
        submitNewModel( model, callback );
    }

    protected abstract void submitNewModel( M model, RemoteCallback<M> callback );

    protected void clearFieldErrors() {
        for ( String field : getInputNames() ) {
            Element group = Document.get().getElementById( field + "_form_group" );
            Element helpBlock = Document.get().getElementById( field + "_help_block" );
            if ( group != null )
                group.removeClassName( "error" );
            if ( helpBlock != null )
                helpBlock.setInnerHTML( "" );
        }
    }

    public abstract void setReadOnly( boolean readOnly );

    public boolean validate() {

        boolean isValid = true;

        clearFieldErrors();

        Set<ConstraintViolation<M>> result = validator.validate( binder.getModel() );
        for ( ConstraintViolation<M> validation : result ) {
            String property = validation.getPropertyPath().toString().replace( ".",
                                                                               "_" );
            if ( !getInputNames().contains( property ) )
                continue;
            isValid = false;
            Element group = Document.get().getElementById( property + "_form_group" );
            Element helpBlock = Document.get().getElementById( property + "_help_block" );
            if ( group != null )
                group.addClassName( "error" );
            if ( helpBlock != null )
                helpBlock.setInnerHTML( validation.getMessage() );
        }
        return isValid;
    }
}
