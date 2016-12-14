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
    String DeploymentPopup_TestConnectionSuccessfulMessage = "DeploymentPopup.TestConnectionSuccessfulMessage";

    @TranslationKey( defaultValue = "" )
    String DeploymentPopup_TestConnectionFailMessage = "DeploymentPopup.TestConnectionFailMessage";

    @TranslationKey( defaultValue = "" )
    String DeploymentPopup_CompleteParametersMessage = "DeploymentPopup.CompleteParametersMessage";

    @TranslationKey( defaultValue = "")
    String DeploymentPopup_PortNumberError = "DeploymentPopup.PortNumberError";

    @TranslationKey( defaultValue = "")
    String DeploymentPopup_ManagementPortNumberError = "DeploymentPopup.ManagementPortNumberError";
}