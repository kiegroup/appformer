package org.dashbuilder.renderer.c3.client;

import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_AMOUNT;
import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_DATE;
import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_ID;
import static org.dashbuilder.dataset.group.AggregateFunctionType.SUM;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.renderer.c3.client.charts.line.C3LineChartDisplayer;
import org.dashbuilder.renderer.c3.client.jsbinding.C3ChartConf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class C3DisplayerGeneralTest extends C3BaseTest {
    
    private static final boolean RECEIVE_NOTIFICATION = true;
    private static final String LEGEND_POSITION = "right";
    private static final int SIZE = 300;
    private static final String TITLE = "Title";
    private static final String BLACK = "black";
    
    DisplayerSettings simpleSettings = DisplayerSettingsFactory.newBarChartSettings()
                                                        .dataset(EXPENSES)
                                                        .group(COLUMN_DATE)
                                                        .column(COLUMN_DATE)
                                                        .column(COLUMN_AMOUNT, SUM)
                                                        .width(SIZE)
                                                        .height(SIZE)
                                                        .title(TITLE)
                                                        .backgroundColor(BLACK)
                                                        .titleVisible(true)
                                                        .legendOn(LEGEND_POSITION)
                                                        .filterOn(RECEIVE_NOTIFICATION, 
                                                                  RECEIVE_NOTIFICATION, 
                                                                  RECEIVE_NOTIFICATION)
                                                        .buildSettings();
    
    private C3LineChartDisplayer displayer;
    
    @Before 
    public void conf() {
        displayer = c3LineChartDisplayer(simpleSettings);
        displayer.draw();
    }
    
    /**
     * Tests if C3 configuration is built based on settings values
     */
    @Test
    public void c3ConfigurationTest() {
        verify(c3Factory).c3ChartSize(SIZE, SIZE);
        verify(c3Factory).c3AxisInfo(eq(false), any(), any());
        verify(c3Factory).c3Legend(true, LEGEND_POSITION);
        verify(c3Factory).c3Selection(RECEIVE_NOTIFICATION, true, false);
    }
    
    /**
     * Check if settings are passed to view
     */
    @Test
    public void viewParametersTest() {
        C3LineChartDisplayer.View view = displayer.getView();
        verify(view).init(any());
        verify(view).updateChart(any(C3ChartConf.class));
        verify(view).showTitle(TITLE);
        verify(view).setBackgroundColor(BLACK);
    }
    
    /**
     * Verify if data is generated correctly
     */
    @Test
    public void c3DataTest() {
        DataSet lastDataSet = displayer.getDataSetHandler().getLastDataSet();
        List<?> categories = lastDataSet.getColumns().get(0).getValues();
        DataColumn seriesColumn = lastDataSet.getColumns().get(1);
        String[][] createdSeries = displayer.createSeries();
        assertArrayEquals(displayer.createCategories(), categories.toArray());
        assertEquals(createdSeries[0].length, seriesColumn.getValues().size() + 1);
        assertEquals(createdSeries[0][0], seriesColumn.getId());
    }
    
    @Test
    public void c3NoData() {
        DisplayerSettings noData = DisplayerSettingsFactory.newLineChartSettings()
                .dataset(EXPENSES)
                .filter(COLUMN_ID, FilterFactory.isNull())
                .group(COLUMN_DATE)
                .column(COLUMN_DATE)
                .column(COLUMN_AMOUNT, SUM)
                .buildSettings();
        displayer = c3LineChartDisplayer(noData);
        displayer.draw();
        C3LineChartDisplayer.View view = displayer.getView();
        verify(view).noData();
    }
    
    @Test
    public void c3Resizable() {
        DisplayerSettings resizableSettings = DisplayerSettingsFactory.newBarChartSettings()
                .dataset(EXPENSES)
                .group(COLUMN_DATE)
                .column(COLUMN_DATE)
                .column(COLUMN_AMOUNT, SUM)
                .width(SIZE)
                .height(SIZE)
                .title(TITLE)
                .backgroundColor(BLACK)
                .titleVisible(true)
                .legendOn(LEGEND_POSITION)
                .resizableOn(SIZE, SIZE)
                .buildSettings();
        displayer = c3LineChartDisplayer(resizableSettings);
        displayer.draw();
        C3LineChartDisplayer.View view = displayer.getView();
        verify(c3Factory, times(0)).c3ChartSize(300, 300);
        verify(view).setResizable(true, SIZE, SIZE);
    }
}
