package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import org.livespark.formmodeler.model.impl.LongBoxFieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public class LongBoxTemplate extends AbstractInputTemplateProvider {

    @Override
    public String getSupportedFieldType() {
        return LongBoxFieldDefinition.class.getName();
    }
}
