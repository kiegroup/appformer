package org.dashbuilder.renderer.c3.client.charts.meter;

import org.dashbuilder.renderer.c3.client.C3DisplayerView;

public class C3MeterView 
      extends C3DisplayerView<C3MeterChartDisplayer> 
      implements C3MeterChartDisplayer.View {

    String[] colors = {
            "#60B044",
            "#F97600",
            "#FF0000"
    };
    
    @Override
    public String getType() {
        return "gauge";
    }

    @Override
    public String[] getColorPattern() {
        return colors;
    }

}
