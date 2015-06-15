package org.livespark.formmodeler.codegen.view.impl.java.inputs;

import org.livespark.formmodeler.model.impl.CheckBoxFieldDefinition;

/**
 * Created by pefernan on 4/28/15.
 */
public class CheckBoxHelper extends AbstractInputCreatorHelper {

    @Override
    public String getSupportedFieldType() {
        return CheckBoxFieldDefinition.class.getName();
    }

    @Override
    public String getInputWidget() {
        return "com.github.gwtbootstrap.client.ui.CheckBox";
    }

    @Override
    public String getReadonlyMethod( String fieldName, String readonlyParam ) {
        return fieldName + ".setEnabled( !" + readonlyParam + ");";
    }

    @Override
    public String getDisplayWidget() {
        return "com.github.gwtbootstrap.client.ui.CheckBox";
    }
}
