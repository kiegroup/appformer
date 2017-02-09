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

import org.guvnor.common.services.project.model.Project;
import org.kie.appformer.provisioning.service.DataSourceInfo;

public class ProvisioningWizardModel {

    private String host;

    private int port = -1;

    private int managementPort = -1;

    private String managementUser;

    private String managementPassword;

    private String managementRealm;

    private Project project;

    private boolean serverConfigValid = false;

    private DataSourceInfo dataSourceInfo;

    private boolean reloadDataSources = true;

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

    public String getManagementUser( ) {
        return managementUser;
    }

    public void setManagementUser( String managementUser ) {
        this.managementUser = managementUser;
    }

    public String getManagementPassword( ) {
        return managementPassword;
    }

    public void setManagementPassword( String managementPassword ) {
        this.managementPassword = managementPassword;
    }

    public String getManagementRealm( ) {
        return managementRealm;
    }

    public void setManagementRealm( String managementRealm ) {
        this.managementRealm = managementRealm;
    }

    public Project getProject( ) {
        return project;
    }

    public void setProject( Project project ) {
        this.project = project;
    }

    public boolean isServerConfigValid( ) {
        return serverConfigValid;
    }

    public void setServerConfigValid( boolean serverConfigValid ) {
        this.serverConfigValid = serverConfigValid;
    }

    public DataSourceInfo getDataSourceInfo( ) {
        return dataSourceInfo;
    }

    public void setDataSourceInfo( DataSourceInfo dataSourceInfo ) {
        this.dataSourceInfo = dataSourceInfo;
    }

    public boolean isReloadDataSources( ) {
        return reloadDataSources;
    }

    public void setReloadDataSources( boolean reloadDataSources ) {
        this.reloadDataSources = reloadDataSources;
    }
}