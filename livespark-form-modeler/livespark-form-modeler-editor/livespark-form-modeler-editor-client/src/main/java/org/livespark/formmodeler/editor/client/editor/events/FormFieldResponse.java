package org.livespark.formmodeler.editor.client.editor.events;

import org.livespark.formmodeler.editor.model.FieldDefinition;

/**
 * Created by pefernan on 8/6/15.
 */
public class FormFieldResponse<F extends  FieldDefinition> extends FormEditorEvent {
    protected F field;

    public F getField() {
        return field;
    }

    public void setField( F field ) {
        this.field = field;
    }

    public FormFieldResponse( String path, String fieldName, F field ) {
        super( path, fieldName );
        this.field = field;

    }
}
