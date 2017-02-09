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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.appformer.provisioning.client.resources.i18n.AppFormerProvisioningConstants;
import org.kie.appformer.provisioning.service.AppFormerProvisioningService;
import org.kie.appformer.provisioning.service.DataSourceInfo;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

@Dependent
public class ConfigureApplicationPage
        extends ProvisioningWizardPage
        implements ConfigureApplicationPageView.Presenter {

    private ConfigureApplicationPageView view;

    private Caller< AppFormerProvisioningService > provisioningService;

    private Map< String, DataSourceInfo > dataSourceInfos = new HashMap<>( );

    @Inject
    public ConfigureApplicationPage( final ConfigureApplicationPageView view,
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
        callback.callback( model.getDataSourceInfo( ) != null );
    }

    @Override
    public void prepareView( ) {
        if ( model.isReloadDataSources( ) ) {
            loadPageInfo( );
        }
    }

    @Override
    public Widget asWidget( ) {
        return ElementWrapperWidget.getWidget( view.getElement( ) );
    }

    public void clear( ) {
        dataSourceInfos.clear( );
        view.clearDataSourceOptions( );
    }

    @Override
    public void onDataSourceChange( ) {
        if ( view.getDataSource( ) == null || "".equals( view.getDataSource( ) ) ) {
            model.setDataSourceInfo( null );
        } else {
            model.setDataSourceInfo( dataSourceInfos.get( view.getDataSource( ) ) );
        }
        notifyChange( );
    }

    private void loadPageInfo( ) {
        clear();
        provisioningService.call( ( RemoteCallback< Collection< DataSourceInfo > > ) this::loadDataSources ).findAvailableDataSources(
                model.getHost( ),
                model.getManagementPort( ),
                model.getManagementUser( ),
                model.getManagementPassword( ),
                model.getManagementRealm( ) );
    }

    private void loadDataSources( Collection< DataSourceInfo > dataSourceInfos ) {
        List< Pair< String, String > > options = new ArrayList<>( );
        dataSourceInfos.forEach( dsInfo -> {
            this.dataSourceInfos.put( dsInfo.getDeploymentId( ), dsInfo );
            options.add( new Pair<>( buildDescription( dsInfo ), dsInfo.getDeploymentId( ) ) );
        } );
        view.loadDataSourceOptions( options, true );
        view.setDataSource( "" );
        model.setReloadDataSources( false );
    }

    private String buildDescription( DataSourceInfo dsInfo ) {
        return dsInfo.isKieDataSource( ) ? dsInfo.getName( ) :
                translationService.format(
                        AppFormerProvisioningConstants.ConfigureApplicationPage_ExternalDataSourceDescriptionFormat,
                        dsInfo.getName( )
                );
    }
}