package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import org.livespark.formmodeler.model.impl.IntBoxFieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public class IntBoxTemplate extends AbstractInputTemplateProvider {

    @Override
    public String getSupportedFieldType() {
        return IntBoxFieldDefinition.class.getName();
    }
}
