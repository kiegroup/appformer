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
 * limitations under the License.
 */

package org.uberfire.ext.security.management.wildfly.cli;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.security.shared.api.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.config.ConfigProperties;
import org.uberfire.ext.security.management.api.ContextualManager;
import org.uberfire.ext.security.management.api.UserManager;
import org.uberfire.ext.security.management.api.UserManagerSettings;
import org.uberfire.ext.security.management.api.UserSystemManager;
import org.uberfire.ext.security.management.api.exception.SecurityManagementException;
import org.uberfire.ext.security.management.wildfly.filesystem.WildflyGroupFileSystemManager;
import org.uberfire.ext.security.management.wildfly.filesystem.WildflyUserFileSystemManager;

/**
 * <p>Users manager service provider implementation for JBoss Wildfly.</p>
 * <p>It wraps the Wildfly users manager based on properties file, but instead of the need to specify the path for the properties files, its absolute path discovery is automatically handled by using to the administration API for the server.</p>
 *
 * @since 0.8.0
 */
public class WildflyUserFileSystemCLIManager extends BaseWildflyCLIManager implements UserManager,
                                                                                      ContextualManager {

    private static final Logger LOG = LoggerFactory.getLogger(WildflyUserFileSystemCLIManager.class);
    private WildflyUserFileSystemManager usersPropertiesManager;

    public WildflyUserFileSystemCLIManager() {
        this(new ConfigProperties(System.getProperties()));
    }

    public WildflyUserFileSystemCLIManager(final Map<String, String> gitPrefs) {
        this(new ConfigProperties(gitPrefs));
    }

    public WildflyUserFileSystemCLIManager(final ConfigProperties gitPrefs) {
        loadConfig(gitPrefs);
    }

    private void init(final UserSystemManager usManager) {
        try {
            final Map<String, String> arguments = new HashMap<String, String>(3);
            arguments.put("org.uberfire.ext.security.management.wildfly.filesystem.folder-path",
                          folderPath);
            arguments.put("org.uberfire.ext.security.management.wildfly.filesystem.levels",
                          levels);
            arguments.put("org.uberfire.ext.security.management.wildfly.filesystem.encoded",
                          encoded);
            this.usersPropertiesManager = new WildflyUserFileSystemManager(arguments) {
                @Override
                protected synchronized WildflyGroupFileSystemManager getGroupsFileSystemManager() {
                    try {
                        return ((WildflyGroupFileSystemCLIManager) usManager.groups()).groupsPropertiesManager;
                    } catch (ClassCastException e) {
                        return super.getGroupsFileSystemManager();
                    }
                }
            };
        } catch (Exception e) {
            LOG.error("Cannot find users properties file using the configuration present in the server instance.",
                      e);
        }
    }

    @Override
    public void initialize(UserSystemManager userSystemManager) throws Exception {
        init(userSystemManager);
        usersPropertiesManager.initialize(userSystemManager);
    }

    @Override
    public void destroy() throws Exception {
        usersPropertiesManager.destroy();
    }

    @Override
    public void assignGroups(String username,
                             Collection<String> groups) throws SecurityManagementException {
        usersPropertiesManager.assignGroups(username,
                                            groups);
    }

    @Override
    public void assignRoles(String username,
                            Collection<String> roles) throws SecurityManagementException {
        usersPropertiesManager.assignRoles(username,
                                           roles);
    }

    @Override
    public void changePassword(String username,
                               String newPassword) throws SecurityManagementException {
        usersPropertiesManager.changePassword(username,
                                              newPassword);
    }

    @Override
    public SearchResponse<User> search(SearchRequest request) throws SecurityManagementException {
        return usersPropertiesManager.search(request);
    }

    @Override
    public User get(String identifier) throws SecurityManagementException {
        return usersPropertiesManager.get(identifier);
    }

    @Override
    public List<User> getAll() throws SecurityManagementException {
        return usersPropertiesManager.getAll();
    }

    @Override
    public User create(User entity) throws SecurityManagementException {
        return usersPropertiesManager.create(entity);
    }

    @Override
    public User update(User entity) throws SecurityManagementException {
        return usersPropertiesManager.update(entity);
    }

    @Override
    public void delete(String... identifiers) throws SecurityManagementException {
        usersPropertiesManager.delete(identifiers);
    }

    @Override
    public UserManagerSettings getSettings() {
        return usersPropertiesManager.getSettings();
    }
}
