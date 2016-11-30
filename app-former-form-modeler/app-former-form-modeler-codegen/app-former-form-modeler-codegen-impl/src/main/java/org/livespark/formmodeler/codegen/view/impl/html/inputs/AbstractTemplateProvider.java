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

package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import javax.inject.Inject;

import org.kie.workbench.common.forms.service.FieldManager;
import org.livespark.formmodeler.codegen.view.impl.html.InputTemplateProvider;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRegistry;

public abstract class AbstractTemplateProvider implements InputTemplateProvider {

    protected FieldManager fieldManager;

    @Inject
    public AbstractTemplateProvider( FieldManager fieldManager ) {
        this.fieldManager = fieldManager;
    }

    @Override
    public void registerTemplates( TemplateRegistry registry ) {
        for ( String fieldCode : getSupportedFieldCodes() ) {
            if ( fieldCode != null && fieldManager.getDefinitionByTypeCode( fieldCode ) != null ) {
                registry.addNamedTemplate( fieldCode,
                        TemplateCompiler.compileTemplate(
                                getClass().getResourceAsStream( getTemplateForFieldTypeCode( fieldCode ) ) ) );
            }
        }
    }

    protected abstract String[] getSupportedFieldCodes();

    protected abstract String getTemplateForFieldTypeCode( String fieldCode );

}
