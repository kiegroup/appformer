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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.dmr.ModelNode;
import org.kie.appformer.ala.wildfly.util.JarUtils;
import org.kie.workbench.common.screens.datamodeller.model.persistence.PersistenceDescriptorModel;
import org.kie.workbench.common.screens.datamodeller.model.persistence.PersistenceUnitModel;
import org.kie.workbench.common.screens.datamodeller.util.PersistenceDescriptorXMLMarshaller;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceRuntimeManager;
import org.kie.workbench.common.screens.datasource.management.backend.integration.wildfly.WildflyBaseClient;
import org.kie.workbench.common.screens.datasource.management.backend.integration.wildfly.WildflyDataSourceDef;
import org.kie.workbench.common.screens.datasource.management.backend.integration.wildfly.WildflyDataSourceManagementClient;
import org.kie.workbench.common.screens.datasource.management.backend.integration.wildfly.WildflyDeploymentClient;
import org.kie.workbench.common.screens.datasource.management.backend.integration.wildfly.WildflyDriverDef;
import org.kie.workbench.common.screens.datasource.management.backend.integration.wildfly.WildflyDriverManagementClient;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefInfo;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefEditorService;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefQueryService;
import org.kie.workbench.common.screens.datasource.management.service.DriverDefEditorService;
import org.kie.workbench.common.screens.datasource.management.util.MavenArtifactResolver;

/**
 * Helper class for the AppFormer application provisioning.
 */
@ApplicationScoped
public class AppFormerProvisioningHelper {

    private static final String PERSISTENCE_SETTINGS_ENTRY = "WEB-INF/classes/META-INF/persistence.xml";

    private DataSourceRuntimeManager dataSourceRuntimeManager;

    private DataSourceDefQueryService dataSourceDefQueryService;

    private DataSourceDefEditorService dataSourceDefEditorService;

    private DriverDefEditorService driverDefEditorService;

    private MavenArtifactResolver mavenArtifactResolver;

    public AppFormerProvisioningHelper( ) {
    }

    @Inject
    public AppFormerProvisioningHelper( DataSourceRuntimeManager dataSourceRuntimeManager,
                                        DataSourceDefQueryService dataSourceDefQueryService,
                                        DataSourceDefEditorService dataSourceDefEditorService,
                                        DriverDefEditorService driverDefEditorService,
                                        MavenArtifactResolver mavenArtifactResolver ) {
        this.dataSourceRuntimeManager = dataSourceRuntimeManager;
        this.dataSourceDefQueryService = dataSourceDefQueryService;
        this.dataSourceDefEditorService = dataSourceDefEditorService;
        this.driverDefEditorService = driverDefEditorService;
        this.mavenArtifactResolver = mavenArtifactResolver;
    }

    public DataSourceDeploymentInfo findDataSourceDeploymentInfo( String dataSourceUUID ) throws Exception {
        return dataSourceRuntimeManager.getDataSourceDeploymentInfo( dataSourceUUID );
    }

    public DataSourceDef findDataSourceDef( String uuid ) {
        Collection< DataSourceDefInfo > defInfoList = dataSourceDefQueryService.findGlobalDataSources( false );
        Optional< DataSourceDefInfo > defInfo =
                defInfoList.stream( ).filter( _defInfo -> uuid.equals( _defInfo.getUuid( ) ) ).findFirst( );
        if ( defInfo.isPresent( ) ) {
            DataSourceDefEditorContent content = dataSourceDefEditorService.loadContent( defInfo.get( ).getPath( ) );
            return content != null ? content.getDef( ) : null;
        } else {
            return null;
        }
    }

    public DriverDef findDriverDef( String uuid ) {
        Collection< DriverDefInfo > defInfoList = dataSourceDefQueryService.findGlobalDrivers( );
        Optional< DriverDefInfo > defInfo =
                defInfoList.stream( ).filter( _defInfo -> uuid.equals( _defInfo.getUuid( ) ) ).findFirst( );
        if ( defInfo.isPresent( ) ) {
            DriverDefEditorContent content = driverDefEditorService.loadContent( defInfo.get( ).getPath( ) );
            return content != null ? content.getDef( ) : null;
        } else {
            return null;
        }
    }

    public String deployDriver( String host,
                                int port,
                                String user,
                                String password,
                                String realm,
                                DriverDef driverDef,
                                String deploymentId ) throws Exception {


        WildflyDriverManagementClient driverManagementClient = new WildflyDriverManagementClient( );
        driverManagementClient.loadConfig( buildConfig( host, port, user, password, realm ) );

        URI uri = mavenArtifactResolver.resolve( driverDef.getGroupId( ), driverDef.getArtifactId( ), driverDef.getVersion( ) );
        driverManagementClient.deploy( deploymentId, uri );
        return findInternalDeploymentId( driverManagementClient, deploymentId, driverDef.getDriverClass( ) );
    }

