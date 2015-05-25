package org.livespark.formmodeler.codegen.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;

/**
 * Used for implementations of {@link FormJavaTemplateSourceGenerator} that generate a JAX-RS interface.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestApi {

}
