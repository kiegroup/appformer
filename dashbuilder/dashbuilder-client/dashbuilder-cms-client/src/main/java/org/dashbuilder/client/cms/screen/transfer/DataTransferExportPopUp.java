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

package org.dashbuilder.client.cms.screen.transfer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.transfer.DataTransferAssets;
import org.dashbuilder.transfer.DataTransferExportModel;
import org.dashbuilder.transfer.DataTransferServices;
import org.jboss.errai.common.client.api.Caller;
import org.uberfire.client.mvp.UberElemental;
import org.uberfire.mvp.ParameterizedCommand;

@ApplicationScoped
public class DataTransferExportPopUp {

    private View view;

    private Caller<DataTransferServices> dataTransferServices;

    private ParameterizedCommand<DataTransferExportModel> dataTransferExportModelCallback;

    public interface View extends UberElemental<DataTransferExportPopUp> {

        void show();

        void setAssetsToExport(DataTransferAssets assetsToExport);

        void showError(Throwable error);

    }

    @Inject
    public DataTransferExportPopUp(View view,
                                   Caller<DataTransferServices> dataTransferServices) {
        this.view = view;
        this.dataTransferServices = dataTransferServices;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public void setCallback(ParameterizedCommand<DataTransferExportModel> dataTransferExportModelCallback) {
        this.dataTransferExportModelCallback = dataTransferExportModelCallback;

    }

    public void load() {
        view.show();
        dataTransferServices.call((DataTransferAssets v) -> view.setAssetsToExport(v),
                                  (message, error) -> {
                                      view.showError(error);
                                      return false;
                                  })
                            .assetsToExport();

    }

    public void receiveSelectedAssets(DataTransferExportModel dataTransferExportModel) {
        dataTransferExportModelCallback.execute(dataTransferExportModel);

    }
}
