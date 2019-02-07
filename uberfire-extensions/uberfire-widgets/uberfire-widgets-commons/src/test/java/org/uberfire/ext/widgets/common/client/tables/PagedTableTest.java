/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.ext.widgets.common.client.tables;

import com.google.gwt.user.client.Element;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.gwtmockito.WithClassesToStub;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Text;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import static org.uberfire.ext.widgets.common.client.tables.PagedTable.DEFAULT_PAGE_SIZE;

@RunWith(GwtMockitoTestRunner.class)
@WithClassesToStub({Image.class, Label.class, Text.class})
public class PagedTableTest {

    @GwtMock
    AsyncDataProvider dataProvider;

    @GwtMock
    Select select;

    @Test
    public void testSetDataProvider() throws Exception {
        PagedTable pagedTable = new PagedTable();

        pagedTable.setDataProvider(dataProvider);
        verify(dataProvider).addDataDisplay(pagedTable);
    }

    @Test
    public void testDataGridHeight() throws Exception {
        final int PAGE_SIZE = 10;
        final int ROWS = 2;
        final int EXPECTED_HEIGHT_PX = PagedTable.HEIGHT_OFFSET_PX;
        PagedTable pagedTable = new PagedTable(PAGE_SIZE, null, null, false, false, false);
        pagedTable.dataGrid = spy(pagedTable.dataGrid);
        when(pagedTable.dataGrid.getRowCount()).thenReturn(ROWS);

        verify(pagedTable.dataGrid,
               times(0)).setHeight(anyString());
        pagedTable.loadPageSizePreferences();
        verify(pagedTable.dataGrid,
               times(1)).setHeight(eq(EXPECTED_HEIGHT_PX + "px"));
    }

    @Test
    public void testDataGridHeightWithMoreItemsThanPaging() throws Exception {
        final int PAGE_SIZE = 10;
        final int ROWS = 12;
        final int EXPECTED_HEIGHT_PX = PagedTable.HEIGHT_OFFSET_PX;
        PagedTable pagedTable = new PagedTable(PAGE_SIZE, null, null, false, false, false);
        pagedTable.dataGrid = spy(pagedTable.dataGrid);
        when(pagedTable.dataGrid.getRowCount()).thenReturn(ROWS);

        verify(pagedTable.dataGrid,
               times(0)).setHeight(anyString());
        pagedTable.loadPageSizePreferences();
        verify(pagedTable.dataGrid,
               times(1)).setHeight(eq(EXPECTED_HEIGHT_PX + "px"));
    }

    @Test
    public void testLoadPageSizePreferencesResetsPageStart() throws Exception {
        final int PAGE_SIZE = 10;

        PagedTable pagedTable = new PagedTable(PAGE_SIZE);
        pagedTable.dataGrid = spy(pagedTable.dataGrid);

        verify(pagedTable.dataGrid,
               times(0)).setPageStart(0);

        pagedTable.loadPageSizePreferences();
        verify(pagedTable.dataGrid,
               times(1)).setPageStart(0);
    }


    @Test
    public void testPageSizeSelectStartValue() throws Exception {
        final int size = 10;

        new PagedTable(size);

        verify(select).setValue(String.valueOf(size));
        verify(select).addValueChangeHandler(any());
    }

    @Test
    public void testDefaultPageSizeValue() throws Exception {
        new PagedTable();

        verify(select).setValue(String.valueOf(DEFAULT_PAGE_SIZE));
        verify(select).addValueChangeHandler(any());
    }

    @Test
    public void testShowExpandDataGridButtonOpened() throws Exception {
        Element expandDataGridWidthButtonElementMock = mock(Element.class);

        final int PAGE_SIZE = 10;
        final int COLUMN_COUNT = 3;

        PagedTable pagedTable = new PagedTable(PAGE_SIZE, null, null, false, false, false);
        pagedTable.dataGrid = spy(pagedTable.dataGrid);
        pagedTable.expandDataGridWidthButton = mock(Button.class);
        pagedTable.dataGridContainer = mock(Column.class);


        when(pagedTable.dataGrid.getColumnCount()).thenReturn(COLUMN_COUNT);
        when(pagedTable.dataGridContainer.getOffsetWidth()).thenReturn(COLUMN_COUNT * 120 - 5);
        when(pagedTable.expandDataGridWidthButton.getElement()).thenReturn(expandDataGridWidthButtonElementMock);
        pagedTable.enableExpandButton(true);
        pagedTable.setTableHeight();

        verify(pagedTable.expandDataGridWidthButton,times(2)).setVisible(true);
        verify(expandDataGridWidthButtonElementMock,times(2)).setAttribute("aria-pressed", "false");
        verify(pagedTable.dataGridContainer,times(3)).setWidth("100%");
        verify(pagedTable.expandDataGridWidthButton,times(2)).setIcon(IconType.CARET_RIGHT);

    }

    @Test
    public void testHideExpandDataGridButton() throws Exception {
        Element expandDataGridWidthButtonElementMock = mock(Element.class);

        final int PAGE_SIZE = 10;
        final int COLUMN_COUNT = 3;

        PagedTable pagedTable = new PagedTable(PAGE_SIZE, null, null, false, false, false);
        pagedTable.dataGrid = spy(pagedTable.dataGrid);
        pagedTable.expandDataGridWidthButton = mock(Button.class);
        pagedTable.dataGridContainer = mock(Column.class);

        when(pagedTable.dataGrid.getColumnCount()).thenReturn(COLUMN_COUNT);
        when(pagedTable.dataGridContainer.getOffsetWidth()).thenReturn(COLUMN_COUNT * 120 + 5);
        when(pagedTable.expandDataGridWidthButton.getElement()).thenReturn(expandDataGridWidthButtonElementMock);
        pagedTable.enableExpandButton(true);
        pagedTable.setTableHeight();

        verify(pagedTable.expandDataGridWidthButton).setVisible(false);

    }
}