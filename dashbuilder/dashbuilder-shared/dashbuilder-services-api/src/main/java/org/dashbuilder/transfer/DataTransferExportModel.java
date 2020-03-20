package org.dashbuilder.transfer;

import java.util.Collections;
import java.util.List;

import org.dashbuilder.dataset.def.DataSetDef;

/**
 * Contains the dashboard elements that should be exported
 *
 */
public class DataTransferExportModel {

    private List<DataSetDef> datasetDefinitions;
    private List<String> pages;
    private boolean exportNavigation;
    private boolean exportAll;
    

    public DataTransferExportModel() {
    }

    public DataTransferExportModel(List<DataSetDef> datasetDefinitions, List<String> pages, boolean exportNavigation) {
        this(datasetDefinitions, pages, exportNavigation, false);
    }

    protected DataTransferExportModel(List<DataSetDef> datasetDefinitions, List<String> pages, boolean exportNavigation, boolean exportAll) {
        this.datasetDefinitions = datasetDefinitions;
        this.pages = pages;
        this.exportNavigation = exportNavigation;
        this.exportAll = exportAll;
    }

    public static DataTransferExportModel exportAll() {
        return new DataTransferExportModel(Collections.emptyList(), Collections.emptyList(), true, true);
    }

    public List<DataSetDef> getDatasetDefinitions() {
        return datasetDefinitions;
    }

    public void setDatasetDefinitions(List<DataSetDef> datasetDefinitions) {
        this.datasetDefinitions = datasetDefinitions;
    }

    public List<String> getPages() {
        return pages;
    }

    public void setPages(List<String> pages) {
        this.pages = pages;
    }

    public boolean isExportNavigation() {
        return exportNavigation;
    }

    public void setExportNavigation(boolean exportNavigation) {
        this.exportNavigation = exportNavigation;
    }

    public boolean isExportAll() {
        return exportAll;
    }

    public void setExportAll(boolean exportAll) {
        this.exportAll = exportAll;
    }

}
