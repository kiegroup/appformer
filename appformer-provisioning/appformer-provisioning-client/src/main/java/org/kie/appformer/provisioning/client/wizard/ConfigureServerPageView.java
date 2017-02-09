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

import org.uberfire.client.mvp.UberElement;

public interface ConfigureServerPageView
        extends UberElement< ConfigureServerPageView.Presenter > {

    interface Presenter {

        void onHostChange( );

        void onPortChange( );

        void onManagementPortChange( );

        void onManagementRealmChange( );

        void onManagementUserChange( );

        void onManagementPasswordChange( );

        void onTestConnection( );
    }

    String getPageTitle( );

    String getHost( );

    void setHost( String host );

    void setHostErrorMessage( String errorMessage );

    void clearHostErrorMessage( );

    String getPort( );

    void setPort( String port );

    void setPortErrorMessage( String errorMessage );

    void clearPortErrorMessage( );

    String getManagementPort( );

    void setManagementPort( String managementPort );

    void setManagementPortErrorMessage( String errorMessage );

    void clearManagementPortErrorMessage( );

    String getManagementRealm( );

    void setManagementRealm( String managementRealm );

    void setManagementRealmErrorMessage( String errorMessage );

    void clearManagementRealmErrorMessage( );

    String getManagementUser( );

    void setManagementUser( String managementUser );

    void setManagementUserErrorMessage( String errorMessage );

    void clearManagementUserErrorMessage( );

    String getManagementPassword( );

    void setManagementPassword( String managementPassword );

    void setManagementPasswordErrorMessage( String errorMessage );

    void clearManagementPasswordErrorMessage( );

    void showMessage( String title, String message );

    void setFormStatusInfoMessage( String message );

    void setFormStatusErrorMessage( String message );

    void setFormStatusSuccessMessage( String message );
}