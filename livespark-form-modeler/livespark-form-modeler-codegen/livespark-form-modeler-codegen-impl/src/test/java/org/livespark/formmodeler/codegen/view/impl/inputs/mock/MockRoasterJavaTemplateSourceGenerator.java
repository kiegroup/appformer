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

package org.livespark.formmodeler.codegen.view.impl.inputs.mock;

import javax.enterprise.inject.Instance;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.livespark.formmodeler.codegen.view.impl.java.RoasterJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;

public class MockRoasterJavaTemplateSourceGenerator extends RoasterJavaTemplateSourceGenerator {

    protected JavaClassSource classSource;

    public MockRoasterJavaTemplateSourceGenerator( JavaClassSource classSource,
                                                   Instance<InputCreatorHelper<? extends FieldDefinition>> creatorInstances ) {
        super( creatorInstances );
        init();
        this.classSource = classSource;
    }

    @Override
    protected JavaClassSource createClassSource() {
        return classSource;
    }

    public InputCreatorHelper getHelperForField( FieldDefinition fieldDefinition ) {
        return creatorHelpers.get( fieldDefinition.getCode() );
    }
}
