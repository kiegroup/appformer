package org.livespark.formmodeler.editor.client.editor.events;

/**
 * Created by pefernan on 8/6/15.
 */
public class FormContextRequest extends FormEditorEvent {

    public FormContextRequest(String formId, String fieldName) {
        super( formId, fieldName );
    }
}
