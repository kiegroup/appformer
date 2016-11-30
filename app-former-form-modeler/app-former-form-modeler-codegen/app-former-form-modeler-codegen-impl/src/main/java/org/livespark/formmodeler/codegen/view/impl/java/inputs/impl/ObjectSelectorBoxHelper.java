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
import org.kie.workbench.common.forms.model.impl.basic.selectors.SelectorOption;
import org.kie.workbench.common.forms.model.impl.relations.ObjectSelectorFieldDefinition;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresExtraFields;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.BEFORE_DISPLAY_METHOD;

public class ObjectSelectorBoxHelper<T extends  SelectorOption> extends AbstractInputCreatorHelper<ObjectSelectorFieldDefinition>
        implements RequiresExtraFields<ObjectSelectorFieldDefinition>, RequiresCustomCode<ObjectSelectorFieldDefinition> {


    public static final String FIELD_MASK_SUFFIX = "_fieldMask";

    public static final String WIDGET_CLASSNAME = "org.kie.workbench.common.forms.common.rendering.client.widgets.typeahead.BindableTypeAhead";

    public static final String DATASET_CLASSNAME = "org.livespark.formmodeler.rendering.client.widgets.typeahead.LiveSparkStaticDataset";

    @Override
    public String getSupportedFieldTypeCode() {
        return ObjectSelectorFieldDefinition.CODE;
    }

    @Override
    public String getInputWidget( ObjectSelectorFieldDefinition fieldDefinition ) {
        return getClassName( WIDGET_CLASSNAME ) + "<" + getClassName( fieldDefinition.getStandaloneClassName() ) + ">";
    }

    protected boolean isEvaluableMask( String mask ) {
        int countOpeners = org.apache.commons.lang3.StringUtils.countMatches( mask, "{" );

        int countClosers = org.apache.commons.lang3.StringUtils.countMatches( mask, "}" );

        return countOpeners != 0 && countOpeners == countClosers;
    }

    protected String getMaskFieldName( ObjectSelectorFieldDefinition fieldDefinition ) {
        return fieldDefinition.getName() + FIELD_MASK_SUFFIX;
    }

    @Override
    public void addCustomCode( ObjectSelectorFieldDefinition fieldDefinition,
                               SourceGenerationContext context,
                               JavaClassSource viewClass ) {

        if ( !isEvaluableMask( fieldDefinition.getMask() ) ) {
            throw new IllegalArgumentException( "Unsupported mask format" );
        }

        viewClass.addImport( DATASET_CLASSNAME );
        viewClass.addImport( WIDGET_CLASSNAME );
        viewClass.addImport( fieldDefinition.getStandaloneClassName() );

        MethodSource<JavaClassSource> beforeDisplayMethod = viewClass.getMethod( BEFORE_DISPLAY_METHOD, void.class );
        StringBuffer body = new StringBuffer( beforeDisplayMethod.getBody() == null ? "" : beforeDisplayMethod.getBody() );


        body.append( fieldDefinition.getName() )
                .append( ".init( " )
                .append( getMaskFieldName( fieldDefinition ) )
                .append( ", new " )
                .append( getClassName( DATASET_CLASSNAME ) )
                .append( "( " )
                .append( getMaskFieldName( fieldDefinition ) )
                .append( ", " )
                .append( getClassName( fieldDefinition.getStandaloneClassName() ) )
                .append( SourceGenerationContext.REST_SERVICE_SUFFIX )
                .append( ".class ) );" );

        beforeDisplayMethod.setBody( body.toString() );

    }

    @Override
    public void addExtraFields( JavaClassSource viewClass,
                                ObjectSelectorFieldDefinition fieldDefinition,
                                SourceGenerationContext context ) {

        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + getClassName( fieldDefinition.getStandaloneClassName() ) + context.REST_SERVICE_SUFFIX );

        PropertySource<JavaClassSource> property = viewClass.addProperty( String.class.getName(), getMaskFieldName( fieldDefinition ) );

        FieldSource<JavaClassSource> field = property.getField();
        field.setPrivate();
        field.setFinal( true );
        field.setLiteralInitializer( "\"" + fieldDefinition.getMask() + "\";" );

        property.removeAccessor();
        property.removeMutator();
    }

    @Override
    public String getExtraReadOnlyCode( ObjectSelectorFieldDefinition fieldDefinition, String readonlyParam ) {
        return "";
    }

    private String getClassName( String qualifiedClassName ) {
        if ( qualifiedClassName == null || qualifiedClassName.isEmpty() || qualifiedClassName.indexOf( "." ) == -1) {
            return "";
        }
        return qualifiedClassName.substring( qualifiedClassName.lastIndexOf( "." ) + 1 );
    }
}
