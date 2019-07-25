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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import org.eclipse.jgit.revwalk.RevCommit;
import org.guvnor.structure.organizationalunit.config.SpaceConfigStorageRegistry;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.repositories.changerequest.ChangeRequest;
import org.guvnor.structure.repositories.changerequest.ChangeRequestComment;
import org.guvnor.structure.repositories.changerequest.ChangeRequestDiff;
import org.guvnor.structure.repositories.changerequest.ChangeRequestListUpdatedEvent;
import org.guvnor.structure.repositories.changerequest.ChangeRequestService;
import org.guvnor.structure.repositories.changerequest.ChangeRequestStatus;
import org.guvnor.structure.repositories.changerequest.ChangeRequestUpdatedEvent;
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
import org.uberfire.java.nio.fs.jgit.util.exceptions.GitException;
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
    private final Event<ChangeRequestUpdatedEvent> changeRequestUpdatedEvent;
    private final BranchAccessAuthorizer branchAccessAuthorizer;
    private final User user;

    private Logger logger = LoggerFactory.getLogger(ChangeRequestServiceImpl.class);

    @Inject
    public ChangeRequestServiceImpl(final SpaceConfigStorageRegistry spaceConfigStorageRegistry,
                                    final RepositoryService repositoryService,
                                    final SpacesAPI spaces,
                                    final Event<ChangeRequestListUpdatedEvent> changeRequestListUpdatedEvent,
                                    final Event<ChangeRequestUpdatedEvent> changeRequestUpdatedEvent,
                                    final BranchAccessAuthorizer branchAccessAuthorizer,
                                    final User user) {
        this.spaceConfigStorageRegistry = spaceConfigStorageRegistry;
        this.repositoryService = repositoryService;
        this.spaces = spaces;
        this.changeRequestListUpdatedEvent = changeRequestListUpdatedEvent;
        this.changeRequestUpdatedEvent = changeRequestUpdatedEvent;
        this.branchAccessAuthorizer = branchAccessAuthorizer;
        this.user = user;
    }

    @Override
    public ChangeRequest createChangeRequest(final String spaceName,
                                             final String repositoryAlias,
                                             final String sourceBranch,
                                             final String targetBranch,
                                             final String summary,
                                             final String description) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotEmpty("sourceBranch", sourceBranch);
        checkNotEmpty("targetBranch", targetBranch);
        checkNotEmpty("summary", summary);
        checkNotEmpty("description", description);

        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        long changeRequestId = this.generateChangeRequestId(spaceName, repositoryAlias);

        final String commonCommitId = getCommonCommitId(repository,
                                                        sourceBranch,
                                                        targetBranch);

        final ChangeRequest newChangeRequest = new ChangeRequest(changeRequestId,
                                                                 spaceName,
                                                                 repositoryAlias,
                                                                 sourceBranch,
                                                                 targetBranch,
                                                                 ChangeRequestStatus.OPEN,
                                                                 user.getIdentifier(),
                                                                 summary,
                                                                 description,
                                                                 new Date(),
                                                                 commonCommitId);

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
                                                 final List<ChangeRequestStatus> statusList) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotEmpty("statusList", statusList);

        return getFilteredChangeRequests(spaceName,
                                         repositoryAlias,
                                         elem -> statusList.contains(elem.getStatus()));
    }

    @Override
    public List<ChangeRequest> getChangeRequests(final String spaceName,
                                                 final String repositoryAlias,
                                                 final List<ChangeRequestStatus> statusList,
                                                 final String filter) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotEmpty("statusList", statusList);

        return getFilteredChangeRequests(spaceName,
                                         repositoryAlias,
                                         elem -> statusList.contains(elem.getStatus()) &&
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
                                                 final List<ChangeRequestStatus> statusList,
                                                 final String filter) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("page", page);
        checkNotNull("pageSize", pageSize);
        checkNotEmpty("statusList", statusList);

        final List<ChangeRequest> changeRequests = getFilteredChangeRequests(spaceName,
                                                                             repositoryAlias,
                                                                             elem -> statusList.contains(elem.getStatus()) &&
                                                                                     composeSearchableElement(elem)
                                                                                             .contains(filter.toLowerCase()));

        return paginate(changeRequests, page, pageSize);
    }

    @Override
    public ChangeRequest getChangeRequest(final String spaceName,
                                          final String repositoryAlias,
                                          final Long changeRequestId) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);

        final List<ChangeRequest> changeRequests = this.getFilteredChangeRequests(spaceName,
                                                                                  repositoryAlias,
                                                                                  elem -> elem.getId() == changeRequestId);

        if (changeRequests.size() == 0) {
            throw new NoSuchElementException("The Change Request with ID #" + changeRequestId + " not found");
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
                                       final List<ChangeRequestStatus> statusList) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotEmpty("statusList", statusList);

        return getChangeRequests(spaceName, repositoryAlias, statusList).size();
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
                                       final List<ChangeRequestStatus> statusList,
                                       final String filter) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotEmpty("statusList", statusList);

        return getChangeRequests(spaceName, repositoryAlias, statusList, filter).size();
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

        return getDiff(repository,
                       sourceBranchName,
                       targetBranchName,
                       null,
                       null);
    }

    @Override
    public List<ChangeRequestDiff> getDiff(final String spaceName,
                                           final String repositoryAlias,
                                           final Long changeRequestId) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);

        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        final ChangeRequest changeRequest = getChangeRequest(spaceName,
                                                             repositoryAlias,
                                                             changeRequestId);

        return getDiff(repository,
                       changeRequest.getSourceBranch(),
                       changeRequest.getTargetBranch(),
                       changeRequest.getCommonCommitId(),
                       changeRequest.getLastCommitId());
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

    @Override
    public void rejectChangeRequest(final String spaceName,
                                    final String repositoryAlias,
                                    final Long changeRequestId) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);

        final ChangeRequest oldChangeRequest = this.getChangeRequest(spaceName,
                                                                     repositoryAlias,
                                                                     changeRequestId);

        if (oldChangeRequest.getStatus() != ChangeRequestStatus.OPEN) {
            throw new IllegalStateException("Cannot reject a change request that is not opened");
        }

        this.updateChangeRequestStatus(spaceName,
                                       repositoryAlias,
                                       oldChangeRequest,
                                       ChangeRequestStatus.REJECTED);
    }

    @Override
    public Boolean acceptChangeRequest(final String spaceName,
                                       final String repositoryAlias,
                                       final Long changeRequestId) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);

        final ChangeRequest oldChangeRequest = this.getChangeRequest(spaceName,
                                                                     repositoryAlias,
                                                                     changeRequestId);

        if (oldChangeRequest.getStatus() != ChangeRequestStatus.OPEN) {
            throw new IllegalStateException("Cannot accept a change request that is not opened");
        }

        final String sourceBranchName = oldChangeRequest.getSourceBranch();
        final String targetBranchName = oldChangeRequest.getTargetBranch();

        final Repository repository = resolveRepository(spaceName,
                                                        repositoryAlias);

        final Branch sourceBranch = repository.getBranch(sourceBranchName)
                .orElseThrow(() -> new IllegalStateException("The branch " + sourceBranchName + " does not exist"));
        repository.getBranch(targetBranchName)
                .orElseThrow(() -> new IllegalStateException("The branch " + targetBranchName + " does not exist"));

        final Git git = getGitFromBranch(sourceBranch);

        try {
            if (!isChangeRequestDiffEmpty(repository, oldChangeRequest)) {
                final String lastCommitId = getLastCommitId(repository,
                                                            sourceBranchName);
                final List<RevCommit> commits = git.listCommits(oldChangeRequest.getCommonCommitId(),
                                                                lastCommitId);

                if (!commits.isEmpty()) {
                    if (commits.size() > 1) {
                        git.squash(sourceBranchName,
                                   commits.get(commits.size() - 1).getName(),
                                   "Squash commits for merging operation.");
                    }

                    git.merge(sourceBranchName,
                              targetBranchName);

                    this.updateChangeRequestStatus(spaceName,
                                                   repositoryAlias,
                                                   oldChangeRequest,
                                                   ChangeRequestStatus.ACCEPTED);
                }
            }

            return true;
        } catch (GitException e) {
            logger.debug(String.format("Cannot merge change request %s: %s", changeRequestId, e));
        }

        return false;
    }

    @Override
    public Boolean tryRevertChangeRequest(final String spaceName,
                                          final String repositoryAlias,
                                          final Long changeRequestId) {
        //TODO: [caponetto]
        return false;
    }

    @Override
    public void updateChangeRequestSummary(final String spaceName,
                                           final String repositoryAlias,
                                           final Long changeRequestId,
                                           final String updatedSummary) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);
        checkNotEmpty("updatedSummary", updatedSummary);

        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        final ChangeRequest oldChangeRequest = this.getChangeRequest(spaceName,
                                                                     repositoryAlias,
                                                                     changeRequestId);

        final ChangeRequest updatedChangeRequest = new ChangeRequest(oldChangeRequest.getId(),
                                                                     oldChangeRequest.getSpaceName(),
                                                                     oldChangeRequest.getRepositoryAlias(),
                                                                     oldChangeRequest.getSourceBranch(),
                                                                     oldChangeRequest.getTargetBranch(),
                                                                     oldChangeRequest.getStatus(),
                                                                     oldChangeRequest.getAuthorId(),
                                                                     updatedSummary,
                                                                     oldChangeRequest.getDescription(),
                                                                     oldChangeRequest.getCreatedDate(),
                                                                     oldChangeRequest.getCommonCommitId());

        spaceConfigStorageRegistry.get(spaceName).saveChangeRequest(repositoryAlias,
                                                                    updatedChangeRequest);

        changeRequestUpdatedEvent.fire(new ChangeRequestUpdatedEvent(repository.getIdentifier(),
                                                                     updatedChangeRequest.getId()));
    }

    @Override
    public void updateChangeRequestDescription(final String spaceName,
                                               final String repositoryAlias,
                                               final Long changeRequestId,
                                               final String updatedDescription) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);
        checkNotEmpty("updatedDescription", updatedDescription);

        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        final ChangeRequest oldChangeRequest = this.getChangeRequest(spaceName,
                                                                     repositoryAlias,
                                                                     changeRequestId);

        final ChangeRequest updatedChangeRequest = new ChangeRequest(oldChangeRequest.getId(),
                                                                     oldChangeRequest.getSpaceName(),
                                                                     oldChangeRequest.getRepositoryAlias(),
                                                                     oldChangeRequest.getSourceBranch(),
                                                                     oldChangeRequest.getTargetBranch(),
                                                                     oldChangeRequest.getStatus(),
                                                                     oldChangeRequest.getAuthorId(),
                                                                     oldChangeRequest.getSummary(),
                                                                     updatedDescription,
                                                                     oldChangeRequest.getCreatedDate(),
                                                                     oldChangeRequest.getCommonCommitId());

        spaceConfigStorageRegistry.get(spaceName).saveChangeRequest(repositoryAlias,
                                                                    updatedChangeRequest);

        changeRequestUpdatedEvent.fire(new ChangeRequestUpdatedEvent(repository.getIdentifier(),
                                                                     updatedChangeRequest.getId()));
    }

    @Override
    public List<ChangeRequestComment> getComments(final String spaceName,
                                                  final String repositoryAlias,
                                                  final Long changeRequestId) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);

        return spaceConfigStorageRegistry.get(spaceName).loadChangeRequestComments(repositoryAlias,
                                                                                   changeRequestId);
    }

    @Override
    public void addComment(final String spaceName,
                           final String repositoryAlias,
                           final Long changeRequestId,
                           final String text) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);
        checkNotEmpty("text", text);

        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        final Long commentId = generateCommentId(spaceName,
                                                 repositoryAlias,
                                                 changeRequestId);

        final ChangeRequestComment newComment = new ChangeRequestComment(commentId,
                                                                         user.getIdentifier(),
                                                                         new Date(),
                                                                         text);

        spaceConfigStorageRegistry.get(spaceName).saveChangeRequestComment(repositoryAlias,
                                                                           changeRequestId,
                                                                           newComment);

        changeRequestUpdatedEvent.fire(new ChangeRequestUpdatedEvent(repository.getIdentifier(),
                                                                     changeRequestId));
    }

    @Override
    public void deleteComment(final String spaceName,
                              final String repositoryAlias,
                              final Long changeRequestId,
                              final Long commentId) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);
        checkNotNull("commentId", commentId);

        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        spaceConfigStorageRegistry.get(spaceName).deleteChangeRequestComment(repositoryAlias,
                                                                             changeRequestId,
                                                                             commentId);

        changeRequestUpdatedEvent.fire(new ChangeRequestUpdatedEvent(repository.getIdentifier(),
                                                                     changeRequestId));
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
                .map(elem -> new ChangeRequest(elem.getId(),
                                               elem.getSpaceName(),
                                               elem.getRepositoryAlias(),
                                               elem.getSourceBranch(),
                                               elem.getTargetBranch(),
                                               elem.getStatus(),
                                               elem.getAuthorId(),
                                               elem.getSummary(),
                                               elem.getDescription(),
                                               elem.getCreatedDate(),
                                               countChangeRequestDiffs(repository, elem),
                                               countChangeRequestComments(repository, elem),
                                               elem.getCommonCommitId(),
                                               elem.getLastCommitId(),
                                               !isChangeRequestConflictFree(repository, elem)))
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

    private long generateCommentId(final String spaceName,
                                   final String repositoryAlias,
                                   final Long changeRequestId) {
        Optional<Long> maxId = spaceConfigStorageRegistry.get(spaceName)
                .getChangeRequestCommentIds(repositoryAlias,
                                            changeRequestId)
                .stream()
                .max(Long::compare);

        long nextId = 1L;

        if (maxId.isPresent()) {
            nextId = maxId.get() + 1;
        }

        return nextId;
    }

    private void updateChangeRequestStatus(final String spaceName,
                                           final String repositoryAlias,
                                           final ChangeRequest oldChangeRequest,
                                           final ChangeRequestStatus status) {
        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        final String lastCommitId =
                oldChangeRequest.getStatus() == ChangeRequestStatus.OPEN && status != ChangeRequestStatus.OPEN ?
                        getLastCommitId(repository, oldChangeRequest.getSourceBranch()) :
                        oldChangeRequest.getLastCommitId();

        final ChangeRequest updatedChangeRequest = new ChangeRequest(oldChangeRequest.getId(),
                                                                     oldChangeRequest.getSpaceName(),
                                                                     oldChangeRequest.getRepositoryAlias(),
                                                                     oldChangeRequest.getSourceBranch(),
                                                                     oldChangeRequest.getTargetBranch(),
                                                                     status,
                                                                     oldChangeRequest.getAuthorId(),
                                                                     oldChangeRequest.getSummary(),
                                                                     oldChangeRequest.getDescription(),
                                                                     oldChangeRequest.getCreatedDate(),
                                                                     oldChangeRequest.getCommonCommitId(),
                                                                     lastCommitId);

        spaceConfigStorageRegistry.get(spaceName).saveChangeRequest(repositoryAlias,
                                                                    updatedChangeRequest);

        changeRequestUpdatedEvent.fire(new ChangeRequestUpdatedEvent(repository.getIdentifier(),
                                                                     updatedChangeRequest.getId()));
    }

    private String getLastCommitId(final Repository repository,
                                   final String branchName) {
        final Branch branch = repository.getBranch(branchName)
                .orElseThrow(() -> new IllegalStateException("The branch " + branchName + " does not exist"));

        final Git git = getGitFromBranch(branch);

        final RevCommit lastCommit = git.getLastCommit(branchName);

        if (lastCommit != null) {
            return lastCommit.getName();
        }

        throw new IllegalStateException("The branch " + branchName + " does not have a last commit");
    }

    private String getCommonCommitId(final Repository repository,
                                     final String sourceBranchName,
                                     final String targetBranchName) {
        final Branch sourceBranch = repository.getBranch(sourceBranchName)
                .orElseThrow(() -> new IllegalStateException("The branch " + sourceBranchName + " does not exist"));

        repository.getBranch(targetBranchName)
                .orElseThrow(() -> new IllegalStateException("The branch " + targetBranchName + " does not exist"));

        final Git git = getGitFromBranch(sourceBranch);

        RevCommit commonAncestorCommit = null;

        try {
            commonAncestorCommit = git.getCommonAncestorCommit(sourceBranchName,
                                                               targetBranchName);
        } catch (GitException e) {
            logger.error(String.format("Failed to get common commit for branches %s and %s: %s",
                                       sourceBranch,
                                       targetBranchName,
                                       e));
        }

        return commonAncestorCommit != null ? commonAncestorCommit.getName() : null;
    }

    private List<ChangeRequestDiff> getDiff(final Repository repository,
                                            final String sourceBranchName,
                                            final String targetBranchName,
                                            final String commonCommitId,
                                            final String lastCommitId) {
        final Branch sourceBranch = repository.getBranch(sourceBranchName)
                .orElseThrow(() -> new IllegalStateException("The branch " + sourceBranchName + " does not exist"));

        final Branch targetBranch = repository.getBranch(targetBranchName)
                .orElseThrow(() -> new IllegalStateException("The branch " + targetBranchName + " does not exist"));

        final List<String> conflicts = getConflicts(repository,
                                                    sourceBranchName,
                                                    targetBranchName);

        return getTextualDiff(repository,
                              sourceBranchName,
                              targetBranchName,
                              commonCommitId,
                              lastCommitId)
                .stream()
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

    private List<TextualDiff> getTextualDiff(final Repository repository,
                                             final String sourceBranchName,
                                             final String targetBranchName,
                                             final String commonCommitId,
                                             final String commitId) {
        final Optional<Branch> sourceBranch = repository.getBranch(sourceBranchName);
        final Optional<Branch> targetBranch = repository.getBranch(targetBranchName);

        if (sourceBranch.isPresent() && targetBranch.isPresent()) {
            final Git git = getGitFromBranch(sourceBranch.get());

            return git.textualDiffRefs(targetBranchName,
                                       sourceBranchName,
                                       commonCommitId,
                                       commitId);
        }

        return Collections.emptyList();
    }

    private int countChangeRequestDiffs(final Repository repository,
                                        final ChangeRequest changeRequest) {
        return getTextualDiff(repository,
                              changeRequest.getSourceBranch(),
                              changeRequest.getTargetBranch(),
                              changeRequest.getCommonCommitId(),
                              changeRequest.getLastCommitId()).size();
    }

    private int countChangeRequestComments(final Repository repository,
                                           final ChangeRequest changeRequest) {
        return getComments(repository.getSpace().getName(),
                           repository.getAlias(),
                           changeRequest.getId()).size();
    }

    private boolean isChangeRequestDiffEmpty(final Repository repository,
                                             final ChangeRequest changeRequest) {
        return countChangeRequestDiffs(repository,
                                       changeRequest) == 0;
    }

    private boolean isChangeRequestConflictFree(final Repository repository,
                                                final ChangeRequest changeRequest) {
        return getConflicts(repository,
                            changeRequest.getSourceBranch(),
                            changeRequest.getTargetBranch()).size() == 0;
    }

    private List<String> getConflicts(final Repository repository,
                                      final String sourceBranchName,
                                      final String targetBranchName) {
        final Optional<Branch> sourceBranch = repository.getBranch(sourceBranchName);
        final Optional<Branch> targetBranch = repository.getBranch(targetBranchName);

        if (sourceBranch.isPresent() && targetBranch.isPresent()) {
            final Git git = getGitFromBranch(sourceBranch.get());

            return git.conflictBranchesChecker(targetBranchName,
                                               sourceBranchName);
        }

        return Collections.emptyList();
    }
}