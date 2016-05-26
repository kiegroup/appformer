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

package org.livespark.formmodeler.renderer.client;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.processing.engine.handling.FieldChangeHandler;
import org.livespark.formmodeler.processing.engine.handling.FormHandler;
import org.livespark.formmodeler.renderer.client.rendering.FieldLayoutComponent;
import org.livespark.formmodeler.renderer.client.rendering.FieldRenderer;
import org.livespark.formmodeler.renderer.client.rendering.renderers.relations.subform.SubFormWidget;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.livespark.formmodeler.renderer.service.Model2FormTransformerService;
import org.livespark.formmodeler.renderer.test.model.Employee;
import org.livespark.formmodeler.renderer.test.util.TestFormGenerator;
import org.livespark.formmodeler.rendering.client.view.validation.FormViewValidator;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mvp.Command;

import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class DynamicFormRendererTest extends TestCase {

    private FieldLayoutComponent component;

    private FieldRenderer fieldRenderer;

    @GwtMock
    private SubFormWidget widget;

    @Mock
    private FieldChangeHandler changeHandler;

    @Mock
    private FormHandler formHandler;

    private DynamicFormRenderer.DynamicFormRendererView view;

    private Model2FormTransformerService model2FormTransformerService;

    private CallerMock<Model2FormTransformerService> transformer;

    private FormViewValidator formViewValidator;

    private DynamicFormRenderer renderer;

    private Employee employee = new Employee();

    @Before
    public void initTest() {
        component = mock(FieldLayoutComponent.class);
        view = mock( DynamicFormRenderer.DynamicFormRendererView.class );
        fieldRenderer = mock( FieldRenderer.class );
        model2FormTransformerService = mock( Model2FormTransformerService.class );
        transformer = new CallerMock<>( model2FormTransformerService );
        formViewValidator = mock( FormViewValidator.class );

        when( model2FormTransformerService.createContext( any( Employee.class ) ) ).thenAnswer( new Answer<FormRenderingContext>() {
            @Override
            public FormRenderingContext answer( InvocationOnMock invocationOnMock ) throws Throwable {
                return TestFormGenerator.getContextForEmployee( employee );
            }
        } );

        when( view.getFieldLayoutComponentForField( any( FieldDefinition.class) ) ).thenReturn( component );

        when( component.getFieldRenderer() ).thenReturn( fieldRenderer );
        when( fieldRenderer.getInputWidget() ).thenReturn( widget );

        renderer = new DynamicFormRendererMock( view, transformer, formHandler );
        renderer.init();
        verify( view ).setPresenter( renderer );
        renderer.asWidget();
        verify( view ).asWidget();
    }

    @Test
    public void testBaseBinding() {
        doBind();

        unBind();
    }

    @Test
    public void testBindingAddingFieldChangeHandler() {
        doBind();

        renderer.addFieldChangeHandler( changeHandler );

        renderer.addFieldChangeHandler( "name", changeHandler );

        renderer.addFieldChangeHandler( "address", changeHandler );

        verify( formHandler ).addFieldChangeHandler( any() );
        verify( formHandler, times(2) ).addFieldChangeHandler( anyString(), any() );

        unBind();
    }

    protected void doBind() {
        renderer.renderDefaultForm( employee, new Command() {
            @Override
            public void execute() {
                verify( view ).render( any() );
                verify( view ).bind();
                verify( formHandler ).setUp( any( Employee.class ) );
            }
        } );
    }

    protected void unBind() {
        renderer.isValid();
        renderer.unBind();
        verify( formHandler ).clear();
    }
}
