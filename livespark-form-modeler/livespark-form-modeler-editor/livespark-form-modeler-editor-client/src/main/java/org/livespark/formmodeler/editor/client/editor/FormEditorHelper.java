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

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.livespark.formmodeler.editor.client.editor.events.FormContextRequest;
import org.livespark.formmodeler.editor.client.editor.events.FormContextResponse;
import org.livespark.formmodeler.editor.client.editor.rendering.DraggableFieldComponent;
import org.livespark.formmodeler.editor.client.editor.service.ClientFieldManagerImpl;
import org.livespark.formmodeler.editor.model.DataHolder;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.FormDefinition;
import org.livespark.formmodeler.editor.model.FormModelerContent;
import org.livespark.formmodeler.editor.service.FieldManager;
import org.livespark.formmodeler.editor.service.FormEditorService;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;
import org.uberfire.ext.layout.editor.client.components.LayoutDragComponent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.*;

/**
 * Created by pefernan on 8/6/15.
 */
@Dependent
public class FormEditorHelper {

    @Inject
    private ClientFieldManagerImpl fieldManager;

    @Inject
    private Event<FormContextResponse> responseEvent;

    @Inject
    private Caller<FormEditorService> editorService;

    private FormModelerContent content;

    private Map<String, FieldDefinition> availableFields = new HashMap<String, FieldDefinition>(  );

    private List<FieldDefinition> baseTypes;

    private List<DraggableFieldComponent> draggableFieldComponents;

    @PostConstruct
    protected void doInit() {
        baseTypes = fieldManager.getBaseTypes();
    }

    public FormModelerContent getContent() {
        return content;
    }

    public void initHelper(FormModelerContent content) {
        this.content = content;

        if (draggableFieldComponents != null && !draggableFieldComponents.isEmpty()) return;
        draggableFieldComponents = new ArrayList<DraggableFieldComponent>();

        for ( FieldDefinition field : baseTypes ) {
            DraggableFieldComponent dragComponent = IOC.getBeanManager().lookupBean( DraggableFieldComponent.class ).getInstance();
            if (dragComponent != null) {
                dragComponent.init( getFormDefinition().getId(), field, content.getPath() );
                draggableFieldComponents.add( dragComponent );
            }
        }
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
        availableFields.put( field.getId(), field );
    }

    public FieldDefinition getFormField( String fieldId ) {
        FieldDefinition result = content.getDefinition().getFieldById(fieldId);
        if ( result == null) {
            result = availableFields.get(fieldId);
            if (result != null) {
                content.getDefinition().getFields().add(result);
                availableFields.remove(fieldId);
            } else {
                for (int i = 0; i < baseTypes.size() && result == null; i++) {
                    FieldDefinition base = baseTypes.get( i );

                    if ( base.getId().equals( fieldId ) ) {
                        result = base;
                    }
                }

                if ( result != null ) {
                    resetBaseType(result);
                    result.setName( generateUnbindedFieldName( result ) );
                    result.setLabel( result.getCode() );
                    content.getDefinition().getFields().add(result);
                }
            }
        }
        return result;
    }

    protected void resetBaseType( FieldDefinition baseFieldType ) {
        FieldDefinition newType = fieldManager.getDefinitionByTypeCode(baseFieldType.getCode());

        newType.setName( generateUnbindedFieldName( newType ));

        newType.setLabel( newType.getCode() );

        baseTypes.remove(baseFieldType);

        baseTypes.add( newType );

        for (DraggableFieldComponent component : draggableFieldComponents ) {
            if ( component.getFieldId().equals( baseFieldType.getId()) ) {
                component.init( getFormDefinition().getId(), newType, content.getPath() );
            }
        }
    }

    public FieldDefinition removeField( String fieldId ) {
        Iterator<FieldDefinition> it = content.getDefinition().getFields().iterator();

        while (it.hasNext()) {
            FieldDefinition field = it.next();
            if (field.getId().equals( fieldId )) {
                it.remove();
                resetField( field );
                return field;
            }
        }
         return null;
    }

    public List<FieldDefinition> getBaseFields() {
        return baseTypes;
    }

    public void onFieldRequest( @Observes FormContextRequest request ) {
        if (request.getFormId().equals( content.getDefinition().getId() )) {
            responseEvent.fire( new FormContextResponse( request.getFormId(), request.getFieldId(), this ) );
        }
    }

