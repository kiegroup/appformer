/*
 * 2016 Red Hat, Inc. and/or its affiliates.
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.structure.organizationalunit.config.SpaceConfigStorageRegistry;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.repositories.changerequest.ChangeRequest;
import org.guvnor.structure.repositories.changerequest.ChangeRequestDiff;
import org.guvnor.structure.repositories.changerequest.ChangeRequestListUpdatedEvent;
import org.guvnor.structure.repositories.changerequest.ChangeRequestService;
import org.guvnor.structure.repositories.changerequest.ChangeRequestStatus;
import org.guvnor.structure.repositories.changerequest.ChangeType;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.java.nio.base.TextualDiff;
import org.uberfire.java.nio.fs.jgit.JGitPathImpl;
import org.uberfire.java.nio.fs.jgit.util.Git;
import org.uberfire.spaces.SpacesAPI;

import static java.lang.Integer.min;
import static java.util.stream.Collectors.toMap;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotEmpty;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

@Service
@ApplicationScoped
public class ChangeRequestServiceImpl implements ChangeRequestService {

    private final SpaceConfigStorageRegistry spaceConfigStorageRegistry;
    private final RepositoryService repositoryService;
    private final SpacesAPI spaces;
    private final Event<ChangeRequestListUpdatedEvent> changeRequestListUpdatedEvent;
    private final BranchAccessAuthorizer branchAccessAuthorizer;
    private final User user;

    private Logger logger = LoggerFactory.getLogger(ChangeRequestServiceImpl.class);

    @Inject
    public ChangeRequestServiceImpl(final SpaceConfigStorageRegistry spaceConfigStorageRegistry,
                                    final RepositoryService repositoryService,
                                    final SpacesAPI spaces,
                                    final Event<ChangeRequestListUpdatedEvent> changeRequestListUpdatedEvent,
                                    final BranchAccessAuthorizer branchAccessAuthorizer,
                                    final User user) {
        this.spaceConfigStorageRegistry = spaceConfigStorageRegistry;
        this.repositoryService = repositoryService;
        this.spaces = spaces;
        this.changeRequestListUpdatedEvent = changeRequestListUpdatedEvent;
        this.branchAccessAuthorizer = branchAccessAuthorizer;
        this.user = user;
    }

    @Override
    public ChangeRequest createChangeRequest(final String spaceName,
                                             final String repositoryAlias,
                                             final String sourceBranch,
                                             final String targetBranch,
                                             final String author,
                                             final String summary,
                                             final String description,
                                             final Integer changedFilesCount) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotEmpty("sourceBranch", sourceBranch);
        checkNotEmpty("targetBranch", targetBranch);
        checkNotEmpty("author", author);
        checkNotEmpty("summary", summary);
        checkNotEmpty("description", description);
        checkNotNull("changedFilesCount", changedFilesCount);

        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        long changeRequestId = this.generateChangeRequestId(spaceName, repositoryAlias);

        final ChangeRequest newChangeRequest = new ChangeRequest(changeRequestId,
                                                                 spaceName,
                                                                 repositoryAlias,
                                                                 sourceBranch,
                                                                 targetBranch,
                                                                 author,
                                                                 summary,
                                                                 description,
                                                                 changedFilesCount);

        spaceConfigStorageRegistry.get(spaceName).saveChangeRequest(repositoryAlias, newChangeRequest);

        changeRequestListUpdatedEvent.fire(new ChangeRequestListUpdatedEvent(repository.getIdentifier()));

        return newChangeRequest;
    }

    @Override
    public List<ChangeRequest> getChangeRequests(final String spaceName,
                                                 final String repositoryAlias) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);

        return getFilteredChangeRequests(spaceName,
                                         repositoryAlias,
                                         elem -> true);
    }

    @Override
    public List<ChangeRequest> getChangeRequests(final String spaceName,
                                                 final String repositoryAlias,
                                                 final String filter) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);

        return getFilteredChangeRequests(spaceName,
                                         repositoryAlias,
                                         elem -> composeSearchableElement(elem)
                                                 .contains(filter.toLowerCase()));
    }

    @Override
    public List<ChangeRequest> getChangeRequests(final String spaceName,
                                                 final String repositoryAlias,
                                                 final ChangeRequestStatus status) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("status", status);

        return getFilteredChangeRequests(spaceName,
                                         repositoryAlias,
                                         elem -> status == elem.getStatus());
    }

    @Override
    public List<ChangeRequest> getChangeRequests(final String spaceName,
                                                 final String repositoryAlias,
                                                 final ChangeRequestStatus status,
                                                 final String filter) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("status", status);

        return getFilteredChangeRequests(spaceName,
                                         repositoryAlias,
                                         elem -> status == elem.getStatus() &&
                                                 composeSearchableElement(elem)
                                                         .contains(filter.toLowerCase()));
    }

    @Override
    public List<ChangeRequest> getChangeRequests(final String spaceName,
                                                 final String repositoryAlias,
                                                 final Integer page,
                                                 final Integer pageSize,
                                                 final String filter) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("page", page);
        checkNotNull("pageSize", pageSize);

        final List<ChangeRequest> changeRequests = getFilteredChangeRequests(spaceName,
                                                                             repositoryAlias,
                                                                             elem -> composeSearchableElement(elem)
                                                                                     .contains(filter.toLowerCase()));

        return paginate(changeRequests, page, pageSize);
    }

    @Override
    public List<ChangeRequest> getChangeRequests(final String spaceName,
                                                 final String repositoryAlias,
                                                 final Integer page,
                                                 final Integer pageSize,
                                                 final ChangeRequestStatus status,
                                                 final String filter) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("page", page);
        checkNotNull("pageSize", pageSize);
        checkNotNull("status", status);

        final List<ChangeRequest> changeRequests = getFilteredChangeRequests(spaceName,
                                                                             repositoryAlias,
                                                                             elem -> status == elem.getStatus() &&
                                                                                     composeSearchableElement(elem)
                                                                                             .contains(filter.toLowerCase()));

        return paginate(changeRequests, page, pageSize);
    }

    @Override
    public ChangeRequest getChangeRequest(final String spaceName,
                                          final String repositoryAlias,
                                          final Long id) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("id", id);

        final List<ChangeRequest> changeRequests = this.getFilteredChangeRequests(spaceName,
                                                                                  repositoryAlias,
                                                                                  elem -> elem.getId() == id);

        if (changeRequests.size() == 0) {
            throw new NoSuchElementException("The Change Request with ID #" + id + " not found");
        }

        return changeRequests.get(0);
    }

    @Override
    public Integer countChangeRequests(final String spaceName,
                                       final String repositoryAlias) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);

        return getChangeRequests(spaceName, repositoryAlias).size();
    }

    @Override
    public Integer countChangeRequests(final String spaceName,
                                       final String repositoryAlias,
                                       final ChangeRequestStatus status) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("status", status);

        return getChangeRequests(spaceName, repositoryAlias, status).size();
    }

    @Override
    public Integer countChangeRequests(final String spaceName,
                                       final String repositoryAlias,
                                       final String filter) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);

        return getChangeRequests(spaceName, repositoryAlias, filter).size();
    }

    @Override
    public Integer countChangeRequests(final String spaceName,
                                       final String repositoryAlias,
                                       final ChangeRequestStatus status,
                                       final String filter) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("status", status);

        return getChangeRequests(spaceName, repositoryAlias, status, filter).size();
    }

    @Override
    public List<ChangeRequestDiff> getDiff(final String spaceName,
                                           final String repositoryAlias,
                                           final String sourceBranchName,
                                           final String targetBranchName) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("sourceBranchName", sourceBranchName);
        checkNotNull("targetBranchName", targetBranchName);

        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        final Branch sourceBranch = repository.getBranch(sourceBranchName)
                .orElseThrow(() -> new IllegalStateException("The branch " + sourceBranchName + " does not exist"));

        final Branch targetBranch = repository.getBranch(targetBranchName)
                .orElseThrow(() -> new IllegalStateException("The branch " + targetBranchName + " does not exist"));

        final Git git = getGitFromBranch(sourceBranch);

        List<String> conflicts = git.conflictBranchesChecker(sourceBranchName, targetBranchName);

        return git.textualDiffRefs(sourceBranchName, targetBranchName).stream()
                .sorted(Comparator.comparing(TextualDiff::getOldFilePath))
                .map(textualDiff -> new ChangeRequestDiff(
                        createPath(sourceBranch.getPath().toURI(), textualDiff.getOldFilePath()),
                        createPath(targetBranch.getPath().toURI(), textualDiff.getNewFilePath()),
                        ChangeType.valueOf(textualDiff.getChangeType()),
                        textualDiff.getLinesAdded(),
                        textualDiff.getLinesDeleted(),
                        textualDiff.getDiffText(),
                        conflicts.contains(textualDiff.getOldFilePath()) ||
                                conflicts.contains(textualDiff.getNewFilePath())
                )).collect(Collectors.toList());
    }

    @Override
    public void deleteChangeRequests(final String spaceName,
                                     final String repositoryAlias,
                                     final String associatedBranchName) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotEmpty("associatedBranchName", associatedBranchName);

        getFilteredChangeRequests(spaceName,
                                  repositoryAlias,
                                  elem -> elem.getSourceBranch().equals(associatedBranchName)
                                          || elem.getTargetBranch().equals(associatedBranchName))
                .forEach(elem -> spaceConfigStorageRegistry.get(spaceName).deleteChangeRequest(repositoryAlias,
                                                                                               elem.getId()));
    }

    Git getGitFromBranch(final Branch branch) {
        return ((JGitPathImpl) Paths.convert(branch.getPath())).getFileSystem().getGit();
    }

    private org.uberfire.backend.vfs.Path createPath(final String branchPath,
                                                     final String filePath) {
        return PathFactory.newPath(filePath,
                                   branchPath + filePath);
    }

    private Repository resolveRepository(final String spaceName,
                                         final String repositoryAlias) {
        Repository repository = repositoryService.getRepositoryFromSpace(spaces.getSpace(spaceName), repositoryAlias);

        if (repository == null) {
            throw new NoSuchElementException("The repository " + repositoryAlias + " was not found in the space " + spaceName);
        }

        return repository;
    }

    private List<ChangeRequest> getFilteredChangeRequests(final String spaceName,
                                                          final String repositoryAlias,
                                                          final Predicate<? super ChangeRequest> filter) {
        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        return spaceConfigStorageRegistry.get(spaceName).loadChangeRequests(repositoryAlias)
                .stream()
                .filter(elem -> branchAccessAuthorizer.authorize(user.getIdentifier(),
                                                                 spaceName,
                                                                 repository.getIdentifier(),
                                                                 repository.getAlias(),
                                                                 elem.getTargetBranch(),
                                                                 BranchAccessAuthorizer.AccessType.READ))
                .filter(filter)
                .collect(Collectors.toList());
    }

    private String composeSearchableElement(final ChangeRequest element) {
        return ("#" + element.getId() + " " + element.getSummary()).toLowerCase();
    }

    private List<ChangeRequest> paginate(final List<ChangeRequest> changeRequests,
                                         final Integer page,
                                         final Integer pageSize) {
        if (page == 0 && pageSize == 0) {
            return changeRequests;
        }

        final Map<Integer, List<ChangeRequest>> map = IntStream.iterate(0,
                                                                        i -> i + pageSize)
                .limit((changeRequests.size() + pageSize - 1) / pageSize)
                .boxed()
                .collect(toMap(i -> i / pageSize,
                               i -> changeRequests.subList(i,
                                                           min(i + pageSize,
                                                               changeRequests.size()))));

        List<ChangeRequest> paginatedChangeRequests = new ArrayList<>();

        if (map.containsKey(page)) {
            paginatedChangeRequests.addAll(map.get(page));
        }

        return paginatedChangeRequests;
    }

    private long generateChangeRequestId(final String spaceName,
                                         final String repositoryAlias) {
        Optional<Long> maxId = spaceConfigStorageRegistry.get(spaceName)
                .getChangeRequestIds(repositoryAlias)
                .stream()
                .max(Long::compare);

        long nextId = 1L;

        if (maxId.isPresent()) {
            nextId = maxId.get() + 1;
        }

        return nextId;
    }
}
