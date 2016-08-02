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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.forms.crud.client.component.CrudComponent;
import org.kie.workbench.common.forms.crud.client.component.formDisplay.FormDisplayer;
import org.kie.workbench.common.forms.crud.client.component.mock.CrudModel;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.test.res.TestFormModel;
import org.livespark.formmodeler.rendering.test.res.TestFormView;
import org.livespark.formmodeler.rendering.test.res.TestListView;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;

@RunWith( GwtMockitoTestRunner.class )
public class ListViewTest {

    @InjectMocks
    private TestListView listView;

    @GwtMock
    private TestFormView formView;

    @GwtMock
    private ClickEvent clickEvent;

    @GwtMock
    private FlowPanel content;

    @Mock
    private CrudComponent<CrudModel, TestFormModel> crudComponent;

    @Mock
    private TestFormModel formModel;

    @Mock
    private LiveSparkRestService<CrudModel> restService;

    @Mock
    private SyncBeanManager beanManager;

    @SuppressWarnings( "rawtypes" )
    @Mock
    private SyncBeanDef beanDef;

    @Captor
    private ArgumentCaptor<FormDisplayer.FormDisplayerCallback> callbackCaptor;

    @Captor
    private ArgumentCaptor<CrudModel> modelCaptor;

    private final List<CrudModel> models = new ArrayList<>();

    @SuppressWarnings( "unchecked" )
    @Before
    public void init() {
        listView.callbacks.clear();
        models.clear();
        when( formView.validate() ).thenReturn( true );
        when( formView.getModel() ).thenReturn( formModel );
        when( beanManager.lookupBean( TestFormView.class ) ).thenReturn( beanDef );
        when( beanDef.getInstance() ).thenReturn( formView );
    }

    @Test
    public void initLoadsDataOnce() throws Exception {
        listView.init();
        verify( restService ).load();
        assertEquals( 1, listView.callbacks.size() );
        verifyNoMoreInteractions( restService );
        verify( crudComponent ).init( listView.getCrudActionsHelper() );
        verify( crudComponent ).setEmbedded( false );

        assertEquals( 1, listView.callbacks.size() );
        @SuppressWarnings( "unchecked" )
        final RemoteCallback<List<CrudModel>> callback = (RemoteCallback<List<CrudModel>>) listView.callbacks.get( listView.callbacks.size()-1 );
        callback.callback( models );

        verify( crudComponent ).refresh();
    }

    @Test
    public void initCallsCrudComponentInit() throws Exception {
        listView.init();
        verify( crudComponent, times( 1 ) ).init( listView.getCrudActionsHelper() );
        verify( crudComponent, times( 1 ) ).setEmbedded( false );
        verify( content, times( 1 ) ).add( crudComponent );
    }

    @Test
    public void createFormAcceptAddsModelAndRefreshes() {
        CrudModel createModel = new CrudModel();
        when( formModel.getModel() ).thenReturn( createModel );

        // Init view
        listView.init();
        verify( restService ).load();
        assertEquals( 1, listView.callbacks.size() );
        listView.loadItems( models );
        verify( crudComponent ).init( listView.getCrudActionsHelper() );
        verify( crudComponent ).setEmbedded( false );
        verify( crudComponent ).refresh();

        // Display form
        listView.getCrudActionsHelper().createInstance();
        verify( crudComponent ).displayForm( same( formView ), callbackCaptor.capture() );

        // Accept form
        callbackCaptor.getValue().onAccept();
        verify( restService ).create( modelCaptor.capture() );
        assertSame( formModel.getModel(), modelCaptor.getValue() );
        assertEquals( 2, listView.callbacks.size() );

        // Invoke rest callback
        @SuppressWarnings( "unchecked" )
        RemoteCallback<CrudModel> callback = (RemoteCallback<CrudModel>) listView.callbacks.get( 1 );
        CrudModel response = new CrudModel();
        callback.callback( response );
        assertEquals( 1, models.size() );
        assertSame( response, models.get( 0 ) );
        verify( crudComponent, times( 2 ) ).refresh();
    }

