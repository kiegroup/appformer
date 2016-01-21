/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import org.livespark.formmodeler.editor.client.editor.rendering.DraggableFieldComponent;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.SelectorOptionFormPresenter;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionRequest;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionResponse;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionUpdate;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.model.impl.basic.selectors.SelectorField;
import org.livespark.formmodeler.renderer.client.rendering.FieldRenderer;
import org.livespark.formmodeler.renderer.client.rendering.renderers.SelectorFieldRenderer;
import org.uberfire.ext.properties.editor.model.CustomPropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;

public abstract class SelectorFieldPropertiesGenerator<F extends SelectorField> extends FieldPropertiesGenerator<F> {

    @Inject
    protected Event<FieldSelectorOptionResponse> responseEvent;

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

            refreshSelectorOptions();
        }
    }

    protected void refreshSelectorOptions() {
        FieldRenderer renderer = draggableFieldComponent.getFieldRenderer();
        if ( renderer instanceof SelectorFieldRenderer ) {
            ((SelectorFieldRenderer) renderer).refreshSelectorOptions();
        }
    }
}