    public void unDeployDriver( String host,
                                int port,
                                String user,
                                String password,
                                String realm,
                                String deploymentId ) throws Exception {

        WildflyDriverManagementClient driverManagementClient = new WildflyDriverManagementClient( );
        driverManagementClient.loadConfig( buildConfig( host, port, user, password, realm ) );
        driverManagementClient.undeploy( deploymentId );
    }

    public void deployDataSource( String host,
                                  int port,
                                  String user,
                                  String password,
                                  String realm,
                                  DataSourceDef dataSourceDef,
                                  String dataSourceDeploymentId,
                                  String dataSourceJNDI,
                                  String driverDeploymentId ) throws Exception {

        WildflyDataSourceManagementClient dataSourceManagementClient = new WildflyDataSourceManagementClient( );
        dataSourceManagementClient.loadConfig( buildConfig( host, port, user, password, realm ) );

        WildflyDataSourceDef wfDataSourceDef = new WildflyDataSourceDef( );
        wfDataSourceDef.setName( dataSourceDeploymentId );
        wfDataSourceDef.setDriverName( driverDeploymentId );
        wfDataSourceDef.setJndi( dataSourceJNDI );
        wfDataSourceDef.setConnectionURL( dataSourceDef.getConnectionURL( ) );
        wfDataSourceDef.setUser( dataSourceDef.getUser( ) );
        wfDataSourceDef.setPassword( dataSourceDef.getPassword( ) );
        wfDataSourceDef.setUseJTA( true );

        dataSourceManagementClient.createDataSource( wfDataSourceDef );
    }

    public void unDeployDataSource( String host,
                                    int port,
                                    String user,
                                    String password,
                                    String realm,
                                    String deploymentId ) throws Exception {

        WildflyDataSourceManagementClient dataSourceManagementClient = new WildflyDataSourceManagementClient( );
        dataSourceManagementClient.loadConfig( buildConfig( host, port, user, password, realm ) );
        dataSourceManagementClient.deleteDataSource( deploymentId );
    }

    public Collection< WildflyDriverDef > findWildflyDrivers( String host,
                                                              int port,
                                                              String user,
                                                              String password,
                                                              String realm,
                                                              String prefix ) throws Exception {
        WildflyDriverManagementClient driverManagementClient = new WildflyDriverManagementClient( );
        driverManagementClient.loadConfig( buildConfig( host, port, user, password, realm ) );


        return driverManagementClient.getDeployedDrivers( )
                .stream( )
                .filter( wildflyDriverDef -> wildflyDriverDef.getDriverName( ).startsWith( prefix ) )
                .collect( Collectors.toList( ) );
    }

    public Collection< WildflyDataSourceDef > findWildflyDataSources( String host,
                                                                      int port,
                                                                      String user,
                                                                      String password,
                                                                      String realm ) throws Exception {
        return findWildflyDataSources( host, port, user, password, realm, null );
    }

    public Collection< WildflyDataSourceDef > findWildflyDataSources( String host,
                                                                      int port,
                                                                      String user,
                                                                      String password,
                                                                      String realm,
                                                                      String prefix ) throws Exception {
        WildflyDataSourceManagementClient dataSourceManagementClient = new WildflyDataSourceManagementClient( );
        dataSourceManagementClient.loadConfig( buildConfig( host, port, user, password, realm ) );


        return dataSourceManagementClient.getDataSources( )
                .stream( )
                .filter( wildflyDataSourceDef -> prefix == null || wildflyDataSourceDef.getName( ).startsWith( prefix ) )
                .collect( Collectors.toList( ) );
    }

    public WildflyDataSourceDef findWildflyDataSource( String host,
                                                       int port,
                                                       String user,
                                                       String password,
                                                       String realm,
                                                       String dataSourceName ) throws Exception {
        Optional< WildflyDataSourceDef > optionalDS = findWildflyDataSources( host, port, user, password, realm, dataSourceName )
                .stream( )
                .filter( wildflyDataSourceDef -> wildflyDataSourceDef.getName( ).equals( dataSourceName ) )
                .findFirst( );
        return optionalDS.orElse( null );
    }

    public Collection< String > findWildflyDeployments( String host,
                                                        int port,
                                                        String user,
                                                        String password,
                                                        String realm,
                                                        String prefix ) throws Exception {
        WildflyDeploymentClient deploymentClient = new WildflyDeploymentClient( );
        deploymentClient.loadConfig( buildConfig( host, port, user, password, realm ) );

        return deploymentClient.getDeployments( )
                .stream( )
                .filter( deploymentName -> deploymentName.startsWith( prefix ) )
                .collect( Collectors.toList( ) );
    }

