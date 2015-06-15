package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import org.livespark.formmodeler.model.impl.BigIntegerBoxFieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public class BigIntegerBoxTemplate extends AbstractInputTemplateProvider {

    @Override
    public String getSupportedFieldType() {
        return BigIntegerBoxFieldDefinition.class.getName();
    }
}
