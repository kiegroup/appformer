/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.java.nio.base.FileSystemId;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.security.FileSystemAuthorizer;
import org.uberfire.security.ResourceAction;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.security.authz.PermissionManager;
import org.uberfire.security.impl.authz.DefaultAuthorizationManager;
import org.uberfire.security.impl.authz.DefaultPermissionManager;
import org.uberfire.security.impl.authz.DefaultPermissionTypeRegistry;
import org.uberfire.spaces.Space;
import org.uberfire.spaces.SpacesAPI;

import javax.enterprise.inject.Instance;
import java.net.URI;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IOServiceSecuritySetupTest {

    @Mock
    Instance<AuthenticationService> authenticationManagers;

    AuthorizationManager authorizationManager;
    RepositoryService repositoryService;
    SpacesAPI spacesAPI;
    IOServiceSecuritySetup setupBean;

    @Before
    public void setup() {
        // this is the fallback configuration when no @IOSecurityAuth bean is found
        System.setProperty("org.uberfire.io.auth",
                MockAuthenticationService.class.getName());

        PermissionManager permissionManager = new DefaultPermissionManager(new DefaultPermissionTypeRegistry());
        authorizationManager = spy(new DefaultAuthorizationManager(permissionManager));
        repositoryService = mock(RepositoryService.class);
        spacesAPI = mock(SpacesAPI.class);
        setupBean = new IOServiceSecuritySetup();
        setupBean.authenticationManagers = authenticationManagers;
        setupBean.authorizationManager = authorizationManager;
        setupBean.repositoryService = repositoryService;
        setupBean.spacesAPI = spacesAPI;
    }

    @After
    public void teardown() {
        System.clearProperty("org.uberfire.io.auth");
    }

    @Test
    public void testSystemPropertyAuthConfig() throws Exception {
        when(authenticationManagers.isUnsatisfied()).thenReturn(true);

        setupBean.setup();

        // setup should have initialized the authenticator and authorizer to their defaults
        MockSecuredFilesystemProvider mockFsp = MockSecuredFilesystemProvider.LATEST_INSTANCE;
        assertNotNull(mockFsp.authenticator);
        assertNotNull(mockFsp.authorizer);

        // and they should work :)
        User user = mockFsp.authenticator.login("fake", "fake");
        assertEquals(MockAuthenticationService.FAKE_USER.getIdentifier(),
                user.getIdentifier());

        final FileSystem mockfs = mock(FileSystem.class);
        final FileSystem mockedFSId = mock(FileSystem.class,
                withSettings().extraInterfaces(FileSystemId.class));
        final Path rootPath = mock(Path.class);
        when(rootPath.toUri()).thenReturn(URI.create("/"));

        when(rootPath.getFileSystem()).thenReturn(mockedFSId);

        when(mockfs.getPath(mockfs.getName())).thenReturn(rootPath);
        Space space = mock(Space.class);
        when(spacesAPI.resolveSpace(any())).thenReturn(Optional.of(space));

        assertTrue(mockFsp.authorizer.authorize(mockfs,
                user));
    }

    @Test
    public void testCustomAuthenticatorBean() throws Exception {

        // this simulates the existence of a @IOServiceAuth AuthenticationService bean
        when(authenticationManagers.isUnsatisfied()).thenReturn(false);
        AuthenticationService mockAuthenticationService = mock(AuthenticationService.class);
        when(authenticationManagers.get()).thenReturn(mockAuthenticationService);

        setupBean.setup();

        AuthenticationService authenticator = MockSecuredFilesystemProvider.LATEST_INSTANCE.authenticator;
        authenticator.login("fake", "fake");

        // make sure the call went to the one we provided
        verify(mockAuthenticationService).login("fake",
                "fake");
    }

    @Test
    public void testCustomAuthorizerBean() throws Exception {
        when(authenticationManagers.isUnsatisfied()).thenReturn(true);

        setupBean.setup();

        FileSystemAuthorizer installedAuthorizer = MockSecuredFilesystemProvider.LATEST_INSTANCE.authorizer;
        AuthenticationService installedAuthenticator = MockSecuredFilesystemProvider.LATEST_INSTANCE.authenticator;
        FileSystem mockfs = mock(FileSystem.class);

        final FileSystem mockedFSId = mock(FileSystem.class,
                withSettings().extraInterfaces(FileSystemId.class));
        final Path rootPath = mock(Path.class);
        final Repository repository = mock(Repository.class);
        when(rootPath.toUri()).thenReturn(URI.create("/"));
        when(rootPath.getFileSystem()).thenReturn(mockedFSId);

        User fileSystemUser = installedAuthenticator.login("fake", "fake");
        when(mockfs.getPath(mockfs.getName())).thenReturn(rootPath);
        Space space = mock(Space.class);
        when(spacesAPI.resolveSpace(any())).thenReturn(Optional.of(space));
        when(repositoryService.getRepositoryFromSpace(any(), any())).thenReturn(repository);
        installedAuthorizer.authorize(mockfs,
                fileSystemUser);
        // make sure the call went to the one we provided
        verify(authorizationManager).authorize(repository, repository.getContributors(), ResourceAction.READ, fileSystemUser);
    }

    @Test
    public void testNonRepositoryAuthorization() throws Exception {
        when(authenticationManagers.isUnsatisfied()).thenReturn(true);

        setupBean.setup();

        FileSystemAuthorizer installedAuthorizer = MockSecuredFilesystemProvider.LATEST_INSTANCE.authorizer;
        AuthenticationService installedAuthenticator = MockSecuredFilesystemProvider.LATEST_INSTANCE.authenticator;
        FileSystem mockfs = mock(FileSystem.class);

        final FileSystem mockedFSId = mock(FileSystem.class,
                withSettings().extraInterfaces(FileSystemId.class));
        final Path rootPath = mock(Path.class);
        final Repository repository = mock(Repository.class);
        when(rootPath.toUri()).thenReturn(URI.create("/"));
        when(rootPath.getFileSystem()).thenReturn(mockedFSId);

        User fileSystemUser = installedAuthenticator.login("fake", "fake");
        when(mockfs.getPath(mockfs.getName())).thenReturn(rootPath);
        Space space = mock(Space.class);
        when(spacesAPI.resolveSpace(any())).thenReturn(Optional.of(space));
        when(repositoryService.getRepositoryFromSpace(any(), any())).thenReturn(null);
        installedAuthorizer.authorize(mockfs,
                fileSystemUser);
        // make sure the call went to the one we provided
        verify(authorizationManager).authorize(any(FileSystemResourceAdaptor.class),
                any(User.class));
    }
}
