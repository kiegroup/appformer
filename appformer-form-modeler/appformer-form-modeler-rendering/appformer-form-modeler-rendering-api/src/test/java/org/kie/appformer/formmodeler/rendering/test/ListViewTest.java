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
package org.kie.appformer.formmodeler.rendering.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.appformer.flow.api.Command;
import org.kie.appformer.flow.api.CrudOperation;
import org.kie.appformer.formmodeler.rendering.client.flow.ListAsyncDataProviderAdapter;
import org.kie.appformer.formmodeler.rendering.test.res.TestFormModel;
import org.kie.appformer.formmodeler.rendering.test.res.TestFormView;
import org.kie.appformer.formmodeler.rendering.test.res.TestListView;
import org.kie.workbench.common.forms.crud.client.component.CrudComponent;
import org.kie.workbench.common.forms.crud.client.component.mock.CrudModel;
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
    private Consumer<Command<CrudOperation, CrudModel>> callback;

    @Captor
    private ArgumentCaptor<Command<CrudOperation, CrudModel>> outputCaptor;

    private final List<CrudModel> models = new ArrayList<>();

    @Before
    public void init() {
        models.clear();
        when( formView.validate() ).thenReturn( true );
        when( formView.getModel() ).thenReturn( formModel );
    }

    @Test
    public void startLoadsDataOnce() throws Exception {
        listView.start( new ListAsyncDataProviderAdapter<>( models ), callback );
        verify( crudComponent ).init( listView.getCrudActionsHelper() );
        verify( crudComponent ).setEmbedded( false );
        verify( crudComponent ).refresh();
    }

    @Test
    public void startCallsCrudComponentInit() throws Exception {
        listView.start( new ListAsyncDataProviderAdapter<>( models ), callback );
        verify( crudComponent, times( 1 ) ).init( listView.getCrudActionsHelper() );
        verify( crudComponent, times( 1 ) ).setEmbedded( false );
        verify( content, times( 1 ) ).add( crudComponent );
    }

    @Test
    public void createInstanceSubmitsCreateCommand() {
        // Init view
        listView.start( new ListAsyncDataProviderAdapter<>( models ), callback );
        verify( crudComponent ).init( listView.getCrudActionsHelper() );
        verify( crudComponent ).setEmbedded( false );
        verify( crudComponent ).refresh();

        // Call helper, verify command submitted
        listView.getCrudActionsHelper().createInstance();
        verify( callback ).accept( outputCaptor.capture() );
        final Command<CrudOperation, CrudModel> submitted = outputCaptor.getValue();
        assertEquals( CrudOperation.CREATE, submitted.commandType );
        assertNotNull( submitted.value );
    }

    @Test
    public void editInstanceCallsUpdateCommand() {
        final CrudModel editModel = new CrudModel();
        models.add( editModel );
        when( formModel.getModel() ).thenReturn( editModel );

        // Init view
        listView.start( new ListAsyncDataProviderAdapter<>( models ), callback );
        verify( crudComponent ).init( listView.getCrudActionsHelper() );
        verify( crudComponent ).setEmbedded( false );
        verify( crudComponent ).refresh();

        // Call helper, verify command submitted
        listView.getCrudActionsHelper().editInstance( 0 );
        verify( callback ).accept( outputCaptor.capture() );
        final Command<CrudOperation, CrudModel> submitted = outputCaptor.getValue();
        assertEquals( CrudOperation.UPDATE, submitted.commandType );
        assertSame( formModel.getModel(), submitted.value );
    }

    @Test
    public void testDeletionInstanceCallsDeleteCommand() {
        final CrudModel deleteModel = new CrudModel();
        models.add( deleteModel );
        when( formModel.getModel() ).thenReturn( deleteModel );

        // Init view
        listView.start( new ListAsyncDataProviderAdapter<>( models ), callback );
        verify( crudComponent ).init( listView.getCrudActionsHelper() );
        verify( crudComponent ).setEmbedded( false );
        verify( crudComponent ).refresh();

        // Call helper, verify command submitted
        listView.getCrudActionsHelper().deleteInstance( 0 );
        verify( callback ).accept( outputCaptor.capture() );
        final Command<CrudOperation, CrudModel> submitted = outputCaptor.getValue();
        assertEquals( CrudOperation.DELETE, submitted.commandType );
        assertSame( formModel.getModel(), submitted.value );
    }

}
