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
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DataSourceInfo {

    private String name;

    private String kieUuid;

    private String deploymentId;

    private String jndi;

    public DataSourceInfo( ) {
    }

    public DataSourceInfo( @MapsTo( "name" ) String name,
                           @MapsTo( "kieUuid" ) String kieUuid,
                           @MapsTo( "deploymentId" ) String deploymentId,
                           @MapsTo( "jndi" ) String jndi ) {
        this.name = name;
        this.kieUuid = kieUuid;
        this.deploymentId = deploymentId;
        this.jndi = jndi;
    }

    public DataSourceInfo( String name,
                           String deploymentId,
                           String jndi ) {
        this.name = name;
        this.deploymentId = deploymentId;
        this.jndi = jndi;
    }

    public String getName( ) {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getKieUuid( ) {
        return kieUuid;
    }

    public void setKieUuid( String kieUuid ) {
        this.kieUuid = kieUuid;
    }

    public boolean isKieDataSource( ) {
        return kieUuid != null;
    }

    public String getDeploymentId( ) {
        return deploymentId;
    }

    public void setDeploymentId( String deploymentId ) {
        this.deploymentId = deploymentId;
    }

    public String getJndi( ) {
        return jndi;
    }

    public void setJndi( String jndi ) {
        this.jndi = jndi;
    }
}