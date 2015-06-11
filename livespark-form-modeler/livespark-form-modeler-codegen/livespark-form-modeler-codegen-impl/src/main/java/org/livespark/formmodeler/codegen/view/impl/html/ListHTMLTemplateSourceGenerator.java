package org.livespark.formmodeler.codegen.view.impl.html;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListView;

@ListView
@ApplicationScoped
public class ListHTMLTemplateSourceGenerator implements FormHTMLTemplateSourceGenerator {

    private static final String LIST_WIDGET_FIELD_NAME = "items";
    private static final String LIST_WIDGET_BUTTON_NAME = "create";

    @Override
    public String generateHTMLTemplateSource( SourceGenerationContext context ) {
        return "<div><div data-field=\"" + LIST_WIDGET_BUTTON_NAME + "\"></div><div data-field=\"" + LIST_WIDGET_FIELD_NAME + "\"></div></div>";
    }

}
