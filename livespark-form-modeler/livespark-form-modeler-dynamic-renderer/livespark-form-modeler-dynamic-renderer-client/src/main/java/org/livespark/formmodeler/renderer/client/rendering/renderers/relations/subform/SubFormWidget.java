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

package org.livespark.formmodeler.renderer.client.rendering.renderers.relations.subform;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.renderer.client.DynamicFormRenderer;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;

@Dependent
public class SubFormWidget extends Composite implements TakesValue<Object> {

    interface SubFormWidgetBinder
            extends
            UiBinder<Widget, SubFormWidget> {

    }

    private static SubFormWidgetBinder uiBinder = GWT.create( SubFormWidgetBinder.class );

    @Inject
    private DynamicFormRenderer formRenderer;

    @UiField
    FlowPanel formContent;

    private FormRenderingContext renderingContext;


    public SubFormWidget() {
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    @PostConstruct
    protected void init() {
        formContent.add( formRenderer );
    }

    public void render( FormRenderingContext renderingContext ) {
        this.renderingContext = renderingContext;
        formRenderer.render( renderingContext );
    }

    protected void render() {
        formRenderer.render( renderingContext );
    }

    @Override
    public Object getValue() {
        return renderingContext != null ? renderingContext.getModel() : null;
    }

    @Override
    public void setValue( Object value ) {
        formRenderer.bind( value );
    }
}
