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
package org.livespark.formmodeler.rendering.test;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.livespark.formmodeler.rendering.test.res.TestFormModel;
import org.livespark.formmodeler.rendering.test.res.TestListView;
import org.livespark.widgets.crud.client.component.AbstractCrudComponentTest;
import org.livespark.widgets.crud.client.component.CrudActionsHelper;
import org.livespark.widgets.crud.client.component.mock.CrudModel;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class ListViewTest extends AbstractCrudComponentTest {

    private TestListView listView;

    @GwtMock
    private FormView<TestFormModel> formView;

    @GwtMock
    private ClickEvent clickEvent;

    @Mock
    private RemoteCallback callback;

    @Mock
    private TestFormModel formModel;

    @Mock
    private LiveSparkRestService<CrudModel> restService;

    @Before
    public void init() {
        super.init();

        listView = new TestListView( crudComponent, formView, restService );

        when( formView.validate() ).thenReturn( true );
        when( formView.getModel() ).thenReturn( formModel );
    }

    @Test
    public void initLoadsDataOnce() throws Exception {
        listView.init();
        verify( restService ).load();
        verifyNoMoreInteractions( restService );
    }

    @Test
    public void callbackParameterOfLoadDataCallsSetItemsOnce() throws Exception {
        try {
            initLoadsDataOnce();
            assertNotNull( listView.crudModelListCallback );
        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }
        listView.crudModelListCallback.callback( models );
    }

    @Test
    public void testCreateModal() {
        listView.init();
        listView.loadItems( models );

        runCreationTest();

        verify( restService ).load();
        verify( restService ).create( any( CrudModel.class ) );
    }

    @Test
    public void testEditModal() {
        listView.init();
        listView.loadItems( models );

        runCreationTest();

        runEditionTest();

        verify( restService ).load();
        verify( restService ).create( any( CrudModel.class ) );
        verify( restService ).update( any( CrudModel.class ) );
    }

    @Test
    public void testDeletion() {
        listView.init();
        listView.loadItems( models );

        runCreationTest();

        runDeletionTest();

        verify( restService ).load();
        verify( restService ).create( any( CrudModel.class ) );
        verify( restService ).delete( any( CrudModel.class ) );
    }


    @Override
    protected void runCreationTest() {
        doAnswer( new Answer() {
            @Override
            public TestFormModel answer( InvocationOnMock invocationOnMock ) throws Throwable {
                CrudModel model = new CrudModel( "Ned", "Stark", new Date() );
                models.clear();
                return new TestFormModel( model );
            }
        } ).when( formView ).getModel();
        super.runCreationTest();
    }

    @Override
    protected void runEditionTest() {
        doAnswer( new Answer() {
            @Override
            public TestFormModel answer( InvocationOnMock invocationOnMock ) throws Throwable {
                CrudModel model = models.get( 0 );
                model.setName( "Tyrion" );
                model.setLastName( "Lannister" );
                return new TestFormModel( model );
            }
        } ).when( formView ).getModel();
        super.runEditionTest();
    }

    protected void runDeletionTest() {

        super.runDeletionTest();
    }

    private void failedPrecondition( final Throwable cause ) {
        throw new AssertionError( "Precondition failed.", cause );
    }

    @Override
    protected CrudActionsHelper getActionsHelper() {
        return listView.getActionsHelper();
    }
}
