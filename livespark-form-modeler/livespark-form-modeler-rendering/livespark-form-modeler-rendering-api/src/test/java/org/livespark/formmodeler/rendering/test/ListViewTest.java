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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.test.res.TestFormModel;
import org.livespark.formmodeler.rendering.test.res.TestFormView;
import org.livespark.formmodeler.rendering.test.res.TestListView;
import org.livespark.widgets.crud.client.component.AbstractCrudComponentTest;
import org.livespark.widgets.crud.client.component.CrudActionsHelper;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.livespark.widgets.crud.client.component.mock.CrudModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;

@RunWith( GwtMockitoTestRunner.class )
public class ListViewTest extends AbstractCrudComponentTest {

    @InjectMocks
    private TestListView listView;

    @GwtMock
    private TestFormView formView;

    @GwtMock
    private ClickEvent clickEvent;

    @GwtMock
    private FlowPanel content;

    @Mock
    private RemoteCallback callback;

    @Mock
    private TestFormModel formModel;

    @Mock
    private LiveSparkRestService<CrudModel> restService;

    @Mock
    private SyncBeanManager beanManager;

    @Mock
    private SyncBeanDef beanDef;

    private final List<CrudModel> models = new ArrayList<>();

    @Override
    @Before
    public void init() {
        super.init();
        crudComponent = Mockito.spy( crudComponent );
        listView.setCrudComponent( crudComponent );
        listView.callbacks.clear();
        models.clear();
        when( formView.validate() ).thenReturn( true );
        when( formView.getModel() ).thenReturn( formModel );
        when( restService.create( any() ) ).then( invocation -> {
            final CrudModel model = (CrudModel) invocation.getArguments()[0];
            @SuppressWarnings( "unchecked" )
            final
            RemoteCallback<CrudModel> callback = (RemoteCallback<CrudModel>) listView.callbacks.get( listView.callbacks.size()-1 );
            callback.callback( model );
            return null;
        } );
        when( beanManager.lookupBean( TestFormView.class ) ).thenReturn( beanDef );
        when( beanDef.getInstance() ).thenReturn( formView );
    }

    @Test
    public void initLoadsDataOnce() throws Exception {
        listView.init();
        verify( restService ).load();
        verifyNoMoreInteractions( restService );
        verify( crudComponent ).refresh();

        assertEquals( 1, listView.callbacks.size() );
        @SuppressWarnings( "unchecked" )
        final RemoteCallback<List<CrudModel>> callback = (RemoteCallback<List<CrudModel>>) listView.callbacks.get( listView.callbacks.size()-1 );
        callback.callback( models );

        verify( crudComponent, times( 2 ) ).refresh();
    }

    @Test
    public void initCallsCrudComponentInit() throws Exception {
        listView.init();
        verify( crudComponent, times( 1 ) ).init( getActionsHelper() );
        verify( crudComponent, times( 1 ) ).setEmbedded( false );
        verify( content, times( 1 ) ).add( crudComponent );
    }

    @Test
    public void testCreateModal() {
        listView.init();
        verify( restService ).load();
        listView.loadItems( models );

        runCreationTest();

        verify( restService ).create( any( CrudModel.class ) );
    }

    @Test
    public void testEditModal() {
        listView.init();
        listView.loadItems( models );

        runCreationTest();

        runEditTest();

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
            public TestFormModel answer( final InvocationOnMock invocationOnMock ) throws Throwable {
                final CrudModel model = new CrudModel( "Ned", "Stark", new Date() );
                models.clear();
                return new TestFormModel( model );
            }
        } ).when( formView ).getModel();
        super.runCreationTest();
    }

    @Override
    protected void runEditTest() {
        doAnswer( new Answer() {
            @Override
            public TestFormModel answer( final InvocationOnMock invocationOnMock ) throws Throwable {
                final CrudModel model = models.get( 0 );
                model.setName( "Tyrion" );
                model.setLastName( "Lannister" );
                return new TestFormModel( model );
            }
        } ).when( formView ).getModel();
        super.runEditTest();
    }

    @Override
    protected void runDeletionTest() {

        super.runDeletionTest();
    }

    @Override
    protected CrudActionsHelper getActionsHelper() {
        return listView.getCrudActionsHelper();
    }

    @Override
    protected IsFormView getFormView() {
        return formView;
    }
}
