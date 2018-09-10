/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.renderer.c3.client;

import java.util.List;
import java.util.Objects;

import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.displayer.DisplayerAttributeDef;
import org.dashbuilder.displayer.DisplayerAttributeGroupDef;
import org.dashbuilder.displayer.DisplayerConstraints;
import org.dashbuilder.displayer.client.AbstractGwtDisplayer;
import org.dashbuilder.renderer.c3.client.jsbinding.C3AxisInfo;
import org.dashbuilder.renderer.c3.client.jsbinding.C3AxisX;
import org.dashbuilder.renderer.c3.client.jsbinding.C3AxisY;
import org.dashbuilder.renderer.c3.client.jsbinding.C3ChartConf;
import org.dashbuilder.renderer.c3.client.jsbinding.C3ChartData;
import org.dashbuilder.renderer.c3.client.jsbinding.C3ChartSize;
import org.dashbuilder.renderer.c3.client.jsbinding.C3Grid;
import org.dashbuilder.renderer.c3.client.jsbinding.C3GridConf;
import org.dashbuilder.renderer.c3.client.jsbinding.C3Point;
import org.dashbuilder.renderer.c3.client.jsbinding.C3Tick;
import org.dashbuilder.renderer.c3.client.jsbinding.C3Transition;

import com.google.gwt.i18n.client.NumberFormat;

import elemental2.core.JsObject;

public class C3Displayer extends AbstractGwtDisplayer<C3Displayer.View> {
    
    protected View view;

    public interface View extends AbstractGwtDisplayer.View<C3Displayer> {

        void updateChart(C3ChartConf conf);
        
        String getType();

        String getGroupsTitle();

        String getColumnsTitle();
        
        void showTitle(String title);
        
    }
    
    
    public C3Displayer(View view) {
        super();
        this.view = view;
        view.init(this);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {
        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints()
                .setGroupRequired(true)
                .setGroupColumn(true)
                .setMaxColumns(10)
                .setMinColumns(2)
                .setExtraColumnsAllowed(true)
                .setExtraColumnsType(ColumnType.NUMBER)
                .setColumnTypes(new ColumnType[]{
                        ColumnType.LABEL,
                        ColumnType.NUMBER});

        return new DisplayerConstraints(lookupConstraints)
                .supportsAttribute(DisplayerAttributeDef.TYPE)
                .supportsAttribute(DisplayerAttributeDef.RENDERER)
                .supportsAttribute(DisplayerAttributeGroupDef.COLUMNS_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.FILTER_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.REFRESH_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.GENERAL_GROUP)
                .supportsAttribute(DisplayerAttributeDef.CHART_WIDTH)
                .supportsAttribute(DisplayerAttributeDef.CHART_HEIGHT)
                .supportsAttribute(DisplayerAttributeDef.CHART_BGCOLOR)
                .supportsAttribute(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.CHART_LEGEND_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.AXIS_GROUP);      
    }

    @Override
    protected void createVisualization() {
        updateVisualization();
    }

    @Override
    protected void updateVisualization() {
        if (displayerSettings.isTitleVisible()) {
            getView().showTitle(displayerSettings.getTitle());
        }
        C3ChartConf conf = buildConfiguration();
        getView().updateChart(conf);
    }

    protected C3ChartConf buildConfiguration() {
        double width = displayerSettings.getChartWidth();
        double height = displayerSettings.getChartHeight();
        C3AxisInfo axis = createAxis();
        C3ChartData data = createData();
        C3Point point = createPoint();
        return C3ChartConf.create(
                    C3ChartSize.create(width, height),
                    data,
                    axis,
                    C3Grid.create(
                            C3GridConf.create(true), 
                            C3GridConf.create(true)
                    ),
                    C3Transition.create(0),
                    point
                );
    }

    protected C3Point createPoint() {
        return C3Point.create(d -> 2.5);
    }

    protected C3ChartData createData() {
        String[][] series = createSeries();
        String type = getView().getType();
        String[][] groups = createGroups();
        JsObject xs = createXs();
        return C3ChartData.create(series, type, groups, xs);
    }

    protected JsObject createXs() {
        return null;
    }

    protected String[][] createGroups() {
        return new String[0][0];
    }

    protected C3AxisInfo createAxis() {
        C3AxisX axisX = createAxisX();
        C3AxisY axisY = createAxisY();
        return C3AxisInfo.create(false, axisX, axisY);
    }
    
    protected C3AxisX createAxisX() {
       String[] categories = createCategories();
       C3Tick tick = createTickX();
       return C3AxisX.create("category", categories, tick);
    }
    
    protected C3Tick createTickX() {
        return C3Tick.create(null);
    }

    protected C3AxisY createAxisY() {
        C3Tick tickX = createTickY();
        return C3AxisY.create(true, tickX);
     }

    protected C3Tick createTickY() {
        return C3Tick.create(f -> {
            try {
                double doubleFormat = Double.parseDouble(f);
                return NumberFormat.getFormat("#,###.##").format(doubleFormat);
            } catch(NumberFormatException e) {
                return f;
            }
        });
    }

    /**
     * This method extracts the categories of a dataset.
     * For most of the charts the first column of the dataset contains the categories. 
     * 
     * @return
     */
    protected String[] createCategories() {
        List<DataColumn> columns = dataSet.getColumns();
        String[] categories = null;
        if(columns.size() > 0) {
            List<?> values = columns.get(0).getValues();
            categories = new String[values.size()];
            for (int i = 0; i < categories.length; i++) {
                Object val = values.get(i);
                if(val != null) {
                    categories[i] = val.toString();
                } else {
                    categories[i] = "cat_" + i;
                }
            }
        }
        return categories;
    }


    /**
     * Extracts the series of the column 1 and other columns
     * @return
     */
    protected String[][] createSeries() {
        List<DataColumn> columns = dataSet.getColumns();
        String[][] data  = null;
        if(columns.size() > 1) {
            data = new String[columns.size() - 1][];
            for (int i = 1; i < columns.size(); i++) {
                DataColumn dataColumn = columns.get(i);
                List<?> values = dataColumn.getValues();
                String[] seriesValues = new String[values.size() + 1];
                seriesValues[0] = columns.get(i).getId();
                for (int j = 0; j < values.size(); j++) {
                    seriesValues[j + 1] = values.get(j).toString(); 
                }
                data[i - 1] = seriesValues;
            }
        }
        return data;
    }

}