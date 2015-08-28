package org.livespark.formmodeler.editor.client.editor.events;

/**
 * Created by pefernan on 8/28/15.
 */
public class FieldRemovedEvent extends FormEditorEvent {

    public FieldRemovedEvent( String formId, String fieldName ) {
        super( formId, fieldName );
    }
}
