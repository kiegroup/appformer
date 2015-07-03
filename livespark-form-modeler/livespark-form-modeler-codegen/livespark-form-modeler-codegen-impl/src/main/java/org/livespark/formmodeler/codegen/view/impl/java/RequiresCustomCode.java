package org.livespark.formmodeler.codegen.view.impl.java;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.model.FieldDefinition;

/**
 * Created by pefernan on 7/2/15.
 */
public interface RequiresCustomCode {

    public void addCustomCode( FieldDefinition definition, SourceGenerationContext context, JavaClassSource parentClass );

}
