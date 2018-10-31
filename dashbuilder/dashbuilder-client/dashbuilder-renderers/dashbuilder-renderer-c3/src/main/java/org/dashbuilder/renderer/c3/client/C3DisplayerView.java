package org.dashbuilder.renderer.c3.client;

import org.dashbuilder.common.client.resources.i18n.DashbuilderCommonConstants;
import org.dashbuilder.displayer.client.AbstractGwtDisplayerView;
import org.dashbuilder.renderer.c3.client.jsbinding.C3;
import org.dashbuilder.renderer.c3.client.jsbinding.C3Chart;
import org.dashbuilder.renderer.c3.client.jsbinding.C3ChartConf;
import org.dashbuilder.renderer.c3.client.resources.i18n.C3DisplayerConstants;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;

public abstract class C3DisplayerView extends AbstractGwtDisplayerView<C3Displayer> implements C3Displayer.View {
    
    private Panel container = new FlowPanel();
    private Panel filterPanel = new FlowPanel();
    private Panel displayerPanel = new FlowPanel();
    private Button btnClearFilters = buildClearFilterButton();
    
    private HTML titleHtml = new HTML();
    private Command clearCommand;
    private ParameterizedCommand<String> removeFilterCommand;
    private C3Chart chart;
    
    @Override
    public void init(C3Displayer presenter) {
        super.setPresenter(presenter);
        super.setVisualization(container);
        container.add(titleHtml);
        container.add(displayerPanel);
        container.add(filterPanel);
        
        filterPanel.getElement().getStyle().setPadding(4, Unit.PX);
    }

    @Override
    public void updateChart(C3ChartConf conf) {
        displayerPanel.clear();
        conf.setBindto(displayerPanel.getElement());
        chart = C3.generate(conf);
    }

    @Override
    public String getGroupsTitle() {
        return C3DisplayerConstants.INSTANCE.common_Categories();
    }
    
    @Override
    public String getColumnsTitle() {
        return C3DisplayerConstants.INSTANCE.common_Series();
    }
    
    @Override
    public void showTitle(String title) {
        titleHtml.setText(title);
    }
    
    @Override
    public void setOnFilterClear(Command clearCommand) {
        this.clearCommand = clearCommand;
    }
    
    // Filters related - marked to be removed when using FilterLabelSet

    @Override
    public void removeFilters() {
        filterPanel.clear();
    }
    
    @Override
    public void setOnFilterRemoved(ParameterizedCommand<String> removeFilterCommand) {
        this.removeFilterCommand = removeFilterCommand;
    }
    
    @Override
    public void addFilter(String filter) {
        if(!filterPanel.iterator().hasNext()) {
            filterPanel.add(btnClearFilters);
        }
        Button btnFilter = buildFilterButton(filter);
        btnFilter.addClickHandler(e -> {
            filterPanel.remove(btnFilter);
            removeFilterCommand.execute(filter);
        });
        filterPanel.add(btnFilter);
        
    }

    
    private Button buildClearFilterButton() {
        Button btn = buildFilterButton(DashbuilderCommonConstants.INSTANCE.clearAll());
        btn.getElement().getStyle().setColor("red");
        btn.getElement().getStyle().setBackgroundColor("white");
        btn.addClickHandler(e -> { 
            removeFilters();
            clearCommand.execute();
        });
        return btn;
    }
    
    private Button buildFilterButton(String text) {
        Button btnFilter = new Button(text);
        btnFilter.getElement().getStyle().setBackgroundColor("blue");
        btnFilter.getElement().getStyle().setColor("#CCCCCC");
        btnFilter.getElement().getStyle().setFontSize(10, Unit.PX);
        return btnFilter;
    }
    
}