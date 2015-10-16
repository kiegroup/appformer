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

package org.livespark.formmodeler.rendering.client.view.display.embedded;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.livespark.formmodeler.rendering.client.view.display.FormDisplayer;
import org.livespark.formmodeler.rendering.client.view.display.FormDisplayerConfig;
import org.livespark.formmodeler.rendering.test.res.TestFormModel;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class EmbeddedFormDisplayerTest extends TestCase {

    private EmbeddedFormDisplayer displayer;

    @Mock
    private FormDisplayerConfig displayerConfig;

    @GwtMock
    private EmbeddedFormDisplayerViewImpl displayerView;

    @Mock
    private TestFormModel formModel;

    @GwtMock
    private FormView formView;

    @Mock
    private FormDisplayer.FormDisplayerCallback displayerCallback;


    @Before
    public void setup() {
        when( displayerConfig.getFormView() ).thenReturn( formView );
        when( displayerConfig.getCallback() ).thenReturn( displayerCallback );
        when( displayerConfig.getFormTitle() ).thenReturn( "Form Title" );

        displayer = new EmbeddedFormDisplayer( displayerView );

        verify( displayerView ).setPresenter( displayer );

        displayer.asWidget();

        verify( displayerView ).asWidget();
    }

    @Test
    public void testCancelForm() {
        displayer.display( displayerConfig );

        verify( displayerView ).show( formView );

        displayer.onCancel();

        verify( displayerCallback ).onCancel();

        verify( displayerView ).clear();

        assertNull( "DisplayerConfig should be null", displayer.getFormDisplayerConfig() );

    }

    @Test
    public void testSubmitFormValidationPassed() {
        testSubmitForm( true );

        verify( displayerCallback ).onSubmit();

        verify( displayerView ).clear();

        assertNull( "DisplayerConfig should be null", displayer.getFormDisplayerConfig() );
    }

    @Test
    public void testSubmitFormValidationFailed() {
        testSubmitForm( false );

        verify( displayerCallback, times( 0 ) ).onSubmit();

        verify( displayerView, times( 0 ) ).clear();

        assertNotNull( "DisplayerConfig shouldn't be null", displayer.getFormDisplayerConfig() );
    }

    private void testSubmitForm( final boolean validate ) {
        when( formView.validate() ).thenReturn( validate );

        displayer.display( displayerConfig );

        verify( displayerView ).show( formView );

        displayer.onSubmit();

        verify( formView ).validate();
    }

}
