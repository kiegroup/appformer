/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.formmodeler.codegen.view.impl.html;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListView;
import org.livespark.formmodeler.codegen.view.impl.html.util.HTMLTemplateFormatter;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.impl.relations.EntityRelationField;

@ListView
@ApplicationScoped
public class ListHTMLTemplateSourceGenerator implements FormHTMLTemplateSourceGenerator {
    private static final String LIST_WIDGET_FIELD_NAME = "items";
    private static final String LIST_WIDGET_BUTTON_NAME = "create";

    private static final String DELETE_BUTTON_NAME = "delete";
    private static final String EDIT_BUTTON_NAME = "edit";

    @Inject
    private HTMLTemplateFormatter formatter;

    @Override
    public String generateHTMLTemplateSource( SourceGenerationContext context ) {
        final StringBuilder builder = new StringBuilder();
        builder.append("<div>");

        builder.append("<div class=\"create-container\">")
                .append( "<button class=\"btn btn-primary\" data-field=\"")
                .append( LIST_WIDGET_BUTTON_NAME )
                .append( "\">Create</button>" )
                .append("<br/>")
                .append("<br/>")
                .append("</div>");

        builder.append("<div>")
                .append( "<table class=\"table blackened\">" )
                .append("<thead>");

        builder.append( "<tr>" );
        for ( FieldDefinition field : context.getFormDefinition().getFields() ) {
            if ( !isSupported( field ) ) continue;
            String label = field.getLabel();
            if ( label == null || "".equals( label ) ) {
                label = field.getName();
            }

            builder.append( "<th>" )
                    .append( label )
                    .append( "</th>" );
        }
        builder.append( "<th></th>" );
        builder.append( "</tr>" );

        final String bodyId = context.getListTBodyId();
        builder.append( "</thead>" )
                .append( "<tbody id=\"" )
                .append( bodyId )
                .append( "\" data-field=\"" )
                .append( LIST_WIDGET_FIELD_NAME )
                .append("\">");

        final String rowId = context.getListItemRowId();
        builder.append( "<tr valign=\"top\" class=\"item\" id=\"")
                .append( rowId )
                .append("\">");
        for ( FieldDefinition field : context.getFormDefinition().getFields() ) {
            if ( !isSupported( field ) ) continue;
            builder.append( "<td data-field=\"" )
                    .append( field.getName() )
                    .append( "\"></td>" );
        }

        builder.append("<td style=\"width:1px; white-space:nowrap;\">");

        builder.append( "<button class=\"btn btn-primary\" data-field=\"" )
                .append( EDIT_BUTTON_NAME )
                .append( "\">Edit</button>" );
        builder.append( "<button class=\"btn btn-primary\" data-field=\"" )
                .append( DELETE_BUTTON_NAME )
                .append( "\">Delete</button>" );

        builder.append("</td>");

        builder.append( "</tr>" );

        builder.append( "</tbody>")
                .append( "</table>" )
                .append( "</div>")
                .append("</div>");

        return formatter.formatHTMLCode( builder.toString() );
    }

    protected boolean isSupported(FieldDefinition definition) {
        return !(definition instanceof EntityRelationField );
    }
}
