package org.livespark.formmodeler.editor.client.editor.events;

import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.uberfire.backend.vfs.Path;

/**
 * Created by pefernan on 8/6/15.
 */
public class FormFieldResponse<F extends  FieldDefinition> extends FormEditorEvent {
    protected F field;
    protected Path path;

    public FormFieldResponse( String formId, String fieldName, F field, Path path ) {
        super( formId, fieldName );
        this.field = field;
        this.path = path;
    }

    public F getField() {
        return field;
    }

    public void setField( F field ) {
        this.field = field;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
