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

import static org.dashbuilder.displayer.DisplayerSubType.AREA;
import static org.dashbuilder.displayer.DisplayerSubType.BAR;
import static org.dashbuilder.displayer.DisplayerSubType.BAR_STACKED;
import static org.dashbuilder.displayer.DisplayerSubType.COLUMN;
import static org.dashbuilder.displayer.DisplayerSubType.COLUMN_STACKED;
import static org.dashbuilder.displayer.DisplayerSubType.DONUT;
import static org.dashbuilder.displayer.DisplayerSubType.LINE;
import static org.dashbuilder.displayer.DisplayerSubType.PIE;
import static org.dashbuilder.displayer.DisplayerSubType.SMOOTH;
import static org.dashbuilder.displayer.DisplayerType.AREACHART;
import static org.dashbuilder.displayer.DisplayerType.BARCHART;
import static org.dashbuilder.displayer.DisplayerType.BUBBLECHART;
import static org.dashbuilder.displayer.DisplayerType.LINECHART;
import static org.dashbuilder.displayer.DisplayerType.PIECHART;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerSubType;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.AbstractRendererLibrary;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.renderer.c3.client.charts.area.C3AreaChartDisplayer;
import org.dashbuilder.renderer.c3.client.charts.bar.C3BarChartDisplayer;
import org.dashbuilder.renderer.c3.client.charts.bubble.C3BubbleChartDisplayer;
import org.dashbuilder.renderer.c3.client.charts.line.C3LineChartDisplayer;
import org.dashbuilder.renderer.c3.client.charts.pie.C3PieChartDisplayer;
import org.dashbuilder.renderer.c3.client.exports.ResourcesInjector;
import org.jboss.errai.ioc.client.container.SyncBeanManager;


@ApplicationScoped
public class C3Renderer extends AbstractRendererLibrary {

    public static final String UUID = "c3";
    
    @Inject
    protected SyncBeanManager beanManager;

    @Override
    public String getUUID() {
        return UUID;
    }

    @Override
    public String getName() {
        return "C3 Charts";
    }

    @Override
    public List<DisplayerSubType> getSupportedSubtypes(DisplayerType displayerType) {
        switch (displayerType) {
            case LINECHART:
                return Arrays.asList(LINE, SMOOTH);
            case BARCHART:
                return Arrays.asList(BAR, BAR_STACKED, COLUMN, COLUMN_STACKED);    
            case PIECHART:
                return Arrays.asList(PIE, DONUT);
            case AREACHART:
                return Arrays.asList(AREA);            
            default:
                return Collections.emptyList();
        }
    }

    public Displayer lookupDisplayer(DisplayerSettings displayerSettings) {
        ResourcesInjector.ensureInjected();
        DisplayerType displayerType = displayerSettings.getType();
        DisplayerSubType subtype = displayerSettings.getSubtype();
        C3Displayer displayer;
        switch (displayerType) {
            case LINECHART:
                displayer = getLineChartForSubType(subtype);
                break;
            case BARCHART:
                displayer = createBarChartForSubType(subtype);
                break;
            case PIECHART:
                displayer = getPieChartForSubType(subtype);
                break;
            case AREACHART:
                displayer = getAreaChartForSubType(subtype);
                break;
            case BUBBLECHART:
                displayer = new C3BubbleChartDisplayer();
                break;
            default:
                return null;
        }
        return displayer;
    }

    private C3Displayer createBarChartForSubType(DisplayerSubType subtype) {
        C3Displayer displayer;
        switch (subtype) {
            case BAR:
                displayer = C3BarChartDisplayer.rotated();
                break;
            case BAR_STACKED:
                displayer = C3BarChartDisplayer.stackedAndRotated();
                break;
            case COLUMN:
                displayer = C3BarChartDisplayer.notRotated();
                break;
            case COLUMN_STACKED:
                displayer = C3BarChartDisplayer.stacked();
                break;
            default:
                displayer = C3BarChartDisplayer.rotated();
                break;
        }
        return displayer;
    }

    private C3Displayer getLineChartForSubType(DisplayerSubType subtype) {
        C3Displayer displayer;
        if(subtype == SMOOTH) { 
            displayer = C3LineChartDisplayer.createSmooth();
        } else {
            displayer = C3LineChartDisplayer.create();
        }
        return displayer;
    }
    
    private C3Displayer getPieChartForSubType(DisplayerSubType subtype) {
        C3Displayer displayer;
        if(subtype == DONUT) { 
            displayer = C3PieChartDisplayer.createDonut();
        } else {
            displayer = C3PieChartDisplayer.create();
        }
        return displayer;
    }
    
    private C3Displayer getAreaChartForSubType(DisplayerSubType subtype) {
        return new C3AreaChartDisplayer();
    }

    @Override
    public List<DisplayerType> getSupportedTypes() {
        return Arrays.asList(LINECHART, BARCHART, PIECHART, AREACHART, BUBBLECHART);
    }
    
    @Override
    public boolean isDefault(DisplayerType type) {
//        return  BARCHART.equals(type) ||
//                PIECHART.equals(type) ||
//                AREACHART.equals(type) ||
//                BUBBLECHART.equals(type) ||
//                LINECHART.equals(type);
        return false;
    }
}
