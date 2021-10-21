/*
 * Copyright 2015 JBoss, by Red Hat, Inc
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

package org.uberfire.ext.security.server;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.errai.security.shared.api.GroupImpl;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.uberfire.backend.server.security.RoleRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.uberfire.ext.security.server.ServletSecurityAuthenticationService.USER_SESSION_ATTR_NAME;

@RunWith(MockitoJUnitRunner.class)
public class ServletSecurityAuthenticationServiceTest {

    private static final String USERNAME = "user1";
    private static final String PASSWORD = "password1";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession httpSession;

    private ServletSecurityAuthenticationService tested;

    @Before
    public void setup() throws Exception {

        Principal p1 = mock(Principal.class);
        doReturn(USERNAME).when(p1).getName();
        doReturn(p1).when(request).getUserPrincipal();
        doReturn(httpSession).when(request).getSession();
        doReturn(null).when(httpSession).getAttribute(eq(USER_SESSION_ATTR_NAME));
        when(request.getSession(anyBoolean())).then(new Answer<HttpSession>() {
            @Override
            public HttpSession answer(InvocationOnMock invocationOnMock) throws Throwable {
                return httpSession;
            }
        });

        tested = spy(new ServletSecurityAuthenticationService());

        // Set the request in the thread context.
        SecurityIntegrationFilter.requests.set(request);
    }

    @Test
    public void testLoggedIn() {
        assertTrue(tested.isLoggedIn());
    }

    @Test
    public void testNotLoggedIn() {
        doReturn(null).when(request).getUserPrincipal();
        assertFalse(tested.isLoggedIn());
    }

    @Test(expected = FailedAuthenticationException.class)
    public void testLoginFailure() throws ServletException {
        doThrow(new ServletException()).when(request).login("test","test");
        tested.login("test","test");
    }

    @Test
    public void testLogin() throws Exception {

        RoleRegistry.get().registerRole("admin");
        RoleRegistry.get().registerRole("role1");
        final ArrayList<Object> principals = new ArrayList<>();
        principals.add("admin");
        principals.add("role1");
        principals.add("group1");
        doReturn(principals).when(tested).getPrincipals();

        User user = tested.login(USERNAME,
                                 PASSWORD);

        assertNotNull(user);
        assertEquals(USERNAME,
                     user.getIdentifier());
        assertEquals(2,
                     user.getRoles().size());
        assertTrue(user.getRoles().contains(new RoleImpl("admin")));
        assertTrue(user.getRoles().contains(new RoleImpl("role1")));
        assertEquals(1,
                     user.getGroups().size());
        assertTrue(user.getGroups().contains(new GroupImpl("group1")));
    }

    @Test
    public void testLoginNoPrincipal() throws Exception {

        doReturn(new ArrayList<>()).when(tested).getPrincipals();

        User user = tested.login(USERNAME,
                                 PASSWORD);

        assertNotNull(user);
        assertEquals(USERNAME,
                     user.getIdentifier());
        assertEquals(0,
                     user.getRoles().size());
        assertEquals(0,
                     user.getGroups().size());
    }

    @Test
    public void testLogout() throws Exception {
        tested.logout();
        verify(request,
               times(1)).logout();
        verify(httpSession,
               times(1)).invalidate();
    }

    @Test
    public void testLogoutNoSession() throws Exception {
        doReturn(null).when(request).getSession(false);
        tested.logout();
        verify(request,
               times(1)).logout();
        verify(httpSession,
               never()).invalidate();
    }

    @Test
    public void testSwallowIllegalStateExceptionDuringLogoutWithKeycloak() {
        doThrow(new IllegalStateException("UT000021: Session already invalidated")).when(httpSession).invalidate();
        tested.logout();
    }

    @Test
    public void testReThrowUnexpectedIllegalStateExceptionDuringLogout() {
        String exceptionMsg = "This exception should be propagated!";
        doThrow(new IllegalStateException(exceptionMsg)).when(httpSession).invalidate();
        try {
            tested.logout();
        } catch (IllegalStateException ise) {
            // the exception message needs to be the same as defined above
            assertEquals(exceptionMsg, ise.getMessage());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testGetRequestForThreadNoRequest() {
        SecurityIntegrationFilter.requests.set(null);
        tested.getRequestForThread();
        SecurityIntegrationFilter.requests.set(request);
    }

    @Test
    public void testGetUserNoUserPrincipal() {
        doReturn(null).when(request).getUserPrincipal();
        assertNull(tested.getUser());
    }

    @Test
    public void testGetUserNoSession() {
        doReturn(null).when(request).getSession();
        assertNull(tested.getUser());
    }

}