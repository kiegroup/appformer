package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import org.livespark.formmodeler.model.impl.FloatBoxFieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public class FloatBoxTemplate extends AbstractInputTemplateProvider {

    @Override
    public String getSupportedFieldType() {
        return FloatBoxFieldDefinition.class.getName();
    }
}
