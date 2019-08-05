/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import org.guvnor.structure.repositories.changerequest.ChangeRequestService;
import org.guvnor.structure.repositories.changerequest.portable.ChangeRequest;
import org.guvnor.structure.repositories.changerequest.portable.ChangeRequestAlreadyOpenException;
import org.guvnor.structure.repositories.changerequest.portable.ChangeRequestComment;
import org.guvnor.structure.repositories.changerequest.portable.ChangeRequestCountSummary;
import org.guvnor.structure.repositories.changerequest.portable.ChangeRequestDiff;
import org.guvnor.structure.repositories.changerequest.portable.ChangeRequestListUpdatedEvent;
import org.guvnor.structure.repositories.changerequest.portable.ChangeRequestStatus;
import org.guvnor.structure.repositories.changerequest.portable.ChangeRequestUpdatedEvent;
import org.guvnor.structure.repositories.changerequest.portable.ChangeType;
import org.guvnor.structure.repositories.changerequest.portable.PaginatedChangeRequestCommentList;
import org.guvnor.structure.repositories.changerequest.portable.PaginatedChangeRequestList;
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
import org.uberfire.java.nio.fs.jgit.util.model.CommitInfo;
import org.uberfire.java.nio.fs.jgit.util.model.RevertCommitContent;
import org.uberfire.rpc.SessionInfo;
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
    private final SessionInfo sessionInfo;

    private Logger logger = LoggerFactory.getLogger(ChangeRequestServiceImpl.class);

    @Inject
    public ChangeRequestServiceImpl(final SpaceConfigStorageRegistry spaceConfigStorageRegistry,
                                    final RepositoryService repositoryService,
                                    final SpacesAPI spaces,
                                    final Event<ChangeRequestListUpdatedEvent> changeRequestListUpdatedEvent,
                                    final Event<ChangeRequestUpdatedEvent> changeRequestUpdatedEvent,
                                    final BranchAccessAuthorizer branchAccessAuthorizer,
                                    final SessionInfo sessionInfo) {
        this.spaceConfigStorageRegistry = spaceConfigStorageRegistry;
        this.repositoryService = repositoryService;
        this.spaces = spaces;
        this.changeRequestListUpdatedEvent = changeRequestListUpdatedEvent;
        this.changeRequestUpdatedEvent = changeRequestUpdatedEvent;
        this.branchAccessAuthorizer = branchAccessAuthorizer;
        this.sessionInfo = sessionInfo;
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

        checkChangeRequestAlreadyOpen(spaceName,
                                      repositoryAlias,
                                      sourceBranch,
                                      targetBranch);

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
                                                                 sessionInfo.getIdentity().getIdentifier(),
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
    public PaginatedChangeRequestList getChangeRequests(final String spaceName,
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

        final List<ChangeRequest> paginatedChangeRequests = paginateChangeRequests(changeRequests,
                                                                                   page,
                                                                                   pageSize);

        return new PaginatedChangeRequestList(paginatedChangeRequests,
                                              page,
                                              pageSize,
                                              changeRequests.size());
    }

    @Override
    public PaginatedChangeRequestList getChangeRequests(final String spaceName,
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

        final List<ChangeRequest> changeRequests =
                getFilteredChangeRequests(spaceName,
                                          repositoryAlias,
                                          elem -> statusList.contains(elem.getStatus()) &&
                                                  composeSearchableElement(elem)
                                                          .contains(filter.toLowerCase()));

        final List<ChangeRequest> paginatedChangeRequests = paginateChangeRequests(changeRequests,
                                                                                   page,
                                                                                   pageSize);

        return new PaginatedChangeRequestList(paginatedChangeRequests,
                                              page,
                                              pageSize,
                                              changeRequests.size());
    }

    @Override
    public ChangeRequest getChangeRequest(final String spaceName,
                                          final String repositoryAlias,
                                          final Long changeRequestId) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);

        final List<ChangeRequest> changeRequests =
                this.getFilteredChangeRequests(spaceName,
                                               repositoryAlias,
                                               elem -> elem.getId() == changeRequestId);

        if (changeRequests.size() == 0) {
            throw new NoSuchElementException("The Change Request with ID #" + changeRequestId + " not found");
        }

        return changeRequests.get(0);
    }

    @Override
    public ChangeRequestCountSummary countChangeRequests(final String spaceName,
                                                         final String repositoryAlias) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);

        final List<ChangeRequest> changeRequests = getChangeRequests(spaceName,
                                                                     repositoryAlias);

        final Integer total = changeRequests.size();
        final long open = changeRequests.stream()
                .filter(elem -> elem.getStatus() == ChangeRequestStatus.OPEN)
                .count();

        return new ChangeRequestCountSummary(total,
                                             (int) open);
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

        final ChangeRequest changeRequest = this.getChangeRequest(spaceName,
                                                                  repositoryAlias,
                                                                  changeRequestId);

        if (changeRequest.getStatus() != ChangeRequestStatus.OPEN) {
            throw new IllegalStateException("Cannot reject a change request that is not opened");
        }

        this.updateChangeRequestStatus(spaceName,
                                       repositoryAlias,
                                       changeRequest,
                                       ChangeRequestStatus.REJECTED);
    }

    @Override
    public Boolean acceptChangeRequest(final String spaceName,
                                       final String repositoryAlias,
                                       final Long changeRequestId) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);

        final ChangeRequest changeRequest = this.getChangeRequest(spaceName,
                                                                  repositoryAlias,
                                                                  changeRequestId);

        if (changeRequest.getStatus() != ChangeRequestStatus.OPEN) {
            throw new IllegalStateException("Cannot accept a change request that is not opened");
        }

        final Repository repository = resolveRepository(spaceName,
                                                        repositoryAlias);

        boolean isDone = false;

        if (!isChangeRequestDiffEmpty(repository,
                                      changeRequest)) {
            isDone = trySquashAndMergeChangeRequest(repository,
                                                    changeRequest);
        }

        if (isDone) {
            this.updateChangeRequestStatus(spaceName,
                                           repositoryAlias,
                                           changeRequest,
                                           ChangeRequestStatus.ACCEPTED);
        }

        return isDone;
    }

    @Override
    public Boolean revertChangeRequest(final String spaceName,
                                       final String repositoryAlias,
                                       final Long changeRequestId) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);

        final ChangeRequest changeRequest = this.getChangeRequest(spaceName,
                                                                  repositoryAlias,
                                                                  changeRequestId);

        if (changeRequest.getStatus() != ChangeRequestStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot revert a change request that is not accepted");
        }

        final Repository repository = resolveRepository(spaceName,
                                                        repositoryAlias);

        boolean isDone = tryRevertChangeRequest(repository,
                                                changeRequest);

        this.updateChangeRequestStatus(spaceName,
                                       repositoryAlias,
                                       changeRequest,
                                       isDone ? ChangeRequestStatus.REVERTED : ChangeRequestStatus.REVERT_FAILED);

        return isDone;
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
    public PaginatedChangeRequestCommentList getComments(final String spaceName,
                                                         final String repositoryAlias,
                                                         final Long changeRequestId,
                                                         final Integer page,
                                                         final Integer pageSize) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);

        final List<ChangeRequestComment> comments =
                spaceConfigStorageRegistry.get(spaceName).loadChangeRequestComments(repositoryAlias,
                                                                                    changeRequestId)
                        .stream()
                        .sorted(Comparator.comparing(ChangeRequestComment::getCreatedDate).reversed())
                        .collect(Collectors.toList());

        final List<ChangeRequestComment> paginatedList = paginateComments(comments,
                                                                          page,
                                                                          pageSize);

        return new PaginatedChangeRequestCommentList(paginatedList,
                                                     page,
                                                     pageSize,
                                                     comments.size());
    }

    @Override
    public Integer countChangeRequestComments(final String spaceName,
                                              final String repositoryAlias,
                                              final Long changeRequestId) {
        checkNotEmpty("spaceName", spaceName);
        checkNotEmpty("repositoryAlias", repositoryAlias);
        checkNotNull("changeRequestId", changeRequestId);

        return spaceConfigStorageRegistry.get(spaceName).getChangeRequestCommentIds(repositoryAlias,
                                                                                    changeRequestId).size();
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
                                                                         sessionInfo.getIdentity().getIdentifier(),
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

    private org.uberfire.backend.vfs.Path createPath(final String branchPath,
                                                     final String filePath) {
        return PathFactory.newPath(filePath,
                                   branchPath + filePath);
    }

    private Repository resolveRepository(final String spaceName,
                                         final String repositoryAlias) {
        Repository repository = repositoryService.getRepositoryFromSpace(spaces.getSpace(spaceName), repositoryAlias);

        if (repository == null) {
            final String msg = String.format("The repository %s was not found in the space %s",
                                             repositoryAlias,
                                             spaceName);

            throw new NoSuchElementException(msg);
        }

        return repository;
    }

    private List<ChangeRequest> getFilteredChangeRequests(final String spaceName,
                                                          final String repositoryAlias,
                                                          final Predicate<? super ChangeRequest> filter) {
        final Repository repository = resolveRepository(spaceName, repositoryAlias);

        return spaceConfigStorageRegistry.get(spaceName).loadChangeRequests(repositoryAlias)
                .stream()
                .filter(elem -> branchAccessAuthorizer.authorize(sessionInfo.getIdentity().getIdentifier(),
                                                                 spaceName,
                                                                 repository.getIdentifier(),
                                                                 repository.getAlias(),
                                                                 elem.getTargetBranch(),
                                                                 BranchAccessAuthorizer.AccessType.READ))
                .filter(filter)
                .sorted(Comparator.comparing(ChangeRequest::getCreatedDate).reversed())
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
                                               countChangeRequestComments(spaceName, repositoryAlias, elem.getId()),
                                               elem.getCommonCommitId(),
                                               elem.getLastCommitId(),
                                               !isChangeRequestConflictFree(repository, elem)))
                .collect(Collectors.toList());
    }

    private String composeSearchableElement(final ChangeRequest element) {
        return ("#" + element.getId() + " " + element.getSummary()).toLowerCase();
    }

    private List<ChangeRequest> paginateChangeRequests(final List<ChangeRequest> changeRequests,
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

    private List<ChangeRequestComment> paginateComments(final List<ChangeRequestComment> comments,
                                                        final Integer page,
                                                        final Integer pageSize) {
        if (page == 0 && pageSize == 0) {
            return comments;
        }

        final Map<Integer, List<ChangeRequestComment>> map = IntStream.iterate(0,
                                                                               i -> i + pageSize)
                .limit((comments.size() + pageSize - 1) / pageSize)
                .boxed()
                .collect(toMap(i -> i / pageSize,
                               i -> comments.subList(i,
                                                     min(i + pageSize,
                                                         comments.size()))));

        List<ChangeRequestComment> paginatedComments = new ArrayList<>();

        if (map.containsKey(page)) {
            paginatedComments.addAll(map.get(page));
        }

        return paginatedComments;
    }

    private long generateChangeRequestId(final String spaceName,
                                         final String repositoryAlias) {
        Optional<Long> maxId = spaceConfigStorageRegistry.get(spaceName)
                .getChangeRequestIds(repositoryAlias)
                .stream()
                .max(Long::compare);

        return maxId.orElse(0L) + 1;
    }

    private long generateCommentId(final String spaceName,
                                   final String repositoryAlias,
                                   final Long changeRequestId) {
        Optional<Long> maxId = spaceConfigStorageRegistry.get(spaceName)
                .getChangeRequestCommentIds(repositoryAlias,
                                            changeRequestId)
                .stream()
                .max(Long::compare);

        return maxId.orElse(0L) + 1;
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
        final Git git = getGitFromBranch(repository,
                                         branchName);

        final RevCommit lastCommit = git.getLastCommit(branchName);

        if (lastCommit != null) {
            return lastCommit.getName();
        }

        throw new IllegalStateException("The branch " + branchName + " does not have a last commit");
    }

    private String getCommonCommitId(final Repository repository,
                                     final String sourceBranchName,
                                     final String targetBranchName) {
        final Git git = getGitFromBranch(repository,
                                         sourceBranchName);

        RevCommit commonAncestorCommit = null;

        try {
            commonAncestorCommit = git.getCommonAncestorCommit(sourceBranchName,
                                                               targetBranchName);
        } catch (GitException e) {
            logger.error(String.format("Failed to get common commit for branches %s and %s: %s",
                                       sourceBranchName,
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
            final Git git = getGitFromBranch(repository,
                                             sourceBranchName);

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
            final Git git = getGitFromBranch(repository,
                                             sourceBranchName);

            return git.conflictBranchesChecker(targetBranchName,
                                               sourceBranchName);
        }

        return Collections.emptyList();
    }

    private CommitInfo createRevertCommitInfo(final String message) {
        return new CommitInfo(sessionInfo.getId(),
                              sessionInfo.getIdentity().getIdentifier(),
                              sessionInfo.getIdentity().getProperty(User.StandardUserProperties.EMAIL),
                              message,
                              null,
                              new Date());
    }

    private boolean trySquashAndMergeChangeRequest(final Repository repository,
                                                   final ChangeRequest changeRequest) {

        final String sourceBranchName = changeRequest.getSourceBranch();
        final String targetBranchName = changeRequest.getTargetBranch();

        final Git git = getGitFromBranch(repository,
                                         sourceBranchName);

        boolean isDone = false;

        try {
            final String lastCommitId = getLastCommitId(repository,
                                                        sourceBranchName);

            final List<RevCommit> commits = git.listCommits(changeRequest.getCommonCommitId(),
                                                            lastCommitId);

            if (!commits.isEmpty()) {
                final String startCommitId = commits.get(commits.size() - 1).getName();
                final String commitMsg = String.format("Squash & Merge - change request #%s",
                                                       changeRequest.getId());
                git.squash(sourceBranchName,
                           startCommitId,
                           commitMsg);

                git.merge(sourceBranchName,
                          targetBranchName);

                isDone = true;
            }
        } catch (GitException e) {
            logger.debug(String.format("Cannot merge change request %s: %s", changeRequest.getId(), e));
        }

        return isDone;
    }

    private boolean tryRevertChangeRequest(final Repository repository,
                                           final ChangeRequest changeRequest) {
        final String targetBranchName = changeRequest.getTargetBranch();

        final String lastTargetCommitId = getLastCommitId(repository,
                                                          targetBranchName);

        boolean isDone = false;

        if (changeRequest.getLastCommitId().equals(lastTargetCommitId)) {
            final Git git = getGitFromBranch(repository,
                                             targetBranchName);

            try {
                final RevCommit startCommit = getFirstCommitParent(git.getLastCommit(targetBranchName));
                final String commitMsg = String.format("Revert - change request #%s", changeRequest.getId());

                isDone = git.commit(targetBranchName,
                                    createRevertCommitInfo(commitMsg),
                                    false,
                                    startCommit,
                                    new RevertCommitContent(targetBranchName));

                if (isDone) {
                    fixSourceBranchAfterRevert(changeRequest.getId(),
                                               git,
                                               changeRequest.getSourceBranch(),
                                               targetBranchName);
                }
            } catch (Exception e) {
                logger.debug(String.format("Failed to revert change request #%s: %s.",
                                           changeRequest.getId(),
                                           e));
            }
        }

        return isDone;
    }

    private void fixSourceBranchAfterRevert(final Long changeRequestId,
                                            final Git git,
                                            final String sourceBranchName,
                                            final String targetBranchName) {
        final String tempBranchName = String.format("%s_%s_cr%s_revert_fix",
                                                    targetBranchName,
                                                    sourceBranchName,
                                                    changeRequestId);

        try {
            git.createRef(targetBranchName,
                          tempBranchName);

            final RevCommit startCommit = getFirstCommitParent(git.getLastCommit(tempBranchName));
            final String commitMsg = String.format("Revert fix - change request #%s", changeRequestId);

            git.commit(tempBranchName,
                       createRevertCommitInfo(commitMsg),
                       false,
                       startCommit,
                       new RevertCommitContent(tempBranchName));

            git.merge(tempBranchName,
                      sourceBranchName);
        } catch (Exception e) {
            logger.error(String.format("Unable to fix source branch %s after reverting change request #%s: %s",
                                       sourceBranchName,
                                       changeRequestId,
                                       e));
        } finally {
            git.deleteRef(git.getRef(tempBranchName));
        }
    }

    private void checkChangeRequestAlreadyOpen(final String spaceName,
                                               final String repositoryAlias,
                                               final String sourceBranchName,
                                               final String targetBranchName) {
        final List<ChangeRequest> changeRequests =
                getFilteredChangeRequests(spaceName,
                                          repositoryAlias,
                                          elem -> elem.getSourceBranch().equals(sourceBranchName)
                                                  && elem.getTargetBranch().equals(targetBranchName)
                                                  && elem.getStatus() == ChangeRequestStatus.OPEN);

        if (!changeRequests.isEmpty()) {
            throw new ChangeRequestAlreadyOpenException(changeRequests.get(0).getId());
        }
    }

    Git getGitFromBranch(final Repository repository,
                         final String branchName) {
        final Branch branch = repository.getBranch(branchName)
                .orElseThrow(() -> new IllegalStateException("The branch " + branchName + " does not exist"));

        return ((JGitPathImpl) Paths.convert(branch.getPath())).getFileSystem().getGit();
    }

    RevCommit getFirstCommitParent(final RevCommit commit) {
        return commit.getParent(0);
    }
}