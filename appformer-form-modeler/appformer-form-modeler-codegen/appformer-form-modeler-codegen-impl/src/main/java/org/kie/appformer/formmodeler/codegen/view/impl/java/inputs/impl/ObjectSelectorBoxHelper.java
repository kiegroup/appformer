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

import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INIT_FORM_METHOD;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.PropertySource;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.kie.appformer.formmodeler.codegen.view.impl.java.RequiresExtraFields;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.SelectorOption;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.objectSelector.definition.ObjectSelectorFieldDefinition;

public class ObjectSelectorBoxHelper<T extends SelectorOption> extends AbstractInputCreatorHelper<ObjectSelectorFieldDefinition>
        implements RequiresExtraFields<ObjectSelectorFieldDefinition>, RequiresCustomCode<ObjectSelectorFieldDefinition> {


    public static final String FIELD_MASK_SUFFIX = "_fieldMask";

    public static final String WIDGET_CLASSNAME = "org.kie.workbench.common.forms.common.rendering.client.widgets.typeahead.BindableTypeAhead";

    public static final String DATASET_CLASSNAME = "org.kie.appformer.formmodeler.rendering.client.widgets.typeahead.AppFormerStaticDataset";

    @Override
    public String getSupportedFieldTypeCode() {
        return ObjectSelectorFieldDefinition.FIELD_TYPE.getTypeName();
    }

    @Override
    public String getInputWidget( final ObjectSelectorFieldDefinition fieldDefinition ) {
        return getClassName( WIDGET_CLASSNAME ) + "<" + getClassName( fieldDefinition.getStandaloneClassName() ) + ">";
    }

    protected boolean isEvaluableMask( final String mask ) {
        final int countOpeners = org.apache.commons.lang3.StringUtils.countMatches( mask, "{" );

        final int countClosers = org.apache.commons.lang3.StringUtils.countMatches( mask, "}" );

        return countOpeners != 0 && countOpeners == countClosers;
    }

    protected String getMaskFieldName( final ObjectSelectorFieldDefinition fieldDefinition ) {
        return fieldDefinition.getName() + FIELD_MASK_SUFFIX;
    }

    @Override
    public void addCustomCode( final ObjectSelectorFieldDefinition fieldDefinition,
                               final SourceGenerationContext context,
                               final JavaClassSource viewClass ) {

        if ( !isEvaluableMask( fieldDefinition.getMask() ) ) {
            throw new IllegalArgumentException( "Unsupported mask format" );
        }

        viewClass.addImport( DATASET_CLASSNAME );
        viewClass.addImport( WIDGET_CLASSNAME );
        viewClass.addImport( fieldDefinition.getStandaloneClassName() );

        final MethodSource<JavaClassSource> initMethod = viewClass.getMethod( INIT_FORM_METHOD );
        final StringBuffer body = new StringBuffer( initMethod.getBody() == null ? "" : initMethod.getBody() );


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

        initMethod.setBody( body.toString() );

    }

    @Override
    public void addExtraFields( final JavaClassSource viewClass,
                                final ObjectSelectorFieldDefinition fieldDefinition,
                                final SourceGenerationContext context ) {

        viewClass.addImport( context.getSharedPackage().getPackageName() + "." + getClassName( fieldDefinition.getStandaloneClassName() ) + context.REST_SERVICE_SUFFIX );

        final PropertySource<JavaClassSource> property = viewClass.addProperty( String.class.getName(), getMaskFieldName( fieldDefinition ) );

        final FieldSource<JavaClassSource> field = property.getField();
        field.setPrivate();
        field.setFinal( true );
        field.setLiteralInitializer( "\"" + fieldDefinition.getMask() + "\";" );

        property.removeAccessor();
        property.removeMutator();
    }

    @Override
    public String getExtraReadOnlyCode( final ObjectSelectorFieldDefinition fieldDefinition, final String readonlyParam ) {
        return "";
    }

    private String getClassName( final String qualifiedClassName ) {
        if ( qualifiedClassName == null || qualifiedClassName.isEmpty() || qualifiedClassName.indexOf( "." ) == -1) {
            return "";
        }
        return qualifiedClassName.substring( qualifiedClassName.lastIndexOf( "." ) + 1 );
    }
}
