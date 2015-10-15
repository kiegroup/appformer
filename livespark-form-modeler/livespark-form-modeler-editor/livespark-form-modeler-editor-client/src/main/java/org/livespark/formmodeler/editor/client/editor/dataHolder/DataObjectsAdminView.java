/*
 * Copyright 2015 JBoss Inc
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
package org.livespark.formmodeler.editor.client.editor.dataHolder;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.livespark.formmodeler.editor.client.editor.FormEditorPresenter;
import org.livespark.formmodeler.editor.client.editor.dataHolder.util.DataHolderComparator;
import org.livespark.formmodeler.editor.client.resources.i18n.Constants;
import org.livespark.formmodeler.editor.model.DataHolder;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;

/**
 * Created by pefernan on 9/29/15.
 */
public class DataObjectsAdminView extends BaseModal implements FormEditorPresenter.DataHolderAdminView {

    interface DataObjectsAdminBinder
            extends UiBinder<Widget, DataObjectsAdminView> {

    }

    private static DataObjectsAdminBinder uiBinder = GWT.create(DataObjectsAdminBinder.class);

    @UiField
    FormGroup dataObjectIDGroup;

    @UiField
    TextBox dataObjectID;

    @UiField
    HelpBlock dataObjectIDHelp;

    @UiField
    FormGroup dataObjectTypeGroup;

    @UiField
    ListBox dataObjectType;

    @UiField
    HelpBlock dataObjectTypeHelp;

    @UiField
    Button newDataObject;

    @UiField
    CellTable<DataHolder> dataObjectTable;

    private ListDataProvider<DataHolder> dataHolderListDataProvider = new ListDataProvider<DataHolder>();

    protected FormEditorPresenter presenter;

    public DataObjectsAdminView() {
        setTitle( Constants.INSTANCE.dataObjects() );
        add(new ModalBody() {{
            add(uiBinder.createAndBindUi(DataObjectsAdminView.this));
        }});

        //Init data objects table
        dataObjectTable.setEmptyTableWidget( new Label( Constants.INSTANCE.emptyDataObjectsTable() ) );

        dataHolderListDataProvider.addDataDisplay( dataObjectTable );
    }

    public void init(final FormEditorPresenter presenter) {
        this.presenter = presenter;
        final TextColumn<DataHolder> nameColumn = new TextColumn<DataHolder>() {

            @Override
            public void render( Cell.Context context,
                                DataHolder object,
                                SafeHtmlBuilder sb ) {
                SafeHtml startDiv = new SafeHtml() {
                    @Override
                    public String asString() {
                        return "<div style=\"cursor: pointer;\">";
                    }
                };
                SafeHtml endDiv = new SafeHtml() {
                    @Override
                    public String asString() {
                        return "</div>";
                    }
                };

                sb.append( startDiv );
                super.render( context, object, sb );
                sb.append( endDiv );
            }

            @Override
            public String getValue( final DataHolder dataHolder ) {
                return dataHolder.getName();
            }
        };

        nameColumn.setSortable(true);
        dataObjectTable.addColumn(nameColumn, Constants.INSTANCE.dataObjectID());
        dataObjectTable.setColumnWidth(nameColumn, 30, Style.Unit.PCT);

        ColumnSortEvent.ListHandler<DataHolder> dataObjectNameColHandler = new ColumnSortEvent.ListHandler<DataHolder>( presenter.getFormDefinition().getDataHolders());
        dataObjectNameColHandler.setComparator(nameColumn, new DataHolderComparator("name"));
        dataObjectTable.addColumnSortHandler(dataObjectNameColHandler);

        //Init property Label column

        final TextColumn<DataHolder> typeColumn = new TextColumn<DataHolder>() {

            @Override
            public void render( Cell.Context context,
                                DataHolder object,
                                SafeHtmlBuilder sb ) {
                SafeHtml startDiv = new SafeHtml() {
                    @Override
                    public String asString() {
                        return "<div style=\"cursor: pointer;\">";
                    }
                };
                SafeHtml endDiv = new SafeHtml() {
                    @Override
                    public String asString() {
                        return "</div>";
                    }
                };

                sb.append( startDiv );
                super.render( context, object, sb );
                sb.append( endDiv );
            }

            @Override
            public String getValue( final DataHolder dataHolder ) {
                return dataHolder.getType();
            }
        };

        typeColumn.setSortable(true);
        dataObjectTable.addColumn(typeColumn, Constants.INSTANCE.dataObjectType());
        dataObjectTable.setColumnWidth(typeColumn, 30, Style.Unit.PCT);

        ColumnSortEvent.ListHandler<DataHolder> dataObjectTypeColHandler = new ColumnSortEvent.ListHandler<DataHolder>( presenter.getFormDefinition().getDataHolders() );
        dataObjectTypeColHandler.setComparator(typeColumn, new DataHolderComparator("type"));
        dataObjectTable.addColumnSortHandler(dataObjectTypeColHandler);

        //Init delete column
        final ButtonCell deleteCell = new ButtonCell( ButtonType.DANGER, IconType.TRASH );
        final Column<DataHolder, String> deleteDataObject = new Column<DataHolder, String>( deleteCell ) {
            @Override
            public String getValue( final DataHolder global ) {
                return Constants.INSTANCE.remove();
            }
        };

        deleteDataObject.setFieldUpdater(new FieldUpdater<DataHolder, String>() {
            public void update(final int index,
                               final DataHolder dataHolder,
                               final String value) {
                boolean doIt = false;
                if (presenter.hasBindedFields(dataHolder)) {
                    doIt = Window.confirm(Constants.INSTANCE.dataObjectIsBindedMessage());
                } else {
                    doIt = Window.confirm(Constants.INSTANCE.areYouSureRemoveDataObject());
                }
                if (doIt) presenter.removeDataHolder(dataHolder.getName());
            }
        });

        dataObjectTable.addColumn(deleteDataObject);
    }

