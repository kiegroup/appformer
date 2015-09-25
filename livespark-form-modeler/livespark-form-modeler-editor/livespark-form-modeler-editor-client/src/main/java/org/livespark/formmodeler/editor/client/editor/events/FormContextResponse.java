package org.livespark.formmodeler.editor.client.editor.events;

import org.livespark.formmodeler.editor.client.editor.FormEditorHelper;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.uberfire.backend.vfs.Path;

/**
 * Created by pefernan on 8/6/15.
 */
public class FormContextResponse extends FormEditorEvent {
    protected FormEditorHelper editorHelper;

    public FormContextResponse(String formId, String fieldName, FormEditorHelper formEditorHelper) {
        super(formId, fieldName);
        this.editorHelper = formEditorHelper;
    }

    public FormEditorHelper getEditorHelper() {
        return editorHelper;
    }

    public void setEditorHelper(FormEditorHelper editorHelper) {
        this.editorHelper = editorHelper;
    }
}
