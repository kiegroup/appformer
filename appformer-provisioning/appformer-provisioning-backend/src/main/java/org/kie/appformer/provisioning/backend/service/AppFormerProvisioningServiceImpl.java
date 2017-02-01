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

package org.kie.appformer.provisioning.backend.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.appformer.ala.wildfly.executor.AppFormerProvisioningHelper;
import org.kie.appformer.provisioning.service.AppFormerProvisioningService;
import org.kie.appformer.provisioning.service.DataSourceInfo;
import org.kie.appformer.provisioning.service.GwtWarBuildService;
import org.kie.appformer.provisioning.service.ServerOptions;
import org.kie.appformer.provisioning.service.TestConnectionResult;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefInfo;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class AppFormerProvisioningServiceImpl
        implements AppFormerProvisioningService {

    private DataSourceDefQueryService dataSourceDefQueryService;

    private AppFormerProvisioningHelper provisioningHelper;

    private GwtWarBuildService buildService;

    private static final String APP_FORMER_PROVISIONING_PREFIX = "appformer.provisioning";

    private static final String APP_FORMER_HOST = APP_FORMER_PROVISIONING_PREFIX + ".host";

    private static final String APP_FORMER_PORT = APP_FORMER_PROVISIONING_PREFIX + ".port";

    private static final String APP_FORMER_MANAGEMENT_PORT = APP_FORMER_PROVISIONING_PREFIX + ".managementPort";

    private static final String APP_FORMER_USER = APP_FORMER_PROVISIONING_PREFIX + ".user";

    private static final String APP_FORMER_PASSWORD = APP_FORMER_PROVISIONING_PREFIX + ".password";

    private static final String APP_FORMER_REALM = APP_FORMER_PROVISIONING_PREFIX + ".realm";

    private static final String DEFAULT_HOST = "localhost";

    private static final int DEFAULT_HTTP_PORT = 8080;

    private static final int DEFAULT_MANAGEMENT_PORT = 9990;

    private static final String DEFAULT_USER = "admin";

    private static final String DEFAULT_PASSWORD = "admin";

    private static final String DEFAULT_REALM = "ManagementRealm";

    private static final Logger logger = LoggerFactory.getLogger( AppFormerProvisioningService.class );

    @Inject
    public AppFormerProvisioningServiceImpl( AppFormerProvisioningHelper provisioningHelper,
                                             DataSourceDefQueryService dataSourceDefQueryService,
                                             GwtWarBuildService buildService ) {
        this.provisioningHelper = provisioningHelper;
        this.dataSourceDefQueryService = dataSourceDefQueryService;
        this.buildService = buildService;
    }

    @Override
    public void startProvisioning( Project project, Map< String, String > params ) {
        buildService.buildAndDeployProvisioningMode( project, params );
    }

    public Collection< DataSourceInfo > findAvailableDataSources( String host,
                                                                  int port,
                                                                  String user,
                                                                  String password,
                                                                  String realm ) {
        try {
            List< DataSourceInfo > result = new ArrayList<>( );
            Map< String, DataSourceInfo > dataSources = new HashMap<>( );
            dataSourceDefQueryService.findGlobalDataSources( false )
                    .stream( )
                    .filter( defInfo -> defInfo.isDeployed( ) && defInfo.isManaged( ) )
                    .forEach( defInfo -> {
                                DataSourceInfo dsInfo = new DataSourceInfo(
                                        defInfo.getName( ),
                                        defInfo.getUuid( ),
                                        defInfo.getDeploymentInfo( ).getDeploymentId( ),
                                        defInfo.getDeploymentInfo( ).getJndi( )
                                );
                                dataSources.put( defInfo.getDeploymentInfo( ).getDeploymentId( ), dsInfo );
                                result.add( dsInfo );
                            }
                    );
            provisioningHelper.findWildflyDataSources( host, port, user, password, realm )
                    .forEach( wfDs -> {
                        DataSourceInfo dsInfo = dataSources.get( wfDs.getName( ) );
                        if ( dsInfo != null ) {
                            dsInfo.setJndi( wfDs.getJndi( ) );
                        } else {
                            dsInfo = new DataSourceInfo( wfDs.getName( ), wfDs.getName( ), wfDs.getJndi( ) );
                            result.add( dsInfo );
                        }
                    } );
            return result;
        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public TestConnectionResult testConnection( String host,
                                                int port,
                                                int managementPort,
                                                String user,
                                                String password,
                                                String realm ) {
        TestConnectionResult result = new TestConnectionResult( );
        try {
            String message = provisioningHelper.testRestManagementConnection( host,
                    managementPort, user, password );
            message = provisioningHelper.testManagementConnection( host,
                    managementPort, user, password, realm );
            result.setManagementConnectionError( false );
            result.setManagementConnectionMessage( message );
        } catch ( Exception e ) {
            result.setManagementConnectionError( true );
            result.setManagementConnectionMessage( e.getMessage( ) );
        }
        return result;
    }

    @Override
    public ServerOptions getLocalServerOptions( ) {
        String host;
        int port;
        int managementPort;
        String user;
        String password;
        String realm;

        host = readProperty( APP_FORMER_HOST, DEFAULT_HOST );
        port = readIntProperty( APP_FORMER_PORT, -1 );
        if ( port < 0 ) {
            port = readIntProperty( "jboss.http.port", DEFAULT_HTTP_PORT );
        }
        managementPort = readIntProperty( APP_FORMER_MANAGEMENT_PORT, -1 );
        if ( managementPort < 0 ) {
            managementPort = readIntProperty( "jboss.management.http.port", DEFAULT_MANAGEMENT_PORT );
        }
        user = readProperty( APP_FORMER_USER, DEFAULT_USER );
        password = readProperty( APP_FORMER_PASSWORD, DEFAULT_PASSWORD );
        realm = readProperty( APP_FORMER_REALM, DEFAULT_REALM );

        return new ServerOptions( host, port, managementPort, user, password, realm );
    }

    private int readIntProperty( String name, int defaultValue ) {
        String strValue = readProperty( name, null );
        int result = defaultValue;
        if ( strValue != null ) {
            try {
                result = Integer.parseInt( strValue );
            } catch ( Exception e ) {
                logger.warn( "A wrong integer value was set for property: " + name + " = " + strValue );
            }
        }
        return result;
    }

    private String readProperty( String name, String defaultValue ) {
        return Optional.ofNullable( System.getProperties( ).getProperty( name ) ).orElse( defaultValue );
    }
}