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
package org.livespark.formmodeler.editor.client.editor.fields;

import java.util.Map;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlternateSize;
import com.github.gwtbootstrap.client.ui.event.HideEvent;
import com.github.gwtbootstrap.client.ui.event.HideHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import org.livespark.formmodeler.editor.client.editor.FieldDefinitionPropertiesModal;
import org.livespark.formmodeler.editor.client.editor.events.FieldDroppedEvent;
import org.livespark.formmodeler.editor.client.editor.events.FieldRemovedEvent;
import org.livespark.formmodeler.editor.client.editor.events.FormFieldRequest;
import org.livespark.formmodeler.editor.client.editor.events.FormFieldResponse;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.uberfire.ext.layout.editor.client.components.HasDragAndDropSettings;
import org.uberfire.ext.layout.editor.client.components.HasModalConfiguration;
import org.uberfire.ext.layout.editor.client.components.HasOnDropNotification;
import org.uberfire.ext.layout.editor.client.components.HasOnRemoveNotification;
import org.uberfire.ext.layout.editor.client.components.LayoutDragComponent;
import org.uberfire.ext.layout.editor.client.components.ModalConfigurationContext;
import org.uberfire.ext.layout.editor.client.components.RenderingContext;
import org.uberfire.ext.properties.editor.client.PropertyEditorWidget;
import org.uberfire.ext.properties.editor.model.PropertyEditorCategory;
import org.uberfire.ext.properties.editor.model.PropertyEditorEvent;

/**
 * Created by pefernan on 7/27/15.
 */
public abstract class FieldLayoutComponent<D extends FieldDefinition> implements LayoutDragComponent, HasDragAndDropSettings, HasModalConfiguration, HasOnDropNotification, HasOnRemoveNotification {
    public static String FORM_ID = "form_id";
    public static String FIELD_NAME = "form_field_name";
    public static String FIELD_DRAG_LABEL = "form_field_label";

    public final String[] SETTINGS_KEYS = new String[] { FORM_ID, FIELD_NAME, FIELD_DRAG_LABEL };

    protected FlowPanel content = new FlowPanel(  );

    protected FieldDefinitionPropertiesModal modal;

    @Inject
    protected PropertyEditorWidget editorWidget;

    @Inject
    protected Event<FormFieldRequest> fieldRequest;

    @Inject
    protected Event<FieldDroppedEvent> fieldDroppedEvent;

    @Inject
    protected Event<FieldRemovedEvent> fieldRemovedEvent;

    private String formId;
    private String fieldDragLabel;
    private String fieldName;

    protected D fieldDefinition;

    public abstract IsWidget generateWidget();
    public abstract PropertyEditorCategory generatePropertyEditorCategory();

    @Override
    public IsWidget getPreviewWidget( RenderingContext ctx ) {
        return generateContent( ctx );
    }

    @Override
    public IsWidget getShowWidget( RenderingContext ctx ) {
        return generateContent( ctx );
    }

    protected FlowPanel generateContent( RenderingContext ctx ) {
        if (fieldDefinition != null) {
            renderContent();
        } else {
            getCurrentField( ctx.getComponent().getProperties() );
        }

        return content;
    }

    protected void renderContent() {
        content.clear();
        content.add( generateWidget() );
    }

    protected void init( String formId, D fieldDefinition ) {
        if (fieldDefinition != null) {
            this.formId = formId;
            this.fieldName = fieldDefinition.getName();

            String property = fieldDefinition.getBoundPropertyName();

            if (property == null) property = fieldDefinition.getModelName();
            fieldDragLabel = property;
        }
    }

    protected void getCurrentField( Map<String, String> properties ) {
        if (fieldDefinition != null) return;

        if (fieldName == null) {
            fieldName = properties.get( FIELD_NAME );
        }

        if (formId == null) {
            formId = properties.get( FORM_ID );
        }

        fieldRequest.fire( new FormFieldRequest( formId, fieldName ) );
    }

    @Override
    public IsWidget getDragWidget() {
        TextBox textBox = GWT.create( TextBox.class );
        textBox.setPlaceholder( fieldDragLabel );
        textBox.setReadOnly( true );
        textBox.setAlternateSize( AlternateSize.MEDIUM );
        return textBox;
    }

    @Override
    public void setSettingValue( String key, String value ) {
        if ( FORM_ID.equals( key )) formId = value;
        else if (FIELD_NAME.equals( key )) fieldName = value;
        else if ( FIELD_DRAG_LABEL.equals( key )) fieldDragLabel = value;
    }

    @Override
    public String getSettingValue( String key ) {
        if ( FORM_ID.equals( key )) return formId;
        else if (FIELD_NAME.equals( key )) return fieldName;
        else if ( FIELD_DRAG_LABEL.equals( key )) return fieldDragLabel;
        return null;
    }

    @Override
    public String[] getSettingsKeys() {
        return SETTINGS_KEYS;
    }

    @Override
    public Modal getConfigurationModal( final ModalConfigurationContext ctx ) {

        ctx.getComponentProperties().put( FORM_ID, formId );
        ctx.getComponentProperties().put( FIELD_NAME, fieldName );
        ctx.getComponentProperties().put( FIELD_DRAG_LABEL, fieldDragLabel );

        if (fieldDefinition == null) getCurrentField( ctx.getComponentProperties() );

        modal = new FieldDefinitionPropertiesModal( new Command() {
            @Override
            public void execute() {
                modal.hide();

            }
        } );

        modal.addHideHandler( new HideHandler() {
            @Override public void onHide( HideEvent hideEvent ) {
                ctx.configurationFinished();
                renderContent();
                modal = null;
            }
        } );
        if ( fieldDefinition != null ) initEditorWidget();

        return modal;
    }

    public void onFieldResponse(@Observes FormFieldResponse<D> response) {
        if ( !response.getFormId().equals( formId ) || !response.getFieldName().equals( fieldName ) ) return;
        fieldDefinition = response.getField();
        renderContent();
        if (modal != null) {
            initEditorWidget();
        }
    }

    protected void initEditorWidget() {
        editorWidget.handle( new PropertyEditorEvent( formId + " -" + fieldName, generatePropertyEditorCategory() ) );
        modal.add( editorWidget );
    }

    public abstract FieldLayoutComponent<D> newInstance(String formId, D fieldDefinition);

    public abstract String getSupportedFieldDefinition();

    @Override
    public void onDropComponent() {
        fieldDroppedEvent.fire( new FieldDroppedEvent( formId, fieldName ) );
    }

    @Override
    public void onRemoveComponent() {
        fieldRemovedEvent.fire( new FieldRemovedEvent( formId, fieldName ) );
    }
}
