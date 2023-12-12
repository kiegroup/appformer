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

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.evidence.Evidence;
import org.wildfly.security.evidence.PasswordGuessEvidence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Default implementation of {@link ElytronIdentityHelper}, it relies in the platform {@link SecurityDomain} to obtain the user credentials
 */
@Alternative
public class DefaultElytronIdentityHelper implements ElytronIdentityHelper {

    private static final Logger logger = LoggerFactory.getLogger(DefaultElytronIdentityHelper.class);

    @Inject
    public DefaultElytronIdentityHelper() {
    }

    @Override
    public User getIdentity(final String userName, final String password) {

        try {
            final Evidence evidence = new PasswordGuessEvidence(password.toCharArray());
            final Iterator<String> userRoles = login(userName, evidence);
            final Collection<Role> roles = new ArrayList<>();
            userRoles.forEachRemaining(role -> roles.add(new RoleImpl(role)));

            return new UserImpl(userName, roles);
        } catch (Exception ex) {
            logger.debug("Identity provided for '{}' not valid", userName);
        }

        throw new FailedAuthenticationException();
    }

    protected Iterator<String> login(final String userName, final Evidence evidence) throws RealmUnavailableException {
        return SecurityDomain.getCurrent().authenticate(userName, evidence).getRoles().iterator();
    }
}
