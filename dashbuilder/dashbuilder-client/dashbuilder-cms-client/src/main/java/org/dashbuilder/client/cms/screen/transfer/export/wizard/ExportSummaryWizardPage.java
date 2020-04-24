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

import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.transfer.DataTransferExportModel;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElemental;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

@ApplicationScoped
public class ExportSummaryWizardPage implements WizardPage {

    @Inject
    View view;

    private Supplier<DataTransferExportModel> exportModelSupplier;
    private ParameterizedCommand<DataTransferExportModel> dataTransferExportModelCallback;
    private DataTransferExportModel dataTransferExportModel;

    private Command goToDataSetsCommand = () -> {
    };

    private Command goToPagesCommand = () -> {
    };

    public interface View extends UberElemental<ExportSummaryWizardPage> {

        void show(DataTransferExportModel dataTransferExportModel);

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
        return "Export Summary";
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
        dataTransferExportModel = exportModelSupplier.get();
        view.show(dataTransferExportModel);
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
        dataTransferExportModelCallback.execute(dataTransferExportModel);
    }

    public void goToDataSetsPage() {
        goToDataSetsCommand.execute();
    }

    public void goToPagesPage() {
        goToPagesCommand.execute();
    }

}