package org.livespark.formmodeler.codegen.model;

import org.livespark.formmodeler.codegen.SourceGenerationContext;

/**
 * Created by pefernan on 4/27/15.
 */
public interface FormModelSourceGenerator {
    public String generateFormModelSource(SourceGenerationContext context);
}
