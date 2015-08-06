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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlternateSize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import org.livespark.formmodeler.editor.client.editor.FieldDefinitionPropertiesModal;
import org.livespark.formmodeler.editor.client.editor.events.FormFieldRequest;
import org.livespark.formmodeler.editor.client.editor.events.FormFieldResponse;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.uberfire.ext.layout.editor.client.components.HasDefaultSettings;
import org.uberfire.ext.layout.editor.client.components.HasModalConfiguration;
import org.uberfire.ext.layout.editor.client.components.LayoutDragComponent;
import org.uberfire.ext.layout.editor.client.components.ModalConfigurationContext;
import org.uberfire.ext.layout.editor.client.components.RenderingContext;

/**
 * Created by pefernan on 7/27/15.
 */
public abstract class FieldLayoutComponent<D extends FieldDefinition> implements LayoutDragComponent, HasDefaultSettings, HasModalConfiguration {
    public static String FORM_URI = "form_uri";
    public static String FIELD_NAME = "form_field_name";
    public static String FIELD_LABEL_TEXT = "form_field_label_text";

    public final String[] SETTINGS_KEYS = new String[] {FORM_URI, FIELD_NAME, FIELD_LABEL_TEXT};

    protected FlowPanel content = new FlowPanel(  );

    protected FieldDefinitionPropertiesModal modal = new FieldDefinitionPropertiesModal();

    @Inject
    protected Event<FormFieldRequest> fieldRequest;

    private String formPath;
    private String fieldName;

    protected D fieldDefinition;

    protected Map<String, String> settings = new HashMap<String, String>(  );


    public abstract IsWidget generateWidget();

    @Override
    public IsWidget getPreviewWidget( RenderingContext ctx ) {
        return generateContent( ctx );
    }

    @Override
    public IsWidget getShowWidget( RenderingContext ctx ) {
        return generateContent( ctx );
    }

    protected FlowPanel generateContent( RenderingContext ctx ) {
        content.clear();
        if (fieldDefinition != null) {
            content.add( generateWidget() );
        } else {
            getCurrentField( ctx.getComponent().getProperties() );
        }

        return content;
    }

    protected void init( String formUri, D fieldDefinition ) {
        if (fieldDefinition != null) {
            settings.put( FORM_URI, formUri );
            settings.put( FIELD_NAME, fieldDefinition.getName() );
            String property = fieldDefinition.getBoundPropertyName();

            if (property == null) property = fieldDefinition.getModelName();

            settings.put( FIELD_LABEL_TEXT, property );
        }
    }

    protected void getCurrentField( Map<String, String> properties ) {
        if (fieldDefinition != null) return;

        if (fieldName == null) {
            fieldName = properties.get( FIELD_NAME );
            if ( fieldName == null ) fieldName = settings.get( FIELD_NAME );
        }

        if (formPath == null) {
            formPath = properties.get( FORM_URI );
            if ( formPath == null ) formPath = settings.get( FORM_URI );
        }

        fieldRequest.fire( new FormFieldRequest( formPath, fieldName ) );
    }

    @Override
    public IsWidget getDragWidget() {
        TextBox textBox = GWT.create( TextBox.class );
        textBox.setPlaceholder( settings.get( FIELD_LABEL_TEXT ) );
        textBox.setReadOnly( true );
        textBox.setAlternateSize( AlternateSize.MEDIUM );
        return textBox;
    }

    @Override
    public void setDefaultSettingValue( String key, String value ) {

        if (Arrays.asList( SETTINGS_KEYS ).contains( key )) {
            settings.put( key, value );
        }
    }

    @Override
    public String getDefaultSettingValue( String key ) {
        return settings.get( key );
    }

    @Override
    public String[] getDefaultSettingsKeys() {
        return SETTINGS_KEYS;
    }

    @Override
    public Modal getConfigurationModal( final ModalConfigurationContext ctx ) {

        if (!settings.isEmpty()) {
            fieldName = settings.get( FIELD_NAME );
            ctx.getComponentProperties().put( FIELD_NAME, fieldName );
            formPath = settings.get( FORM_URI );
            ctx.getComponentProperties().put( FORM_URI, formPath );
        }

        if (fieldDefinition == null) getCurrentField( ctx.getComponentProperties() );

        modal.init( ctx );
        return modal;
    }

    public void onFieldResponse(@Observes FormFieldResponse<D> response) {
        if ( !response.getPath().equals( formPath ) || !response.getFieldName().equals( fieldName ) ) return;
        fieldDefinition = response.getField();
    }
}
