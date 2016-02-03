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

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.FormView;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.widgets.crud.client.component.formDisplay.FormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.livespark.widgets.crud.client.component.formDisplay.embedded.EmbeddedFormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.modal.ModalFormDisplayer;
import org.livespark.widgets.crud.client.component.mock.CrudComponentMock;
import org.livespark.widgets.crud.client.component.mock.CrudComponentTestHelper;
import org.livespark.widgets.crud.client.component.mock.CrudModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class CrudComponentTest extends AbstractCrudComponentTest {

    @Test
    public void testModelCreateOnEmbeddedForms() {
        initTest();

        helper.setEmbeddedForms( true );

        FormDisplayer displayer = crudComponent.getFormDisplayer();

        assertTrue( displayer.equals( embeddedFormDisplayer ) );

        runCreationTest();
    }

    @Test
    public void testModelCreateOnModalForms() {
        initTest();

        helper.setEmbeddedForms( false );

        FormDisplayer displayer = crudComponent.getFormDisplayer();

        assertTrue( displayer.equals( modalFormDisplayer ) );

        runCreationTest();
    }

    @Test
    public void testModelEditionOnEmbeddedForms() {
        initTest();

        helper.setEmbeddedForms( true );

        FormDisplayer displayer = crudComponent.getFormDisplayer();

        assertTrue( displayer.equals( embeddedFormDisplayer ) );

        runCreationTest();

        runEditionTest();
    }

    @Test
    public void testModelEditionOnModalForms() {
        initTest();

        helper.setEmbeddedForms( false );

        FormDisplayer displayer = crudComponent.getFormDisplayer();
        assertTrue( displayer.equals( modalFormDisplayer ) );

        runCreationTest();

        runEditionTest();
    }


    @Test
    public void testModelDeletion() {
        initTest();

        runCreationTest();

        runDeletionTest();
    }



}
