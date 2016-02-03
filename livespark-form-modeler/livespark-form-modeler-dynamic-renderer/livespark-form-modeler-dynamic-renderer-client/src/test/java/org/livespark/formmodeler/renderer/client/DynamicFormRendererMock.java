/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.formmodeler.renderer.client;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.databinding.client.PropertyChangeUnsubscribeHandle;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeHandler;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.renderer.service.Model2FormTransformerService;
import org.livespark.formmodeler.rendering.client.view.validation.FormViewValidator;

public class DynamicFormRendererMock extends DynamicFormRenderer {

    protected Object model;

    protected boolean binded = false;

    public DynamicFormRendererMock( DynamicFormRendererView view, Caller<Model2FormTransformerService> transformerService, FormViewValidator formViewValidator ) {
        super( view, transformerService, formViewValidator );
    }

    @Override
    protected void doBind( Object model ) {
        this.model = model;
        binded = model != null;
    }

    @Override
    protected void doBind( Widget input, FieldDefinition field ) {
    }

    @Override
    protected PropertyChangeUnsubscribeHandle doRegister( String property, PropertyChangeHandler handler ) {
        return new PropertyChangeUnsubscribeHandle() {
            @Override
            public void unsubscribe() {
            }
        };
    }

    @Override
    protected void doUnbind() {
        binded = false;
    }

    @Override
    protected boolean isBinded() {
        return binded;
    }

    @Override
    protected Object getBinderModel() {
        return model;
    }
}
