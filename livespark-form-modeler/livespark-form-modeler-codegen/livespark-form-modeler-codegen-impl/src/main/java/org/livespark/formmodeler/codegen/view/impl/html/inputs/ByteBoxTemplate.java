package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import org.livespark.formmodeler.model.impl.ByteBoxFieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public class ByteBoxTemplate extends AbstractInputTemplateProvider {

    @Override
    public String getSupportedFieldType() {
        return ByteBoxFieldDefinition.class.getName();
    }
}
