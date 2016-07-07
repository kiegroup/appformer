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

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.codegen.view.impl.inputs.AbstractInputHelperTest;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.impl.ObjectSelectorBoxHelper;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.impl.relations.ObjectSelectorFieldDefinition;
import org.mockito.runners.MockitoJUnitRunner;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.BEFORE_DISPLAY_METHOD;
import static org.livespark.formmodeler.codegen.view.impl.java.inputs.impl.ObjectSelectorBoxHelper.LOAD_LIST_VALUES_METHOD_NAME;

@RunWith( MockitoJUnitRunner.class )
public class ObjectSelectorBoxHelperTest extends AbstractInputHelperTest {

    @Override
    protected void runFieldTests( FieldDefinition field, InputCreatorHelper helper ) {
        super.runFieldTests( field, helper );


        String providerName = "Address" + ObjectSelectorBoxHelper.INSTANCE_PROVIDER_SUFFIX;

        // check that exists a AddressDataProvider to load values
        assertNotNull( "There must be a DataProvider to load the combo values", classSource.getNestedType( providerName ) );

        String initListMethodName = LOAD_LIST_VALUES_METHOD_NAME + field.getName();

        assertNotNull( "'"+ initListMethodName + "' is missing!", classSource.getMethod( initListMethodName ) );

        MethodSource<JavaClassSource> beforeDisplayMethod = classSource.getMethod( BEFORE_DISPLAY_METHOD );

        assertNotNull( "Class must have a '" + BEFORE_DISPLAY_METHOD + "'", beforeDisplayMethod );


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
