/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.widgets.crud.client.component;

import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.widgets.crud.client.component.formDisplay.FormDisplayer;

import com.google.gwtmockito.GwtMockitoTestRunner;

@RunWith(GwtMockitoTestRunner.class)
public class CrudComponentTest extends AbstractCrudComponentTest {

    @Test
    public void testModelCreateOnEmbeddedForms() {
        initTest();

        when( helper.showEmbeddedForms() ).thenReturn( true );

        final FormDisplayer displayer = crudComponent.getFormDisplayer();

        assertTrue( displayer.equals( embeddedFormDisplayer ) );

        runCreationTest();
    }

    @Test
    public void testModelCreateCancellationOnEmbeddedForms() {
        initTest();

        when( helper.showEmbeddedForms() ).thenReturn( true );

        final FormDisplayer displayer = crudComponent.getFormDisplayer();

        assertTrue( displayer.equals( embeddedFormDisplayer ) );

        runCreationCancelTest();
    }

    @Test
    public void testModelCreateOnModalForms() {
        initTest();

        when( helper.showEmbeddedForms() ).thenReturn( false );

        final FormDisplayer displayer = crudComponent.getFormDisplayer();

        assertTrue( displayer.equals( modalFormDisplayer ) );

        runCreationTest();
    }

    @Test
    public void testModelCreateCancellationOnModalForms() {
        initTest();

        when( helper.showEmbeddedForms() ).thenReturn( false );

        final FormDisplayer displayer = crudComponent.getFormDisplayer();

        assertTrue( displayer.equals( modalFormDisplayer ) );

        runCreationCancelTest();
    }

    @Test
    public void testModelEditOnEmbeddedForms() {
        initTest();

        when( helper.showEmbeddedForms() ).thenReturn( true );

        final FormDisplayer displayer = crudComponent.getFormDisplayer();

        assertTrue( displayer.equals( embeddedFormDisplayer ) );

        runEditTest();
    }

    @Test
    public void testModelEditCancellationOnEmbeddedForms() {
        initTest();

        when( helper.showEmbeddedForms() ).thenReturn( true );

        final FormDisplayer displayer = crudComponent.getFormDisplayer();

        assertTrue( displayer.equals( embeddedFormDisplayer ) );

        runEditCancelTest();
    }

    @Test
    public void testModelEditOnModalForms() {
        initTest();

        when( helper.showEmbeddedForms() ).thenReturn( false );

        final FormDisplayer displayer = crudComponent.getFormDisplayer();
        assertTrue( displayer.equals( modalFormDisplayer ) );

        runEditTest();
    }

    @Test
    public void testModelEditCancellationOnModalForms() {
        initTest();

        when( helper.showEmbeddedForms() ).thenReturn( false );

        final FormDisplayer displayer = crudComponent.getFormDisplayer();
        assertTrue( displayer.equals( modalFormDisplayer ) );

        runEditCancelTest();
    }


    @Test
    public void testModelDeletion() {
        initTest();

        runDeletionTest();
    }



}
