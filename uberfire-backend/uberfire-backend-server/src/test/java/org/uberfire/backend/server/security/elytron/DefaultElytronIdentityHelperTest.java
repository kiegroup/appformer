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

import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityIdentity;
import org.wildfly.security.authz.Roles;
import org.wildfly.security.evidence.Evidence;

import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultElytronIdentityHelperTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    private DefaultElytronIdentityHelper helper;
    private ArrayList<String> roles = new ArrayList<>();

    @Before
    public void init() {
        helper = spy(new DefaultElytronIdentityHelper() {
            @Override
            protected Iterator<String> login(String userName, Evidence evidence) {
                return roles.iterator();
            }
        });
    }

    @After
    public void after() {
        roles.clear();
    }

    @Test
    public void testSuccessfulLogin() throws RealmUnavailableException {
        roles.add("admin");
        roles.add("rest-all");

        final User identity = helper.getIdentity(USERNAME, PASSWORD);

        assertEquals(USERNAME, identity.getIdentifier());
        assertTrue(identity.getRoles().contains(new RoleImpl("admin")));
        assertTrue(identity.getRoles().contains(new RoleImpl("rest-all")));
        assertEquals(2, identity.getRoles().size());
    }

    @Test
    public void testSuccessfulLoginNoRoles() throws RealmUnavailableException {

        final User identity = helper.getIdentity(USERNAME, PASSWORD);

        assertTrue(identity.getRoles().isEmpty());
    }

    @Test(expected = FailedAuthenticationException.class)
    public void testUnSuccessfulLogin() throws RealmUnavailableException {

        doThrow(new RuntimeException("whatever error")).when(helper).login(any(), any());

        helper.getIdentity(USERNAME, PASSWORD);
    }
}
