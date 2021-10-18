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

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.interceptor.Interceptor;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.keycloak.adapters.jaas.RolePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.security.elytron.ElytronIdentityHelper;
import org.keycloak.adapters.jaas.DirectAccessGrantsLoginModule;

/**
 * Implementation of {@link ElytronIdentityHelper} for Keycloak integration. It tries to authenticate the given credentials
 * to Keycloak by using the {@link DirectAccessGrantsLoginModule}. Requires a keycloak-config-file and a SystemProperty
 * {@value KIE_GIT_FILE_SYSTEM_PROP} specifying the path of that file.
 */
@Priority(Interceptor.Priority.APPLICATION+10)
@Alternative
public class KeyCloakElytronIdentityHelper implements ElytronIdentityHelper {

    public static final String KEYCLOAK_CONFIG_FILE_KEY = "keycloak-config-file";
    public static final String KIE_GIT_FILE_SYSTEM_PROP = "org.uberfire.ext.security.keycloak.keycloak-config-file";
    public static final String DEFAULT_KIE_GIT_FILE_PATH = System.getProperty("jboss.home.dir") + "/kie-git.json";

    private static final Logger logger = LoggerFactory.getLogger(KeyCloakElytronIdentityHelper.class);

    private final String configFile;
    private final DirectAccessGrantsLoginModule keycloakDelegate;

    public KeyCloakElytronIdentityHelper() {
        this(new DirectAccessGrantsLoginModule());
    }

    KeyCloakElytronIdentityHelper(DirectAccessGrantsLoginModule keycloakDelegate) {
        this.keycloakDelegate = keycloakDelegate;
        configFile = System.getProperty(KIE_GIT_FILE_SYSTEM_PROP, DEFAULT_KIE_GIT_FILE_PATH);
    }

    @Override
    public User getIdentity(String userName, String password) {
        Subject subject = new Subject();
        subject.getPrincipals().add(new Principal() {
            private final String name = userName;

            @Override
            public String getName() {
                return name;
            }
        });
        subject.getPublicCredentials().add(password);

        Map<String, String> options = new HashMap<>();
        options.put(KEYCLOAK_CONFIG_FILE_KEY, configFile);

        keycloakDelegate.initialize(subject, new ElytronHelperCallbackHandler(userName, password), new HashMap<>(), options);

        try {
            if(keycloakDelegate.login()) {
                keycloakDelegate.commit();

                Collection<Role> roles = subject.getPrincipals(RolePrincipal.class)
                        .stream()
                        .map(principal -> new RoleImpl(principal.getName()))
                        .collect(Collectors.toList());

                return new UserImpl(userName, roles);
            }
        } catch (Exception ex) {
            logger.debug("Identity provided for '{}' not valid", userName);
        } finally {
            try {
                keycloakDelegate.logout();
            } catch (LoginException e) {
                logger.debug("Error logging out user '{}'", userName);
            }
        }

        throw new FailedAuthenticationException();
    }

    static class ElytronHelperCallbackHandler implements CallbackHandler {
        private final String userName;
        private final String password;

        public ElytronHelperCallbackHandler(final String userName, final String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        public void handle(Callback[] callbacks) {
            Stream.of(callbacks).forEach(callback -> {
                if (callback instanceof NameCallback) {
                    NameCallback nc = (NameCallback) callback;
                    nc.setName(userName);
                } else if (callback instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback) callback;
                    pc.setPassword(password.toCharArray());
                } else {
                    logger.debug("Unrecognized Callback {}", callback);
                }
            });
        }
    }
}


