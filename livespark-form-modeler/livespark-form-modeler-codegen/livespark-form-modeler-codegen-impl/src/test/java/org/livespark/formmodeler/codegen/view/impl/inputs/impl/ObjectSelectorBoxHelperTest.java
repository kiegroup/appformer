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

package org.livespark.formmodeler.codegen.view.impl.inputs.impl;

import java.util.Arrays;
import java.util.List;

import org.junit.runner.RunWith;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.impl.relations.ObjectSelectorFieldDefinition;
import org.livespark.formmodeler.codegen.view.impl.inputs.AbstractInputHelperTest;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.impl.ObjectSelectorBoxHelper;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith( MockitoJUnitRunner.class )
public class ObjectSelectorBoxHelperTest extends AbstractInputHelperTest {

    @Override
    protected void runFieldTests( FieldDefinition field, InputCreatorHelper helper ) {
        super.runFieldTests( field, helper );

        assertNotNull( "Selector must have a mask field!",
                       classSource.getField( field.getName() + ObjectSelectorBoxHelper.FIELD_MASK_SUFFIX ) );


    }

    @Override
    protected List<FieldDefinition> getFieldsToTest() {

        ObjectSelectorFieldDefinition selector = new ObjectSelectorFieldDefinition();

        selector.setMask( "{street}, {num}" );

        selector.setStandaloneClassName( "org.test.Address" );

        return Arrays.asList( initFieldDefinition( selector ) );
    }

    @Override
    protected List<InputCreatorHelper> getInputHelpersToTest() {
        return Arrays.asList( new ObjectSelectorBoxHelper() );
    }
}
