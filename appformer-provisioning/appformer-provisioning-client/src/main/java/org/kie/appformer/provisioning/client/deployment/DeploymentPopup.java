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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.appformer.provisioning.client.resources.i18n.AppFormerProvisioningConstants;
import org.kie.appformer.provisioning.service.GwtWarBuildService;
import org.kie.appformer.provisioning.service.TestConnectionResult;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefQueryService;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.resources.i18n.CommonConstants;

@ApplicationScoped
public class DeploymentPopup
        implements DeploymentPopupView.Presenter {

    private DeploymentPopupView view;

    private Caller<GwtWarBuildService> buildCaller;

    private Caller<DataSourceDefQueryService> queryService;

    private TranslationService translationService;

    private Project project;

    private Map<String, DataSourceDefInfo> dataSourceInfos = new HashMap<>(  );

    public DeploymentPopup( ) {
    }

    @Inject
    public DeploymentPopup( DeploymentPopupView view,
                            Caller<DataSourceDefQueryService > queryService,
                            Caller<GwtWarBuildService> buildCaller,
                            TranslationService translationService ) {
        this.view = view;
        this.queryService = queryService;
        this.buildCaller = buildCaller;
        this.translationService = translationService;
        this.view.init( this );
    }

    public void show( Project project ) {
        this.project = project;
        setDefaultParams();
        queryService.call( getLoadSuccessCallback() ).findGlobalDataSources( true );
    }

    private void setDefaultParams() {
        view.setLocal( true );
        view.setRemoteServerOptionsHidden( true );
        view.setHost( "localhost" );
        view.setPort( "8080" );
        view.setManagementPort( "9990" );
        view.setManagementUser( "testadmin" );
        view.setManagementPassword( "testadmin" );
    }

    private Map<String, String> getInputParams() {
        Map<String, String> result = new HashMap<>(  );
        if ( view.getLocal() ) {
            result.put( "provider-name", "local" );
        } else {
            result.put( "host", view.getHost( ) );
            result.put( "port", view.getPort( ) );
            result.put( "management-port", view.getManagementPort( ) );
            result.put( "wildfly-user", view.getManagementUser( ) );
            result.put( "wildfly-password", view.getManagementPassword( ) );
        }

        DataSourceDefInfo dataSourceDefInfo = dataSourceInfos.get( view.getDataSource() );
        if ( dataSourceDefInfo.isManaged() ) {
            result.put( "kie-data-source", dataSourceDefInfo.getUuid() );
        } else if ( dataSourceDefInfo.isDeployed() ) {
            result.put( "jndi-data-source", dataSourceDefInfo.getDeploymentInfo().getJndi() );
        }

        return result;
    }

    @Override
    public void onOk( ) {
        if ( view.getRemote() && !validateRemoteParams() ) {
            return;
        }
        buildCaller.call( ).buildAndDeployProvisioningMode( project, getInputParams() );
        view.hide();
    }

    private boolean validateRemoteParams() {
        boolean result = !view.getHost().trim().isEmpty() &&
                !view.getPort().trim().isEmpty() &&
                !view.getManagementPort().trim().isEmpty() &&
                !view.getManagementUser().trim().isEmpty() &&
                !view.getManagementPassword().trim().isEmpty();
        if ( !result ) {
            view.showMessage( CommonConstants.INSTANCE.Error(),
                    translationService.getTranslation( AppFormerProvisioningConstants.DeploymentPopup_CompleteParametersMessage ) );
            return false;
        }
        try {
            Integer.parseInt( view.getPort( ) );
        } catch ( NumberFormatException e ) {
            view.showMessage( CommonConstants.INSTANCE.Error(),
                    translationService.getTranslation( AppFormerProvisioningConstants.DeploymentPopup_PortNumberError ) );
            return false;
        }
        try {
            Integer.parseInt( view.getManagementPort() );
        } catch ( NumberFormatException e ) {
            view.showMessage( CommonConstants.INSTANCE.Error(),
                    translationService.getTranslation( AppFormerProvisioningConstants.DeploymentPopup_ManagementPortNumberError ) );
            return false;
        }
        return true;
    }

    @Override
    public void onLocalChange( ) {
        view.setRemoteServerOptionsHidden( view.getLocal() );
    }

    @Override
    public void onRemoteChange( ) {
        view.setRemoteServerOptionsHidden( !view.getRemote() );
    }

    @Override
    public void onTestConnection( ) {
        if ( validateRemoteParams() ) {
            buildCaller.call( new RemoteCallback< TestConnectionResult >( ) {
                @Override
                public void callback( TestConnectionResult response ) {
                    String message = response.getManagementConnectionError( ) ?
                            translationService.format( AppFormerProvisioningConstants.DeploymentPopup_TestConnectionFailMessage, response.getManagementConnectionMessage( ) ) :
                            translationService.format( AppFormerProvisioningConstants.DeploymentPopup_TestConnectionSuccessfulMessage, response.getManagementConnectionMessage( ) );
                    view.showMessage( CommonConstants.INSTANCE.Information( ), message );
                }
            } ).testConnection( view.getHost( ),
                    Integer.parseInt( view.getPort( ) ), Integer.parseInt( view.getManagementPort( ) ),
                    view.getManagementUser( ), view.getManagementPassword( ) );
        }
    }

    private RemoteCallback<Collection<DataSourceDefInfo>> getLoadSuccessCallback() {
        return dataSourceDefInfos -> {
            loadDataSources( dataSourceDefInfos );
            view.show();
        };
    }

    private void loadDataSources( Collection<DataSourceDefInfo> dataSourceDefInfos ) {
        List<Pair<String, String>> options = new ArrayList<>(  );
        dataSourceDefInfos
                .stream()
                .forEach( dataSourceDefInfo -> {
                    options.add( new Pair<>( dataSourceDefInfo.getName( ), dataSourceDefInfo.getUuid( ) ) );
                    dataSourceInfos.put( dataSourceDefInfo.getUuid(), dataSourceDefInfo );
                }
                );
        view.loadDataSourceOptions( options, false );
    }
}