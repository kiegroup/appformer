package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import org.livespark.formmodeler.model.impl.DoubleBoxFieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public class DoubleBoxTemplate extends AbstractInputTemplateProvider {

    @Override
    public String getSupportedFieldType() {
        return DoubleBoxFieldDefinition.class.getName();
    }
}
