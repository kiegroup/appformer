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

package org.livespark.formmodeler.codegen.view.impl.java.inputs;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.selectors.ListBoxFieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.selectors.SelectorOption;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

public class ListBoxHelper extends AbstractInputCreatorHelper implements RequiresCustomCode {

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getSupportedFieldTypeCode() {
        return ListBoxFieldDefinition._CODE;
    }

    @Override
    public String getInputWidget() {
        return "org.gwtbootstrap3.client.ui.ValueListBox";
    }

    @Override
    public String getInputInitLiteral( SourceGenerationContext context, FieldDefinition fieldDefinition ) {
        return "new ValueListBox<String>( new " + LISTBOX_STRING_RENDERER_NAME + "() );";
    }

    @Override
    public void addCustomCode( FieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass ) {
        viewClass.addImport( LISTBOX_STRING_RENDERER_CLASSNAME );

        MethodSource<JavaClassSource> doInitMethod = viewClass.getMethod( "doInit", void.class );
        if ( doInitMethod != null ) {

            viewClass.addImport( JAVA_UTIL_LIST_CLASSNAME );
            viewClass.addImport( JAVA_UTIL_ARRAYLIST_CLASSNAME );

            StringBuffer body = new StringBuffer( doInitMethod.getBody() );

            String valuesFieldName = fieldDefinition.getName() + "ListValues";

            body.append( "List<String> " )
                .append( valuesFieldName )
                .append( " = new ArrayList<String>();" );

            String defaultValue = null;

            for ( SelectorOption option : ((ListBoxFieldDefinition)fieldDefinition).getOptions() ) {
                body.append( valuesFieldName ).append( ".add( \"" )
                    .append( option.getValue() )
                    .append( "\" );" );
                if ( option.getDefaultValue() ) defaultValue = option.getValue();
            }

            body.append( fieldDefinition.getName() )
                .append( ".setAcceptableValues( " )
                .append( valuesFieldName )
                .append( " );" );

            if ( defaultValue != null ) {
                body.append( fieldDefinition.getName() )
                    .append( ".setValue( \"" )
                    .append( defaultValue )
                    .append( "\", true );" );
            }

            doInitMethod.setBody( body.toString() );
        }
    }

    @Override
    public String getReadonlyMethod( String fieldName, String readonlyParam ) {
        return fieldName + ".setEnabled( !" + readonlyParam + " );";
    }
}
