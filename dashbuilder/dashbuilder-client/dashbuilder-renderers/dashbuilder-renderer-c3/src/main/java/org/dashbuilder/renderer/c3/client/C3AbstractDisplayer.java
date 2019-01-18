package org.dashbuilder.renderer.c3.client;

import java.util.List;
import java.util.Set;

import org.dashbuilder.common.client.widgets.FilterLabel;
import org.dashbuilder.common.client.widgets.FilterLabelSet;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.group.Interval;
import org.dashbuilder.displayer.client.AbstractGwtDisplayer;

public abstract class C3AbstractDisplayer<V extends C3AbstractDisplayer.View> extends AbstractGwtDisplayer<V>  {

    protected FilterLabelSet filterLabelSet;
    
    public C3AbstractDisplayer(FilterLabelSet filterLabelSet) {
        super();
        this.filterLabelSet = filterLabelSet;
        this.filterLabelSet.setOnClearAllCommand(this::onFilterClearAll);
    }
    
    public interface View<P extends C3AbstractDisplayer> extends AbstractGwtDisplayer.View<P> {

        void noData();
        
        void setSize(int width, int height);

        void setFilterLabelSet(FilterLabelSet filterLabelSet);
        
        void showTitle(String title);

    }
    
    @Override
    protected void createVisualization() {
        getView().setFilterLabelSet(filterLabelSet);
        updateVisualization();
    }
    
    @Override
    protected void updateVisualization() {
        getView().setSize(displayerSettings.getChartWidth(), 
                          displayerSettings.getChartHeight());
        if (dataSet.getRowCount() == 0) {
            getView().noData();
        } else {
            updateFilterStatus();
            updateVisualizationWithData();
        }
    }
    
    
    protected abstract void updateVisualizationWithData();

    void onFilterClearAll() {
        super.filterReset();

        // Update the displayer view in order to reflect the current selection
        // (only if not has already been redrawn in the previous filterUpdate() call)
        if (!displayerSettings.isFilterSelfApplyEnabled()) {
            updateVisualization();
        }
    }
    
    void onFilterLabelRemoved(String columnId, int row) {
        super.filterUpdate(columnId, row);

        // Update the displayer view in order to reflect the current selection
        // (only if not has already been redrawn in the previous filterUpdate() call)
        if (!displayerSettings.isFilterSelfApplyEnabled()) {
            updateVisualization();
        }
    }
    
    protected void addToSelection(int row) {
        String columnId =  dataSet.getColumns().get(0).getId();
        Integer maxSelections = displayerSettings.isFilterSelfApplyEnabled() ? null : dataSet.getRowCount();
        filterUpdate(columnId, row, maxSelections);

        if (!displayerSettings.isFilterSelfApplyEnabled()) {
            updateVisualization();
        }
    }
    
    protected void updateFilterStatus() {
        filterLabelSet.clear();
        Set<String> columnFilters = filterColumns();
        if (displayerSettings.isFilterEnabled() && !columnFilters.isEmpty()) {

            for (String columnId : columnFilters) {
                List<Interval> selectedValues = filterIntervals(columnId);
                DataColumn column = dataSet.getColumnById(columnId);
                for (Interval interval : selectedValues) {
                    String formattedValue = formatInterval(interval, column);
                    FilterLabel filterLabel = filterLabelSet.addLabel(formattedValue);
                    filterLabel.setOnRemoveCommand(() -> onFilterLabelRemoved(columnId, interval.getIndex()));
                }
            }
        }
    }
    
    protected String columnValueToString(Object mightBeNull) {
        return mightBeNull == null ? "" : mightBeNull.toString();
    }
}
