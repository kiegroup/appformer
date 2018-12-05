package org.dashbuilder.renderer.c3.client;

import java.util.List;

import org.dashbuilder.common.client.widgets.FilterLabelSet;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.renderer.c3.client.jsbinding.C3AxisInfo;
import org.dashbuilder.renderer.c3.client.jsbinding.C3ChartConf;
import org.dashbuilder.renderer.c3.client.jsbinding.C3Tick;

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
    
    protected C3Tick createTickY() {
        return C3Tick.create(f -> {
            List<DataColumn> columns = dataSet.getColumns();
            if (columns.size() > 1) {
                DataColumn dataColumn = columns.get(1);
                f = super.formatValue(f, dataColumn);
            }
            return f;
        });
    }
    
    private void applyPropertiesToAxes(C3AxisInfo axis) {
        axis.getX().setLabel(displayerSettings.getXAxisTitle());
        axis.getX().setShow(displayerSettings.isXAxisShowLabels());
        axis.getX().getTick().setRotate(displayerSettings.getXAxisLabelsAngle());
        axis.getY().setShow(displayerSettings.isYAxisShowLabels());
        axis.getY().setLabel(displayerSettings.getYAxisTitle());
    }

}
