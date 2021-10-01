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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.config.ConfigProperties;
import org.uberfire.ext.security.management.api.Capability;
import org.uberfire.ext.security.management.api.CapabilityStatus;
import org.uberfire.ext.security.management.api.ContextualManager;
import org.uberfire.ext.security.management.api.GroupManager;
import org.uberfire.ext.security.management.api.GroupManagerSettings;
import org.uberfire.ext.security.management.api.UserSystemManager;
import org.uberfire.ext.security.management.api.exception.GroupNotFoundException;
import org.uberfire.ext.security.management.api.exception.SecurityManagementException;
import org.uberfire.ext.security.management.api.exception.UnsupportedServiceCapabilityException;
import org.uberfire.ext.security.management.impl.GroupManagerSettingsImpl;
import org.uberfire.ext.security.management.search.GroupsIdentifierRuntimeSearchEngine;
import org.uberfire.ext.security.management.search.IdentifierRuntimeSearchEngine;
import org.uberfire.ext.security.management.util.SecurityManagementUtils;
import org.wildfly.security.auth.principal.NamePrincipal;
import org.wildfly.security.auth.realm.FileSystemSecurityRealm;
import org.wildfly.security.auth.server.ModifiableRealmIdentity;
import org.wildfly.security.auth.server.ModifiableRealmIdentityIterator;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.authz.Attributes;
import org.wildfly.security.authz.MapAttributes;

public class WildflyGroupFileSystemManager
        implements GroupManager,
                   ContextualManager {

    private static final Logger LOG = LoggerFactory.getLogger(WildflyGroupFileSystemManager.class);

    protected final IdentifierRuntimeSearchEngine<Group> groupsSearchEngine = new GroupsIdentifierRuntimeSearchEngine();
    private final RealmProvider realmProvider;

    public WildflyGroupFileSystemManager() {
        this(new ConfigProperties(System.getProperties()));
    }

    public WildflyGroupFileSystemManager(final Map<String, String> gitPrefs) {
        this(new ConfigProperties(gitPrefs));
    }

    public WildflyGroupFileSystemManager(final ConfigProperties gitPrefs) {
        realmProvider = new RealmProvider(gitPrefs);
    }

    @Override
    public SearchResponse<Group> search(final SearchRequest request) throws SecurityManagementException {
        final Set<String> result = getAllGroups();
        final SearchResponse<Group> groupSearchResponse = groupsSearchEngine.searchByIdentifiers(result,
                                                                                                 request);
        return groupSearchResponse;
    }

    @Override
    public Group get(final String identifier) throws SecurityManagementException {
        if (identifier == null) {
            throw new NullPointerException();
        }
        final Set<String> result = getAllGroups();
        if (result != null && result.contains(identifier)) {
            return SecurityManagementUtils.createGroup(identifier);
        }
        throw new GroupNotFoundException(identifier);
    }

    @Override
    public List<Group> getAll() throws SecurityManagementException {

        final Set<Group> result = new HashSet<>();
        final Set<String> allGroup = getAllGroups();

        for (String groupName : allGroup) {
            result.add(SecurityManagementUtils.createGroup(groupName));
        }

        return new ArrayList<>(result);
    }

    protected Set<String> getAllGroups() {
        final Set<String> result = new HashSet<>();
        try {
            final ModifiableRealmIdentityIterator realmIdentityIterator = realmProvider.getRealm().getRealmIdentityIterator();
            while (realmIdentityIterator.hasNext()) {
                final ModifiableRealmIdentity identity = realmIdentityIterator.next();
                final Attributes attributes = identity.getAttributes();
                final Attributes.Entry roles = attributes.get("role");
                for (String role : roles) {
                    result.add(role);
                }
            }
        } catch (Exception e) {
            throw new SecurityManagementException(e);
        }
        return result;
    }

    @Override
    public Group create(final Group entity) throws SecurityManagementException {
        if (entity == null) {
            throw new NullPointerException();
        }
        return new GroupImpl(entity.getName());
    }

    @Override
    public Group update(final Group entity) throws SecurityManagementException {
        throw new UnsupportedServiceCapabilityException(Capability.CAN_UPDATE_GROUP);
    }

    @Override
    public void delete(final String... identifiers) throws SecurityManagementException {
        if (identifiers == null) {
            throw new NullPointerException();
        }
        final List<String> groupsToBeRemoved = Arrays.asList(identifiers);
        try {
            final ModifiableRealmIdentityIterator iterator = realmProvider.getRealm().getRealmIdentityIterator();

            while (iterator.hasNext()) {
                final ModifiableRealmIdentity identity = iterator.next();
                final Attributes attributes = new MapAttributes(identity.getAttributes());

                boolean found = false;

                final HashSet<String> groups = new HashSet<>();
                for (String group : identity.getAttributes().get("role")) {
                    if (groupsToBeRemoved.contains(group)) {
                        found = true;
                    } else {
                        groups.add(group);
                    }
                }

                attributes.remove("role");
                attributes.addAll("role", groups);

                if (found) {
                    identity.setAttributes(attributes);
                    identity.dispose();
                }
            }
        } catch (Exception e) {
            LOG.error("Error removing the folowing group names: " + Arrays.toString(identifiers),
                      e);
            throw new SecurityManagementException(e);
        }
    }

    @Override
    public GroupManagerSettings getSettings() {
        final Map<Capability, CapabilityStatus> capabilityStatusMap = new HashMap<>(8);
        for (final Capability capability : SecurityManagementUtils.GROUPS_CAPABILITIES) {
            capabilityStatusMap.put(capability,
                                    getCapabilityStatus(capability));
        }
        return new GroupManagerSettingsImpl(capabilityStatusMap,
                                            false);
    }

    protected CapabilityStatus getCapabilityStatus(Capability capability) {
        if (capability != null) {
            switch (capability) {
                case CAN_ADD_GROUP:
                case CAN_DELETE_GROUP:
                case CAN_SEARCH_GROUPS:
                case CAN_READ_GROUP:
                    return CapabilityStatus.ENABLED;
            }
        }
        return CapabilityStatus.UNSUPPORTED;
    }

    @Override
    public void initialize(final UserSystemManager userSystemManager) {
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void assignUsers(final String groupName, final Collection<String> users) throws SecurityManagementException {
        for (String username : users) {
            try {
                final FileSystemSecurityRealm realm = realmProvider.getRealm();
                final ModifiableRealmIdentity identity = realm.getRealmIdentityForUpdate(new NamePrincipal(username));
                final MapAttributes attributes = new MapAttributes(identity.getAttributes());

                final HashSet<String> groups = new HashSet<>();
                for (String group : identity.getAttributes().get("role")) {
                    groups.add(group);
                }
                groups.add(groupName);

                attributes.addAll("role", groups);

                identity.setAttributes(attributes);

                identity.dispose();
            } catch (RealmUnavailableException e) {
                throw new SecurityManagementException(e);
            }
        }
    }
}
