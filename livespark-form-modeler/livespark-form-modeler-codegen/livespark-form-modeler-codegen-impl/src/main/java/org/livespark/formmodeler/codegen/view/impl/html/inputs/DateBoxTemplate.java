package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import org.livespark.formmodeler.model.impl.DateBoxFieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public class DateBoxTemplate extends AbstractInputTemplateProvider {

    @Override
    public String getSupportedFieldType() {
        return DateBoxFieldDefinition.class.getName();
    }
}
