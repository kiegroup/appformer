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

package org.kie.appformer.provisioning.client.resources.i18n;

import org.jboss.errai.ui.shared.api.annotations.TranslationKey;

public interface AppFormerProvisioningConstants {

    @TranslationKey( defaultValue = "" )
    String ProvisioningWizard_title = "ProvisioningWizard.title";

    @TranslationKey( defaultValue = "" )
    String ProvisioningWizard_provisioningStartedMessage = "ProvisioningWizard.provisioningStartedMessage";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_title = "ConfigureServerPage.title";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_TestConnectionSuccessfulMessage = "ConfigureServerPage.TestConnectionSuccessfulMessage";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_TestConnectionFailMessage = "ConfigureServerPage.TestConnectionFailMessage";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_CompleteParametersMessage = "ConfigureServerPage.CompleteParametersMessage";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_HostError = "ConfigureServerPage.HostError";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_PortNumberError = "ConfigureServerPage.PortNumberError";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_ManagementPortNumberError = "ConfigureServerPage.ManagementPortNumberError";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_ManagementUserError = "ConfigureServerPage.ManagementUserError";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_ManagementPasswordError = "ConfigureServerPage.ManagementPasswordError";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_ManagementRealmError = "ConfigureServerPage.ManagementRealmError";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_ValidateConnectionMessage = "ConfigureServerPage.ValidateConnectionMessage";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_ConnectionValidatedMessage = "ConfigureServerPage.ConnectionValidatedMessage";

    @TranslationKey( defaultValue = "" )
    String ConfigureServerPage_ConnectionFailuresMessage = "ConfigureServerPage.ConnectionFailuresMessage";

    @TranslationKey( defaultValue = "" )
    String ConfigureApplicationPage_title = "ConfigureApplicationPage.title";

    @TranslationKey( defaultValue = "" )
    String ConfigureApplicationPage_ExternalDataSourceDescriptionFormat = "ConfigureApplicationPage.ExternalDataSourceDescriptionFormat";
}