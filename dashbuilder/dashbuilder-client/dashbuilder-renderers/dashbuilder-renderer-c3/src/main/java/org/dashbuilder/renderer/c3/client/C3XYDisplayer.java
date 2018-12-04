package org.dashbuilder.renderer.c3.client;

import org.dashbuilder.common.client.widgets.FilterLabelSet;
import org.dashbuilder.renderer.c3.client.jsbinding.C3AxisInfo;
import org.dashbuilder.renderer.c3.client.jsbinding.C3ChartConf;

public abstract class C3XYDisplayer<V extends C3Displayer.View> extends C3Displayer {
    
    public C3XYDisplayer(FilterLabelSet filterLabelSet) {
        super(filterLabelSet);
    }

    @Override
    protected C3ChartConf buildConfiguration() {
         C3ChartConf conf = super.buildConfiguration();
         applyPropertiesToAxes(conf.getAxis());
         return conf;
    }
    
    private void applyPropertiesToAxes(C3AxisInfo axis) {
        axis.getX().setLabel(displayerSettings.getXAxisTitle());
        axis.getX().setShow(displayerSettings.isXAxisShowLabels());
        axis.getX().getTick().setRotate(displayerSettings.getXAxisLabelsAngle());
        axis.getY().setShow(displayerSettings.isYAxisShowLabels());
        axis.getY().setLabel(displayerSettings.getYAxisTitle());
    }

}
