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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.backend.server.security.elytron.DefaultElytronIdentityHelper;
import org.uberfire.backend.server.security.elytron.ElytronIdentityHelper;
import org.uberfire.ext.security.management.keycloak.KCAdapterUserManagementService;
import org.uberfire.ext.security.management.keycloak.KCCredentialsUserManagementService;
import org.uberfire.security.WorkbenchUserManager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.uberfire.ext.security.management.keycloak.elytron.KeyCloakElytronIdentityHelperProducer.MANAGEMENT_SERVICES_SYSTEM_PROP;

@RunWith(MockitoJUnitRunner.class)
public class KeyCloakElytronIdentityHelperProducerTest {

    @Mock
    private WorkbenchUserManager workbenchUserManager;

    private KeyCloakElytronIdentityHelperProducer producer;

    @Before
    public void init() {
        producer = new KeyCloakElytronIdentityHelperProducer(workbenchUserManager);
    }

    @Test
    public void testProduceKeycloakHelperCredentials() {
        System.getProperties().setProperty(MANAGEMENT_SERVICES_SYSTEM_PROP, KCCredentialsUserManagementService.NAME);

        producer.init();

        ElytronIdentityHelper helper = producer.getDefaultElytronIdentityHelper();

        assertNotNull(helper);
        assertTrue(helper instanceof KeyCloakElytronIdentityHelper);
    }

    @Test
    public void testProduceKeycloakHelperAdapter() {
        System.getProperties().setProperty(MANAGEMENT_SERVICES_SYSTEM_PROP, KCAdapterUserManagementService.NAME);

        producer.init();

        ElytronIdentityHelper helper = producer.getDefaultElytronIdentityHelper();

        assertNotNull(helper);
        assertTrue(helper instanceof KeyCloakElytronIdentityHelper);
    }

    @Test
    public void testProduceDefaultHelperAnyValue() {
        System.getProperties().setProperty(MANAGEMENT_SERVICES_SYSTEM_PROP, "any");

        producer.init();

        ElytronIdentityHelper helper = producer.getDefaultElytronIdentityHelper();

        assertNotNull(helper);
        assertTrue(helper instanceof DefaultElytronIdentityHelper);
    }

    @Test
    public void testProduceDefaultHelperNoValue() {
        producer.init();

        ElytronIdentityHelper helper = producer.getDefaultElytronIdentityHelper();

        assertNotNull(helper);
        assertTrue(helper instanceof DefaultElytronIdentityHelper);
    }

    @After
    public void clear() {
        System.getProperties().remove(MANAGEMENT_SERVICES_SYSTEM_PROP);
    }
}
