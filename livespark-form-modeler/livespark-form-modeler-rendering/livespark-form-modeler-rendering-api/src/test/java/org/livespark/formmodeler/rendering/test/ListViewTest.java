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
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.jboss.errai.ui.client.widget.ListWidget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.livespark.formmodeler.rendering.client.view.FormViewModal;
import org.livespark.formmodeler.rendering.client.view.ListItemView;
import org.livespark.formmodeler.rendering.test.res.TestFormModel;
import org.livespark.formmodeler.rendering.test.res.TestListView;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;

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
    private FormViewModal modalForm;

    @GwtMock
    private ClickEvent clickEvent;

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
        verify( listWidget ).setItems( models );
    }

    @Test
    public void callbackParameterOfLoadDataSetsParentViewOfListItemView() throws Exception {
        final List<TestFormModel> response = Arrays.asList( formModel );

        try {
            initLoadsDataOnce();
            assertNotNull( listView.lastLoadDataCallback );

            when( listWidget.getValue() ).thenReturn( response );
            when( listWidget.getWidget( formModel ) ).thenReturn( listItemView );
        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        listView.lastLoadDataCallback.callback( response );
        verify( listItemView ).setParentView( listView );
    }

    @Test
    public void modalIsShownOnCreateButtonClick() throws Exception {
        listView.onCreateClick( clickEvent );

        verify( modalForm ).show();
    }

    @Test
    public void modalHasSubmitAndCancelCallbacksSetOnCreateButtonClick() throws Exception {
        listView.onCreateClick( clickEvent );

        verify( modalForm ).addSubmitClickHandler( notNull( ClickHandler.class ) );
        verify( modalForm ).addCancelClickHandler( notNull( ClickHandler.class ) );
    }

    @Test
    public void createModalCancelHandlerCallsHide() throws Exception {
        final ArgumentCaptor<ClickHandler> handlerCaptor = ArgumentCaptor.forClass( ClickHandler.class );

        try {
            listView.onCreateClick( clickEvent );
            verify( modalForm ).addCancelClickHandler( handlerCaptor.capture() );
        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        handlerCaptor.getValue().onClick( clickEvent );

        verify( modalForm ).hide();
        verifyNoMoreInteractions( restService, listWidget );
    }

    @Test
    public void createModalSubmitHandlerCallsRestCreateAndModalHide() throws Exception {
        final ArgumentCaptor<ClickHandler> handlerCaptor = ArgumentCaptor.forClass( ClickHandler.class );
        try {
            listView.onCreateClick( clickEvent );

            verify( modalForm ).addSubmitClickHandler( handlerCaptor.capture() );
            when( formView.validate() ).thenReturn( true );
            when( formView.getModel() ).thenReturn( formModel );

        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        handlerCaptor.getValue().onClick( clickEvent );

        verify( restService ).create( formModel );
        verifyNoMoreInteractions( restService );
        verify( modalForm ).hide();
    }

    @Test
    public void createCallbackAddsModelToListAndSetsParentView() throws Exception {
        try {
            listView.onCreateClick( clickEvent );
            final ArgumentCaptor<ClickHandler> captor = ArgumentCaptor.forClass( ClickHandler.class );
            verify( modalForm ).addSubmitClickHandler( captor.capture() );
            final ClickHandler submitHandler = captor.getValue();
            assertNotNull( submitHandler );

            when( formView.validate() ).thenReturn( true );
            when( formView.getModel() ).thenReturn( formModel );

            listView.lastLoadDataCallback = null;
            submitHandler.onClick( clickEvent );

            verify( restService ).create( formModel );
            verifyNoMoreInteractions( restService );
            assertNotNull( listView.lastLoadDataCallback );


            when( listWidget.getValue() ).thenReturn( models );
            when( listWidget.getWidget( differentModel ) ).thenReturn( listItemView );
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
        listView.onDelete( differentModel );

        verify( restService ).delete( differentModel );
        verifyNoMoreInteractions( restService );
    }

    @Test
    public void onDeleteCallbackRemovesModelFromList() throws Exception {
        try {
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
        final TestFormModel differentFormModel = new TestFormModel();
        listView.onEdit( differentFormModel );

        verify( formView ).setModel( differentFormModel );
        verify( modalForm ).show();
    }

    @Test
    public void onEditAttachesSubmitAndCancelHandlers() throws Exception {
        listView.onEdit( formModel );

        verify( modalForm ).addSubmitClickHandler( notNull( ClickHandler.class ) );
        verify( modalForm ).addCancelClickHandler( notNull( ClickHandler.class ) );
    }

    @Test
    public void editModalSubmitCallbackCallsRestUpdateAndHidesModal() throws Exception {
        final ArgumentCaptor<ClickHandler> captor = ArgumentCaptor.forClass( ClickHandler.class );
        try {
            listView.onEdit( formModel );
            verify( modalForm ).addSubmitClickHandler( captor.capture() );
            assertNotNull( captor.getValue() );

            when( formView.validate() ).thenReturn( true );
            when( formView.getModel() ).thenReturn( formModel );
        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        captor.getValue().onClick( clickEvent );

        verify( restService ).update( formModel );
        verify( modalForm ).hide();
    }

    @Test
    public void editModalCancelCallbackHidesModal() throws Exception {
        final ArgumentCaptor<ClickHandler> captor = ArgumentCaptor.forClass( ClickHandler.class );
        try {
            listView.onEdit( formModel );
            verify( modalForm ).addCancelClickHandler( captor.capture() );
            assertNotNull( captor.getValue() );
        } catch ( RuntimeException e ) {
            failedPrecondition( e );
        }

        captor.getValue().onClick( clickEvent );
        verify( modalForm ).hide();
        verifyNoMoreInteractions( restService, listWidget );
    }


    @Test
    public void updateCallbackDoesNotModifyList() throws Exception {
        try {
            listView.onEdit( formModel );
            final ArgumentCaptor<ClickHandler> captor = ArgumentCaptor.forClass( ClickHandler.class );
            verify( modalForm ).addSubmitClickHandler( captor.capture() );

            when( formView.validate() ).thenReturn( true );
            when( formView.getModel() ).thenReturn( formModel );
            captor.getValue().onClick( clickEvent );

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
