package org.livespark.formmodeler.codegen.view.impl.html;

import javax.enterprise.context.ApplicationScoped;

import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListView;
import org.livespark.formmodeler.model.FieldDefinition;

@ListView
@ApplicationScoped
public class ListHTMLTemplateSourceGenerator implements FormHTMLTemplateSourceGenerator {

    private static final String LIST_WIDGET_FIELD_NAME = "items";
    private static final String LIST_WIDGET_BUTTON_NAME = "create";

    private static final String DELETE_BUTTON_NAME = "delete";
    private static final String EDIT_BUTTON_NAME = "edit";

    @Override
    public String generateHTMLTemplateSource( SourceGenerationContext context ) {
        final StringBuilder builder = new StringBuilder();
        builder.append( "<div>" );

        builder.append( "<button data-field=\"")
               .append( LIST_WIDGET_BUTTON_NAME )
               .append( "\">Create</button>" );

        builder.append( "<table class=\"table blackened\">" )
               .append( "<thead>" );

        builder.append( "<tr>" );
        for ( FieldDefinition<?> field : context.getFormDefinition().getFields() ) {
            String label = field.getLabel();
            if ( label == null || "".equals( label ) ) {
                label = field.getName();
            }

            builder.append( "<th>" )
                   .append( label )
                   .append( "</th>" );
        }
        builder.append( "</tr>" );

        final String bodyId = context.getListTBodyId();
        builder.append( "</thead>" )
               .append( "<tbody id=\"" )
               .append( bodyId )
               .append( "\" data-field=\"" )
               .append( LIST_WIDGET_FIELD_NAME )
               .append( "\">" );

        final String rowId = context.getListItemRowId();
        builder.append( "<tr valign=\"top\" class=\"item\" id=\"")
               .append( rowId )
               .append( "\">" );
        for ( FieldDefinition<?> field : context.getFormDefinition().getFields() ) {
            builder.append( "<td data-field=\"" )
                   .append( field.getName() )
                   .append( "\"></td>" );
        }

        builder.append( "<td><button class=\"btn btn-primary\" data-field=\"" )
               .append( EDIT_BUTTON_NAME )
               .append( "\">Edit</button></td>" );
        builder.append( "<td><button class=\"btn btn-primary\" data-field=\"" )
               .append( DELETE_BUTTON_NAME )
               .append( "\">Delete</button></td>" );

        builder.append( "</tr>" );

        builder.append( "</tbody>")
               .append( "</table>" )
               .append( "</div>");

        return builder.toString();
    }
}
