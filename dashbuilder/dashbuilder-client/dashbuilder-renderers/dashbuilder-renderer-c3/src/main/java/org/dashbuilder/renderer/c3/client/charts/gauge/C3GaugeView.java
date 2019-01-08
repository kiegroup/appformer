package org.dashbuilder.renderer.c3.client.charts.gauge;

import org.dashbuilder.renderer.c3.client.C3DisplayerView;

public class C3GaugeView 
      extends C3DisplayerView<C3GaugeDisplayer> 
      implements C3GaugeDisplayer.View {

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
