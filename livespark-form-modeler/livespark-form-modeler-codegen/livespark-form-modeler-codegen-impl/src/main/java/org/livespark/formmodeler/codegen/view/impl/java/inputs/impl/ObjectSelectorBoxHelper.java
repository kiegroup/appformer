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

import javax.inject.Inject;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.kie.workbench.common.forms.model.impl.basic.selectors.SelectorOption;
import org.kie.workbench.common.forms.model.impl.relations.ObjectSelectorFieldDefinition;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

public class ObjectSelectorBoxHelper<T extends  SelectorOption> extends AbstractInputCreatorHelper<ObjectSelectorFieldDefinition>
        implements RequiresCustomCode<ObjectSelectorFieldDefinition> {


    public static final String LISTBOX_RENDERER_CLASSNAME = "org.livespark.formmodeler.rendering.client.view.util.ObjectValuesRenderer";

    public static final String REMOTE_VALUES_PROVIDER =  "org.livespark.formmodeler.rendering.client.view.util.RemoteListBoxValuesProvider";
    public static final String INSTANCE_PROVIDER_SUFFIX = "Provider";

    public static final String LOAD_LIST_VALUES_METHOD_NAME = "loadListValues_";

    public static final String WIDGET = "org.gwtbootstrap3.client.ui.ValueListBox";

    @Override
    public boolean isInputInjectable() {
        return false;
    }

    @Override
    public String getSupportedFieldTypeCode() {
        return ObjectSelectorFieldDefinition.CODE;
    }

    @Override
    public String getInputWidget( ObjectSelectorFieldDefinition fieldDefinition ) {
        return getClassName( WIDGET ) + "<" + getClassName( fieldDefinition.getStandaloneClassName() ) + ">";
    }

    @Override
    public String getInputInitLiteral( SourceGenerationContext context, ObjectSelectorFieldDefinition fieldDefinition ) {

        String mask = "";

        if ( isEvaluableMask( fieldDefinition.getMask() ) ) {
            mask = "\"" + fieldDefinition.getMask() + "\"";
        }

        StringBuffer buffer = new StringBuffer( "new ValueListBox<" )
                .append( getClassName( fieldDefinition.getStandaloneClassName() ) )
                .append( ">( new " )
                .append( getClassName( LISTBOX_RENDERER_CLASSNAME ) )
                .append( "( " )
                .append( mask )
                .append( " ) );" );

        return buffer.toString();
    }

    @Override
    public void addCustomCode( ObjectSelectorFieldDefinition fieldDefinition, SourceGenerationContext context, JavaClassSource viewClass ) {

        String typeName = getClassName( fieldDefinition.getStandaloneClassName() );

        viewClass.addImport( WIDGET );
        viewClass.addImport( LISTBOX_RENDERER_CLASSNAME );
        viewClass.addImport( fieldDefinition.getStandaloneClassName() );

        String dataProvider = typeName + INSTANCE_PROVIDER_SUFFIX;

        if ( !viewClass.hasNestedType( dataProvider ) ) {
            String sharedPackage = context.getSharedPackage().getPackageName();

            String restServiceName = typeName + SourceGenerationContext.REST_SERVICE_SUFFIX;

            viewClass.addImport( REMOTE_VALUES_PROVIDER );
            viewClass.addImport( sharedPackage + "." + restServiceName );

            JavaClassSource instanceProvider = Roaster.create( JavaClassSource.class );

            instanceProvider.setName( dataProvider );
            instanceProvider.setSuperType( getClassName( REMOTE_VALUES_PROVIDER ) + "<" + typeName + ">" );

            instanceProvider.addMethod()
                    .setName(  "getRemoteServiceClass" )
                    .setProtected()
                    .setReturnType( "Class<" + restServiceName + ">" )
                    .setBody( "return " + restServiceName + ".class;" )
                    .addAnnotation( Override.class);

            viewClass.addNestedType( instanceProvider ).setProtected();
        }

        StringBuffer body = new StringBuffer();

        body.append( "new " ).append( dataProvider ).append( "().loadValues( " ).append( fieldDefinition.getName() ).append( " );" );

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

    protected boolean isEvaluableMask( String mask ) {
        int countOpeners = org.apache.commons.lang3.StringUtils.countMatches( mask, "{" );

        int countClosers = org.apache.commons.lang3.StringUtils.countMatches( mask, "}" );

        return countOpeners != 0 && countOpeners == countClosers;
    }

    @Override
    public String getReadonlyMethod( String fieldName, String readonlyParam ) {
        return fieldName + ".setEnabled( !" + readonlyParam + " );";
    }

    private String getClassName( String qualifiedClassName ) {
        if ( qualifiedClassName == null || qualifiedClassName.isEmpty() || qualifiedClassName.indexOf( "." ) == -1) {
            return "";
        }
        return qualifiedClassName.substring( qualifiedClassName.lastIndexOf( "." ) + 1 );
    }
}
