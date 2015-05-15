package org.livespark.formmodeler.codegen;

import org.livespark.formmodeler.model.FormDefinition;
import org.uberfire.backend.vfs.Path;

/**
 * Created by pefernan on 5/5/15.
 */
public interface FormSourcesGenerator {

    void generateFormSources( FormDefinition form, Path resourcePath );
}