    @Override
    public void initView() {
        doInit(true);
        show();
    }

    @Override
    public void refreshView() {
        doInit( false );
    }

    protected void doInit( boolean clearTypes ) {
        clearState(clearTypes);
        dataHolderListDataProvider.setList(presenter.getFormDefinition().getDataHolders());
        dataHolderListDataProvider.refresh();
    }

    @UiHandler("newDataObject")
    protected void addDataObject( ClickEvent event ) {
        if ( validate() ) {
            presenter.addDataHolder(getDataObjectID(), getDataObjectType());
            refreshView();
        }
    }

    public String getDataObjectID() {
        return dataObjectID.getText();
    }

    public String getDataObjectType() {
        return dataObjectType.getSelectedValue();
    }

    public boolean validate() {
        boolean validateId = validateDataObjectId();
        boolean validateType = validateDataObjectType();
        return validateId && validateType;
    }

    protected boolean validateDataObjectId() {
        boolean valid = true;

        String value = getDataObjectID();
        String errorMsg = "";
        if (value == null || value.isEmpty()) {
            errorMsg = Constants.INSTANCE.idCannotBeEmpty();
            valid = false;
        } else if ( presenter.getFormDefinition().getDataHolderByName(value) != null ) {
            errorMsg = Constants.INSTANCE.idAreadyExists();
            valid = false;
        }

        if ( !valid ) {
            dataObjectIDGroup.setValidationState(ValidationState.ERROR);
        } else {
            dataObjectIDGroup.setValidationState( ValidationState.NONE );
        }
        dataObjectIDHelp.setText( errorMsg );

        return valid;
    }

    protected boolean validateDataObjectType() {
        boolean valid = true;

        String value = getDataObjectType();
        String errorMsg = "";
        if (value == null || value.isEmpty()) {
            errorMsg = Constants.INSTANCE.typeCannotBeEmpty();
            valid = false;
        }

        if ( !valid ) {
            dataObjectTypeGroup.setValidationState(ValidationState.ERROR);
        } else {
            dataObjectTypeGroup.setValidationState( ValidationState.NONE );
        }
        dataObjectTypeHelp.setText(errorMsg);

        return valid;
    }

    @Override
    public void addDataType(String dataType) {
        dataObjectType.addItem(dataType);
    }

    protected void clearState( boolean clearTypes ) {
        dataObjectIDGroup.setValidationState(ValidationState.NONE);
        dataObjectID.setValue("");
        dataObjectIDHelp.setText("");
        dataObjectTypeGroup.setValidationState(ValidationState.NONE);
        if ( clearTypes ) {
            dataObjectType.clear();
            dataObjectType.addItem( "" );
        } else {
            dataObjectType.setSelectedIndex( 0 );
        }
        dataObjectTypeHelp.setText("");
    }
}
