package org.livespark.formmodeler.codegen.view.impl.java.inputs;

import org.livespark.formmodeler.codegen.view.impl.java.InputCreatorHelper;

/**
 * Created by pefernan on 4/28/15.
 */
public abstract class AbstractInputCreatorHelper implements InputCreatorHelper {

    @Override
    public boolean isInputInjectable() {
        return true;
    }

    @Override
    public String getInputInitLiteral() {
        return null;
    }

    @Override
    public boolean isDisplayInjectable() {
        return false;
    }

    @Override
    public String getDisplayInitLiteral() {
        return "com.google.gwt.user.client.DOM.createTD()";
    }

    @Override
    public String getDisplayWidget() {
        return "com.google.gwt.user.client.Element";
    }

    @Override
    public String getReadonlyMethod( String fieldName, String readonlyParam ) {
        return fieldName + ".setReadOnly( " + readonlyParam + ");";
    }
}
