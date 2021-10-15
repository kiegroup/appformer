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

package org.uberfire.backend.server.security.elytron;

import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.security.WorkbenchUserManager;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultElytronIdentityHelperTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    private DefaultElytronIdentityHelper helper;

    @Mock
    private WorkbenchUserManager workbenchUserManager;

    @Before
    public void init() {
        helper = spy(new DefaultElytronIdentityHelper(workbenchUserManager)  {
            @Override
            protected boolean login(String userName, String password) {
                return true;
            }
        });
    }

    @Test
    public void testSuccessfulLogin() {

        when(helper.login(eq(USERNAME), eq(PASSWORD))).thenReturn(true);

        helper.getIdentity(USERNAME, PASSWORD);

        verify(workbenchUserManager).getUser(USERNAME);
    }

    @Test(expected = FailedAuthenticationException.class)
    public void testUnSuccessfulLogin() {

        doThrow(new RuntimeException("whatever error")).when(helper).login(eq(USERNAME), eq(PASSWORD));

        helper.getIdentity(USERNAME, PASSWORD);

        verify(workbenchUserManager, never()).getUser(USERNAME);
    }
}
