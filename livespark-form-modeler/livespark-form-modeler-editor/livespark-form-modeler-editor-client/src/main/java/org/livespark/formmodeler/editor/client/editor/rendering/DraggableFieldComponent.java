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
package org.livespark.formmodeler.editor.client.editor.rendering;

import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.Modal;
import org.livespark.formmodeler.editor.client.editor.FormEditorHelper;
import org.livespark.formmodeler.editor.client.editor.events.FieldDroppedEvent;
import org.livespark.formmodeler.editor.client.editor.events.FieldRemovedEvent;
import org.livespark.formmodeler.editor.client.editor.events.FormEditorContextRequest;
import org.livespark.formmodeler.editor.client.editor.events.FormEditorContextResponse;
import org.livespark.formmodeler.editor.client.editor.properties.FieldPropertiesRenderer;
import org.livespark.formmodeler.editor.client.editor.properties.FieldPropertiesRendererHelper;
import org.livespark.formmodeler.editor.service.FormEditorRenderingContext;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.renderer.client.rendering.FieldLayoutComponent;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.layout.editor.client.components.HasDragAndDropSettings;
import org.uberfire.ext.layout.editor.client.components.HasModalConfiguration;
import org.uberfire.ext.layout.editor.client.components.HasOnDropNotification;
import org.uberfire.ext.layout.editor.client.components.HasOnRemoveNotification;
import org.uberfire.ext.layout.editor.client.components.ModalConfigurationContext;
import org.uberfire.ext.layout.editor.client.components.RenderingContext;

/**
 * Created by pefernan on 9/22/15.
 */
@Dependent
public class DraggableFieldComponent extends FieldLayoutComponent<FormEditorRenderingContext> implements HasDragAndDropSettings,
        HasModalConfiguration, HasOnDropNotification, HasOnRemoveNotification {

    public final String[] SETTINGS_KEYS = new String[] { FORM_ID, FIELD_ID};

    @Inject
    protected FieldPropertiesRenderer propertiesRenderer;

    boolean showProperties = false;

    @Inject
    protected Event<FormEditorContextRequest> fieldRequest;

    @Inject
    protected Event<FieldDroppedEvent> fieldDroppedEvent;

    @Inject
    protected Event<FieldRemovedEvent> fieldRemovedEvent;

    protected FormEditorHelper editorHelper;

    private FieldPropertiesRendererHelper propertiesRendererHelper;

    private ModalConfigurationContext configContext;

    private String fieldId;

    private String formId;

    @Override
    public void init( FormEditorRenderingContext renderingContext, FieldDefinition field ) {
        super.init( renderingContext, field );
        initPropertiesConfig();
    }

    protected void initPropertiesConfig() {
        propertiesRendererHelper = new FieldPropertiesRendererHelper() {

            @Override
            public FormRenderingContext getCurrentRenderingContext() {
                return renderingContext;
            }

            @Override
            public FieldDefinition getCurrentField() {
                return field;
            }

            @Override
            public List<String> getAvailableFields() {
                return  editorHelper.getCompatibleFields( field );
            }

            @Override
            public List<String> getCompatibleFieldTypes() {
                return editorHelper.getCompatibleFieldTypes( field );
            }

            @Override
            public void onClose() {
                fieldRenderer.initInputWidget();
                renderContent();
                showProperties = false;
                if ( configContext != null) {
                    configContext.configurationFinished();
                    configContext = null;
                }
            }

            @Override
            public void onFieldTypeChange( String newType ) {
                switchToFieldType( newType );
            }

            @Override
            public void onFieldBindingChange( String newBinding ) {
                switchToField( newBinding );
            }

            @Override
            public Path getPath() {
                return renderingContext.getFormPath();
            }
        };
    }

    @Override
    public String[] getSettingsKeys() {
        return SETTINGS_KEYS;
    }

    @Override
    public void setSettingValue( String key, String value ) {
        if ( FORM_ID.equals( key )) {
            formId = value;
        } else if (FIELD_ID.equals( key )) {
            fieldId = value;
        }
    }

    @Override
    public String getSettingValue( String key ) {
        if ( FORM_ID.equals( key )) {
            if ( renderingContext != null ) {
                return renderingContext.getRootForm().getId();
            }
            return formId;
        }
        else if (FIELD_ID.equals( key )) {
            if ( field != null ) {
                return field.getId();
            }
            return fieldId;
        }
        return null;
    }

    @Override
    public Modal getConfigurationModal( final ModalConfigurationContext ctx ) {

        ctx.getComponentProperties().put( FORM_ID, formId );
        ctx.getComponentProperties().put( FIELD_ID, fieldId );

        showProperties = true;

        configContext = ctx;

        if (field == null) {
            getEditionContext( ctx.getComponentProperties() );
        } else {
            propertiesRenderer.render( propertiesRendererHelper );

        }

        return propertiesRenderer.getView().getPropertiesModal();
    }

    @Override
    public void onDropComponent() {
        fieldDroppedEvent.fire( new FieldDroppedEvent( formId, fieldId ) );
    }

    @Override
    public void onRemoveComponent() {
        fieldRemovedEvent.fire( new FieldRemovedEvent( formId, fieldId ) );
    }

    @Override
    protected IsWidget generateContent( RenderingContext ctx ) {
        if ( fieldRenderer != null) {
            renderContent();
        } else {
            getEditionContext( ctx.getComponent().getProperties() );
        }
        return content;
    }

    protected void getEditionContext( Map<String, String> properties ) {
        if (field != null) return;

        if (fieldId == null) {
            fieldId = properties.get(FIELD_ID);
        }

        if (formId == null) {
            formId = properties.get( FORM_ID );
        }

        fieldRequest.fire( new FormEditorContextRequest( formId, fieldId ) );
    }

    public void onFieldResponse(@Observes FormEditorContextResponse response) {
        if ( !response.getFormId().equals( formId ) ) {
            return;
        } else if ( fieldId.startsWith( FormEditorHelper.UNBINDED ) && field != null) {
            return;
        } else  if ( !response.getFieldId().equals( fieldId ) ) {
            return;
        }

        editorHelper = response.getEditorHelper();
        init( editorHelper.getRenderingContext(), editorHelper.getFormField( response.getFieldId() ) );
        renderContent();
        if ( showProperties ) {
            propertiesRenderer.render( propertiesRendererHelper );
        }
    }

    public List<String> getCompatibleFields() {
        return editorHelper.getCompatibleFields( field );
    }

    public List<String> getCompatibleFieldTypes() {
        return editorHelper.getCompatibleFieldTypes( field );
    }

    public void switchToField(String bindingExpression) {
        if (field.getBindingExpression().equals( bindingExpression )) return;

        FieldDefinition destField = editorHelper.switchToField( field, bindingExpression );

        if ( destField == null ) return;

        fieldDroppedEvent.fire( new FieldDroppedEvent( formId, destField.getId() ) );
        fieldRemovedEvent.fire( new FieldRemovedEvent( formId, field.getId() ) );

        fieldId = destField.getId();
        field = destField;

        if ( showProperties ) {
            propertiesRenderer.render( propertiesRendererHelper );
        }

        renderContent();
    }

    public void switchToFieldType( String typeCode ) {
        if ( field.getCode().equals(typeCode) ) return;

        field = editorHelper.switchToFieldType( field, typeCode);

        initComponent();

        if ( showProperties ) {
            propertiesRenderer.render( propertiesRendererHelper );
        }
    }

    public Path getFormPath() {
        return renderingContext.getFormPath();
    }
}
