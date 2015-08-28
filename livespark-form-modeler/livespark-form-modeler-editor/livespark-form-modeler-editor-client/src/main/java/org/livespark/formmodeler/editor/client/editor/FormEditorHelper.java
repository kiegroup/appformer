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
package org.livespark.formmodeler.editor.client.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.livespark.formmodeler.editor.client.editor.events.FormFieldRequest;
import org.livespark.formmodeler.editor.client.editor.events.FormFieldResponse;
import org.livespark.formmodeler.editor.model.DataHolder;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.FormDefinition;
import org.livespark.formmodeler.editor.model.FormModelerContent;

/**
 * Created by pefernan on 8/6/15.
 */
@Dependent
public class FormEditorHelper {

    @Inject
    private Event<FormFieldResponse> responseEvent;

    private FormModelerContent content;

    private Map<String, FieldDefinition> availableFields = new HashMap<String, FieldDefinition>(  );

    public FormModelerContent getContent() {
        return content;
    }

    public void setContent( FormModelerContent content ) {
        this.content = content;
    }

    public FormDefinition getFormDefinition () {
        return content.getDefinition();
    }

    public void addAvailableFields ( List<FieldDefinition> fields ) {
        for ( FieldDefinition field : fields ) {
            addAvailableField( field );
        }
    }

    public void addAvailableField( FieldDefinition field ) {
        availableFields.put( field.getName(), field );
    }

    public FieldDefinition getAvailableField( String fieldName ) {
        return availableFields.get( fieldName );
    }

    public FieldDefinition getFormField( String fieldName ) {
        FieldDefinition result = content.getDefinition().getFieldByName( fieldName );
        if ( result == null) {
            result = availableFields.get( fieldName );
            if (result != null) {
                content.getDefinition().getFields().add( result );
                availableFields.remove( fieldName );
            }
        }
        return result;
    }

    public FieldDefinition removeField( String fieldName ) {
        Iterator<FieldDefinition> it = content.getDefinition().getFields().iterator();

        while (it.hasNext()) {
            FieldDefinition field = it.next();
            if (field.getName().equals( fieldName )) {
                it.remove();
                if (field.getBindingExpression() != null) {
                    addAvailableField( field );
                }
                return field;
            }
        }
        return null;
    }

    public void onFieldRequest( @Observes FormFieldRequest request ) {
        if (request.getFormId().equals( content.getDefinition().getId() )) {
            FieldDefinition field = getFormField( request.getFieldName() );
            responseEvent.fire( new FormFieldResponse( request.getFormId(), request.getFieldName(), field ) );
        }
    }

    public boolean addDataHolder( String name, String type ) {
        for ( DataHolder holder : content.getDefinition().getDataHolders() ) {
            if (holder.getName().equals( name )) return false;
        }
        content.getDefinition().getDataHolders().add( new DataHolder( name, type ) );
        return true;
    }
}
