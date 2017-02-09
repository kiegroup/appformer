/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.provisioning.client.wizard;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.appformer.provisioning.client.resources.i18n.AppFormerProvisioningConstants;
import org.kie.appformer.provisioning.service.AppFormerProvisioningService;
import org.kie.appformer.provisioning.service.TestConnectionResult;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.common.client.resources.i18n.CommonConstants;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

@Dependent
public class ConfigureServerPage
        extends ProvisioningWizardPage
        implements ConfigureServerPageView.Presenter {

    public interface ConfigureServerPageHandler {
        void onServerConfigChange( );
    }

    private ConfigureServerPageView view;

    private Caller< AppFormerProvisioningService > provisioningService;

    private ConfigureServerPageHandler handler;

    @Inject
    public ConfigureServerPage( final ConfigureServerPageView view,
                                final TranslationService translationService,
                                final Event< WizardPageStatusChangeEvent > statusChangeEvent,
                                final Caller< AppFormerProvisioningService > provisioningService ) {
        super( translationService, statusChangeEvent );
        this.view = view;
        this.provisioningService = provisioningService;
        view.init( this );
    }

    @Override
    public String getTitle( ) {
        return view.getPageTitle( );
    }

    @Override
    public void isComplete( Callback< Boolean > callback ) {
        callback.callback( model.isServerConfigValid( ) );
    }

    @Override
    public Widget asWidget( ) {
        return ElementWrapperWidget.getWidget( view.getElement( ) );
    }

    public void clear( ) {
        view.setFormStatusInfoMessage(
                translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_ValidateConnectionMessage ) );
        view.setHost( null );
        view.clearHostErrorMessage( );
        view.setPort( null );
        view.clearPortErrorMessage( );
        view.setManagementPort( null );
        view.clearManagementPortErrorMessage( );
        view.setManagementUser( null );
        view.clearManagementUserErrorMessage( );
        view.setManagementPassword( null );
        view.clearManagementPasswordErrorMessage( );
        view.setManagementRealm( null );
        view.clearManagementRealmErrorMessage( );
    }

    @Override
    public void setModel( ProvisioningWizardModel model ) {
        super.setModel( model );
        view.setHost( model.getHost( ) );
        view.setPort( String.valueOf( model.getPort( ) ) );
        view.setManagementPort( String.valueOf( model.getManagementPort( ) ) );
        view.setManagementUser( model.getManagementUser( ) );
        view.setManagementPassword( model.getManagementPassword( ) );
        view.setManagementRealm( model.getManagementRealm( ) );
    }

    public void setHandler( ConfigureServerPageHandler handler ) {
        this.handler = handler;
    }

    @Override
    public void onHostChange( ) {
        view.clearHostErrorMessage( );
        model.setHost( view.getHost( ).trim( ) );
        if ( isEmpty( model.getHost( ) ) ) {
            view.setHostErrorMessage(
                    translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_HostError ) );
        }
        serverParamChange( );
    }

    @Override
    public void onPortChange( ) {
        view.clearPortErrorMessage( );
        try {
            model.setPort( Integer.parseInt( view.getPort( ) ) );
        } catch ( Exception e ) {
            model.setPort( -1 );
        }
        if ( !isValidPort( model.getPort( ) ) ) {
            view.setPortErrorMessage(
                    translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_PortNumberError ) );
        }
        serverParamChange( );
    }

    @Override
    public void onManagementPortChange( ) {
        view.clearManagementPortErrorMessage( );
        try {
            model.setManagementPort( Integer.parseInt( view.getManagementPort( ) ) );
        } catch ( Exception e ) {
            model.setManagementPort( -1 );
        }
        if ( !isValidPort( model.getManagementPort( ) ) ) {
            view.setManagementPortErrorMessage(
                    translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_ManagementPortNumberError ) );
        }
        serverParamChange( );
    }

    @Override
    public void onManagementRealmChange( ) {
        view.clearManagementRealmErrorMessage( );
        model.setManagementRealm( view.getManagementRealm( ).trim( ) );
        if ( isEmpty( model.getManagementRealm( ) ) ) {
            view.setManagementRealmErrorMessage(
                    translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_ManagementRealmError ) );
        }
        serverParamChange( );
    }

    @Override
    public void onManagementUserChange( ) {
        view.clearManagementUserErrorMessage( );
        model.setManagementUser( view.getManagementUser( ).trim( ) );
        if ( isEmpty( model.getManagementUser( ) ) ) {
            view.setManagementUserErrorMessage(
                    translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_ManagementUserError ) );
        }
        serverParamChange( );
    }

    @Override
    public void onManagementPasswordChange( ) {
        view.clearManagementPasswordErrorMessage( );
        model.setManagementPassword( view.getManagementPassword( ).trim( ) );
        if ( isEmpty( model.getManagementPassword( ) ) ) {
            view.setManagementPasswordErrorMessage(
                    translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_ManagementPasswordError ) );
        }
        serverParamChange( );
    }

    private void serverParamChange( ) {
        view.setFormStatusInfoMessage(
                translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_ValidateConnectionMessage ) );
        model.setServerConfigValid( false );
        handler.onServerConfigChange( );
    }

    @Override
    public void onTestConnection( ) {
        if ( validateRemoteParams( ) ) {
            provisioningService.call( ( TestConnectionResult response ) -> {
                String message = response.getManagementConnectionError( ) ?
                        translationService.format( AppFormerProvisioningConstants.ConfigureServerPage_TestConnectionFailMessage, response.getManagementConnectionMessage( ) ) :
                        translationService.format( AppFormerProvisioningConstants.ConfigureServerPage_TestConnectionSuccessfulMessage, response.getManagementConnectionMessage( ) );
                view.showMessage( CommonConstants.INSTANCE.Information( ), message );
                model.setServerConfigValid( !response.getManagementConnectionError( ) );
                handler.onServerConfigChange( );
                if ( response.getManagementConnectionError( ) ) {
                    view.setFormStatusErrorMessage(
                            translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_ConnectionFailuresMessage ) );
                } else {
                    view.setFormStatusSuccessMessage(
                            translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_ConnectionValidatedMessage ) );
                }
            } ).testConnection( view.getHost( ), model.getPort( ), model.getManagementPort( ),
                    model.getManagementUser( ), model.getManagementPassword( ), model.getManagementRealm( ) );
        }
    }

    private boolean validateRemoteParams( ) {
        boolean result = !isEmpty( model.getHost( ) ) &&
                isValidPort( model.getPort( ) ) &&
                isValidPort( model.getManagementPort( ) ) &&
                !isEmpty( model.getManagementUser( ) ) &&
                !isEmpty( model.getManagementPassword( ) ) &&
                !isEmpty( model.getManagementRealm( ) );
        if ( !result ) {
            view.showMessage( CommonConstants.INSTANCE.Error( ),
                    translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_CompleteParametersMessage ) );
            return false;
        }
        return true;
    }

    private boolean isValidPort( int port ) {
        return port > 0 && port <= 65535;
    }

    private boolean isEmpty( String value ) {
        return value == null || value.trim( ).isEmpty( );
    }
}