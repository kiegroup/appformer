package org.livespark.formmodeler.codegen.view.impl.java.inputs;

import org.livespark.formmodeler.model.impl.DateBoxFieldDefinition;

/**
 * Created by pefernan on 4/28/15.
 */
public class DateBoxHelper extends AbstractInputCreatorHelper {

    @Override
    public String getSupportedFieldType() {
        return DateBoxFieldDefinition.class.getName();
    }

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getInputWidget() {
        return "com.google.gwt.user.datepicker.client.DatePicker";
    }

    @Override
    public String getInputInitLiteral() {
        return "new DatePicker()";
    }
}