    @Test
    public void createFormCancelDoesNothing() {
        CrudModel createModel = new CrudModel();
        when( formModel.getModel() ).thenReturn( createModel );

        // Init view
        listView.init();
        verify( restService ).load();
        assertEquals( 1, listView.callbacks.size() );
        listView.loadItems( models );
        verify( crudComponent ).init( listView.getCrudActionsHelper() );
        verify( crudComponent ).setEmbedded( false );
        verify( crudComponent ).refresh();

        // Display form
        listView.getCrudActionsHelper().createInstance();
        verify( crudComponent ).displayForm( same( formView ), callbackCaptor.capture() );

        // Cancel form
        callbackCaptor.getValue().onCancel();

        // Verify nothing happened
        assertEquals( 1, listView.callbacks.size() );
        assertEquals( 0, models.size() );
        verifyNoMoreInteractions( restService, crudComponent );
    }

    @Test
    public void editFormAcceptCallsResumeBindingAndRefresh() {
        CrudModel editModel = new CrudModel();
        models.add( editModel );
        when( formModel.getModel() ).thenReturn( editModel );

        // Init view
        listView.init();
        verify( restService ).load();
        assertEquals( 1, listView.callbacks.size() );
        listView.loadItems( models );
        verify( crudComponent ).init( listView.getCrudActionsHelper() );
        verify( crudComponent ).setEmbedded( false );
        verify( crudComponent ).refresh();

        // Display form
        listView.getCrudActionsHelper().editInstance( 0 );
        verify( crudComponent ).displayForm( same( formView ), callbackCaptor.capture() );

        // Accept form
        callbackCaptor.getValue().onAccept();
        verify( restService ).update( modelCaptor.capture() );
        assertSame( formModel.getModel(), modelCaptor.getValue() );
        assertEquals( 2, listView.callbacks.size() );

        // Invoke rest callback
        @SuppressWarnings( "unchecked" )
        RemoteCallback<Boolean> callback = (RemoteCallback<Boolean>) listView.callbacks.get( 1 );
        callback.callback( true );
        assertEquals( 1, models.size() );
        verify( formView ).resumeBinding( true );
        verify( crudComponent, times( 2 ) ).refresh();
    }

    @Test
    public void editFormCancelDoesNothing() {
        CrudModel editModel = new CrudModel();
        models.add( editModel );
        when( formModel.getModel() ).thenReturn( editModel );

        // Init view
        listView.init();
        verify( restService ).load();
        assertEquals( 1, listView.callbacks.size() );
        listView.loadItems( models );
        verify( crudComponent ).init( listView.getCrudActionsHelper() );
        verify( crudComponent ).setEmbedded( false );
        verify( crudComponent ).refresh();

        // Display form
        listView.getCrudActionsHelper().editInstance( 0 );
        verify( crudComponent ).displayForm( same( formView ), callbackCaptor.capture() );

        // Cancel form
        callbackCaptor.getValue().onCancel();

        // Verify nothing happened
        assertEquals( 1, listView.callbacks.size() );
        assertEquals( 1, models.size() );
        verifyNoMoreInteractions( restService, crudComponent );
    }

    @Test
    public void testDeletion() {
        CrudModel deleteModel = new CrudModel();
        models.add( deleteModel );
        when( formModel.getModel() ).thenReturn( deleteModel );

        // Init view
        listView.init();
        verify( restService ).load();
        assertEquals( 1, listView.callbacks.size() );
        listView.loadItems( models );
        verify( crudComponent ).init( listView.getCrudActionsHelper() );
        verify( crudComponent ).setEmbedded( false );
        verify( crudComponent ).refresh();

        // Trigger rest call
        listView.getCrudActionsHelper().deleteInstance( 0 );
        verify( restService ).delete( modelCaptor.capture() );
        assertSame( formModel.getModel(), modelCaptor.getValue() );
        assertEquals( 2, listView.callbacks.size() );

        // Invoke rest callback
        @SuppressWarnings( "unchecked" )
        RemoteCallback<Boolean> callback = (RemoteCallback<Boolean>) listView.callbacks.get( 1 );
        callback.callback( true );
        assertEquals( 0, models.size() );
        verify( crudComponent, times( 2 ) ).refresh();
    }

}
