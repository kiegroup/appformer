package org.livespark.formmodeler.codegen.view.impl.html;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListView;
import org.uberfire.java.nio.base.NotImplementedException;

@ListView
@ApplicationScoped
public class MVELListHTMLTemplateSourceGenerator implements FormHTMLTemplateSourceGenerator {

    @Override
    public String generateHTMLTemplateSource( SourceGenerationContext context ) {
        throw new NotImplementedException();
    }

}
