/*
 * Copyright 2021 JBoss, by Red Hat, Inc
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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.security.elytron.ElytronIdentityHelper;

@ApplicationScoped
@Alternative
public class ElytronAuthenticationService implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(ElytronAuthenticationService.class);

    private final ElytronIdentityHelper elytronIdentityHelper;

    private final ThreadLocal<User> userOnThisThread = new ThreadLocal<>();

    @Inject
    public ElytronAuthenticationService(final  ElytronIdentityHelper elytronIdentityHelper) {
        this.elytronIdentityHelper = elytronIdentityHelper;
    }

    @Override
    public User login(final String username,
                      final String password) {

        try {
            User user = elytronIdentityHelper.getIdentity(username, password);

            userOnThisThread.set(user);

            return user;
        } catch (Exception ex) {
            logger.debug("Cannot login user '{}':", username, ex);
        }
        throw new FailedAuthenticationException();
    }

    @Override
    public boolean isLoggedIn() {
        return userOnThisThread.get() != null;
    }

    @Override
    public void logout() {
        userOnThisThread.remove();
    }

    @Override
    public User getUser() {
        User user = userOnThisThread.get();
        if (user == null) {
            return User.ANONYMOUS;
        }
        return user;
    }
}
