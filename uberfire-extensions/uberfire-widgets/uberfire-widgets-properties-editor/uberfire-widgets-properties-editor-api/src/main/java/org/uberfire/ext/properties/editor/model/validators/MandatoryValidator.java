package org.uberfire.ext.properties.editor.model.validators;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class MandatoryValidator implements PropertyFieldValidator{

    @Override
    public boolean validate(Object value) {
        return value != null && !value.toString().trim().isEmpty();
    }

    @Override
    public String getValidatorErrorMessage() {
        return "Field is mandatory.";
    }

}
