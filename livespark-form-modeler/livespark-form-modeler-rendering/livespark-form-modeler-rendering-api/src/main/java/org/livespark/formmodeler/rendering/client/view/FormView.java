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
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.livespark.formmodeler.rendering.client.shared.FormModel;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

/**
 * Created by pefernan on 4/17/15.
 */
public abstract class FormView<M extends FormModel> extends BaseView<M> {

    @Inject
    protected Validator validator;

    @Override
    public void setModel( M model ) {
        super.setModel( model );
        clearFieldErrors();
        updateNestedModels(false);
    }

    @PostConstruct
    private void init() {
        List entites = getEntities();
        if (entites == null || entites.isEmpty() || entites.size() < getEntitiesCount()) {
            initEntities();
        }
        updateNestedModels(true);
    }

    protected void clearFieldErrors() {
        for ( String field : getInputNames() ) {
            Element group = Document.get().getElementById( field + "_form_group" );
            Element helpBlock = Document.get().getElementById( field + "_help_block" );
            if ( group != null )
                group.removeClassName( "has-error" );
            if ( helpBlock != null )
                helpBlock.setInnerHTML( "" );
        }
    }

    public abstract void setReadOnly( boolean readOnly );

    protected abstract void updateNestedModels(boolean init);

    protected abstract int getEntitiesCount();

    protected abstract List getEntities();

    protected abstract void initEntities();

    public abstract boolean doExtraValidations();

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
                group.addClassName( "has-error" );
            if ( helpBlock != null )
                helpBlock.setInnerHTML( validation.getMessage() );
        }

        boolean extraValidations = doExtraValidations();

        return isValid && extraValidations;
    }
}
