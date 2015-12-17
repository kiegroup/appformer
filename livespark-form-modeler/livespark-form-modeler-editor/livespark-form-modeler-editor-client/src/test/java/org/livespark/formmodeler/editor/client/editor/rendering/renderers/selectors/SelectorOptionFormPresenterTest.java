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

package org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors;

import java.util.ArrayList;
import java.util.List;

import com.google.gwtmockito.GwtMockitoTestRunner;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.editor.client.editor.rendering.DraggableFieldComponent;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionRequest;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionResponse;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionUpdate;
import org.livespark.formmodeler.editor.model.impl.basic.selectors.SelectorOption;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.mocks.EventSourceMock;

import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class SelectorOptionFormPresenterTest extends TestCase {

    protected SelectorOptionFormPresenter presenter;

    @Mock
    protected SelectorOptionFormPresenter.SelectorOptionFormView view;

    @Mock
    protected EventSourceMock<FieldSelectorOptionRequest> requestEventMock;

    @Mock
    protected EventSourceMock<FieldSelectorOptionUpdate> updateEventMock;

    @Mock
    protected PropertyEditorFieldInfo propertyEditorFieldInfo;

    @Before
    public void initTest() {
        presenter = new SelectorOptionFormPresenter( view,
                requestEventMock,
                updateEventMock,
                new SelectorOptionFormPresenter.ParamsReader() {
                    @Override
                    public String getFormId( String params ) {
                        return DraggableFieldComponent.FORM_ID;
                    }

                    @Override
                    public String getFieldId( String params ) {
                        return DraggableFieldComponent.FIELD_ID;
                    }
        } );

        when( propertyEditorFieldInfo.getCurrentStringValue() ).thenAnswer( new Answer<String>() {
            @Override
            public String answer( InvocationOnMock invocation ) throws Throwable {
                String jsonResponse = "{ \"" + DraggableFieldComponent.FORM_ID + "\":\""
                        + DraggableFieldComponent.FORM_ID + "\","
                        + "\"" + DraggableFieldComponent.FIELD_ID + "\":\""
                        + DraggableFieldComponent.FIELD_ID + "\" }";
                return jsonResponse;
            }
        } );

        verify( view ).setPresenter( presenter );
    }

    @Test
    public void testEmptyOptions() {
        presenter.widget( propertyEditorFieldInfo );
        verify( view ).asWidget();

        ArgumentCaptor<FieldSelectorOptionRequest> requestCaptor = ArgumentCaptor.forClass( FieldSelectorOptionRequest.class );

        verify( requestEventMock ).fire( requestCaptor.capture() );

        assertEquals( "FormId param should be '" + DraggableFieldComponent.FORM_ID + "'.", DraggableFieldComponent.FORM_ID, requestCaptor.getValue().getFormId() );
        assertEquals( "FieldId param should be '" + DraggableFieldComponent.FIELD_ID + "'.", DraggableFieldComponent.FIELD_ID, requestCaptor.getValue().getFieldId() );

        presenter.onResponse( new FieldSelectorOptionResponse( DraggableFieldComponent.FORM_ID,
                DraggableFieldComponent.FIELD_ID,
                new ArrayList<SelectorOption>() ) );

        verify( view ).setOptions( anyList() );

        ArgumentCaptor<FieldSelectorOptionUpdate> updateCaptor = ArgumentCaptor.forClass( FieldSelectorOptionUpdate.class );

        presenter.addOption( "option1", "option1" );

        verify( view, times( 2 ) ).setOptions( anyList() );

        verify( updateEventMock ).fire( updateCaptor.capture() );

        assertEquals( "There should be at least 1 option ", 1, updateCaptor.getValue().getOptions().size() );

        assertTrue( "There should exist 'option1' option.", presenter.existOption( "option1" ) );

        presenter.addOption( "option2", "option2" );

        verify( view, times( 3 ) ).setOptions( anyList() );

        presenter.addOption( "option3", "option3" );

        verify( view, times( 4 ) ).setOptions( anyList() );

        verify( updateEventMock, times( 3 ) ).fire( updateCaptor.capture() );

        List<SelectorOption> options = updateCaptor.getValue().getOptions();
        assertEquals( "There should be at least 3 option ", 3, options.size() );

        assertTrue( "There should exist 'option2' option.", presenter.existOption( "option2" ) );
        assertTrue( "There should exist 'option3' option.", presenter.existOption( "option3" ) );

        SelectorOption option1 = options.get( 0 );

        presenter.setDefaultValue( option1 );

        verify( view, times( 5 ) ).setOptions( anyList() );

        verify( updateEventMock, times( 4 ) ).fire( updateCaptor.capture() );
        assertTrue( "Option1 should be marked as default value.", option1.getDefaultValue() );
        assertTrue( "Presenter should have Option1 as a default value", presenter.getDefaultValue().equals( option1 ) );

        SelectorOption option2 = options.get( 1 );
        presenter.setDefaultValue( option2 );

        verify( view, times( 6 ) ).setOptions( anyList() );

        verify( updateEventMock, times( 5 ) ).fire( updateCaptor.capture() );
        assertFalse( "Option1 shouldn't be marked as default value.", option1.getDefaultValue() );
        assertTrue( "Option2 should be marked as default value.", option2.getDefaultValue() );

        presenter.removeOption( option2 );
        verify( view, times( 7 ) ).setOptions( anyList() );
        verify( updateEventMock, times( 6 ) ).fire( updateCaptor.capture() );
        assertEquals( "There should be at least 2 option ", 2, options.size() );
        assertFalse( "Option2 shouldn't exist", updateCaptor.getValue().getOptions().contains( option2 ) );
        assertNull( "Presenter shouldn't have a default value", presenter.getDefaultValue() );

    }
}