    public boolean addDataHolder( String name, String type ) {
        for ( DataHolder holder : content.getDefinition().getDataHolders() ) {
            if (holder.getName().equals( name )) return false;
        }
        content.getDefinition().getDataHolders().add( new DataHolder( name, type ) );
        return true;
    }

    public List<String> getCompatibleFields(FieldDefinition field) {
        List<String> compatibles = fieldManager.getCompatibleFieldTypes(field);

        Set result = new TreeSet();
        if ( field.getBindingExpression() != null ) result.add( field.getBindingExpression() );
        for ( String compatibleType : compatibles ) {
            for ( FieldDefinition definition : availableFields.values()) {
                if ( definition.getCode().equals( compatibleType )) result.add( definition.getBindingExpression() );
            }
        }
        return new ArrayList<String>(result);
    }

    public List<String> getCompatibleFieldTypes(FieldDefinition field) {
        return fieldManager.getCompatibleFieldTypes(field);
    }

    public FieldDefinition switchToField(FieldDefinition field, String bindingExpression) {
        FieldDefinition resultDefinition = fieldManager.getDefinitionByTypeCode(field.getCode());

        // TODO: make settings copy optional
        resultDefinition.copyFrom(field);

        // Handling the change when the field binding is going to be removed
        if ( bindingExpression == null || bindingExpression.equals("") ) {
            resultDefinition.setName( generateUnbindedFieldName(resultDefinition) );
            content.getDefinition().getFields().add(resultDefinition);
            return resultDefinition;
        }

        // Handling the binding change
        for (Iterator<FieldDefinition> it = availableFields.values().iterator(); it.hasNext(); ) {
            FieldDefinition destField = it.next();
            if (destField.getBindingExpression().equals( bindingExpression )) {

                resultDefinition.setId( destField.getId() );
                resultDefinition.setName(destField.getName());
                resultDefinition.setStandaloneClassName(destField.getStandaloneClassName());
                resultDefinition.setAnnotatedId(destField.isAnnotatedId());
                resultDefinition.setModelName(destField.getModelName());
                resultDefinition.setBoundPropertyName(destField.getBoundPropertyName());

                content.getDefinition().getFields().add(resultDefinition);

                it.remove();

                resetField( field );

                return resultDefinition;
            }
        }

        return null;
    }

    protected void resetField( FieldDefinition field ) {
        if ( field.getName().startsWith(FieldManager.UNBINDED_FIELD_NAME_PREFFIX) ) return;

        editorService.call(new RemoteCallback<FieldDefinition>() {
            @Override
            public void callback(FieldDefinition field) {
                availableFields.put(field.getId(), field);
            }
        }).resetField(content.getDefinition(), field, content.getPath());
    }

    public FieldDefinition switchToFieldType(FieldDefinition field, String typeCode) {
        FieldDefinition resultDefinition = fieldManager.getDefinitionByTypeCode( typeCode );

        resultDefinition.copyFrom(field);
        resultDefinition.setId( field.getId() );
        resultDefinition.setName( field.getName() );
        resultDefinition.setStandaloneClassName( field.getStandaloneClassName() );
        resultDefinition.setAnnotatedId( field.isAnnotatedId() );
        resultDefinition.setModelName( field.getModelName() );
        resultDefinition.setBoundPropertyName( field.getBoundPropertyName() );

        removeField(field.getId());

        content.getDefinition().getFields().add(resultDefinition);

        return resultDefinition;
    }

    public void removeDataHolder(String holderName, LayoutTemplate layout) {
        getFormDefinition().removeDataHolder(holderName);
        for (FieldDefinition field : getFormDefinition().getFields()) {
            if (holderName.equals(field.getModelName())) {
                field.setName( generateUnbindedFieldName( field ) );
                field.setModelName("");
                field.setBoundPropertyName("");
            }
        }
        for (Iterator<FieldDefinition> it = availableFields.values().iterator(); it.hasNext();) {
            FieldDefinition field = it.next();
            if ( field.getModelName().equals( holderName ) ) {
                it.remove();
            }
        }
    }

    public String generateUnbindedFieldName( FieldDefinition field ) {
        return FieldManager.UNBINDED_FIELD_NAME_PREFFIX + field.getId();
    }

    public List<DraggableFieldComponent> getBaseFieldsDraggables() {
        return draggableFieldComponents;
    }

    public boolean hasBindedFields(DataHolder dataHolder) {
        for (FieldDefinition field : getFormDefinition().getFields()) {
            if (dataHolder.getName().equals(field.getModelName())) {
                return true;
            }
        }
        return false;
    }
}
