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

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import org.gwtbootstrap3.client.shared.event.ModalHideEvent;
import org.gwtbootstrap3.client.shared.event.ModalHideHandler;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Modal;
import org.livespark.formmodeler.editor.client.editor.FormEditorHelper;
import org.livespark.formmodeler.editor.client.editor.events.FieldDroppedEvent;
import org.livespark.formmodeler.editor.client.editor.events.FieldRemovedEvent;
import org.livespark.formmodeler.editor.client.editor.events.FormContextRequest;
import org.livespark.formmodeler.editor.client.editor.events.FormContextResponse;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.FieldRenderer;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.FormLayoutComponent;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.layout.editor.client.components.*;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Created by pefernan on 9/22/15.
 */
@Dependent
public class DraggableFieldComponent implements FormLayoutComponent,
        LayoutDragComponent, HasDragAndDropSettings, HasModalConfiguration, HasOnDropNotification, HasOnRemoveNotification {

    public final String[] SETTINGS_KEYS = new String[] { FORM_ID, FIELD_ID};

    protected SimplePanel content = new SimplePanel(  );

    protected FieldDefinitionPropertiesModal modal;

    @Inject
    protected FieldRendererManager fieldRendererManager;

    @Inject
    protected Event<FormContextRequest> fieldRequest;

    @Inject
    protected Event<FieldDroppedEvent> fieldDroppedEvent;

    @Inject
    protected Event<FieldRemovedEvent> fieldRemovedEvent;

    protected FormEditorHelper editorHelper;

    protected String formId;

    protected String fieldId;

    protected Path formPath;

    protected FieldDefinition field;

    protected FieldRenderer renderer;

    public void init( String formId, FieldDefinition field, Path formPath) {
        this.formId = formId;
        this.field = field;
        this.formPath = formPath;

        this.fieldId = field.getId();

        findRenderer();
    }

    protected void findRenderer() {
        renderer = fieldRendererManager.getRendererForField( field );
        if ( renderer != null ) {
            renderer.init(this, formPath);
        }
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
        if ( FORM_ID.equals( key )) return formId;
        else if (FIELD_ID.equals( key )) return fieldId;
        return null;
    }

    @Override
    public Modal getConfigurationModal( final ModalConfigurationContext ctx ) {

        ctx.getComponentProperties().put(FORM_ID, formId);
        ctx.getComponentProperties().put(FIELD_ID, fieldId);

        if (field == null) getCurrentField( ctx.getComponentProperties() );

        modal = new FieldDefinitionPropertiesModal( new Command() {
            @Override
            public void execute() {
                modal.hide();

            }
        } );

        modal.addHideHandler( new ModalHideHandler() {
            @Override
            public void onHide(ModalHideEvent evt) {
                ctx.setComponentProperty(FIELD_ID, fieldId);
                ctx.configurationFinished();
                if ( renderer != null ) renderContent();
                modal = null;
            }
        } );
        if ( renderer != null ) renderer.loadFieldProperties( modal );

        return modal;
    }

    @Override
    public void onDropComponent() {
        fieldDroppedEvent.fire(new FieldDroppedEvent(formId, fieldId));
    }

    @Override
    public void onRemoveComponent() {
        fieldRemovedEvent.fire(new FieldRemovedEvent(formId, fieldId));
    }

    @Override
    public IsWidget getDragWidget() {
        return renderer.getDragWidget();
    }

    @Override
    public IsWidget getPreviewWidget( RenderingContext ctx ) {
        return generateContent(ctx);
    }

    @Override
    public IsWidget getShowWidget( RenderingContext ctx ) {
        return generateContent(ctx);
    }

    protected IsWidget generateContent( RenderingContext ctx ) {
        if (renderer != null) {
            renderContent();
        } else {
            getCurrentField( ctx.getComponent().getProperties() );
        }

        return content;
    }

    protected void renderContent() {
        content.clear();
        content.add(renderer.renderWidget());
    }

    protected void getCurrentField( Map<String, String> properties ) {
        if (field != null) return;

        if (fieldId == null) {
            fieldId = properties.get(FIELD_ID);
        }

        if (formId == null) {
            formId = properties.get( FORM_ID );
        }

        fieldRequest.fire(new FormContextRequest(formId, fieldId));
    }

    public void onFieldResponse(@Observes FormContextResponse response) {
        if ( !response.getFormId().equals( formId ) ) {
            return;
        } else if ( fieldId.startsWith( FormEditorHelper.UNBINDED ) && field != null) {
            return;
        } else  if ( !response.getFieldId().equals( fieldId ) ) {
            return;
        }

        editorHelper = response.getEditorHelper();
        init(formId, editorHelper.getFormField(response.getFieldId()), editorHelper.getContent().getPath());
        if ( renderer != null ) {
            renderContent();
            if ( modal != null ) {
                renderer.loadFieldProperties( modal );
            }
        }
    }

    public List<String> getCompatibleFields() {
        return editorHelper.getCompatibleFields(field);
    }

    public List<String> getCompatibleFieldTypes() {
        return editorHelper.getCompatibleFieldTypes(field);
    }

    public void switchToField(String bindingExpression) {
        if (field.getBindingExpression().equals( bindingExpression )) return;

        FieldDefinition destField = editorHelper.switchToField( field, bindingExpression );

        if ( destField == null ) return;

        fieldDroppedEvent.fire( new FieldDroppedEvent( formId, destField.getId() ) );
        fieldRemovedEvent.fire( new FieldRemovedEvent( formId, field.getId() ) );

        fieldId = destField.getId();
        field = destField;

        renderer.setField(destField );

        renderContent();
    }

    public void switchToFieldType( String typeCode ) {
        if ( field.getCode().equals(typeCode) ) return;

        field = editorHelper.switchToFieldType( field, typeCode);

        findRenderer();

        if ( renderer != null ) {
            if ( modal != null ) renderer.loadFieldProperties( modal );
            renderContent();
        }
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getFormId() {
        return formId;
    }
}
