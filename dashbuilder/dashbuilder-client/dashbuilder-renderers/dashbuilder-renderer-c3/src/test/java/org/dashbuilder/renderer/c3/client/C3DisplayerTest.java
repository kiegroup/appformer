package org.dashbuilder.renderer.c3.client;

import static org.mockito.Mockito.mock;

import org.dashbuilder.common.client.widgets.FilterLabelSet;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.AbstractDisplayerTest;
import org.dashbuilder.renderer.c3.client.charts.line.C3LineChartDisplayer;

public class C3DisplayerTest extends AbstractDisplayerTest {

    public C3LineChartDisplayer c3LineChartDisplayer(DisplayerSettings settings) {
        // LineChart is the most basic C3 displayer
        return initDisplayer(
                        new C3LineChartDisplayer(mock(C3LineChartDisplayer.View.class), 
                                                 mock(FilterLabelSet.class)), 
                        settings);
    }

}
