package org.livespark.formmodeler.editor.client.editor.events;

/**
 * Created by pefernan on 8/28/15.
 */
public class FieldDroppedEvent extends FormEditorEvent {

    public FieldDroppedEvent( String formId, String fieldName ) {
        super( formId, fieldName );
    }
}
