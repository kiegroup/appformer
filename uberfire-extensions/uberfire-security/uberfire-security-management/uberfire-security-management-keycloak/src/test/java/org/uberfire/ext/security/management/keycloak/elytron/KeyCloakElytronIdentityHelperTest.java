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

import javax.security.auth.login.LoginException;

import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.adapters.jaas.DirectAccessGrantsLoginModule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyCloakElytronIdentityHelperTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    @Mock
    private DirectAccessGrantsLoginModule loginModule;
    private KeyCloakElytronIdentityHelper helper;

    @Before
    public void init() {
        helper = new KeyCloakElytronIdentityHelper(loginModule);
    }

    @Test
    public void testSuccessfulLogin() throws LoginException {
        when(loginModule.login()).thenReturn(true);

        helper.getIdentity(USERNAME, PASSWORD);

        verify(loginModule).initialize(any(), any(), any(), any());
        verify(loginModule).commit();
        verify(loginModule).logout();
    }

    @Test(expected = FailedAuthenticationException.class)
    public void testUnSuccessfulLogin() throws LoginException {
        when(loginModule.login()).thenReturn(false);

        helper.getIdentity(USERNAME, PASSWORD);

        verify(loginModule).initialize(any(), any(), any(), any());
        verify(loginModule, never()).commit();
        verify(loginModule).logout();
    }

    @Test(expected = FailedAuthenticationException.class)
    public void testUnSuccessfulLoginWithException() throws LoginException {

        doThrow(new RuntimeException("error")).when(loginModule).login();

        helper.getIdentity(USERNAME, PASSWORD);

        verify(loginModule).initialize(any(), any(), any(), any());
        verify(loginModule, never()).commit();
        verify(loginModule).logout();
    }
}
