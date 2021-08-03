/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations  under the License.
 */
package org.uberfire.ext.security.management.wildfly.filesystem;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jboss.as.domain.management.security.PropertiesFileLoader;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.config.ConfigProperties;
import org.uberfire.ext.security.management.api.Capability;
import org.uberfire.ext.security.management.api.CapabilityStatus;
import org.uberfire.ext.security.management.api.ContextualManager;
import org.uberfire.ext.security.management.api.UserManager;
import org.uberfire.ext.security.management.api.UserManagerSettings;
import org.uberfire.ext.security.management.api.UserSystemManager;
import org.uberfire.ext.security.management.api.exception.InvalidEntityIdentifierException;
import org.uberfire.ext.security.management.api.exception.SecurityManagementException;
import org.uberfire.ext.security.management.api.exception.UserNotFoundException;
import org.uberfire.ext.security.management.impl.UserManagerSettingsImpl;
import org.uberfire.ext.security.management.search.IdentifierRuntimeSearchEngine;
import org.uberfire.ext.security.management.search.UsersIdentifierRuntimeSearchEngine;
import org.uberfire.ext.security.management.util.SecurityManagementUtils;
import org.wildfly.security.auth.principal.NamePrincipal;
import org.wildfly.security.auth.realm.FileSystemSecurityRealm;
import org.wildfly.security.auth.server.ModifiableRealmIdentity;
import org.wildfly.security.auth.server.ModifiableRealmIdentityIterator;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.authz.Attributes;
import org.wildfly.security.authz.MapAttributes;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.DigestPassword;
import org.wildfly.security.password.spec.DigestPasswordAlgorithmSpec;
import org.wildfly.security.password.spec.DigestPasswordSpec;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;

import static com.google.common.base.Preconditions.checkNotNull;

