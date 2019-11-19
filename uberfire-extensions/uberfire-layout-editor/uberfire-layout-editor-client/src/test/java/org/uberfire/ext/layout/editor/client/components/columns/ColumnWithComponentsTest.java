/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.uberfire.ext.layout.editor.client.components.columns;

import java.util.function.Supplier;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.uberfire.client.mvp.LockRequiredEvent;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;
import org.uberfire.ext.layout.editor.client.AbstractLayoutEditorTest;
import org.uberfire.ext.layout.editor.client.api.LayoutEditorElement;
import org.uberfire.ext.layout.editor.client.components.rows.Row;
import org.uberfire.ext.layout.editor.client.infra.ColumnDrop;
import org.uberfire.mvp.ParameterizedCommand;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ColumnWithComponentsTest extends AbstractLayoutEditorTest {

    @Mock
    private Event<LockRequiredEvent> lockRequiredEvent;

    @Spy
    @InjectMocks
    private ColumnWithComponents columnWithComponents;

    @Mock
    private ParameterizedCommand<ColumnDrop> dropCommand;

    @Test
    public void testOnDrop() throws Exception {
        loadLayout(SINGLE_ROW_COMPONENT_LAYOUT_WITH_PARTS);
        columnWithComponents.onDrop(ColumnDrop.Orientation.UP, "this-is-a-requirement-to-firefox-html5dnd");
        verify(lockRequiredEvent,
               times(1)).fire(any(LockRequiredEvent.class));
    }

}