    private String findInternalDeploymentId( WildflyDriverManagementClient driverManagementClient,
                                             String deploymentId,
                                             String driverClassName ) throws Exception {

        Optional< WildflyDriverDef > optional = driverManagementClient.getDeployedDrivers( ).stream( ).filter(
                wildflyDriverDef ->
                        wildflyDriverDef.getDeploymentName( ).equals( deploymentId ) ||
                                wildflyDriverDef.getDeploymentName( ).startsWith( deploymentId + "_" + driverClassName )
        ).findFirst( );
        return optional.map( value -> value.getDeploymentName( ) ).orElse( null );
    }

    private Properties buildConfig( String host,
                                    int port,
                                    String user,
                                    String password,
                                    String realm ) {
        Properties config = new Properties( );
        config.put( WildflyBaseClient.HOST, host );
        config.put( WildflyBaseClient.PORT, port + "" );
        config.put( WildflyDriverManagementClient.REALM, realm );
        config.put( WildflyBaseClient.ADMIN, user );
        config.put( WildflyBaseClient.PASSWORD, password );
        return config;
    }

    public boolean hasPersistenceSettings( Path warPath ) throws Exception {
        return JarUtils.getStrEntry( warPath, PERSISTENCE_SETTINGS_ENTRY ) != null;
    }

    public void updatePersistenceSettings( Path warPath, String dataSourceJNDI ) throws Exception {
        String content = JarUtils.getStrEntry( warPath, PERSISTENCE_SETTINGS_ENTRY );
        if ( content == null ) {
            throw new Exception( "Persistence configuration was not found for application: " + warPath );
        } else {
            try (
                    ByteArrayInputStream in = new ByteArrayInputStream( content.getBytes( StandardCharsets.UTF_8 ) );
                    ByteArrayOutputStream out = new ByteArrayOutputStream( );
            ) {
                PersistenceDescriptorModel model = PersistenceDescriptorXMLMarshaller.fromXML( in, false );
                PersistenceUnitModel persistenceUnit = model.getPersistenceUnit( );
                if ( persistenceUnit != null ) {
                    persistenceUnit.setJtaDataSource( dataSourceJNDI );
                    persistenceUnit.setNonJtaDataSource( null );
                    PersistenceDescriptorXMLMarshaller.toXML( model, out );
                    content = out.toString( StandardCharsets.UTF_8.name( ) );
                    JarUtils.addStrEntry( warPath, PERSISTENCE_SETTINGS_ENTRY, content );
                } else {
                    throw new Exception( "Persistence unit was not found for application: " + warPath );
                }
            }
        }
    }

    public String testManagementConnection( String host,
                                            int port,
                                            String user,
                                            String password,
                                            String realm ) throws Exception {
        WildflyDeploymentClient deploymentClient = new WildflyDeploymentClient( );
        deploymentClient.loadConfig( buildConfig( host, port, user, password, realm ) );
        return deploymentClient.testConnection( );
    }

    public String testRestManagementConnection( String host,
                                                int port,
                                                String user,
                                                String password ) throws Exception {

        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider( );
        credsProvider.setCredentials( new AuthScope( host, port ), new UsernamePasswordCredentials( user, password ) );

        HttpPost post = new HttpPost( "http://" + host + ":" + port + "/management" );
        post.addHeader( "X-Management-Client-Name", "APPFORMER-CLIENT" );

        ModelNode op = new ModelNode( );
        op.get( "operation" ).set( "read-resource" );
        post.setEntity( new StringEntity( op.toJSONString( true ), ContentType.APPLICATION_JSON ) );

        try (
                CloseableHttpClient httpclient = HttpClients.custom( ).setDefaultCredentialsProvider( credsProvider ).build( );
                CloseableHttpResponse httpResponse = httpclient.execute( post );
        ) {
            if ( HttpStatus.SC_OK != httpResponse.getStatusLine( ).getStatusCode( ) ) {
                throw new Exception( "Authentication failed. " );
            } else {
                String json = EntityUtils.toString( httpResponse.getEntity( ) );
                ModelNode returnVal = ModelNode.fromJSONString( json );
                String productName = returnVal.get( "result" ).get( "product-name" ).asString( );
                String productVersion = returnVal.get( "result" ).get( "product-version" ).asString( );
                String releaseVersion = returnVal.get( "result" ).get( "release-version" ).asString( );
                String releaseCodeName = returnVal.get( "result" ).get( "release-codename" ).asString( );
                StringBuilder stringBuilder = new StringBuilder( );
                stringBuilder.append( productName + ", " + productVersion );
                stringBuilder.append( " (" + releaseCodeName + ", " + releaseVersion + ")" );
                return stringBuilder.toString( );
            }
        }
    }
}