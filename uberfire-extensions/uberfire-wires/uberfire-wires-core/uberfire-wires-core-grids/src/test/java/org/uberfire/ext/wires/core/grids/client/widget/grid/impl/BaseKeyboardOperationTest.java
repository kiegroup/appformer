/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.ext.wires.core.grids.client.widget.grid.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.ait.lienzo.test.LienzoMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.ext.wires.core.grids.client.model.GridColumn;
import org.uberfire.ext.wires.core.grids.client.model.GridData;
import org.uberfire.ext.wires.core.grids.client.model.GridRow;
import org.uberfire.ext.wires.core.grids.client.model.impl.BaseBounds;
import org.uberfire.ext.wires.core.grids.client.widget.grid.GridWidget;
import org.uberfire.ext.wires.core.grids.client.widget.grid.renderers.grids.GridRenderer;
import org.uberfire.ext.wires.core.grids.client.widget.grid.renderers.grids.impl.BaseGridRendererHelper;
import org.uberfire.ext.wires.core.grids.client.widget.layer.impl.DefaultGridLayer;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_LEFT;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_RIGHT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LienzoMockitoTestRunner.class)
public class BaseKeyboardOperationTest {

    private static final double BOUNDS_WIDTH = 1000.0;
    private static final double BOUNDS_HEIGHT = 1000.0;

    @Mock
    private DefaultGridLayer layer;

    @Mock
    private GridWidget gridWidget;

    @Mock
    private GridData gridData;

    @Mock
    private GridColumn gridColumn;

    @Mock
    private GridRow gridRow;

    @Mock
    private BaseGridRendererHelper baseGridRendererHelperMock;

    @Mock
    private BaseGridRendererHelper.RenderingInformation baseGridRendererInformationMock;

    @Mock
    private GridRenderer gridRenderer;

    private BaseKeyboardOperation baseKeyboardOperationSpy;
    private int currentKeyCode = 0;

    @Before
    public void setup() {
        when(gridWidget.getModel()).thenReturn(gridData);
        when(gridWidget.getRendererHelper()).thenReturn(baseGridRendererHelperMock);
        when(baseGridRendererHelperMock.getRenderingInformation()).thenReturn(baseGridRendererInformationMock);
        when(gridData.getColumns()).thenReturn(Collections.singletonList(gridColumn));
        when(gridColumn.getIndex()).thenReturn(0);
        when(gridWidget.getRenderer()).thenReturn(gridRenderer);
        when(layer.getVisibleBounds()).thenReturn(new BaseBounds(0, 0, BOUNDS_WIDTH, BOUNDS_HEIGHT));

        baseKeyboardOperationSpy = spy(new BaseKeyboardOperation(layer) {
            @Override
            public int getKeyCode() {
                return currentKeyCode;
            }

            @Override
            public TriStateBoolean isControlKeyDown() {
                return super.isControlKeyDown();
            }

            @Override
            public boolean perform(GridWidget gridWidget, boolean isShiftKeyDown, boolean isControlKeyDown) {
                return false;
            }
        });
    }

    @Test
    public void scrollSelectedCellIntoView_NoSelection() {
        assertFalse(baseKeyboardOperationSpy.scrollSelectedCellIntoView(gridWidget));
    }

    @Test
    public void scrollSelectedCellIntoView_NoRenderedInformation() {
        List<GridData.SelectedCell> selectedCells = Arrays.asList(new GridData.SelectedCell(0,0));
        when(gridData.getSelectedHeaderCells()).thenReturn(selectedCells);
        when(baseGridRendererHelperMock.getRenderingInformation()).thenReturn(null);
        assertFalse(baseKeyboardOperationSpy.scrollSelectedCellIntoView(gridWidget));
    }

    @Test
    public void scrollSelectedCellIntoView_HeaderSelected() {
        when(gridData.getSelectedHeaderCells()).thenReturn(Collections.singletonList(new GridData.SelectedCell(0,0)));
        assertTrue(baseKeyboardOperationSpy.scrollSelectedCellIntoView(gridWidget));
    }

    @Test
    public void scrollSelectedCellIntoView_CellSelected() {
        when(gridData.getSelectedCellsOrigin()).thenReturn(new GridData.SelectedCell(0,0));
        when(gridData.getRow(0)).thenReturn(gridRow);
        when(gridRow.getHeight()).thenReturn(30d);
        assertTrue(baseKeyboardOperationSpy.scrollSelectedCellIntoView(gridWidget));
    }

    @Test
    public void getSelectedCellOrigin_NotHeaderCell() {
        when(gridData.getSelectedCellsOrigin()).thenReturn(new GridData.SelectedCell(0,0));
        baseKeyboardOperationSpy.getSelectedCellOrigin(gridData, false);
        verify(gridData, times(1)).getSelectedCellsOrigin();
        verify(gridData, never()).getSelectedHeaderCells();

    }

    @Test
    public void getSelectedCellOrigin_SingleHeaderCell_KeyRight() {
        currentKeyCode = KEY_RIGHT;
        GridData.SelectedCell headerCell = new GridData.SelectedCell(0, 0);
        when(gridData.getSelectedHeaderCells()).thenReturn(Collections.singletonList(headerCell));
        assertEquals(headerCell, baseKeyboardOperationSpy.getSelectedCellOrigin(gridData, true));
        verify(gridData, times(1)).getSelectedHeaderCells();
        verify(gridData, never()).getSelectedCellsOrigin();
    }

    @Test
    public void getSelectedCellOrigin_SingleHeaderCell_KeyLeft() {
        currentKeyCode = KEY_LEFT;
        GridData.SelectedCell headerCell = new GridData.SelectedCell(0, 0);
        when(gridData.getSelectedHeaderCells()).thenReturn(Collections.singletonList(headerCell));
        assertEquals(headerCell, baseKeyboardOperationSpy.getSelectedCellOrigin(gridData, true));
        verify(gridData, times(1)).getSelectedHeaderCells();
        verify(gridData, never()).getSelectedCellsOrigin();
    }

    @Test
    public void getSelectedCellOrigin_MultipleHeaderCell_KeyRight() {
        currentKeyCode = KEY_RIGHT;
        GridData.SelectedCell headerCell = new GridData.SelectedCell(0, 0);
        GridData.SelectedCell headerCell2 = new GridData.SelectedCell(0, 1);
        when(gridData.getSelectedHeaderCells()).thenReturn(Arrays.asList(headerCell, headerCell2));
        assertEquals(headerCell2, baseKeyboardOperationSpy.getSelectedCellOrigin(gridData, true));
        verify(gridData, times(1)).getSelectedHeaderCells();
        verify(gridData, never()).getSelectedCellsOrigin();
    }

    @Test
    public void getSelectedCellOrigin_MultipleHeaderCell_KeyLeft() {
        currentKeyCode = KEY_LEFT;
        GridData.SelectedCell headerCell = new GridData.SelectedCell(0, 0);
        GridData.SelectedCell headerCell2 = new GridData.SelectedCell(0, 1);
        when(gridData.getSelectedHeaderCells()).thenReturn(Arrays.asList(headerCell, headerCell2));
        assertEquals(headerCell, baseKeyboardOperationSpy.getSelectedCellOrigin(gridData, true));
        verify(gridData, times(1)).getSelectedHeaderCells();
        verify(gridData, never()).getSelectedCellsOrigin();
    }
}
