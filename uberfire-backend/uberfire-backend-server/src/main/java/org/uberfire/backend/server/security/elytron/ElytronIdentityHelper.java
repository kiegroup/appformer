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

import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.backend.server.security.ElytronAuthenticationService;

/**
 * Helper for {@link ElytronAuthenticationService} to obtain the identity for a given credentials
 */
public interface ElytronIdentityHelper {

    /**
     * Obtains a valid (and authenticated) user for the given credentials.
     * @param userName The name of the user
     * @param password The password
     * @return a valid User
     */
    User getIdentity(String userName, String password);
}
