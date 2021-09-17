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

import org.jboss.errai.security.shared.api.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.config.ConfigProperties;
import org.uberfire.ext.security.management.api.ContextualManager;
import org.uberfire.ext.security.management.api.GroupManager;
import org.uberfire.ext.security.management.api.GroupManagerSettings;
import org.uberfire.ext.security.management.api.UserSystemManager;
import org.uberfire.ext.security.management.api.exception.SecurityManagementException;
import org.uberfire.ext.security.management.wildfly.filesystem.WildflyGroupFileSystemManager;

/**
 * <p>Groups manager service provider implementation for JBoss Wildfly.</p>
 * <p>It wraps the Wildfly groups manager based on properties file, but instead of the need to specify the path for the properties files, its absolute path discovery is automatically handled by using to the administration API for the server.</p>
 *
 * @since 0.8.0
 */
public class WildflyGroupFileSystemCLIManager extends BaseWildflyCLIManager implements GroupManager,
                                                                                       ContextualManager {

    private static final Logger LOG = LoggerFactory.getLogger(WildflyGroupFileSystemCLIManager.class);
    protected WildflyGroupFileSystemManager groupsPropertiesManager;

    public WildflyGroupFileSystemCLIManager() {
        this(new ConfigProperties(System.getProperties()));
    }

    public WildflyGroupFileSystemCLIManager(final Map<String, String> gitPrefs) {
        this(new ConfigProperties(gitPrefs));
    }

    public WildflyGroupFileSystemCLIManager(final ConfigProperties gitPrefs) {
        loadConfig(gitPrefs);
    }

    private void init() {
        try {
            final Map<String, String> arguments = new HashMap<String, String>(3);
            arguments.put("org.uberfire.ext.security.management.wildfly.filesystem.folder-path",
                          folderPath);
            arguments.put("org.uberfire.ext.security.management.wildfly.filesystem.levels",
                          levels);
            arguments.put("org.uberfire.ext.security.management.wildfly.filesystem.encoded",
                          encoded);
            this.groupsPropertiesManager = new WildflyGroupFileSystemManager(arguments);
        } catch (Exception e) {
            LOG.error("Cannot find groups properties file using the configuration present in the server instance.",
                      e);
        }
    }

    @Override
    public void initialize(UserSystemManager userSystemManager) throws Exception {
        init();
        groupsPropertiesManager.initialize(userSystemManager);
    }

    @Override
    public void destroy() throws Exception {
        groupsPropertiesManager.destroy();
    }

    @Override
    public SearchResponse<Group> search(SearchRequest request) throws SecurityManagementException {
        return groupsPropertiesManager.search(request);
    }

    @Override
    public Group get(String identifier) throws SecurityManagementException {
        return groupsPropertiesManager.get(identifier);
    }

    @Override
    public List<Group> getAll() throws SecurityManagementException {
        return groupsPropertiesManager.getAll();
    }

    @Override
    public Group create(Group entity) throws SecurityManagementException {
        return groupsPropertiesManager.create(entity);
    }

    @Override
    public Group update(Group entity) throws SecurityManagementException {
        return groupsPropertiesManager.update(entity);
    }

    @Override
    public void delete(String... identifiers) throws SecurityManagementException {
        groupsPropertiesManager.delete(identifiers);
    }

    @Override
    public GroupManagerSettings getSettings() {
        return groupsPropertiesManager.getSettings();
    }

    @Override
    public void assignUsers(String name,
                            Collection<String> users) throws SecurityManagementException {
        groupsPropertiesManager.assignUsers(name,
                                            users);
    }
}
