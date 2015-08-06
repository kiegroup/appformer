package org.livespark.formmodeler.editor.client.editor.events;

/**
 * Created by pefernan on 8/6/15.
 */
public class FormFieldRequest extends FormEditorEvent {

    public FormFieldRequest(String path, String fieldName) {
        super( path, fieldName );
    }
}
