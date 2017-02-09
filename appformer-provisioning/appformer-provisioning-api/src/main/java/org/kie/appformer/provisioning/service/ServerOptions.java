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

package org.kie.appformer.provisioning.service;

import org.jboss.errai.common.client.api.annotations.MapsTo;

public class ServerOptions {

    private String host;

    private int port;

    private int managementPort;

    private String user;

    private String password;

    private String realm;

    public ServerOptions( @MapsTo( "host" ) String host,
                          @MapsTo( "port" ) int port,
                          @MapsTo( "managementPort" ) int managementPort,
                          @MapsTo( "user" ) String user,
                          @MapsTo( "password" ) String password,
                          @MapsTo( "realm" ) String realm ) {
        this.host = host;
        this.port = port;
        this.managementPort = managementPort;
        this.user = user;
        this.password = password;
        this.realm = realm;
    }

    public String getHost( ) {
        return host;
    }

    public void setHost( String host ) {
        this.host = host;
    }

    public int getPort( ) {
        return port;
    }

    public void setPort( int port ) {
        this.port = port;
    }

    public int getManagementPort( ) {
        return managementPort;
    }

    public void setManagementPort( int managementPort ) {
        this.managementPort = managementPort;
    }

    public String getUser( ) {
        return user;
    }

    public void setUser( String user ) {
        this.user = user;
    }

    public String getPassword( ) {
        return password;
    }

    public void setPassword( String password ) {
        this.password = password;
    }

    public String getRealm( ) {
        return realm;
    }

    public void setRealm( String realm ) {
        this.realm = realm;
    }

}