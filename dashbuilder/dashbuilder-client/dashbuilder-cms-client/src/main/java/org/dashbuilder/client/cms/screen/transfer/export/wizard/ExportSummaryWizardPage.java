/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dashbuilder.client.cms.screen.transfer.export.wizard;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.client.cms.resources.i18n.ContentManagerConstants;
import org.dashbuilder.transfer.DataTransferExportModel;
import org.dashbuilder.transfer.ExportModelValidationService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElemental;
import org.uberfire.ext.widgets.common.client.common.BusyIndicatorView;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

@ApplicationScoped
public class ExportSummaryWizardPage implements WizardPage {

    ContentManagerConstants i18n = ContentManagerConstants.INSTANCE;

    @Inject
    View view;

    @Inject
    Caller<ExportModelValidationService> exportModelValidationService;

    @Inject
    private BusyIndicatorView busyIndicatorView;

    private Supplier<DataTransferExportModel> exportModelSupplier;
    private ParameterizedCommand<DataTransferExportModel> dataTransferExportModelCallback;
    private DataTransferExportModel exportModel;

    private Command goToDataSetsCommand = () -> {
    };

    private Command goToPagesCommand = () -> {
    };

    public interface View extends UberElemental<ExportSummaryWizardPage> {

        void success(DataTransferExportModel dataTransferExportModel);

        void validationErrors(DataTransferExportModel dataTransferExportModel,
                              Map<String, List<String>> pageDependencies);
        
        void warning(DataTransferExportModel dataTransferExportModel, String message);

    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @Override
    public Widget asWidget() {
        return ElementWrapperWidget.getWidget(view.getElement());
    }

    @Override
    public String getTitle() {
        return i18n.exportWizardTitle();
    }

    @Override
    public void isComplete(Callback<Boolean> callback) {
        callback.callback(true);
    }

    @Override
    public void initialise() {
        view.init(this);
    }

    @Override
    public void prepareView() {
        validateAndUpdateView();
    }

    public void setGoToDataSetsCommand(Command goToDatasets) {
        this.goToDataSetsCommand = goToDatasets;
    }

    public void setGoToPagesCommand(Command goToPages) {
        this.goToPagesCommand = goToPages;
    }

    public void setExportSummary(Supplier<DataTransferExportModel> exportModelSupplier) {
        this.exportModelSupplier = exportModelSupplier;
    }

    public void setCallback(ParameterizedCommand<DataTransferExportModel> dataTransferExportModelCallback) {
        this.dataTransferExportModelCallback = dataTransferExportModelCallback;
    }

    void confirmDownload() {
        dataTransferExportModelCallback.execute(exportModel);
    }

    public void goToDataSetsPage() {
        goToDataSetsCommand.execute();
    }

    public void goToPagesPage() {
        goToPagesCommand.execute();
    }

    private void validateAndUpdateView() {
        exportModel = exportModelSupplier.get();
        if (exportModel.getPages().isEmpty()) {
            view.warning(exportModel, i18n.noPagesExported());
            return;
        }
        
        busyIndicatorView.showBusyIndicator(i18n.validatingExport());
        exportModelValidationService.call((Map<String, List<String>> validation) -> {
            busyIndicatorView.hideBusyIndicator();
            if (validation.isEmpty()) {
                view.success(exportModel);
            } else {
                view.validationErrors(exportModel, validation);
            }
        }).checkMissingDatasets(exportModel);
    }

}