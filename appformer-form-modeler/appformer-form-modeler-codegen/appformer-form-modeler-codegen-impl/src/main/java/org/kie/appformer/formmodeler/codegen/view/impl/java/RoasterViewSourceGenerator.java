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

package org.kie.appformer.formmodeler.codegen.view.impl.java;

import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BOUND;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_DATAFIELD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.JAVA_LANG_OVERRIDE;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.READONLY_PARAM;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.SET_READONLY_METHOD;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.PropertySource;
import org.kie.appformer.formmodeler.codegen.JavaSourceGenerator;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.RequiresValueConverter;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.EmbedsForm;
import org.kie.workbench.common.forms.model.FieldDefinition;

import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_BOUND;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_DATAFIELD;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.JAVA_LANG_OVERRIDE;
import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.READONLY_PARAM;

public abstract class RoasterViewSourceGenerator implements JavaSourceGenerator {

    private final Instance<InputCreatorHelper<? extends FieldDefinition>> creatorInstances;

    protected Map<String, InputCreatorHelper> creatorHelpers = new HashMap<>();

    public RoasterViewSourceGenerator(final Instance<InputCreatorHelper<? extends FieldDefinition>> creatorInstances) {
        this.creatorInstances = creatorInstances;
    }

    @PostConstruct
    protected void init() {
        for (final InputCreatorHelper helper : creatorInstances) {
            creatorHelpers.put(helper.getSupportedFieldTypeCode(),
                               helper);
        }
    }

    @Override
    public String generateJavaSource(final SourceGenerationContext context) {
        final JavaClassSource viewClass = createClassSource();
        final String packageName = getPackageName(context);

        addTypeSignature(context,
                         viewClass,
                         packageName);
        addImports(context,
                   viewClass);
        addAnnotations(context,
                       viewClass);
        addAdditional(context,
                      viewClass);

        addBaseViewFieldsAndMethodImpls(context,
                                        viewClass);
        return viewClass.toString();
    }

    protected JavaClassSource createClassSource() {
        return Roaster.create(JavaClassSource.class);
    }

    protected abstract void addAdditional(SourceGenerationContext context,
                                          JavaClassSource viewClass);

    protected void addBaseViewFieldsAndMethodImpls(final SourceGenerationContext context,
                                                   final JavaClassSource viewClass) {
        final StringBuffer readOnlyMethodSrc = new StringBuffer();

        for (final FieldDefinition fieldDefinition : context.getFormDefinition().getFields()) {

            if (isBanned(fieldDefinition)) {
                continue;
            }

            final InputCreatorHelper helper = creatorHelpers.get(fieldDefinition.getFieldType().getTypeName());
            if (helper == null) {
                continue;
            }

            if (extraFieldsEnabled()) {
                addExtraFields(helper,
                               context,
                               viewClass,
                               fieldDefinition);
            }

            final PropertySource<JavaClassSource> property = viewClass.addProperty(getWidgetFromHelper(helper,
                                                                                                       fieldDefinition),
                                                                                   fieldDefinition.getName());

            final FieldSource<JavaClassSource> field = property.getField();
            field.setPrivate();

            initializeProperty(helper,
                               context,
                               viewClass,
                               fieldDefinition,
                               field);

            if (helper instanceof RequiresCustomCode) {
                ((RequiresCustomCode) helper).addCustomCode(fieldDefinition,
                                                            context,
                                                            viewClass);
            }

            if (!StringUtils.isEmpty(fieldDefinition.getBinding()) && !(fieldDefinition instanceof EmbedsForm)) {
                String bindingExpression = context.getFormDefinition().getModel().getName() + "." + fieldDefinition.getBinding();
                AnnotationSource<JavaClassSource> boundAnnotation = field.addAnnotation(ERRAI_BOUND);
                boundAnnotation.setStringValue("property",
                                               bindingExpression);
                if (helper instanceof RequiresValueConverter) {

                    String converterClassName = ((RequiresValueConverter) helper).getConverterClassName(fieldDefinition);
                    if (!StringUtils.isEmpty(converterClassName)) {
                        if (!StringUtils.endsWith(converterClassName,
                                                  ".class")) {
                            converterClassName += ".class";
                        }
                        boundAnnotation.setLiteralValue("converter",
                                                        converterClassName);
                    }
                }
            }

            field.addAnnotation(ERRAI_DATAFIELD);

            property.removeAccessor();
            property.removeMutator();

            readOnlyMethodSrc.append(helper.getReadonlyMethod(fieldDefinition.getName(),
                                                              READONLY_PARAM));

            if (helper instanceof RequiresExtraFields) {
                readOnlyMethodSrc.append(((RequiresExtraFields) helper).getExtraReadOnlyCode(fieldDefinition,
                                                                                             READONLY_PARAM));
            }
        }

        if (isEditable()) {
            final MethodSource<JavaClassSource> readonlyMethod = viewClass.addMethod()
                    .setName(SET_READONLY_METHOD)
                    .setBody(readOnlyMethodSrc.toString())
                    .setPublic()
                    .setReturnTypeVoid();
            readonlyMethod.addParameter(boolean.class,
                                        SourceGenerationUtil.READONLY_PARAM);
            readonlyMethod.addAnnotation(JAVA_LANG_OVERRIDE);
        }
    }

    protected void addExtraFields(final InputCreatorHelper helper,
                                  final SourceGenerationContext context,
                                  final JavaClassSource viewClass,
                                  final FieldDefinition fieldDefinition) {

    }

    protected abstract void initializeProperty(InputCreatorHelper helper,
                                               SourceGenerationContext context,
                                               JavaClassSource viewClass,
                                               FieldDefinition fieldDefinition,
                                               FieldSource<JavaClassSource> field);

    protected abstract boolean isEditable();

    protected abstract String getWidgetFromHelper(InputCreatorHelper helper,
                                                  FieldDefinition fieldDefinition);

    protected abstract void addAnnotations(SourceGenerationContext context,
                                           JavaClassSource viewClass);

    protected abstract void addImports(SourceGenerationContext context,
                                       JavaClassSource viewClass);

    protected abstract void addTypeSignature(SourceGenerationContext context,
                                             JavaClassSource viewClass,
                                             String packageName);

    protected abstract boolean isBanned(FieldDefinition definition);

    protected boolean extraFieldsEnabled() {
        return false;
    }

    private String getPackageName(final SourceGenerationContext context) {
        return context.getLocalPackage().getPackageName();
    }
}
