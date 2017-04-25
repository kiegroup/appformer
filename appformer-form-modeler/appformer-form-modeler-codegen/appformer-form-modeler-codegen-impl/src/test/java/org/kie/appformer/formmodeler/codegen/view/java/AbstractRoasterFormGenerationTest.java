/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.formmodeler.codegen.view.java;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.Instance;

import org.guvnor.common.services.project.model.Package;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Before;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.kie.workbench.common.forms.data.modeller.model.DataObjectFormModel;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.mockito.Mock;
import org.uberfire.backend.vfs.Path;

import static org.mockito.Mockito.*;

public abstract class AbstractRoasterFormGenerationTest {

    public static final String FORM_NAME = "Test";

    public static final String MODEL = "employee";
    public static final String MODEL_TYPE = "org.test.Employee";

    @Mock
    protected Instance<InputCreatorHelper<? extends FieldDefinition>> creatorHelpers;

    @Mock
    protected Path path;

    @Mock
    protected Package rootPackage;

    @Mock
    protected Package localPackage;

    @Mock
    protected Package sharedPackage;

    @Mock
    protected Package serverPackage;

    protected JavaClassSource classSource;

    protected List<InputCreatorHelper> helpers;

    protected FormDefinition formDefinition;

    protected SourceGenerationContext context;

    @Before
    public void initTest() {
        when(rootPackage.getPackageName()).thenReturn("org");

        when(localPackage.getPackageName()).thenReturn("org.client.local");

        when(sharedPackage.getPackageName()).thenReturn("org.client.shared");

        when(serverPackage.getPackageName()).thenReturn("org.server");

        helpers = getInputHelpersToTest();

        when(creatorHelpers.iterator()).then(it -> helpers.iterator());

        classSource = Roaster.create(JavaClassSource.class);

        formDefinition = new FormDefinition();

        formDefinition.setName(FORM_NAME);
        formDefinition.setId(FORM_NAME);
        formDefinition.setModel(new DataObjectFormModel(MODEL,
                                                        MODEL_TYPE));

        formDefinition.getFields().addAll(getFieldsToTest());

        context = new SourceGenerationContext(formDefinition,
                                              path,
                                              rootPackage,
                                              localPackage,
                                              sharedPackage,
                                              serverPackage,
                                              new ArrayList<>());
    }

    protected abstract List<FieldDefinition> getFieldsToTest();

    protected abstract List<InputCreatorHelper> getInputHelpersToTest();
}
