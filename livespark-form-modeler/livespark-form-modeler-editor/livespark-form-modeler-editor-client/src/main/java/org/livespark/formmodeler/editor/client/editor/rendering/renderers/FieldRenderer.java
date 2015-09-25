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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.TextBox;
import org.livespark.formmodeler.editor.client.editor.rendering.FieldDefinitionPropertiesModal;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.properties.editor.client.PropertyEditorWidget;
import org.uberfire.ext.properties.editor.model.PropertyEditorCategory;
import org.uberfire.ext.properties.editor.model.PropertyEditorEvent;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorType;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by pefernan on 9/21/15.
 */
public abstract class FieldRenderer<F extends FieldDefinition> {

    @Inject
    protected PropertyEditorWidget editorWidget;

    protected F field;

    protected Path path;

    public abstract String getName();

    public abstract IsWidget renderWidget();

    public abstract String getSupportedFieldDefinition();

    protected abstract List<PropertyEditorFieldInfo> getCustomFieldProperties();

    public F getField() {
        return field;
    }

    public void setField(F field) {
        this.field = field;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public IsWidget getDragWidget() {
        TextBox textBox = GWT.create(TextBox.class);

        textBox.setReadOnly( true );
        if ( field != null ) {
            textBox.setPlaceholder( field.getBoundPropertyName() );
        } else {
            textBox.setPlaceholder( getName() );
        }

        return textBox;
    }

    public void loadFieldProperties( final FieldDefinitionPropertiesModal modal ) {
        editorWidget.handle( new PropertyEditorEvent( field.getName(), generatePropertyEditorCategory() ) );
        modal.addPropertiesEditor( editorWidget );
    }

    protected PropertyEditorCategory generatePropertyEditorCategory() {
        PropertyEditorCategory fieldProperties = new PropertyEditorCategory( FieldProperties.INSTANCE.generalSettings() );

        fieldProperties.withField( new PropertyEditorFieldInfo( FieldProperties.INSTANCE.label(), String.valueOf( field.getLabel() ), PropertyEditorType.TEXT ) {
            @Override
            public void setCurrentStringValue( final String currentStringValue ) {
                super.setCurrentStringValue( currentStringValue );
                field.setLabel(currentStringValue);
            }
        } );

        List<PropertyEditorFieldInfo> customProperties = getCustomFieldProperties();
        if (customProperties != null ){
            for ( PropertyEditorFieldInfo field : customProperties ) {
                fieldProperties.withField( field );
            }
        }

        fieldProperties.withField( new PropertyEditorFieldInfo( FieldProperties.INSTANCE.required(), String.valueOf( field.getRequired() ), PropertyEditorType.BOOLEAN ) {
            @Override
            public void setCurrentStringValue( final String currentStringValue ) {
                super.setCurrentStringValue( currentStringValue );
                field.setRequired(Boolean.valueOf(currentStringValue));
            }
        } );

        // If the field has the ID annotation it has to be always readonly
        if (!field.isAnnotatedId()) {
            fieldProperties.withField(new PropertyEditorFieldInfo(FieldProperties.INSTANCE.readonly(), String.valueOf(field.getReadonly()), PropertyEditorType.BOOLEAN) {
                @Override
                public void setCurrentStringValue(final String currentStringValue) {
                    super.setCurrentStringValue(currentStringValue);
                    field.setReadonly(Boolean.valueOf(currentStringValue));
                }
            });
        } else {
            field.setReadonly(true);
        }

        return fieldProperties;
    }

}
