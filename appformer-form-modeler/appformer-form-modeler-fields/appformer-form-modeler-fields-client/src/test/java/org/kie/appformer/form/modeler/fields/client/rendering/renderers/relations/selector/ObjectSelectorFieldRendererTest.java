/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.form.modeler.fields.client.rendering.renderers.relations.selector;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.appformer.form.modeler.fields.shared.fieldTypes.relations.objectSelector.definition.ObjectSelectorFieldDefinition;
import org.kie.workbench.common.forms.common.rendering.client.widgets.flatViews.impl.ObjectFlatView;
import org.kie.workbench.common.forms.common.rendering.client.widgets.typeahead.BindableTypeAhead;
import org.kie.workbench.common.forms.dynamic.client.helper.MapModelBindingHelper;
import org.kie.workbench.common.forms.processing.engine.handling.FieldChangeHandler;
import org.kie.workbench.common.forms.processing.engine.handling.FormHandler;
import org.mockito.Mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class ObjectSelectorFieldRendererTest extends TestCase {

    public static final String FIELD_MASK = "Random: {mask}";

    private ObjectSelectorFieldRenderer fieldRenderer;

    @GwtMock
    private BindableTypeAhead widgetMock;

    @Mock
    private ObjectSelectorFieldDefinition fieldMock;

    @Mock
    private FieldChangeHandler changeHandler;

    @Mock
    private FormHandler formHandler;

    @Mock
    private MapModelBindingHelper helper;

    @Before
    public void initTest() {
        when(fieldMock.getMask()).thenReturn(FIELD_MASK);
        fieldRenderer = new ObjectSelectorFieldRenderer() {
            {
                widget = widgetMock;
                field = fieldMock;
            }
        };
    }

    @Test
    public void testGetName() {
        String name = fieldRenderer.getName();
        assertEquals(ObjectSelectorFieldDefinition.FIELD_TYPE.getTypeName(),
                     name);
    }

    @Test
    public void testInitInputWidget() {
        fieldRenderer.initInputWidget();
        verify(widgetMock).init(anyString(),
                                any());
    }

    @Test
    public void testGetInputWidget() {
        IsWidget widget = fieldRenderer.getInputWidget();
        assertSame(widgetMock,
                   widget);
    }

    @Test
    public void testGetSupportedCode() {
        String name = fieldRenderer.getSupportedCode();
        assertEquals(ObjectSelectorFieldDefinition.FIELD_TYPE.getTypeName(),
                     name);
    }

    @Test
    public void testSetReadOnlyTrue() {
        testSetReadOnly(true);
    }

    @Test
    public void testSetReadOnlyFalse() {
        testSetReadOnly(false);
    }

    public void testSetReadOnly(boolean value) {
        fieldRenderer.setReadOnly(value);
        verify(widgetMock).setReadOnly(eq(value));
    }

    @Test
    public void testGetPrettyViewWidget() {
        IsWidget widget = fieldRenderer.getPrettyViewWidget();
        assertTrue(widget instanceof ObjectFlatView);
    }

    @Test
    public void testDatasetFindMatches() {
        fieldRenderer.dataset.findMatches(null,
                                          null);
    }
}
