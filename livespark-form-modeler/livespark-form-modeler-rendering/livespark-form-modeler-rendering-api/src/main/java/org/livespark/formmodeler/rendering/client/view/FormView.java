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

    private RemoteCallback<M> createCallback = new NoOpCallback();
    private RemoteCallback<Boolean> updateCallback;

    @Inject
    @DataField
    private Button submit;

    @Inject
    protected Validator validator;

    private boolean edit;
    
    public void setCreateCallback( RemoteCallback<M> callback ) {
        if ( callback == null ) {
            this.createCallback = new NoOpCallback();
        } else {
            this.createCallback = callback;
        }
    }
    
    public void setUpdateCallback( RemoteCallback<Boolean> callback ) {
        if ( callback == null ) {
            this.updateCallback = new RemoteCallback<Boolean>() {
                @Override
                public void callback( Boolean response ) {
                }
            };
        } else {
            this.updateCallback = callback;
        }
    }

    @Override
    public void setModel( M model ) {
        super.setModel( model );
        clearFieldErrors();
    }
    
    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    @EventHandler( "submit" )
    private void onSubmit( ClickEvent event ) {
        M model = binder.getModel();
        if (edit) {
            updateModel( model, updateCallback );
        }
        else {
            createModel( model, createCallback );
        }
    }

    protected abstract void createModel( M model, RemoteCallback<M> callback );
    
    protected abstract void updateModel( M model, RemoteCallback<Boolean> callback );

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
