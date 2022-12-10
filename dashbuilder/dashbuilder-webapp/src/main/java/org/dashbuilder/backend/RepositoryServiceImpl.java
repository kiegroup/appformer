/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.backend;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryAlreadyExistsException;
import org.guvnor.structure.repositories.RepositoryEnvironmentConfigurations;
import org.guvnor.structure.repositories.RepositoryInfo;
import org.guvnor.structure.repositories.RepositoryService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.java.nio.base.version.VersionRecord;
import org.uberfire.security.Contributor;
import org.uberfire.spaces.Space;

/**
 * This is not used with Dashbuilder WebApp, but mocks the service so dashbuilder webapp can run
 *
 */
@Default
@ApplicationScoped
public class RepositoryServiceImpl implements RepositoryService {

    @Override
    public void addGroup(Repository arg0, String arg1) {
        // empty

    }

    @Override
    public Repository createRepository(OrganizationalUnit arg0,
                                       String arg1,
                                       String arg2,
                                       RepositoryEnvironmentConfigurations arg3) throws RepositoryAlreadyExistsException {
        // empty
        return null;
    }

    @Override
    public Repository createRepository(OrganizationalUnit arg0,
                                       String arg1,
                                       String arg2,
                                       RepositoryEnvironmentConfigurations arg3,
                                       Collection<Contributor> arg4) throws RepositoryAlreadyExistsException {
        // empty
        return null;
    }

    @Override
    public Collection<Repository> getAllDeletedRepositories(Space arg0) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Repository> getAllRepositories(Space arg0) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Repository> getAllRepositories(Space arg0, boolean arg1) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Repository> getAllRepositoriesFromAllUserSpaces() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Repository> getRepositories(Space arg0) {
        return Collections.emptyList();
    }

    @Override
    public Repository getRepository(Path arg0) {
        return null;
    }

    @Override
    public Repository getRepository(Space arg0, Path arg1) {
        return null;
    }

    @Override
    public Repository getRepositoryFromSpace(Space arg0, String arg1) {
        return null;
    }

    @Override
    public List<VersionRecord> getRepositoryHistory(Space arg0, String arg1, int arg2) {
        return Collections.emptyList();
    }

    @Override
    public List<VersionRecord> getRepositoryHistory(Space arg0, String arg1, int arg2, int arg3) {
        return Collections.emptyList();
    }

    @Override
    public List<VersionRecord> getRepositoryHistoryAll(Space arg0, String arg1) {
        return Collections.emptyList();
    }

    @Override
    public RepositoryInfo getRepositoryInfo(Space arg0, String arg1) {
        return null;
    }

    @Override
    public String normalizeRepositoryName(String arg0) {
        return null;
    }

    @Override
    public void removeGroup(Repository arg0, String arg1) {
        // empty
    }

    @Override
    public void removeRepositories(Space arg0, Set<String> arg1) {
        // empty
    }

    @Override
    public void removeRepository(Space arg0, String arg1) {
        // empty
    }

    @Override
    public void updateContributors(Repository arg0, List<Contributor> arg1) {
        // empty
    }

    @Override
    public boolean validateRepositoryName(String arg0) {
        // empty
        return false;
    }

}
