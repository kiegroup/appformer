package org.dashbuilder.renderer.c3.client;

import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_AMOUNT;
import static org.dashbuilder.dataset.ExpenseReportsData.COLUMN_DATE;
import static org.dashbuilder.dataset.group.AggregateFunctionType.SUM;
import static org.mockito.Matchers.any;

import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSettingsFactory;
import org.dashbuilder.renderer.c3.client.charts.line.C3LineChartDisplayer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class C3DisplayerConfigurationTest extends C3DisplayerTest {
    
    DisplayerSettings byYear = DisplayerSettingsFactory.newBarChartSettings()
            .dataset(EXPENSES)
            .group(COLUMN_DATE)
            .column(COLUMN_DATE)
            .column(COLUMN_AMOUNT, SUM)
            .filterOn(false, true, true)
            .sort(COLUMN_DATE, SortOrder.ASCENDING)
            .buildSettings();
    
    @Test
    public void testC3DisplayerConfiguration() {
        C3LineChartDisplayer displayer = c3LineChartDisplayer(byYear);
        C3LineChartDisplayer.View view = displayer.getView();
        Mockito.verify(view).init(any());;
    }

}
