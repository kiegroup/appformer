/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.structure.backend.repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.structure.config.SystemRepositoryChangedEvent;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.server.config.ConfigGroup;
import org.guvnor.structure.server.config.ConfigurationService;
import org.guvnor.structure.server.repositories.RepositoryFactory;
import org.uberfire.backend.vfs.Path;
import org.uberfire.java.nio.file.FileSystem;

import static org.guvnor.structure.server.config.ConfigType.REPOSITORY;
import static org.uberfire.backend.server.util.Paths.convert;

/**
 * Cache for configured repositories.
 * <p>
 * If you plan to use this outside of ProjectService make sure you know what you are doing.
 * <p>
 * It is safe to get data from this class, but any editing should be done through ProjectService.
 * Still if possible use ProjectService for accessing the repositories. It is part of a public API
 * and this is hidden in the -backend on purpose.
 */
@ApplicationScoped
public class ConfiguredRepositories {

    private ConfigurationService configurationService;
    private RepositoryFactory repositoryFactory;
    private Repository systemRepository;

    private Map<String, Repository> repositoriesByAlias = new HashMap<>();
    private Map<Path, Repository> repositoriesByBranchRoot = new HashMap<>();

    public ConfiguredRepositories() {
    }

    @Inject
    public ConfiguredRepositories(final ConfigurationService configurationService,
                                  final RepositoryFactory repositoryFactory,
                                  final @Named("system") Repository systemRepository) {
        this.configurationService = configurationService;
        this.repositoryFactory = repositoryFactory;
        this.systemRepository = systemRepository;
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    void reloadRepositories() {
        repositoriesByAlias.clear();
        repositoriesByBranchRoot.clear();

        final List<ConfigGroup> repoConfigs = configurationService.getConfiguration(REPOSITORY);
        if (!(repoConfigs == null || repoConfigs.isEmpty())) {
            for (final ConfigGroup configGroup : repoConfigs) {
                final Repository repository = repositoryFactory.newRepository(configGroup);

                add(repository);
            }
        }
    }

    /**
     * @param alias Name of the repository.
     * @return Repository that has a random branch as a root, usually master if master exists.
     */
    public Repository getRepositoryByRepositoryAlias(final String alias) {
        return repositoriesByAlias.get(alias);
    }

    /**
     * This can also return System Repository.
     * @param fs
     * @return
     */
    public Repository getRepositoryByRepositoryFileSystem(final FileSystem fs) {
        if (fs == null) {
            return null;
        }

        if (systemRepository.getDefaultBranch().isPresent()
                && convert(systemRepository.getDefaultBranch().get().getPath()).getFileSystem().equals(fs)) {
            return systemRepository;
        }

        for (final Repository repository : repositoriesByAlias.values()) {
            if (repository.getDefaultBranch().isPresent()
                    && convert(repository.getDefaultBranch().get().getPath()).getFileSystem().equals(fs)) {
                return repository;
            }
        }

        return null;
    }

    /**
     * @param root Path to the repository root in any branch.
     * @return Repository root branch is still the default, usually master.
     */
    public Repository getRepositoryByRootPath(final Path root) {
        return repositoriesByBranchRoot.get(root);
    }

    /**
     * @return Does not include system repository.
     */
    List<Repository> getAllConfiguredRepositories() {
        return new ArrayList<>(repositoriesByAlias.values());
    }

    public boolean containsAlias(final String alias) {
        return repositoriesByAlias.containsKey(alias) || SystemRepository.SYSTEM_REPO.getAlias().equals(alias);
    }

    void add(final Repository repository) {
        repositoriesByAlias.put(repository.getAlias(),
                                repository);

        if (repository.getBranches() != null) {
            for (final Branch branch : repository.getBranches()) {
                repositoriesByBranchRoot.put(branch.getPath(),
                                             repository);
            }
        }
    }

    void update(final Repository updatedRepo) {
        remove(updatedRepo.getAlias());
        add(updatedRepo);
    }

    Repository remove(final String alias) {

        final Repository removed = repositoriesByAlias.remove(alias);

        removeFromRootByAlias(alias);

        return removed;
    }

    void removeFromRootByAlias(final String alias) {
        for (Path path : findFromRootMapByAlias(alias)) {
            repositoriesByBranchRoot.remove(path);
        }
    }

    private List<Path> findFromRootMapByAlias(final String alias) {
        List<Path> result = new ArrayList<>();
        for (Path path : repositoriesByBranchRoot.keySet()) {
            if (repositoriesByBranchRoot.get(path).getAlias().equals(alias)) {
                result.add(path);
            }
        }
        return result;
    }

    public void flush(final @Observes @org.guvnor.structure.backend.config.Repository SystemRepositoryChangedEvent changedEvent) {
        reloadRepositories();
    }
}
