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

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.renderer.client.rendering.FieldLayoutComponent;
import org.livespark.formmodeler.renderer.client.rendering.FormLayoutGenerator;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;

@Dependent
@Templated
public class DynamicFormRendererViewImpl extends Composite implements DynamicFormRenderer.DynamicFormRendererView {

    @Inject @Any
    private FormLayoutGenerator layoutGenerator;

    @Inject
    @DataField
    private FlowPanel formContent;

    private DynamicFormRenderer presenter;

    @Override
    public void setPresenter( DynamicFormRenderer presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void render( FormRenderingContext context ) {
        formContent.clear();

        if( context != null ) {
            formContent.add( layoutGenerator.buildLayout( context ) );
        }
    }

    @Override
    public void bind() {
        for ( FieldLayoutComponent fieldComponent : layoutGenerator.getLayoutFields() ) {
            FieldDefinition field = fieldComponent.getField();
            Widget input = fieldComponent.getFieldRenderer().getInputWidget().asWidget();

            presenter.bind( input, field );
        }
    }

    @Override
    public FieldLayoutComponent getFieldLayoutComponentForField( FieldDefinition field ) {
        return layoutGenerator.getFieldLayoutComponentForField( field );
    }
}
