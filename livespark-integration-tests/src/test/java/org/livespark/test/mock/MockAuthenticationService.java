/*
 * Copyright 2016 JBoss Inc
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
package org.livespark.test.mock;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.uberfire.ext.security.server.ServletSecurityAuthenticationService;

/**
 * Specializes the uberfire auth service to allow arbitrary logins to simplify test setup.
 */
@ApplicationScoped
@Service
@Specializes
public class MockAuthenticationService extends ServletSecurityAuthenticationService {

    private Optional<User> user = Optional.empty();

    @Override
    public User login( String username,
                       String password ) {
        user = Optional.of( new UserImpl( username ) );

        return user.get();
    }

    @Override
    public boolean isLoggedIn() {
        return user.isPresent();
    }

    @Override
    public void logout() {
        user = Optional.empty();
    }

    @Override
    public User getUser() {
        return user.orElseGet( () -> User.ANONYMOUS );
    }

}
