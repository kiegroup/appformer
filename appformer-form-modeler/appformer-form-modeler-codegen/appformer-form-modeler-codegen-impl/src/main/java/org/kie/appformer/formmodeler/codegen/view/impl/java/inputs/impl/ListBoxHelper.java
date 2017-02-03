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

package org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl;

import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.*;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.PropertySource;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.kie.appformer.formmodeler.codegen.view.impl.java.RequiresExtraFields;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.SelectorOption;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.listBox.definition.ListBoxBaseDefinition;

public class ListBoxHelper<T extends SelectorOption> extends AbstractInputCreatorHelper<ListBoxBaseDefinition<T>>
        implements RequiresCustomCode<ListBoxBaseDefinition<T>>, RequiresExtraFields<ListBoxBaseDefinition<T>> {

    public static final String LOAD_LIST_VALUES_METHOD_NAME = "loadListValues_";
    public static final String LISTBOX_RENDERER_SUFFIX = "_ListValueRenderer";
    public static final String LISTBOX_STRING_RENDERER_NAME = "StringListBoxRenderer";
    public static final String LISTBOX_STRING_RENDERER_CLASSNAME = "org.kie.appformer.formmodeler.rendering.client.view.util.StringListBoxRenderer";

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getSupportedFieldTypeCode() {
        return ListBoxBaseDefinition.FIELD_TYPE.getTypeName();
    }

    @Override
    public String getInputWidget( final ListBoxBaseDefinition fieldDefinition ) {
        return "org.gwtbootstrap3.client.ui.ValueListBox";
    }

    @Override
    public String getInputInitLiteral( final SourceGenerationContext context, final ListBoxBaseDefinition fieldDefinition ) {
        return "new ValueListBox<String>( " + fieldDefinition.getName() + LISTBOX_RENDERER_SUFFIX + " );";
    }

    @Override
    public void addCustomCode( final ListBoxBaseDefinition<T> fieldDefinition, final SourceGenerationContext context, final JavaClassSource viewClass ) {

        viewClass.addImport( JAVA_UTIL_MAP_CLASSNAME );
        viewClass.addImport( JAVA_UTIL_HASHMAP_CLASSNAME );

        StringBuffer body = new StringBuffer();

        body.append( JAVA_UTIL_MAP_NAME )
                .append( "<String, String> values = new " )
                .append( JAVA_UTIL_HASHMAP_NAME )
                .append( "<String, String>();" );

        String defaultValue = null;
        for ( final T option : fieldDefinition.getOptions() ) {
            body.append( "values.put( \"" )
                    .append( option.getValue() )
                    .append( "\", \"" )
                    .append( option.getText() )
                    .append( "\" );" );
            if ( option.isDefaultValue() ) defaultValue = option.getValue().toString();
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

        final String initListMethodName = LOAD_LIST_VALUES_METHOD_NAME + fieldDefinition.getName();

        viewClass.addMethod()
                .setName( initListMethodName )
                .setReturnTypeVoid()
                .setProtected()
                .setBody( body.toString() );

        final MethodSource<JavaClassSource> beforeDisplayMethod = viewClass.getMethod( BEFORE_DISPLAY_METHOD );
        body = new StringBuffer( beforeDisplayMethod.getBody() == null ? "" : beforeDisplayMethod.getBody() );
        body.append( initListMethodName ).append( "();" );
        beforeDisplayMethod.setBody( body.toString() );
    }

    @Override
    public String getReadonlyMethod( final String fieldName, final String readonlyParam ) {
        return fieldName + ".setEnabled( !" + readonlyParam + " );";
    }

    @Override
    public String getExtraReadOnlyCode( final ListBoxBaseDefinition fieldDefinition, final String readonlyParam ) {
        return "";
    }

    @Override
    public void addExtraFields( final JavaClassSource viewClass,
                                final ListBoxBaseDefinition fieldDefinition,
                                final SourceGenerationContext context ) {
        viewClass.addImport( LISTBOX_STRING_RENDERER_CLASSNAME );
        final PropertySource<JavaClassSource> property = viewClass.addProperty( LISTBOX_STRING_RENDERER_NAME, fieldDefinition.getName() + LISTBOX_RENDERER_SUFFIX );

        final FieldSource<JavaClassSource> field = property.getField();
        field.setPrivate();
        field.setLiteralInitializer( "new " + LISTBOX_STRING_RENDERER_NAME + "();" );

        property.removeAccessor();
        property.removeMutator();
    }
}
