package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import org.livespark.formmodeler.model.impl.ShortBoxFieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public class ShortBoxTemplate extends AbstractInputTemplateProvider {

    @Override
    public String getSupportedFieldType() {
        return ShortBoxFieldDefinition.class.getName();
    }
}
