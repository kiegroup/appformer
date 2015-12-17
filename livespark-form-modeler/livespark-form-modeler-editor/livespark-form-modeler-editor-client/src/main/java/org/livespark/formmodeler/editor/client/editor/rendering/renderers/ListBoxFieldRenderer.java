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
package org.livespark.formmodeler.editor.client.editor.rendering.renderers;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.*;
import org.livespark.formmodeler.editor.client.editor.rendering.DraggableFieldComponent;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.SelectorOptionFormPresenter;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionRequest;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionResponse;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionUpdate;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.editor.model.impl.basic.selectors.ListBoxFieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.selectors.SelectorOption;
import org.livespark.formmodeler.rendering.client.view.util.StringListBoxRenderer;
import org.uberfire.ext.properties.editor.model.CustomPropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Dependent
public class ListBoxFieldRenderer extends FieldRenderer<ListBoxFieldDefinition> {

    @Inject
    protected Event<FieldSelectorOptionResponse> responseEvent;

    protected StringListBoxRenderer optionsRenderer = new StringListBoxRenderer();

    protected ValueListBox<String> widgetList = new ValueListBox<String>(optionsRenderer);

    @Override
    public String getName() {
        return "ListBox";
    }

    @Override
    public IsWidget renderWidget() {
        refreshListBoxOptions();
        FormGroup group = new FormGroup(  );
        FormLabel label = new FormLabel(  );
        widgetList.setEnabled( !field.getReadonly() );
        label.setText( field.getLabel() );
        label.setFor( widgetList.getId() );
        group.add(label);
        group.add( widgetList );
        group.add(new HelpBlock());
        return group;
    }

    @Override
    public String getSupportedFieldDefinitionCode() {
        return ListBoxFieldDefinition._CODE;
    }

    @Override
    protected List<PropertyEditorFieldInfo> getCustomFieldSettings() {
        List<PropertyEditorFieldInfo> result = new ArrayList<PropertyEditorFieldInfo>();

        JSONObject obj = new JSONObject();
        obj.put( DraggableFieldComponent.FORM_ID, new JSONString( draggableFieldComponent.getFormId() ) );
        obj.put( DraggableFieldComponent.FIELD_ID, new JSONString( draggableFieldComponent.getFieldId() ) );

        result.add( new CustomPropertyEditorFieldInfo( FieldProperties.INSTANCE.options(), obj.toString(), SelectorOptionFormPresenter.class ) );
        return result;
    }

    protected void onRequest( @Observes FieldSelectorOptionRequest requestEvent ) {
        if ( draggableFieldComponent == null ) return;
        if ( draggableFieldComponent.getFormId().equals( requestEvent.getFormId() ) &&
                draggableFieldComponent.getFieldId().equals( requestEvent.getFieldId() ) ) {

            responseEvent.fire( new FieldSelectorOptionResponse( requestEvent.getFormId(),
                    requestEvent.getFieldId(),
                    field.getOptions()) );
        }
    }

    protected void onUpdate( @Observes FieldSelectorOptionUpdate updateEvent ) {
        if ( draggableFieldComponent == null ) return;
        if ( draggableFieldComponent.getFormId().equals( updateEvent.getFormId() ) &&
                draggableFieldComponent.getFieldId().equals( updateEvent.getFieldId() )) {
            field.setOptions( updateEvent.getOptions() );

            refreshListBoxOptions();
        }
    }

    protected void refreshListBoxOptions() {
        Map<String, String> optionsValues = new HashMap<String, String>( );
        widgetList.reset();
        if ( field.getOptions() != null ) {

            String defaultValue = null;
            for ( SelectorOption option : field.getOptions() ) {
                optionsValues.put( option.getValue(), option.getText() );
                if ( option.getDefaultValue() ) {
                    defaultValue = option.getValue();
                }
            }

            if ( defaultValue != null ) {
                widgetList.setValue( defaultValue );
            } else {
                widgetList.setValue( "" );
            }
        }
        optionsRenderer.setValues( optionsValues );
        widgetList.setAcceptableValues( optionsValues.keySet() );
    }
}
