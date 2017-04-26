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

package org.kie.appformer.ala.wildfly.executor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import org.guvnor.ala.config.Config;
import org.guvnor.ala.exceptions.ProvisioningException;
import org.guvnor.ala.registry.RuntimeRegistry;
import org.guvnor.ala.wildfly.access.WildflyAccessInterface;
import org.guvnor.ala.wildfly.access.WildflyAppState;
import org.guvnor.ala.wildfly.config.WildflyRuntimeConfiguration;
import org.guvnor.ala.wildfly.config.WildflyRuntimeExecConfig;
import org.guvnor.ala.wildfly.executor.WildflyRuntimeExecExecutor;
import org.guvnor.ala.wildfly.model.WildflyProvider;
import org.guvnor.ala.wildfly.model.WildflyRuntime;
import org.kie.appformer.ala.wildfly.config.AppFormerWildflyRuntimeExecConfig;
import org.kie.workbench.common.screens.datasource.management.backend.integration.wildfly.WildflyDataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Specializes
public class AppFormerWildflyRuntimeExecExecutor<T extends WildflyRuntimeConfiguration>
        extends WildflyRuntimeExecExecutor<T> {

    private static final Logger logger = LoggerFactory.getLogger( AppFormerWildflyRuntimeExecExecutor.class );

    private RuntimeRegistry runtimeRegistry;

    private WildflyAccessInterface wildfly;

    private AppFormerProvisioningHelper provisioningHelper;

    @Inject
    public AppFormerWildflyRuntimeExecExecutor( final RuntimeRegistry runtimeRegistry,
                                                final WildflyAccessInterface wildfly,
                                                final AppFormerProvisioningHelper provisioningHelper ) {
        super( runtimeRegistry, wildfly );
        this.runtimeRegistry = runtimeRegistry;
        this.wildfly = wildfly;
        this.provisioningHelper = provisioningHelper;
    }

    @Override
    public Optional< WildflyRuntime > apply( final WildflyRuntimeConfiguration runtimeConfig ) {
        if ( runtimeConfig instanceof  AppFormerWildflyRuntimeExecConfig ) {
            logger.info("Starting executor");
            AppFormerWildflyRuntimeExecConfig appFormerRuntimeConfig = (AppFormerWildflyRuntimeExecConfig) runtimeConfig;
            final Optional< WildflyProvider > optional = runtimeRegistry.getProvider(appFormerRuntimeConfig.getProviderId(),
                                                                                     WildflyProvider.class);
            WildflyProvider wildflyProvider = optional.get();

            logger.info("jndi-data-source: " + appFormerRuntimeConfig.getJndiDataSource());
            logger.info("kie-data-source: " + appFormerRuntimeConfig.getKieDataSource());
            logger.info("kie-data-source-deployment-id: " + appFormerRuntimeConfig.getKieDataSourceDeploymentId());
            logger.info("providerId: " + wildflyProvider.getId());
            logger.info("hostId: " + wildflyProvider.getHostId());
            logger.info("port: " + wildflyProvider.getPort());
            logger.info("managementPort: " + wildflyProvider.getManagementPort());
            logger.info("user: " + wildflyProvider.getUser());
            logger.info("password: " + (wildflyProvider.getPassword() != null ? "******" : null));

            Path warPath = Paths.get(appFormerRuntimeConfig.getWarPath());
            if (appFormerRuntimeConfig.getKieDataSource() != null || appFormerRuntimeConfig.getJndiDataSource() != null) {
                boolean hasPersistence = false;
                try {
                    hasPersistence = provisioningHelper.hasPersistenceSettings(warPath);
                } catch (Exception e) {
                    //un-common case, let the provisioning continue.
                    logger.warn("It was not possible to establish if the persistence is configured for current application: " + warPath,
                                e);
                }
                if (!hasPersistence) {
                    logger.warn("Persistence configuration was not found for current application: " + warPath);
                } else {
                    prepareForProvisioning(appFormerRuntimeConfig,
                                           wildflyProvider);
                }
            }
        }
        Optional< WildflyRuntime > result = super.apply( ( WildflyRuntimeConfiguration ) runtimeConfig );
        logger.info( "Executor finished, wildflyRuntime: " + result.get( ) );
        return result;
    }

    private void prepareForProvisioning( AppFormerWildflyRuntimeExecConfig runtimeConfig, WildflyProvider wildflyProvider ) {
        Path warPath = Paths.get( runtimeConfig.getWarPath( ) );
        String warName = warPath.getFileName( ).toString( );

        WildflyAppState appState = wildfly.getWildflyClient( wildflyProvider ).getAppState( warName );
        if ( "Running".equals( appState.getState( ) ) && runtimeConfig.getRedeployStrategy( ).equals( "auto" ) ) {
            //the application is already deployed and re-deployment was selected.
            try {
                wildfly.getWildflyClient( wildflyProvider ).undeploy( warName );
                unDeployDataSources( runtimeConfig, wildflyProvider, warName );
                unDeployDrivers( runtimeConfig, wildflyProvider, warName );
            } catch ( Exception e ) {
                String msg = "An error was produced during application un-deployment: " + warPath;
                logger.error( msg, e );
                throw new ProvisioningException( msg, e );
            }
        }

        String targetDataSource;
        if ( runtimeConfig.getKieDataSource( ) == null ) {
            targetDataSource = runtimeConfig.getJndiDataSource( );
        } else {
            // the wildlfyProvider must also provide a realm.
            String realm = runtimeConfig.getRealm();
            int port = Integer.parseInt( wildflyProvider.getManagementPort( ) );

            boolean deploy;
            try {
                deploy = runtimeConfig.getKieDataSourceDeploymentId( ) != null &&
                        provisioningHelper.findWildflyDataSource( wildflyProvider.getHostId( ),
                                port,
                                wildflyProvider.getUser( ),
                                wildflyProvider.getPassword( ),
                                realm,
                                runtimeConfig.getKieDataSourceDeploymentId( ) ) == null;

            } catch ( Exception e ) {
                throw new ProvisioningException( "It was not possible to verify data source availability on target server.", e );
            }

            if ( !deploy ) {
                targetDataSource = runtimeConfig.getJndiDataSource( );
            } else {
                DataSourceDef dataSourceDef = provisioningHelper.findDataSourceDef( runtimeConfig.getKieDataSource( ) );
                DriverDef driverDef;
                if ( dataSourceDef == null ) {
                    throw new ProvisioningException( "Data source definition was not found for data source: " + runtimeConfig.getKieDataSource( ) );
                }
                driverDef = provisioningHelper.findDriverDef( dataSourceDef.getDriverUuid( ) );
                if ( driverDef == null ) {
                    throw new ProvisioningException( "Driver definition was not found for driver: " + dataSourceDef.getDriverUuid( ) );
                }

                String randomSufix = UUID.randomUUID( ).toString( );
                String appName = warPath.getFileName( ).toString( );
                String driverDeploymentId = appName + "_driver_" + driverDef.getUuid( ) + "_" + randomSufix;
                try {
                    driverDeploymentId = provisioningHelper.deployDriver( wildflyProvider.getHostId( ),
                            port, wildflyProvider.getUser( ), wildflyProvider.getPassword( ), realm, driverDef, driverDeploymentId );
                } catch ( Exception e ) {
                    final String msg = "It was not possible to provision driver: " + driverDef + " for application: " + warPath;
                    logger.error( msg, e );
                    throw new ProvisioningException( msg, e );
                }

                String dataSourceDeploymentId = appName + "_datasource_" + dataSourceDef.getUuid( ) + "_" + randomSufix;
                targetDataSource = "java:jboss/appformer/datasources/" + appName + "_" + randomSufix;
                try {
                    provisioningHelper.deployDataSource( wildflyProvider.getHostId( ), port,
                            wildflyProvider.getUser( ), wildflyProvider.getPassword( ), realm, dataSourceDef,
                            dataSourceDeploymentId, targetDataSource, driverDeploymentId );
                } catch ( Exception e ) {
                    final String msg = "It was not possible to provision data source: " + dataSourceDef + " for application: " + warPath;
                    logger.error( msg, e );
                    throw new ProvisioningException( msg, e );
                }
            }
        }
        updatePersistenceSettings( warPath, targetDataSource );
    }

    private void updatePersistenceSettings( Path warPath, String targetDataSource ) {
        try {
            provisioningHelper.updatePersistenceSettings( warPath, targetDataSource );
        } catch ( Exception e ) {
            final String msg = "It was not possible to update persistence configuration for application: " + warPath;
            logger.error( msg, e );
            throw new ProvisioningException( msg, e );
        }
    }

    private void unDeployDrivers( AppFormerWildflyRuntimeExecConfig runtimeConfig, WildflyProvider wildflyProvider, String warName ) throws Exception {
        int port = Integer.parseInt( wildflyProvider.getManagementPort( ) );
        String realm = runtimeConfig.getRealm();
        String appDriverPrefix = warName + "_driver_";
        Collection< String > appDrivers = provisioningHelper.findWildflyDeployments( wildflyProvider.getHostId( ),
                port, wildflyProvider.getUser( ), wildflyProvider.getPassword( ), realm, appDriverPrefix );

        for ( String deploymentId : appDrivers ) {
            provisioningHelper.unDeployDriver( wildflyProvider.getHostId( ),
                    port, wildflyProvider.getUser( ), wildflyProvider.getPassword( ), realm, deploymentId );
        }
    }

    private void unDeployDataSources( AppFormerWildflyRuntimeExecConfig runtimeConfig, WildflyProvider wildflyProvider, String warName ) throws Exception {
        int port = Integer.parseInt( wildflyProvider.getManagementPort( ) );
        String realm = runtimeConfig.getRealm();
        Collection< WildflyDataSourceDef > currentDataSources = provisioningHelper.findWildflyDataSources(
                wildflyProvider.getHostId( ), port, wildflyProvider.getUser( ), wildflyProvider.getPassword( ), realm, warName );
        for ( WildflyDataSourceDef dataSourceDef : currentDataSources ) {
            provisioningHelper.unDeployDataSource( wildflyProvider.getHostId( ),
                    port, wildflyProvider.getUser( ), wildflyProvider.getPassword( ), realm, dataSourceDef.getName( ) );
        }
    }

    @Override
    public Class< ? extends Config > executeFor( ) {
        return WildflyRuntimeExecConfig.class;
    }

}