package org.livespark.formmodeler.codegen.view.impl.java;

/**
 * Created by pefernan on 4/28/15.
 */
public interface InputCreatorHelper {
    String getSupportedFieldType();
    boolean isInputInjectable();
    boolean isDisplayInjectable();
    String getDisplayInitLiteral();
    String getInputWidget();
    String getDisplayWidget();
    String getInputInitLiteral();

    String getReadonlyMethod( String fieldName, String readonlyParam );
}
