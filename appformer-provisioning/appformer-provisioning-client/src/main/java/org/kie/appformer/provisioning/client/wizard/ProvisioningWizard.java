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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.appformer.provisioning.client.resources.i18n.AppFormerProvisioningConstants;
import org.kie.appformer.provisioning.service.AppFormerProvisioningService;
import org.kie.appformer.provisioning.service.DataSourceInfo;
import org.kie.appformer.provisioning.service.ServerOptions;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.core.client.wizards.AbstractWizard;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
public class ProvisioningWizard
        extends AbstractWizard {

    private final List< WizardPage > pages = new ArrayList<>( );

    private ConfigureServerPage configureServerPage;

    private ConfigureApplicationPage configureApplicationPage;

    private TranslationService translationService;

    private Event< NotificationEvent > notification;

    private ProvisioningWizardModel model;

    private ServerOptions serverOptions;

    private Caller< AppFormerProvisioningService > provisioningService;

    @Inject
    public ProvisioningWizard( final ConfigureServerPage configureServerPage,
                               final ConfigureApplicationPage configureApplicationPage,
                               final TranslationService translationService,
                               final Event< NotificationEvent > notification,
                               final Caller< AppFormerProvisioningService > provisioningService ) {
        this.configureServerPage = configureServerPage;
        this.configureApplicationPage = configureApplicationPage;
        this.translationService = translationService;
        this.notification = notification;
        this.provisioningService = provisioningService;
    }

    @PostConstruct
    public void init( ) {
        configureServerPage.setHandler( ( ) -> doServerConfigChange( ) );
        pages.add( configureServerPage );
        provisioningService.call( ( RemoteCallback< ServerOptions > ) serverOptions -> {
                    this.serverOptions = serverOptions;
                }
        ).getLocalServerOptions( );
    }

    public void start( Project project ) {
        safeDelete( configureApplicationPage );
        model = new ProvisioningWizardModel( );
        model.setProject( project );
        model.setReloadDataSources( true );
        if ( serverOptions != null ) {
            model.setHost( serverOptions.getHost( ) );
            model.setPort( serverOptions.getPort( ) );
            model.setManagementPort( serverOptions.getManagementPort( ) );
            model.setManagementUser( serverOptions.getUser( ) );
            model.setManagementPassword( serverOptions.getPassword( ) );
            model.setManagementRealm( serverOptions.getRealm( ) );
        }
        configureServerPage.clear( );
        configureServerPage.setModel( model );
        configureApplicationPage.clear( );
        configureApplicationPage.setModel( model );
        start( );
    }

    @Override
    public void start( ) {
        if ( model == null ) {
            throw new RuntimeException( "Use the ProvisioningWizard.start( Project project ) method instead" );
        }
        super.start( );
    }

    @Override
    public List< WizardPage > getPages( ) {
        return pages;
    }

    @Override
    public Widget getPageWidget( int pageNumber ) {
        WizardPage page = pages.get( pageNumber );
        page.prepareView( );
        return page.asWidget( );
    }

    @Override
    public String getTitle( ) {
        return translationService.getTranslation( AppFormerProvisioningConstants.ProvisioningWizard_title );
    }

    @Override
    public int getPreferredHeight( ) {
        return 600;
    }

    @Override
    public int getPreferredWidth( ) {
        return 700;
    }

    @Override
    public void isComplete( Callback< Boolean > callback ) {
        final int[] unCompletedPages = { this.pages.size( ) };
        pages.forEach( wizardPage -> wizardPage.isComplete( result -> {
            if ( Boolean.TRUE.equals( result ) ) {
                unCompletedPages[ 0 ]--;
            }
        } ) );
        callback.callback( unCompletedPages[ 0 ] == 0 );
    }

    @Override
    public void complete( ) {
        provisioningService.call( ).startProvisioning( model.getProject( ), buildParams( ) );
        notification.fire( new NotificationEvent(
                translationService.getTranslation( AppFormerProvisioningConstants.ProvisioningWizard_provisioningStartedMessage ) ) );
        super.complete( );
    }

    private void doServerConfigChange( ) {
        model.setDataSourceInfo( null );
        model.setReloadDataSources( true );
        if ( model.isServerConfigValid( ) ) {
            safeAdd( configureApplicationPage );
        } else {
            safeDelete( configureApplicationPage );
        }
        super.start( );
    }

    private void safeAdd( ProvisioningWizardPage page ) {
        if ( !pages.contains( page ) ) {
            pages.add( page );
        }
    }

    private void safeDelete( ProvisioningWizardPage page ) {
        pages.remove( page );
    }

    private Map< String, String > buildParams( ) {
        Map< String, String > result = new HashMap<>( );
        DataSourceInfo dataSourceInfo = model.getDataSourceInfo( );
        result.put( "host", model.getHost( ) );
        result.put( "port", String.valueOf( model.getPort( ) ) );
        result.put( "management-port", String.valueOf( model.getManagementPort( ) ) );
        result.put( "management-realm", model.getManagementRealm( ) );
        result.put( "wildfly-user", model.getManagementUser( ) );
        result.put( "wildfly-password", model.getManagementPassword( ) );
        result.put( "wildfly-realm", model.getManagementRealm( ) );
        result.put( "jndi-data-source", dataSourceInfo.getJndi( ) );
        if ( dataSourceInfo.isKieDataSource( ) ) {
            result.put( "kie-data-source", dataSourceInfo.getKieUuid( ) );
            result.put( "kie-data-source-deployment-id", dataSourceInfo.getDeploymentId( ) );
        }
        return result;
    }
}