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

public abstract class FormRenderingContext {

    protected Map<String, FormDefinition> availableForms = new HashMap<String, FormDefinition>();

    protected FormDefinition rootForm;

    protected Object model;

    public FormRenderingContext() {
    }

    public FormRenderingContext( @MapsTo( "rootForm" ) FormDefinition rootForm,
                                 @MapsTo( "model" ) Object model ) {
        this.rootForm = rootForm;
        this.model = model;
    }

    public FormDefinition getRootForm() {
        return rootForm;
    }

    public void setRootForm( FormDefinition rootForm ) {
        this.rootForm = rootForm;
    }

    public void setModel( Object model ) {
        this.model = model;
    }

    public Object getModel() {
        return model;
    }

    public Map<String, FormDefinition> getAvailableForms() {
        return availableForms;
    }

    protected abstract FormRenderingContext getNewInstance( FormDefinition form, Object model );

    public FormRenderingContext getCopyFor( String formKey, Object model ) {
        FormRenderingContext copy = getNewInstance( availableForms.get( formKey ), model );
        copy.availableForms = availableForms;
        return copy;
    }
}
