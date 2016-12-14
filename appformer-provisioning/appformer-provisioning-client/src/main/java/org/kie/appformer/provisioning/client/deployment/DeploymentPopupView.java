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

import java.util.List;

import org.uberfire.client.mvp.UberElement;
import org.uberfire.commons.data.Pair;

public interface DeploymentPopupView
        extends UberElement<DeploymentPopupView.Presenter > {

    interface Presenter {

        void onOk( );

        void onLocalChange();

        void onRemoteChange();

        void onTestConnection( );
    }

    boolean getLocal();

    void setLocal( boolean checked );

    boolean getRemote();

    void setRemote( boolean checked );

    void setRemoteServerOptionsHidden( boolean hidden );

    String getHost();

    void setHost( String host );

    String getPort();

    void setPort( String port );

    String getManagementPort();

    void setManagementPort( String managementPort );

    String getManagementUser();

    void setManagementUser( String managementUser );

    String getManagementPassword();

    void setManagementPassword( String managementPassword );

    void loadDataSourceOptions( final List<Pair<String, String>> dataSourceOptions, final boolean addEmptyOption );

    String getDataSource();

    void setDataSource( String dataSource );

    void showMessage( String tile, String message );

    void show();

    void hide();
}