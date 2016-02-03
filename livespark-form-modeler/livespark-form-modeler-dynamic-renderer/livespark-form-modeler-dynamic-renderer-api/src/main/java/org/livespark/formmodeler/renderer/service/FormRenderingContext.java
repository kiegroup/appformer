/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.renderer.service;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.livespark.formmodeler.model.FormDefinition;

public abstract class FormRenderingContext<T> {

    protected Map<String, FormDefinition> availableForms = new HashMap<String, FormDefinition>();

    protected String rootFormId;

    protected T model;

    protected FormRenderingContext parentContext;

    public FormRenderingContext() {
    }

    public FormDefinition getRootForm() {
        return availableForms.get( rootFormId );
    }

    public void setRootForm( FormDefinition rootForm ) {
        this.rootFormId = rootForm.getId();
        availableForms.put( rootFormId, rootForm );
    }

    public void setModel( T model ) {
        this.model = model;
    }

    public T getModel() {
        return model;
    }

    public FormRenderingContext getParentContext() {
        return parentContext;
    }

    public void setParentContext( FormRenderingContext parentContext ) {
        this.parentContext = parentContext;
    }

    public Map<String, FormDefinition> getAvailableForms() {
        return availableForms;
    }

    protected abstract FormRenderingContext getNewInstance();

    public FormRenderingContext getCopyFor( String formKey, T model ) {
        if ( formKey == null || formKey.isEmpty() ) {
            return null;
        }
        FormRenderingContext copy = getNewInstance();
        copy.setRootForm( availableForms.get( formKey ) );
        copy.setModel( model );
        copy.availableForms = availableForms;
        copy.setParentContext( this );
        return copy;
    }
}
