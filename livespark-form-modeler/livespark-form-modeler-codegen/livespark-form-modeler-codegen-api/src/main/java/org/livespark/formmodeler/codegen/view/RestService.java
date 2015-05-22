package org.livespark.formmodeler.codegen.view;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Used for implementations of {@link FormJavaTemplateSourceGenerator} that generate a rest end-point for a form.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestService {

}
