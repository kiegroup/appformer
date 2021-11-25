/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.ext.security.management.keycloak.elytron;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import org.uberfire.backend.server.security.elytron.DefaultElytronIdentityHelper;
import org.uberfire.backend.server.security.elytron.ElytronIdentityHelper;
import org.uberfire.backend.server.security.elytron.ElytronIdentityHelperProducer;
import org.uberfire.ext.security.management.keycloak.KCAdapterUserManagementService;
import org.uberfire.ext.security.management.keycloak.KCCredentialsUserManagementService;
import org.uberfire.security.WorkbenchUserManager;

/**
 * Produces {@link ElytronIdentityHelper} based on the user management service configured on the
 * {@value MANAGEMENT_SERVICES_SYSTEM_PROP} SystemProperty. If it refers to a Keycloak installation
 * {@link KCAdapterUserManagementService} or {@link KCCredentialsUserManagementService}
 * it will produce an instance of {@link KeyCloakElytronIdentityHelper} otherwhise it will produce
 * a {@link DefaultElytronIdentityHelper}
 */
@ApplicationScoped
@Specializes
public class KeyCloakElytronIdentityHelperProducer extends ElytronIdentityHelperProducer {

    public static final String MANAGEMENT_SERVICES_SYSTEM_PROP = "org.uberfire.ext.security.management.api.userManagementServices";

    private boolean isKeyCloak;

    @Inject
    public KeyCloakElytronIdentityHelperProducer(WorkbenchUserManager workbenchUserManager) {
        super(workbenchUserManager);
    }

    @PostConstruct
    public void init() {
        String managementService = System.getProperties().getProperty(MANAGEMENT_SERVICES_SYSTEM_PROP, "");
        isKeyCloak = (KCCredentialsUserManagementService.NAME.equals(managementService) || KCAdapterUserManagementService.NAME.equals(managementService));
    }

    @Produces
    @Override
    public ElytronIdentityHelper getDefaultElytronIdentityHelper() {
        if (isKeyCloak) {
            return new KeyCloakElytronIdentityHelper();
        }
        return super.getDefaultElytronIdentityHelper();
    }
}
