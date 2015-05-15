package org.livespark.formmodeler.codegen.view.impl.java.inputs;

import org.livespark.formmodeler.model.impl.TextAreaFieldDefinition;

/**
 * Created by pefernan on 4/28/15.
 */
public class TextAreaHelper extends AbstractInputCreatorHelper {

    @Override
    public String getSupportedFieldType() {
        return TextAreaFieldDefinition.class.getName();
    }

    @Override
    public String getInputWidget() {
        return "com.github.gwtbootstrap.client.ui.TextArea";
    }
}
