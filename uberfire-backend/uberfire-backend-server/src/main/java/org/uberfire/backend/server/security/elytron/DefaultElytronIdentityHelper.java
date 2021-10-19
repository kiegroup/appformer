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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.security.WorkbenchUserManager;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.evidence.Evidence;
import org.wildfly.security.evidence.PasswordGuessEvidence;

/**
 * Default implementation of {@link ElytronIdentityHelper}, it relies in the platform {@link SecurityDomain} to obtain
 * the user credentials
 */
@ApplicationScoped
public class DefaultElytronIdentityHelper implements ElytronIdentityHelper {

    private static final Logger logger = LoggerFactory.getLogger(DefaultElytronIdentityHelper.class);

    private final WorkbenchUserManager workbenchUserManager;

    @Inject
    public DefaultElytronIdentityHelper(final WorkbenchUserManager workbenchUserManager) {
        this.workbenchUserManager = workbenchUserManager;
    }

    @Override
    public User getIdentity(String userName, String password) {

        try {
            if (login(userName, password)) {
                return workbenchUserManager.getUser(userName);
            }
        } catch (Exception ex) {
            logger.debug("Identity provided for '{}' not valid", userName);
        }

        throw new FailedAuthenticationException();
    }

    protected boolean login(String userName, String password) {
        final Evidence evidence = new PasswordGuessEvidence(password.toCharArray());
        try {
            SecurityDomain.getCurrent().authenticate(userName, evidence);
            return true;
        } catch (Exception e) {
            throw new FailedAuthenticationException(e.getMessage());
        }
    }
}
