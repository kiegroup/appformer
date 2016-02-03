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

import com.google.gwtmockito.GwtMock;
import junit.framework.TestCase;
import org.junit.Before;
import org.livespark.widgets.crud.client.component.formDisplay.FormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.livespark.widgets.crud.client.component.formDisplay.embedded.EmbeddedFormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.modal.ModalFormDisplayer;
import org.livespark.widgets.crud.client.component.mock.CrudComponentMock;
import org.livespark.widgets.crud.client.component.mock.CrudComponentTestHelper;
import org.livespark.widgets.crud.client.component.mock.CrudModel;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public abstract class AbstractCrudComponentTest extends TestCase {
    @Mock
    protected CrudComponent.CrudComponentView view;

    @GwtMock
    protected EmbeddedFormDisplayer embeddedFormDisplayer;

    @GwtMock
    protected ModalFormDisplayer modalFormDisplayer;

    @Mock
    protected IsFormView formView;

    protected CrudComponentTestHelper helper;

    protected List<CrudModel> models = new ArrayList<>();

    protected CrudComponent crudComponent;

    @Before
    public void init() {
        doAnswer( new Answer<Void>() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                crudComponent.refresh();
                return null;
            }
        } ).when( view ).init( any( CrudComponent.CrudMetaDefinition.class ) );

        doAnswer( new Answer<Void>() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                view.renderNestedForm( "New Instance",
                        crudComponent.getCreateForm(), new FormDisplayer.FormDisplayerCallback() {
                            @Override
                            public void onAccept() {
                                view.doCreate();
                            }

                            @Override
                            public void onCancel() {
                                view.doCancel();
                            }
                        } );
                return null;
            }
        } ).when( view ).showCreateForm();

        doAnswer( new Answer<Void>() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                view.restoreTable();
                crudComponent.createInstance();
                return null;
            }
        } ).when( view ).doCreate();

        doAnswer( new Answer<Void>() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                view.restoreTable();
                return null;
            }
        } ).when( view ).doCancel();

        doAnswer( new Answer<Void>() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                view.renderNestedForm( "Edit Instance",
                        crudComponent.getEditForm( 0 ), new FormDisplayer.FormDisplayerCallback() {
                            @Override
                            public void onAccept() {
                                view.doCreate();
                            }

                            @Override
                            public void onCancel() {
                                view.doCancel();
                            }
                        } );
                return null;
            }
        } ).when( view ).showEditionForm( anyInt() );

        doAnswer( new Answer<Void>() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                view.restoreTable();
                crudComponent.editInstance();
                return null;
            }
        } ).when( view ).doEdit();

        doAnswer( new Answer<Void>() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                crudComponent.deleteInstance( 0 );
                return null;
            }
        } ).when( view ).deleteInstance( anyInt() );

        models.clear();

        helper = new CrudComponentTestHelper( formView, models );

        crudComponent = new CrudComponentMock( view, embeddedFormDisplayer, modalFormDisplayer );
    }

    protected void initTest() {
        verify( view ).setPresenter( crudComponent );

        crudComponent.init( getActionsHelper() );
        verify( view ).init( any( CrudComponent.CrudMetaDefinition.class ) );

        crudComponent.getCurrentPage();
        verify( view ).getCurrentPage();

        crudComponent.asWidget();
    }

    protected void runCreationTest() {
        view.showCreateForm();

        verify( view ).showCreateForm();
        verify( view ).renderNestedForm( anyString(), any( IsFormView.class), any( FormDisplayer.FormDisplayerCallback.class ) );

        view.doCreate();

        verify( view ).doCreate();
        verify( view ).restoreTable();

        assertTrue( !models.isEmpty() );

        CrudModel model = models.get( 0 );

        assertEquals( "Ned", model.getName() );
        assertEquals( "Stark", model.getLastName() );

        // TEST CANCEL
        view.showCreateForm();

        verify( view, times( 2 ) ).showCreateForm();
        verify( view, times( 2 ) ).renderNestedForm( anyString(), any( IsFormView.class), any( FormDisplayer.FormDisplayerCallback.class ) );

        view.doCancel();

        verify( view ).doCancel();
        verify( view, times( 2 ) ).restoreTable();
    }

    protected void runEditionTest() {
        view.showEditionForm( 0 );

        verify( view ).showEditionForm( anyInt() );
        verify( view, times( 3 ) ).renderNestedForm( anyString(), any( IsFormView.class), any( FormDisplayer.FormDisplayerCallback.class ) );

        view.doEdit();

        verify( view ).doEdit();
        verify( view, times( 3 ) ).restoreTable();

        assertTrue( !models.isEmpty() );

        CrudModel model = models.get( 0 );

        assertEquals( "Tyrion", model.getName() );
        assertEquals( "Lannister", model.getLastName() );

        // TEST CANCEL
        view.showEditionForm( 0 );

        verify( view, times( 2 ) ).showEditionForm( anyInt() );
        verify( view, times( 4 ) ).renderNestedForm( anyString(), any( IsFormView.class), any( FormDisplayer.FormDisplayerCallback.class ) );

        view.doCancel();

        verify( view, times( 2 ) ).doCancel();
        verify( view, times( 4 ) ).restoreTable();
    }

    protected void runDeletionTest() {
        view.deleteInstance( 0 );

        verify( view ).deleteInstance( anyInt() );

        assertTrue( models.isEmpty() );
    }

    protected CrudActionsHelper getActionsHelper() {
        return helper;
    }
}