public class WildflyUserFileSystemManager
        implements ContextualManager,
                   UserManager {

    public static final String VALID_USERNAME_SYMBOLS = "\",\", \"-\", \".\", \"/\", \"=\", \"@\", \"\\\"";
    private static final Logger LOG = LoggerFactory.getLogger(WildflyUserFileSystemManager.class);

    private static final Provider ELYTRON_PROVIDER = new WildFlyElytronPasswordProvider();

    protected final IdentifierRuntimeSearchEngine<User> usersSearchEngine = new UsersIdentifierRuntimeSearchEngine();
    private final RealmProvider realmProvider;
    private UserSystemManager userSystemManager;

    public WildflyUserFileSystemManager() {
        this(new ConfigProperties(System.getProperties()));
    }

    public WildflyUserFileSystemManager(final Map<String, String> gitPrefs) {
        this(new ConfigProperties(gitPrefs));
    }

    public WildflyUserFileSystemManager(final ConfigProperties gitPrefs) {
        realmProvider = new RealmProvider(gitPrefs);
    }

    @Override
    public void initialize(final UserSystemManager userSystemManager) {
        this.userSystemManager = userSystemManager;
    }

    protected synchronized WildflyGroupFileSystemManager getGroupsFileSystemManager() {
        try {
            return (WildflyGroupFileSystemManager) userSystemManager.groups();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public SearchResponse<User> search(final SearchRequest request) throws SecurityManagementException {
        final List<String> users = new ArrayList<>();
        try {
            final ModifiableRealmIdentityIterator realmIdentityIterator = realmProvider.getRealm().getRealmIdentityIterator();
            while (realmIdentityIterator.hasNext()) {
                ModifiableRealmIdentity next = realmIdentityIterator.next();
                users.add(next.getRealmIdentityPrincipal().getName());
            }
        } catch (RealmUnavailableException e) {
            throw new SecurityManagementException(e);
        }

        return usersSearchEngine.searchByIdentifiers(users,
                                                     request);
    }

    @Override
    public User get(final String identifier) throws SecurityManagementException {
        validateUserIdentifier(identifier);
        final ModifiableRealmIdentity modifiableIdentity = realmProvider.getRealm().getRealmIdentityForUpdate(new NamePrincipal(identifier));

        try {
            final Optional<User> user = getUser(modifiableIdentity);

            if (user.isPresent()) {
                return user.get();
            } else {
                throw new UserNotFoundException(identifier);
            }
        } catch (RealmUnavailableException e) {
            throw new UserNotFoundException(identifier);
        }
    }

    @Override
    public List<User> getAll() throws SecurityManagementException {
        final ArrayList<User> result = new ArrayList<>();

        try {
            final ModifiableRealmIdentityIterator realmIdentityIterator = realmProvider.getRealm().getRealmIdentityIterator();
            while (realmIdentityIterator.hasNext()) {
                final ModifiableRealmIdentity identity = realmIdentityIterator.next();

                final Optional<User> user = getUser(identity);
                if (user.isPresent()) {
                    result.add(user.get());
                }
            }
        } catch (RealmUnavailableException e) {
            throw new SecurityManagementException(e);
        }
        return result;
    }

    private Optional<User> getUser(final ModifiableRealmIdentity identity) throws RealmUnavailableException {
        final String userName = identity.getRealmIdentityPrincipal().getName();
        final Attributes attributes = identity.getAttributes();
        final Attributes.Entry roles = attributes.get("role");
        final Set<String> userGroups = new HashSet<>();
        final Set<Group> groups = new HashSet<>();

        for (String role : roles) {
            userGroups.add(role);
        }

        final Set<String> registeredRoles = SecurityManagementUtils.getRegisteredRoleNames();
        if (groups != null) {
            final Set<String> allGroups = getGroupsFileSystemManager().getAllGroups();
            if (allGroups != null) {
                final Set<Group> _groups = new HashSet<>();
                final Set<Role> _roles = new HashSet<>();
                for (final String name : userGroups) {
                    if (!allGroups.contains(name)) {
                        String error = "Error getting groups for user. User's group '" + name + "' does not exist.";
                        LOG.error(error);
                        throw new SecurityManagementException(error);
                    }
                    SecurityManagementUtils.populateGroupOrRoles(name,
                                                                 registeredRoles,
                                                                 _groups,
                                                                 _roles);
                }

                return Optional.of(SecurityManagementUtils.createUser(userName,
                                                                      _groups,
                                                                      _roles));
            }
        }
        return Optional.empty();
    }

    @Override
    public User create(final User entity) throws SecurityManagementException {
        checkNotNull("entity",
                     entity);
        final String username = entity.getIdentifier();
        if (null == username || 0 == username.trim().length()) {
            throw new IllegalArgumentException("No username specified.");
        }
        validateUserIdentifier(username);

        try {
            final ModifiableRealmIdentity modifiableIdentity = realmProvider.getRealm().getRealmIdentityForUpdate(new NamePrincipal(username));
            if (!modifiableIdentity.exists()) {
                modifiableIdentity.create();
            }
        } catch (RealmUnavailableException e) {
            LOG.error("Error creating user " + username,
                      e);
            throw new SecurityManagementException(e);
        }
        return entity;
    }

    @Override
    public User update(final User entity) throws SecurityManagementException {
        checkNotNull("entity",
                     entity);
        return entity;
    }

    @Override
    public void delete(final String... usernames) throws SecurityManagementException {
        checkNotNull("usernames",
                     usernames);

        for (final String username : usernames) {
            final ModifiableRealmIdentity identity = realmProvider.getRealm().getRealmIdentityForUpdate(new NamePrincipal(username));
            try {
                if (identity.exists()) {
                    identity.delete();
                }
            } catch (RealmUnavailableException e) {
                throw new SecurityManagementException(e);
            }
        }
    }

    @Override
    public UserManagerSettings getSettings() {
        final Map<Capability, CapabilityStatus> capabilityStatusMap = new HashMap<>(8);
        for (final Capability capability : SecurityManagementUtils.USERS_CAPABILITIES) {
            capabilityStatusMap.put(capability,
                                    getCapabilityStatus(capability));
        }
        return new UserManagerSettingsImpl(capabilityStatusMap,
                                           null);
    }

    protected CapabilityStatus getCapabilityStatus(Capability capability) {
        if (capability != null) {
            switch (capability) {
                case CAN_SEARCH_USERS:
                case CAN_ADD_USER:
                case CAN_UPDATE_USER:
                case CAN_DELETE_USER:
                case CAN_READ_USER:
                case CAN_ASSIGN_GROUPS:
                    /** As it is using the UberfireRoleManager. **/
                case CAN_ASSIGN_ROLES:
                case CAN_CHANGE_PASSWORD:
                    return CapabilityStatus.ENABLED;
            }
        }
        return CapabilityStatus.UNSUPPORTED;
    }

    @Override
    public void assignGroups(final String username, final Collection<String> groups) throws SecurityManagementException {
        try {
            final FileSystemSecurityRealm realm = realmProvider.getRealm();
            final ModifiableRealmIdentity identity = realm.getRealmIdentityForUpdate(new NamePrincipal(username));
            final MapAttributes attributes = new MapAttributes();
            final Set<String> userRoles = SecurityManagementUtils.rolesToString(SecurityManagementUtils.getRoles(userSystemManager,
                                                                                                                 username));
            userRoles.addAll(groups);
            attributes.addAll("role", userRoles);
            identity.setAttributes(attributes);
            identity.dispose();
        } catch (RealmUnavailableException e) {
            throw new SecurityManagementException(e);
        }
    }

    @Override
    public void assignRoles(final String username, final Collection<String> roles) throws SecurityManagementException {
        try {
            final FileSystemSecurityRealm realm = realmProvider.getRealm();
            final ModifiableRealmIdentity identity = realm.getRealmIdentityForUpdate(new NamePrincipal(username));
            final MapAttributes attributes = new MapAttributes();
            final Set<String> userGroups = SecurityManagementUtils.groupsToString(SecurityManagementUtils.getGroups(userSystemManager,
                                                                                                                    username));
            userGroups.addAll(roles);
            attributes.addAll("role", userGroups);
            identity.setAttributes(attributes);
            identity.dispose();
        } catch (RealmUnavailableException e) {
            throw new SecurityManagementException(e);
        }
    }

    @Override
    public void changePassword(final String username, final String newPassword) throws SecurityManagementException {
        checkNotNull("username",
                     username);
        if (0 == username.trim().length()) {
            throw new IllegalArgumentException("No username specified for updating password.");
        }

        try {
            final ModifiableRealmIdentity modifiableIdentity = realmProvider.getRealm().getRealmIdentityForUpdate(new NamePrincipal(username));

            final String TEST_REALM = "ApplicationRealm";

            final PasswordFactory passwordFactory = PasswordFactory.getInstance(DigestPassword.ALGORITHM_DIGEST_MD5, ELYTRON_PROVIDER);

            final DigestPasswordAlgorithmSpec digestAlgorithmSpec = new DigestPasswordAlgorithmSpec(username, TEST_REALM);
            final EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(newPassword.toCharArray(), digestAlgorithmSpec);

            final DigestPassword original = (DigestPassword) passwordFactory.generatePassword(encryptableSpec);

            final byte[] digest = original.getDigest();

            final DigestPasswordSpec digestPasswordSpec = new DigestPasswordSpec(username, TEST_REALM, digest);

            final DigestPassword restored = (DigestPassword) passwordFactory.generatePassword(digestPasswordSpec);

            modifiableIdentity.setCredentials(Collections.singleton(new PasswordCredential(restored)));

            modifiableIdentity.dispose();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
            LOG.error("Error changing user's password",
                      e);
            throw new SecurityManagementException(e);
        }
    }

    /**
     * Validates the candidate user identifier by following same Wildfly's patterns for usernames in properties realms,
     * and by following the behavior for the <code>add-user.sh</code> script as well,
     * here is the actual username validation constraints:
     * <code>
     * WFLYDM0028: Username must be alphanumeric with the exception of
     * the following accepted symbols (",", "-", ".", "/", "=", "@", "\")
     * </code>
     *
     * @param identifier The identifier to validate.
     */
    private void validateUserIdentifier(String identifier) {
        if (!PropertiesFileLoader.PROPERTY_PATTERN
                .matcher(identifier + "=0")
                .matches()) {
            throw new InvalidEntityIdentifierException(identifier,
                                                       VALID_USERNAME_SYMBOLS);
        }
    }
}
