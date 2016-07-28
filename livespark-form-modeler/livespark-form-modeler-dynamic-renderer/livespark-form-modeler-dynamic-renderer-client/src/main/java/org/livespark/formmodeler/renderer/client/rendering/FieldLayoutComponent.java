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
package org.livespark.formmodeler.renderer.client.rendering;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import org.gwtbootstrap3.client.ui.TextBox;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormLayoutComponent;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.uberfire.ext.layout.editor.client.api.LayoutDragComponent;
import org.uberfire.ext.layout.editor.client.api.RenderingContext;

@Dependent
public class FieldLayoutComponent<T extends FormRenderingContext> implements FormLayoutComponent, LayoutDragComponent {

    protected SimplePanel content = new SimplePanel();

    @Inject
    protected FieldRendererManager fieldRendererManager;

    @Inject
    protected TranslationService translationService;

    protected FieldDefinition field;

    protected FieldRenderer fieldRenderer;

    protected T renderingContext;

    public void init( T renderingContext, FieldDefinition field ) {
        this.renderingContext = renderingContext;

        this.field = field;

        initComponent();
    }

    protected void initComponent() {
        fieldRenderer = fieldRendererManager.getRendererForField( field );
        if ( fieldRenderer != null ) {
            fieldRenderer.init( renderingContext, field );
        }
    }

    @Override
    public IsWidget getDragWidget() {
        TextBox textBox = GWT.create( TextBox.class );

        textBox.setReadOnly(true);

        String name = "";

        if ( field.getModelName() != null ) {
            if ( field.getBoundPropertyName() != null ) {
                name = field.getBoundPropertyName();
            } else {
                name = field.getModelName();
            }
        } else {
            name = translationService.getTranslation( fieldRenderer.getName() );
            if ( name == null || name.isEmpty() ) {
                name = fieldRenderer.getName();
            }
        }

        textBox.setPlaceholder( name );

        return textBox;
    }

    @Override
    public IsWidget getPreviewWidget( RenderingContext ctx ) {
        return generateContent( ctx );
    }

    @Override
    public IsWidget getShowWidget( RenderingContext ctx ) {
        return generateContent(ctx);
    }

    protected IsWidget generateContent( RenderingContext ctx ) {
        if ( fieldRenderer != null) {
            renderContent();
        }

        return content;
    }

    protected void renderContent() {
        content.clear();
        content.add( fieldRenderer.renderWidget() );
    }

    public String getFieldId() {
        return field.getId();
    }

    public String getFormId() {
        return renderingContext.getRootForm().getId();
    }

    public FieldRenderer getFieldRenderer() {
        return fieldRenderer;
    }

    public FieldDefinition getField() {
        return field;
    }
}
