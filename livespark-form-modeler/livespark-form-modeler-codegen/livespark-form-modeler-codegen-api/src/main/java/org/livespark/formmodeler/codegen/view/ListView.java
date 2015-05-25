package org.livespark.formmodeler.codegen.view;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;

/**
 * Used for implementations of {@link FormJavaTemplateSourceGenerator} or {@link FormHTMLTemplateSourceGenerator} that
 * generate a list view for viewing, updating, and deleting entries.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface ListView {

}
