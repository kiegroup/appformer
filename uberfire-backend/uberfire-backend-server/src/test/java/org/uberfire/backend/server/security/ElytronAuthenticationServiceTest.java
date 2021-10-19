/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.backend.server.security;

import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.junit.Before;
import org.junit.Test;
import org.uberfire.backend.server.security.elytron.ElytronIdentityHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ElytronAuthenticationServiceTest {

    private ElytronAuthenticationService tested;
    private ElytronIdentityHelper elytronIdentityHelper;

    @Before
    public void setup() {
        elytronIdentityHelper = mock(ElytronIdentityHelper.class);

        tested = new ElytronAuthenticationService(elytronIdentityHelper);
    }

    @Test
    public void testNoLogin() throws Exception {
        assertEquals(User.ANONYMOUS,
                     tested.getUser());
    }

    @Test
    public void testGetAnonymous() throws Exception {
        assertFalse(tested.isLoggedIn());
    }

    @Test
    public void testLogin() throws Exception {
        final String username = "user1";
        final String password = "password1";

        assertFalse(tested.isLoggedIn());

        final User mock = mock(User.class);
        doReturn(mock).when(elytronIdentityHelper).getIdentity(username, password);

        assertEquals(mock, tested.login(username,
                                        password));

        assertEquals(mock, tested.getUser());

        assertTrue(tested.isLoggedIn());

        tested.logout();

        assertFalse(tested.isLoggedIn());
    }

    @Test(expected = FailedAuthenticationException.class)
    public void testLoginFailure() throws Exception {
        tested = new ElytronAuthenticationService(elytronIdentityHelper);

        final String username = "user1";
        final String password = "wrong pass";

        doThrow(new FailedAuthenticationException()).when(elytronIdentityHelper).getIdentity(username, password);

        tested.login(username,
                     password);
    }
}