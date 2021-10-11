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
import org.uberfire.security.WorkbenchUserManager;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.evidence.Evidence;
import org.wildfly.security.evidence.PasswordGuessEvidence;

@ApplicationScoped
@Alternative
public class ElytronAuthenticationService implements AuthenticationService {

    private WorkbenchUserManager workbenchUserManager;

    private final ThreadLocal<User> userOnThisThread = new ThreadLocal<>();

    public ElytronAuthenticationService() {

    }

    @Inject
    public ElytronAuthenticationService(final WorkbenchUserManager workbenchUserManager) {
        this.workbenchUserManager = workbenchUserManager;
    }

    @Override
    public User login(final String username,
                      final String password) {

        try {
            final Evidence evidence = new PasswordGuessEvidence(password.toCharArray());
            login(username, evidence);
            final User user = workbenchUserManager.getUser(username);

            userOnThisThread.set(user);

            return user;
        } catch (RealmUnavailableException e) {
            throw new FailedAuthenticationException();
        } catch (Exception e) {
            throw new FailedAuthenticationException();
        }
    }

    protected void login(final String username,
                         final Evidence evidence) throws RealmUnavailableException {
        SecurityDomain.getCurrent().authenticate(username, evidence);
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
