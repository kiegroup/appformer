/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.renderer.backend.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.codegen.layout.FormLayoutTemplateGenerator;
import org.livespark.formmodeler.codegen.layout.impl.DynamicFormLayoutTemplateGenerator;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.DefaultFieldAnnotationProcessor;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.FieldAnnotationProcessor;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.ListBoxFieldAnnotationProcessor;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.RadioGroupFieldAnnotationProcessor;
import org.livespark.formmodeler.renderer.test.model.Employee;
import org.livespark.formmodeler.renderer.test.model.Person;
import org.livespark.formmodeler.service.FieldManager;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Model2FormTransformerServiceImplTest {

    private Instance<FieldAnnotationProcessor> annotationProcessors;

    private FormLayoutTemplateGenerator layoutTemplateGenerator;

    private Model2FormTransformerServiceImpl service;

    @SuppressWarnings( "unchecked" )
    @Before
    public void init() {
        final FieldManager fieldManager = new MockFieldManager();
        final List<FieldAnnotationProcessor> processors = Arrays.asList( new DefaultFieldAnnotationProcessor( fieldManager ),
                                                                         new ListBoxFieldAnnotationProcessor( fieldManager ),
                                                                         new RadioGroupFieldAnnotationProcessor( fieldManager ) );
        annotationProcessors = mock( Instance.class );
        when( annotationProcessors.iterator() ).then( inv -> processors.iterator() );

        layoutTemplateGenerator = new DynamicFormLayoutTemplateGenerator();

        service = new Model2FormTransformerServiceImpl( annotationProcessors, layoutTemplateGenerator );
    }

    @Test
    public void testFormGeneration() {
        doTest( new Person(), 3 );
    }

    @Test
    public void testFormGenerationWithInheritance() {
        doTest( new Employee(), 6 );
    }

    protected void doTest( Object model, int expectedFields ) {
        FormDefinition form = service.createContext( model ).getRootForm();

        assertNotNull( "Form shouldn't be null!", form );

        assertNotNull( "Form must contain fields!", form.getFields() );

        assertEquals( "Form should have " + expectedFields + " fields", expectedFields, form.getFields().size() );

        for ( FieldDefinition field : form.getFields() ) {
            assertNotNull( "Field should have an ID!", field.getId() );
            assertNotNull( "Field should have a name!", field.getName() );
            assertNotNull( "Field should have a label!", field.getLabel() );
            assertNotNull( "Field should have a model!", field.getModelName() );
        }
    }
}
