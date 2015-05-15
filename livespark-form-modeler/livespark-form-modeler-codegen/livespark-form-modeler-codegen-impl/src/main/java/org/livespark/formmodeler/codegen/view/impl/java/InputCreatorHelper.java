package org.livespark.formmodeler.codegen.view.impl.java;

/**
 * Created by pefernan on 4/28/15.
 */
public interface InputCreatorHelper {
    String getSupportedFieldType();
    boolean isInjectable();
    String getInputWidget();
    String getInitLiteral();

    String getReadonlyMethod( String fieldName, String readonlyParam );
}
