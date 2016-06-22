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

package org.livespark.formmodeler.codegen.view.impl.java;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.PropertySource;
import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.util.SourceGenerationUtil;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.impl.relations.EmbeddedFormField;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

public abstract class RoasterClientTemplateSourceGenerator implements FormJavaTemplateSourceGenerator {

    private Instance<InputCreatorHelper<? extends FieldDefinition>> creatorInstances;

    protected Map<String, InputCreatorHelper> creatorHelpers = new HashMap<String, InputCreatorHelper>(  );

    public RoasterClientTemplateSourceGenerator( Instance<InputCreatorHelper<? extends FieldDefinition>> creatorInstances ) {
        this.creatorInstances = creatorInstances;
    }

    @PostConstruct
    protected void init() {
        for ( InputCreatorHelper helper : creatorInstances ) {
            creatorHelpers.put( helper.getSupportedFieldTypeCode(),
                    helper );
        }
    }

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaClassSource viewClass = createClassSource();
        String packageName = getPackageName( context );

        addTypeSignature( context, viewClass, packageName );
        addImports( context, viewClass );
        addAnnotations( context, viewClass );
        addAdditional( context, viewClass );

        addBaseViewFieldsAndMethodImpls( context, viewClass );
        return viewClass.toString();
    }

    protected JavaClassSource createClassSource() {
        return Roaster.create( JavaClassSource.class );
    }

    protected abstract void addAdditional( SourceGenerationContext context,
            JavaClassSource viewClass );

    protected void addBaseViewFieldsAndMethodImpls( SourceGenerationContext context,
            JavaClassSource viewClass ) {
        StringBuffer readOnlyMethodSrc = new StringBuffer(  );

        for ( FieldDefinition fieldDefinition : context.getFormDefinition().getFields() ) {

            if ((fieldDefinition.isAnnotatedId() && !displaysId()) || isBanned( fieldDefinition ))
                continue;

            InputCreatorHelper helper = creatorHelpers.get( fieldDefinition.getCode() );
            if (helper == null) continue;

            if ( extraFieldsEnabled() ) {
                addExtraFields( helper, context, viewClass, fieldDefinition );
            }

            PropertySource<JavaClassSource> property = viewClass.addProperty( getWidgetFromHelper( helper, fieldDefinition), fieldDefinition.getName() );

            FieldSource<JavaClassSource> field = property.getField();
            field.setPrivate();

            initializeProperty( helper, context, viewClass,fieldDefinition, field );

            if (helper instanceof RequiresCustomCode ) ((RequiresCustomCode )helper).addCustomCode( fieldDefinition, context, viewClass );

            if (fieldDefinition.getBindingExpression() != null && !(fieldDefinition instanceof EmbeddedFormField)) {
                field.addAnnotation( ERRAI_BOUND ).setStringValue( "property", fieldDefinition.getBindingExpression() );
            }

            field.addAnnotation( ERRAI_DATAFIELD );

            property.removeAccessor();
            property.removeMutator();

            if ( !fieldDefinition.isAnnotatedId() ) {
                readOnlyMethodSrc.append( helper.getReadonlyMethod( fieldDefinition.getName(), READONLY_PARAM ) );
            }
            if ( helper instanceof RequiresExtraFields ) {
                readOnlyMethodSrc.append( ( (RequiresExtraFields) helper ).getExtraReadOnlyCode( fieldDefinition, READONLY_PARAM) );
            }
        }

        if ( isEditable() ) {
            MethodSource<JavaClassSource> readonlyMethod = viewClass.addMethod()
                    .setName( "setReadOnly" )
                    .setBody( readOnlyMethodSrc.toString() )
                    .setPublic()
                    .setReturnTypeVoid();
            readonlyMethod.addParameter( boolean.class, SourceGenerationUtil.READONLY_PARAM );
            readonlyMethod.addAnnotation( JAVA_LANG_OVERRIDE );
        }
    }

    protected void addExtraFields( InputCreatorHelper helper,
                                            SourceGenerationContext context,
                                            JavaClassSource viewClass,
                                            FieldDefinition fieldDefinition ){

    }

    protected abstract void initializeProperty( InputCreatorHelper helper,
                                                SourceGenerationContext context,
                                                JavaClassSource viewClass,
                                                FieldDefinition fieldDefinition,
                                                FieldSource<JavaClassSource> field );

    protected abstract boolean isEditable();

    protected abstract String getWidgetFromHelper( InputCreatorHelper helper, FieldDefinition fieldDefinition );

    protected abstract void addAnnotations( SourceGenerationContext context,
            JavaClassSource viewClass );

    protected abstract void addImports( SourceGenerationContext context,
            JavaClassSource viewClass );

    protected abstract void addTypeSignature( SourceGenerationContext context,
            JavaClassSource viewClass,
            String packageName );

    protected abstract boolean isBanned( FieldDefinition definition );

    protected boolean extraFieldsEnabled() {
        return false;
    }

    private String getPackageName( SourceGenerationContext context ) {
        return context.getLocalPackage().getPackageName();
    }

    protected boolean displaysId() {
        return true;
    }
}
