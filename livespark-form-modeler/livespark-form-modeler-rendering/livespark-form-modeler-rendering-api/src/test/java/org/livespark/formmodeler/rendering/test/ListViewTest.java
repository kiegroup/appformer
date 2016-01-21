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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.livespark.formmodeler.rendering.client.view.ListItemView;
import org.livespark.formmodeler.rendering.client.view.display.modal.ModalFormDisplayer;
import org.livespark.formmodeler.rendering.client.view.util.ListViewActionsHelper;
import org.livespark.formmodeler.rendering.test.res.TestFormModel;
import org.livespark.formmodeler.rendering.test.res.TestListView;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith( GwtMockitoTestRunner.class )
public class ListViewTest {

    @InjectMocks
    private TestListView listView;

    @GwtMock
    private ListWidget<TestFormModel, ListItemView<TestFormModel>> listWidget;

    @GwtMock
    private ListItemView<TestFormModel> listItemView;

    @GwtMock
    private FormView<TestFormModel> formView;

    @GwtMock
    private ClickEvent clickEvent;

    @Mock
    private ListViewActionsHelper listViewActionsHelper;

    @GwtMock
    private ModalFormDisplayer modalFormDisplayer;

    @Mock
    private RemoteCallback callback;

    @Mock
    private TestFormModel formModel;

    @Mock
    private LiveSparkRestService<TestFormModel> restService;

    @Mock
    private List<TestFormModel> models;

    // This model won't be injected into anything.
    private TestFormModel differentModel = new TestFormModel();

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
            assertNotNull( listView.lastLoadDataCallback );
        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        final List<TestFormModel> models = Arrays.asList( formModel );
        listView.lastLoadDataCallback.callback( models );
        verify( listWidget ).setValue( models );
    }

    @Test
    public void callbackParameterOfLoadDataSetsParentViewOfListItemView() throws Exception {
        final List<TestFormModel> response = Arrays.asList( formModel );

        try {
            initLoadsDataOnce();
            assertNotNull( listView.lastLoadDataCallback );

            when( listWidget.getValue() ).thenReturn( response );
            when( listWidget.getComponent( formModel ) ).thenReturn( listItemView );
        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        listView.lastLoadDataCallback.callback( response );
        verify( listItemView ).setParentView( listView );
    }

    @Test
    public void modalIsShownOnCreateButtonClick() throws Exception {
        listView.onCreateClick( clickEvent );

    }

    @Test
    public void modalHasSubmitAndCancelCallbacksSetOnCreateButtonClick() throws Exception {
        listView.onCreateClick( clickEvent );

    }

    @Test
    public void createModalSubmitHandlerCallsRestCreateAndModalHide() throws Exception {
        try {
            listView.setActionsHelper( listViewActionsHelper );
            when( listViewActionsHelper.getFormDisplayer() ).thenReturn( modalFormDisplayer );
            doAnswer( new Answer() {
                @Override
                public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
                    listView.createRestCaller( callback ).create( formModel );
                    return null;
                }
            } ).when( modalFormDisplayer ).onSubmit();

            listView.onCreateClick( clickEvent );

            when( formView.validate() ).thenReturn( true );
            when( formView.getModel() ).thenReturn( formModel );

        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        verify( listViewActionsHelper ).startCreate( listView );

        modalFormDisplayer.onSubmit();

        verify( restService ).create( formModel );
        verifyNoMoreInteractions( restService );
    }

    @Test
    public void createCallbackAddsModelToListAndSetsParentView() throws Exception {
        try {
            listView.setActionsHelper( listViewActionsHelper );

            when( listViewActionsHelper.getFormDisplayer() ).thenReturn( modalFormDisplayer );
            doAnswer( new Answer() {
                @Override
                public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
                    listView.createRestCaller( callback ).create( formModel );
                    models.add( differentModel );
                    listItemView.setParentView( listView );
                    return null;
                }
            } ).when( modalFormDisplayer ).onSubmit();

            when( formView.validate() ).thenReturn( true );
            when( formView.getModel() ).thenReturn( formModel );

            listView.lastLoadDataCallback = null;
            modalFormDisplayer.onSubmit();

            verify( restService ).create( formModel );
            verifyNoMoreInteractions( restService );
            assertNotNull( listView.lastLoadDataCallback );


            when( listWidget.getValue() ).thenReturn( models );
            when( listWidget.getComponent( differentModel ) ).thenReturn( listItemView );
        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        listView.lastLoadDataCallback.callback( differentModel );

        verify( models ).add( differentModel );
        verify( listItemView ).setParentView( listView );
        verifyNoMoreInteractions( models, listItemView );
    }

    @Test
    public void onDeleteCallsRestDelete() throws Exception {
        doAnswer( new Answer() {
            @Override
            public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
                listView.createRestCaller( callback ).delete( differentModel );
                return null;
            }
        } ).when( listViewActionsHelper ).delete(differentModel );

        listView.onDelete( differentModel );

        verify( restService ).delete( differentModel );
        verifyNoMoreInteractions( restService );
    }

    @Test
    public void onDeleteCallbackRemovesModelFromList() throws Exception {
        try {
            doAnswer( new Answer() {
                @Override
                public TestFormModel answer( InvocationOnMock invocationOnMock ) throws Throwable {
                    models.remove( differentModel );
                    return null;
                }
            } ).when( listViewActionsHelper ).delete( differentModel );
            listView.onDelete( differentModel );
            assertNotNull( listView.lastLoadDataCallback );
            when( listWidget.getValue() ).thenReturn( models );

        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        listView.lastLoadDataCallback.callback( true );

        verify( models ).remove( differentModel );
        verifyNoMoreInteractions( models );
    }

    @Test
    public void onEditDisplaysModalWithCorrectModel() throws Exception {
        doAnswer( new Answer() {
            @Override
            public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
                formView.setModel( formModel );
                return null;
            }
        } ).when( listViewActionsHelper ).startEdit( listView, formModel );

        listView.onEdit( formModel );

        verify( formView ).setModel( formModel );
    }

    @Test
    public void editModalSubmitCallbackCallsRestUpdateAndHidesModal() throws Exception {
        try {
            doAnswer( new Answer() {
                @Override
                public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
                    listView.createRestCaller( callback ).update( formModel );
                    return null;
                }
            } ).when( listViewActionsHelper ).startEdit( listView, formModel );

            listView.onEdit( formModel );

            when( formView.validate() ).thenReturn( true );
            when( formView.getModel() ).thenReturn( formModel );
        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        verify( restService ).update( formModel );
    }

    @Test
    public void updateCallbackDoesNotModifyList() throws Exception {
        try {
            listView.setActionsHelper( listViewActionsHelper );
            when( listViewActionsHelper.getFormDisplayer() ).thenReturn( modalFormDisplayer );
            doAnswer( new Answer() {
                @Override
                public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
                    listView.createRestCaller( callback ).update( formModel );
                    return null;
                }
            } ).when( modalFormDisplayer ).onSubmit();

            listView.onEdit( formModel );

            verify( listViewActionsHelper ).startEdit( listView, formModel );

            when( formView.validate() ).thenReturn( true );
            when( formView.getModel() ).thenReturn( formModel );

            modalFormDisplayer.onSubmit();

            verify( restService ).update( formModel );
            verifyNoMoreInteractions( restService );
            assertNotNull( listView.lastLoadDataCallback );
        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        listView.lastLoadDataCallback.callback( true );

        verifyNoMoreInteractions( listWidget, restService );
    }

    private void failedPrecondition( final Throwable cause ) {
        throw new AssertionError( "Precondtion failed.", cause );
    }
}
