/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.provisioning.client.deployment;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Command;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.Label;
import org.jboss.errai.common.client.dom.RadioInput;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.appformer.provisioning.client.deployment.DeploymentPopupView;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKCancelButtons;

@Templated
public class DeploymentPopupViewImpl
        implements DeploymentPopupView, IsElement {

    private Presenter presenter;

    @Inject
    @DataField( "local-server-label" )
    private Label localServerLabel;

    @Inject
    @DataField( "local-server-radio-input" )
    private RadioInput localServerRadioInput;

    @Inject
    @DataField( "remote-server-label" )
    private Label remoteServerLabel;

    @Inject
    @DataField( "remote-server-radio-input" )
    private RadioInput remoteServerRadioInput;

    @Inject
    @DataField( "remote-server-options" )
    private Div remoteServerOptionsDiv;

    @Inject
    @DataField( "host-label" )
    private Label hostLabel;

    @Inject
    @DataField( "host-text-input" )
    private TextInput hostTextInput;

    @Inject
    @DataField( "port-label" )
    private Label portLabel;

    @Inject
    @DataField( "port-text-input" )
    private TextInput portTextInput;

    @Inject
    @DataField( "management-port-label" )
    private Label managementPortLabel;

    @Inject
    @DataField( "management-port-text-input" )
    private TextInput managementPortTextInput;

    @Inject
    @DataField( "management-user-label" )
    private Label managementUserLabel;

    @Inject
    @DataField( "management-user-text-input" )
    private TextInput managementUserTextInput;

    @Inject
    @DataField( "management-password-label" )
    private Label managementPasswordLabel;

    @Inject
    @DataField( "management-password-text-input" )
    private TextInput managementPasswordTextInput;

    @Inject
    @DataField( "test-connection-button" )
    private Button testConnectionButton;

    @Inject
    @DataField ( "datasource-selector" )
    private Select dataSourceSelector;

    private BaseModal modal;

    public DeploymentPopupViewImpl(  ) {
    }

    private final Command okCommand = new Command() {
        @Override
        public void execute() {
            presenter.onOk();
        }
    };

    private final Command cancelCommand = new Command() {
        @Override
        public void execute() {
            hide();
        }
    };

    private final ModalFooterOKCancelButtons footer = new ModalFooterOKCancelButtons( okCommand, cancelCommand );

    @PostConstruct
    private void init() {
        modal = new BaseModal();
        modal.setBody( ElementWrapperWidget.getWidget( this.getElement() ) );
        modal.add( footer );
        setLocal( true );
    }

    @Override
    public void init( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public boolean getLocal( ) {
        return localServerRadioInput.getChecked();
    }

    @Override
    public void setLocal( boolean checked ) {
        localServerRadioInput.setChecked( checked );
    }

    @Override
    public boolean getRemote( ) {
        return remoteServerRadioInput.getChecked();
    }

    @Override
    public void setRemote( boolean checked ) {
        remoteServerRadioInput.setChecked( checked );
    }

    @Override
    public void setRemoteServerOptionsHidden( boolean hidden ) {
        remoteServerOptionsDiv.setHidden( hidden );
    }

    @Override
    public String getHost( ) {
        return hostTextInput.getValue();
    }

    @Override
    public void setHost( String host ) {
        hostTextInput.setValue( host );
    }

    @Override
    public String getPort( ) {
        return portTextInput.getValue();
    }

    @Override
    public void setPort( String port ) {
        this.portTextInput.setValue( port );
    }

    @Override
    public String getManagementPort( ) {
        return managementPortTextInput.getValue();
    }

    @Override
    public void setManagementPort( String managementPort ) {
        this.managementPortTextInput.setValue( managementPort );
    }

    @Override
    public String getManagementUser( ) {
        return managementUserTextInput.getValue();
    }

    @Override
    public void setManagementUser( String managementUser ) {
        this.managementUserTextInput.setValue( managementUser );
    }

    @Override
    public String getManagementPassword( ) {
        return managementPasswordTextInput.getValue();
    }

    @Override
    public void setManagementPassword( String managementPassword ) {
        this.managementPasswordTextInput.setValue( managementPassword );
    }

    @Override
    public void loadDataSourceOptions( final List<Pair<String, String>> dataSourceOptions, final boolean addEmptyOption ) {
        dataSourceSelector.clear();
        if ( addEmptyOption ) {
            dataSourceSelector.add( newOption( "", "" ) );
        }
        for ( Pair<String, String> optionPair: dataSourceOptions ) {
            dataSourceSelector.add( newOption( optionPair.getK1(), optionPair.getK2() ));
        }
        refreshDataSourceSelector();
    }

    @Override
    public String getDataSource( ) {
        return dataSourceSelector.getValue();
    }

    @Override
    public void setDataSource( String dataSource ) {
        dataSourceSelector.setValue( dataSource );
        refreshDataSourceSelector();
    }

    private Option newOption( final String text, final String value ) {
        final Option option = new Option();
        option.setValue( value );
        option.setText( text );
        return option;
    }

    private void refreshDataSourceSelector() {
        Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                dataSourceSelector.refresh();
            }
        } );
    }

    @Override
    public void show() {
        modal.show();
    }

    @Override
    public void hide() {
        modal.hide();
    }

    @Override
    public void showMessage( String title, String message ) {
        YesNoCancelPopup.newYesNoCancelPopup( title, message, null, null, ( org.uberfire.mvp.Command ) ( ) -> {
            //do nothing.
        } ).show();
    }

    @EventHandler( "local-server-radio-input" )
    private void onLocalServerRadioChange( @ForEvent( "change" ) Event event ) {
        presenter.onLocalChange();
    }

    @EventHandler( "remote-server-radio-input" )
    private void onRemoteServerRadioChange( @ForEvent( "change" ) Event event ) {
        presenter.onRemoteChange();
    }

    @EventHandler( "test-connection-button" )
    private void onTestConnection( @ForEvent( "click" ) Event event ) {
        presenter.onTestConnection();
    }
}