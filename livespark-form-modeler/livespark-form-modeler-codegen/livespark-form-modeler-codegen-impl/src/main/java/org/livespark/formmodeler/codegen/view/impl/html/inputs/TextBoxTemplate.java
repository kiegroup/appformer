package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import org.livespark.formmodeler.model.impl.TextBoxFieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public class TextBoxTemplate extends AbstractInputTemplateProvider {

    @Override
    public String getSupportedFieldType() {
        return TextBoxFieldDefinition.class.getName();
    }
}
