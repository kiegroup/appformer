/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.codegen.view.impl.java.inputs.impl;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.PropertySource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresExtraFields;
import org.livespark.formmodeler.model.impl.basic.selectors.ListBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.selectors.SelectorOption;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

public class ListBoxHelper extends AbstractInputCreatorHelper<ListBoxFieldDefinition> implements RequiresCustomCode<ListBoxFieldDefinition>, RequiresExtraFields<ListBoxFieldDefinition> {

    public static final String LOAD_LIST_VALUES_METHOD_NAME = "loadListValues_";
    public static final String LISTBOX_RENDERER_SUFFIX = "_ListValueRenderer";
    public static final String LISTBOX_STRING_RENDERER_NAME = "StringListBoxRenderer";
    public static final String LISTBOX_STRING_RENDERER_CLASSNAME = "org.livespark.formmodeler.rendering.client.view.util.StringListBoxRenderer";

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getSupportedFieldTypeCode() {
        return ListBoxFieldDefinition.CODE;
    }

    @Override
    public String getInputWidget( ListBoxFieldDefinition fieldDefinition ) {
        return "org.gwtbootstrap3.client.ui.ValueListBox";
    }

    @Override
    public String getInputInitLiteral( SourceGenerationContext context, ListBoxFieldDefinition fieldDefinition ) {
        return "new ValueListBox<String>( " + fieldDefinition.getName() + LISTBOX_RENDERER_SUFFIX + " );";
    }

    @Override
    public void addCustomCode( ListBoxFieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass ) {

        viewClass.addImport( JAVA_UTIL_MAP_CLASSNAME );
        viewClass.addImport( JAVA_UTIL_HASHMAP_CLASSNAME );

        StringBuffer body = new StringBuffer();

        body.append( JAVA_UTIL_MAP_NAME )
                .append( "<String, String> values = new " )
                .append( JAVA_UTIL_HASHMAP_NAME )
                .append( "<String, String>();" );

        String defaultValue = null;
        for ( SelectorOption option : fieldDefinition.getOptions() ) {
            body.append( "values.put( \"" )
                    .append( option.getValue() )
                    .append( "\", \"" )
                    .append( option.getText() )
                    .append( "\" );" );
            if ( option.getDefaultValue() ) defaultValue = option.getValue();
        }

        if ( defaultValue != null ) {
            body.append( "if (" ).append( IS_NEW_MODEL_METHOD_CALL).append( ") {" );
            body.append( fieldDefinition.getName() )
                    .append( ".setValue( \"" )
                    .append( defaultValue )
                    .append( "\", true );" );
            body.append( "}" );
        }
        body.append( fieldDefinition.getName() )
                .append( LISTBOX_RENDERER_SUFFIX )
                .append( ".setValues( values );" );
        body.append( fieldDefinition.getName() )
                .append( ".setAcceptableValues( values.keySet() );" );

        String initListMethodName = LOAD_LIST_VALUES_METHOD_NAME + fieldDefinition.getName();

        viewClass.addMethod()
                .setName( initListMethodName )
                .setReturnTypeVoid()
                .setProtected()
                .setBody( body.toString() );

        MethodSource<JavaClassSource> beforeDisplayMethod = viewClass.getMethod( BEFORE_DISPLAY_METHOD, void.class );
        body = new StringBuffer( beforeDisplayMethod.getBody() == null ? "" : beforeDisplayMethod.getBody() );
        body.append( initListMethodName ).append( "();" );
        beforeDisplayMethod.setBody( body.toString() );
    }

    @Override
    public String getReadonlyMethod( String fieldName, String readonlyParam ) {
        return fieldName + ".setEnabled( !" + readonlyParam + " );";
    }

    @Override
    public String getExtraReadOnlyCode( ListBoxFieldDefinition fieldDefinition, String readonlyParam ) {
        return "";
    }

    @Override
    public void addExtraFields( JavaClassSource viewClass, ListBoxFieldDefinition fieldDefinition ) {
        viewClass.addImport( LISTBOX_STRING_RENDERER_CLASSNAME );
        PropertySource<JavaClassSource> property = viewClass.addProperty( LISTBOX_STRING_RENDERER_NAME, fieldDefinition.getName() + LISTBOX_RENDERER_SUFFIX );

        FieldSource<JavaClassSource> field = property.getField();
        field.setPrivate();
        field.setLiteralInitializer( "new " + LISTBOX_STRING_RENDERER_NAME + "();" );

        property.removeAccessor();
        property.removeMutator();
    }
}
