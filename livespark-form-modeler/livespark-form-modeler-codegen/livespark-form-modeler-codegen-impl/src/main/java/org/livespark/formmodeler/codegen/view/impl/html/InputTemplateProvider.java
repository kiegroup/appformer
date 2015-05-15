package org.livespark.formmodeler.codegen.view.impl.html;

import java.io.InputStream;

/**
 * Created by pefernan on 4/29/15.
 */
public interface InputTemplateProvider {
    String getSupportedFieldType();
    InputStream getTemplateInputStream();
}
