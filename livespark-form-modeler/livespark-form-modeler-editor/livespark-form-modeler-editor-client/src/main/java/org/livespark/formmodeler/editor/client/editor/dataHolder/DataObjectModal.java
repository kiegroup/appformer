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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.livespark.formmodeler.editor.client.editor.FormEditorPresenter;
import org.livespark.formmodeler.editor.client.resources.i18n.Constants;
import org.livespark.formmodeler.editor.model.FormDefinition;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKCancelButtons;

import java.util.List;

/**
 * Created by pefernan on 7/9/15.
 */
public class DataObjectModal extends BaseModal {


    interface DataObjectModalBinder
            extends
            UiBinder<Widget, DataObjectModal> {

    }

    private static DataObjectModalBinder uiBinder = GWT.create(DataObjectModalBinder.class);

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

    private final Command okCommand = new Command() {
        @Override
        public void execute() {
            if ( validate() ) {
                presenter.addDataHolder( getDataObjectID(), getDataObjectType() );
                hide();
                clearState();
            }
        }
    };

    private final Command cancelCommand = new Command() {
        @Override
        public void execute() {
            hide();
            clearState();
        }
    };

    private final ModalFooterOKCancelButtons footer = new ModalFooterOKCancelButtons( okCommand,
            cancelCommand );

    private FormEditorPresenter presenter;

    private FormDefinition definition;

    public DataObjectModal( ) {
        setTitle( Constants.INSTANCE.addDataObject() );
        add(new ModalBody() {{
            add(uiBinder.createAndBindUi(DataObjectModal.this));
        }});
        add( footer );
    }

    public void init( List<String> classes ) {
        clearState();
        if (classes == null) return;

        for (String className : classes) {
            dataObjectType.addItem(className);
        }
    }

    protected void clearState() {
        dataObjectIDGroup.setValidationState(ValidationState.NONE);
        dataObjectID.setValue("");
        dataObjectIDHelp.setText("");
        dataObjectTypeGroup.setValidationState(ValidationState.NONE);
        dataObjectType.clear();
        dataObjectTypeHelp.setText("");
    }

    public String getDataObjectID() {
        return dataObjectID.getText();
    }

    public String getDataObjectType() {
        return dataObjectType.getSelectedValue();
    }

    public boolean validate() {
        return validateDataObjectId() && validateDataObjectType();
    }

    protected boolean validateDataObjectId() {
        boolean valid = true;

        String value = getDataObjectID();
        String errorMsg = "";
        if (value == null || value.isEmpty()) {
            errorMsg = Constants.INSTANCE.idCannotBeEmpty();
            valid = false;
        } else if ( definition.getDataHolderByName(value) != null ) {
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
        dataObjectTypeHelp.setText( errorMsg );

        return valid;
    }

    public void setPresenter(FormEditorPresenter presenter) {
        this.presenter = presenter;
        this.definition = presenter.getFormDefinition();
    }
}
