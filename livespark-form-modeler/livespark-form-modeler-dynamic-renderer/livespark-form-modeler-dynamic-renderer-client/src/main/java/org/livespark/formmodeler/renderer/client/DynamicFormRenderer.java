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
package org.livespark.formmodeler.renderer.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.livespark.formmodeler.renderer.service.Model2FormTransformerService;

@Dependent
public class DynamicFormRenderer implements IsWidget {

    public interface DynamicFormRendererView extends IsWidget {
        void setPresenter( DynamicFormRenderer presenter );

        void render();
        void bind();

        boolean validate();
    }

    @Inject
    private DynamicFormRendererView view;

    @Inject
    private Caller<Model2FormTransformerService> transformerService;

    private FormRenderingContext context;

    private FormDefinition form;

    private Object model;

    @PostConstruct
    protected void init() {
        view.setPresenter( this );
    }

    public void renderDefaultForm( final Object model ) {
        transformerService.call( new RemoteCallback<FormRenderingContext>() {
            @Override
            public void callback( FormRenderingContext context ) {
                render( context );
            }
        } ).createContext( model );
    }

    public void render ( FormRenderingContext context ) {
        this.context = context;
        view.render();
        if ( context.getModel() != null ) {
            view.bind();
        }
    }

    public void bind( Object model ) {
        if ( context != null && model != null ) {
            context.setModel( model );
            view.bind();
        }
    }

    public FormRenderingContext getContext() {
        return context;
    }

    public boolean validate() {
        return view.validate();
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }
}
