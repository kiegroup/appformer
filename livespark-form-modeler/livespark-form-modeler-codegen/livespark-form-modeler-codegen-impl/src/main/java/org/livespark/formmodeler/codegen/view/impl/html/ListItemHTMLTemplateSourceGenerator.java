package org.livespark.formmodeler.codegen.view.impl.html;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListItemView;
import org.livespark.formmodeler.model.FieldDefinition;

@ListItemView
@ApplicationScoped
public class ListItemHTMLTemplateSourceGenerator implements FormHTMLTemplateSourceGenerator {

    @Override
    public String generateHTMLTemplateSource( SourceGenerationContext context ) {
        final StringBuilder builder = new StringBuilder();

        builder.append( "<div>\n" );

        for ( FieldDefinition<?> field : context.getFormDefinition().getFields() ) {
            builder.append( "  <span data-field=\"" )
                   .append( field.getName() )
                   .append( "\">\n" );
        }

        builder.append( "</div>" );

        return builder.toString();
    }

}
