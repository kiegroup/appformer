package org.livespark.formmodeler.codegen.view.impl.html;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListItemView;
import org.livespark.formmodeler.codegen.view.ListView;

@ListView
@ListItemView // TODO make separate ListItemView implementation
@ApplicationScoped
public class MVELListHTMLTemplateSourceGenerator implements FormHTMLTemplateSourceGenerator {

    @Override
    public String generateHTMLTemplateSource( SourceGenerationContext context ) {
        return "Not yet implemented";
    }

}
