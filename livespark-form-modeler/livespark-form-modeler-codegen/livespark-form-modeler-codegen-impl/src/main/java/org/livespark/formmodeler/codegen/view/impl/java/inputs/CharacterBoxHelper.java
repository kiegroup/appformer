package org.livespark.formmodeler.codegen.view.impl.java.inputs;

import org.livespark.formmodeler.model.impl.CharacterBoxFieldDefinition;

/**
 * Created by pefernan on 4/28/15.
 */
public class CharacterBoxHelper extends AbstractInputCreatorHelper {

    @Override
    public String getSupportedFieldType() {
        return CharacterBoxFieldDefinition.class.getName();
    }

    public String getInputWidget() {
        return "com.github.gwtbootstrap.client.ui.TextBox";
    }
}
