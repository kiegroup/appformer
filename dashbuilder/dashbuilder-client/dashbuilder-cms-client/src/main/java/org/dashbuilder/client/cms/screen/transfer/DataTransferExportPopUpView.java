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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import elemental2.dom.Element;
import elemental2.dom.Element.OnclickCallbackFn;
import elemental2.dom.HTMLCollection;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.NodeList;
import org.dashbuilder.client.cms.resources.i18n.ContentManagerConstants;
import org.dashbuilder.client.cms.screen.util.DomFactory;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.transfer.DataTransferAssets;
import org.dashbuilder.transfer.DataTransferExportModel;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;
import org.jboss.errai.common.client.dom.elemental2.Elemental2DomUtil;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.ext.editor.commons.client.file.popups.CommonModalBuilder;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKCancelButtons;
import org.uberfire.workbench.events.NotificationEvent;

@Templated
@Dependent
public class DataTransferExportPopUpView implements DataTransferExportPopUp.View {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataTransferExportPopUpView.class);

    private ContentManagerConstants i18n = ContentManagerConstants.INSTANCE;
    private DataTransferExportPopUp presenter;
    private BaseModal modal;
    
    @Inject
    ToggleSwitch toggleSwitch;
    
    @Inject
    Elemental2DomUtil elem2Dom;
    
    @Inject
    @DataField 
    HTMLDivElement toggleSwitchContainer;

    @Inject
    @DataField
    HTMLTableElement datasetsTable;

    @Inject
    @DataField
    HTMLTableElement pagesTable;

    @Inject
    @DataField
    private HTMLDivElement dataTransferExportModalBody;

    @Inject
    @DataField
    HTMLInputElement searchDatasets;

    @Inject
    @DataField
    HTMLInputElement searchPages;

    @Inject
    @DataField
    HTMLInputElement selectAllDatasets;

    @Inject
    @DataField
    HTMLInputElement selectAllPages;

    @Inject
    @DataField
    HTMLDivElement progressIndicator;

    @Inject
    @DataField
    HTMLDivElement dataSelectionAccordion;

    @Inject
    private Event<NotificationEvent> workbenchNotification;

    @Inject
    DomFactory domFactory;

    private DataTransferAssets assetsToExport;

    @Override
    public HTMLElement getElement() {
        return dataTransferExportModalBody;
    }

    @Override
    public void init(DataTransferExportPopUp presenter) {
        this.presenter = presenter;
        ModalFooter modalFooter = new ModalFooterOKCancelButtons(this::getSelectedAssets,
                                                                 () -> modal.hide());
        modal = new CommonModalBuilder().addHeader(i18n.dataTransferExportPopUpViewTitle())
                                        .addBody(dataTransferExportModalBody)
                                        .addFooter(modalFooter)
                                        .build();

        selectAllPages.onclick = e -> {
            allInputsForTable(pagesTable).forEach(chk -> chk.checked = selectAllPages.checked);
            return null;
        };

        selectAllDatasets.onclick = e -> {
            allInputsForTable(datasetsTable).forEach(chk -> chk.checked = selectAllDatasets.checked);
            return null;
        };

        searchPages.onkeyup = e -> {
            filterTable(searchPages, pagesTable);
            return null;
        };

        searchDatasets.onkeyup = e -> {
            filterTable(searchDatasets, datasetsTable);
            return null;
        };
        
        elem2Dom.appendWidgetToElement(toggleSwitchContainer, toggleSwitch);
        toggleSwitch.setValue(true);

    }

    @Override
    public void show() {
        modal.show();
        loading();
    }

    @Override
    public void setAssetsToExport(DataTransferAssets assetsToExport) {
        this.assetsToExport = assetsToExport;
        finishedLoading();
    }

    private void fillPagesTable() {
        HTMLElement pageBody = pagesTable.tBodies.getAt(0);
        pageBody.innerHTML = "";
        for (String page : this.assetsToExport.getPages()) {
            HTMLTableCellElement pageCell = domFactory.tableCell();
            HTMLTableCellElement selectCell = domFactory.tableCell();
            HTMLInputElement select = createCheckBox();
            select.onclick = verifyAllCheck(selectAllPages, pagesTable);
            select.id = page;
            selectCell.appendChild(select);
            pageCell.innerHTML = page;

            HTMLTableRowElement pageRow = domFactory.tableRow();
            pageRow.appendChild(selectCell);
            pageRow.appendChild(pageCell);
            pageBody.appendChild(pageRow);
        }
    }

    private void fillDataSetsTable() {
        HTMLElement datasetsBody = datasetsTable.tBodies.getAt(0);
        datasetsBody.innerHTML = "";
        for (DataSetDef dataSetDef : this.assetsToExport.getDatasetsDefinitions()) {
            HTMLInputElement select = createCheckBox();
            select.onclick = verifyAllCheck(selectAllDatasets, datasetsTable);
            HTMLTableCellElement dsSelectCell = domFactory.tableCell();
            select.id = dataSetDef.getUUID();
            dsSelectCell.appendChild(select);
            HTMLTableRowElement dsRow = domFactory.tableRow();
            dsRow.appendChild(dsSelectCell);
            Stream.of(dataSetDef.getUUID(),
                      dataSetDef.getName(),
                      dataSetDef.getProvider().getName())
                  .map(this::createCell).forEach(dsRow::appendChild);
            datasetsBody.appendChild(dsRow);
        }

    }

    private OnclickCallbackFn verifyAllCheck(HTMLInputElement parent, HTMLTableElement table) {
        return e -> {
            parent.checked = allInputsForTable(table).allMatch(input -> input.checked);
            return null;
        };
    }

    public void getSelectedAssets() {
        modal.hide();

        List<DataSetDef> datasets = getSelectedRows(datasetsTable, assetsToExport.getDatasetsDefinitions());
        List<String> pages = getSelectedRows(pagesTable, assetsToExport.getPages());

        DataTransferExportModel exportModel = new DataTransferExportModel(datasets,
                                                                          pages,
                                                                          toggleSwitch.getValue());
        presenter.receiveSelectedAssets(exportModel);

    }

    private HTMLInputElement createCheckBox() {
        HTMLInputElement checkbox = domFactory.input();
        checkbox.type = "checkbox";
        checkbox.checked = true;
        return checkbox;
    }

    private HTMLTableCellElement createCell(String content) {
        HTMLTableCellElement cell = domFactory.tableCell();
        cell.innerHTML = content;
        return cell;
    }

    private Stream<HTMLInputElement> allInputsForTable(HTMLTableElement table) {
        // asArray throws cast exception
        NodeList<Element> items = table.querySelectorAll("tbody > tr > td:first-of-type > input[type=checkbox]");
        return IntStream.range(0, items.getLength()).mapToObj(i -> (HTMLInputElement) items.getAt(i));
    }

    protected void filterTable(HTMLInputElement filter, HTMLTableElement table) {
        String query = filter.value.trim().toLowerCase();
        HTMLCollection<HTMLTableRowElement> rows = table.tBodies.getAt(0).rows;
        IntStream.range(0, rows.getLength()).mapToObj(rows::getAt).forEach(row -> {
            row.hidden = false;
            if (!query.isEmpty()) {
                row.hidden = IntStream.range(0, row.cells.getLength())
                                      .mapToObj(row.cells::getAt)
                                      .noneMatch(c -> c.textContent.toLowerCase().contains(query));
            }

        });

    }

    private <T> List<T> getSelectedRows(HTMLTableElement table, List<T> list) {
        HTMLCollection<HTMLTableRowElement> rows = table.tBodies.getAt(0).rows;
        return IntStream.range(0, rows.getLength())
                        .filter(i -> {
                            Element checkBox = rows.getAt(i).querySelector("td > input[type=checkbox]");
                            return ((HTMLInputElement) checkBox).checked;
                        }).mapToObj(list::get)
                        .collect(Collectors.toList());
    }

    private void loading() {
        progressIndicator.hidden = false;
        dataSelectionAccordion.hidden = true;
    }

    private void finishedLoading() {
        progressIndicator.hidden = true;
        dataSelectionAccordion.hidden = false;
        selectAllDatasets.checked = true;
        selectAllPages.checked = true;
        fillDataSetsTable();
        fillPagesTable();
    }

    @Override
    public void showError(Throwable error) {
        String dataTransferExportError = i18n.dataTransferExportError();
        LOGGER.error(dataTransferExportError, error);
        NotificationEvent errorEvent = new NotificationEvent(dataTransferExportError,
                                                             NotificationEvent.NotificationType.ERROR);
        workbenchNotification.fire(errorEvent);
        modal.hide();
    }

}
