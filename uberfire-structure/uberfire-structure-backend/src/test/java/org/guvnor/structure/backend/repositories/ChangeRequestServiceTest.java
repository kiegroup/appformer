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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.enterprise.event.Event;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.guvnor.structure.organizationalunit.config.SpaceConfigStorage;
import org.guvnor.structure.organizationalunit.config.SpaceConfigStorageRegistry;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.repositories.changerequest.ChangeRequest;
import org.guvnor.structure.repositories.changerequest.ChangeRequestComment;
import org.guvnor.structure.repositories.changerequest.ChangeRequestDiff;
import org.guvnor.structure.repositories.changerequest.ChangeRequestListUpdatedEvent;
import org.guvnor.structure.repositories.changerequest.ChangeRequestStatus;
import org.guvnor.structure.repositories.changerequest.ChangeRequestUpdatedEvent;
import org.jboss.errai.security.shared.api.identity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.java.nio.base.TextualDiff;
import org.uberfire.java.nio.fs.jgit.util.Git;
import org.uberfire.java.nio.fs.jgit.util.exceptions.GitException;
import org.uberfire.java.nio.fs.jgit.util.model.CommitInfo;
import org.uberfire.java.nio.fs.jgit.util.model.RevertCommitContent;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.spaces.Space;
import org.uberfire.spaces.SpacesAPI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChangeRequestServiceTest {

    private ChangeRequestServiceImpl service;

    @Mock
    private SpaceConfigStorageRegistry spaceConfigStorageRegistry;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private SpacesAPI spaces;

    @Mock
    private Event<ChangeRequestListUpdatedEvent> changeRequestListUpdatedEvent;

    @Mock
    private Event<ChangeRequestUpdatedEvent> changeRequestUpdatedEvent;

    @Mock
    private BranchAccessAuthorizer branchAccessAuthorizer;

    @Mock
    private SessionInfo sessionInfo;

    @Mock
    private SpaceConfigStorage spaceConfigStorage;

    @Mock
    private Repository repository;

    @Mock
    private Branch sourceBranch;

    @Mock
    private Branch targetBranch;

    @Mock
    private Branch hiddenBranch;

    @Mock
    private Git git;

    @Mock
    private RevCommit commonCommit;

    @Mock
    private RevCommit lastCommit;

    @Before
    public void setUp() {
        Space mySpace = mock(Space.class);

        User user = mock(User.class);

        doReturn(user).when(sessionInfo).getIdentity();
        doReturn("authorId").when(user).getIdentifier();

        doReturn(spaceConfigStorage).when(spaceConfigStorageRegistry).get("mySpace");
        doReturn(mySpace).when(spaces).getSpace("mySpace");
        doReturn(repository).when(repositoryService).getRepositoryFromSpace(mySpace,
                                                                            "myRepository");

        doReturn("myRepository").when(repository).getAlias();
        doReturn(mySpace).when(repository).getSpace();
        doReturn("mySpace").when(mySpace).getName();

        doReturn(Optional.of(sourceBranch)).when(repository).getBranch("sourceBranch");
        doReturn(Optional.of(targetBranch)).when(repository).getBranch("targetBranch");
        doReturn(Optional.of(hiddenBranch)).when(repository).getBranch("hiddenBranch");

        doReturn(commonCommit).when(git).getCommonAncestorCommit("sourceBranch",
                                                                 "targetBranch");

        doReturn(lastCommit).when(git).getLastCommit(anyString());

        doReturn(true).when(branchAccessAuthorizer).authorize(anyString(),
                                                              anyString(),
                                                              anyString(),
                                                              anyString(),
                                                              anyString(),
                                                              any());

        this.service = spy(new ChangeRequestServiceImpl(spaceConfigStorageRegistry,
                                                        repositoryService,
                                                        spaces,
                                                        changeRequestListUpdatedEvent,
                                                        changeRequestUpdatedEvent,
                                                        branchAccessAuthorizer,
                                                        sessionInfo));

        doReturn(git).when(service).getGitFromBranch(repository, "sourceBranch");
        doReturn(git).when(service).getGitFromBranch(repository, "targetBranch");
        doReturn(git).when(service).getGitFromBranch(repository, "hiddenBranch");
    }

    @Test
    public void createFirstChangeRequestTest() {
        doReturn(Collections.emptyList()).when(spaceConfigStorage).getChangeRequestIds("myRepository");

        ChangeRequest newChangeRequest = service.createChangeRequest("mySpace",
                                                                     "myRepository",
                                                                     "sourceBranch",
                                                                     "targetBranch",
                                                                     "summary",
                                                                     "description");

        assertThat(newChangeRequest.getId()).isEqualTo(1L);
        verify(spaceConfigStorageRegistry.get("mySpace")).saveChangeRequest("myRepository",
                                                                            newChangeRequest);
        verify(changeRequestListUpdatedEvent).fire(any(ChangeRequestListUpdatedEvent.class));
    }

    @Test
    public void createChangeRequestTest() {
        List<Long> ids = Arrays.asList(1L, 10L, 2L, 3L, 4L);
        doReturn(ids).when(spaceConfigStorage).getChangeRequestIds("myRepository");

        ChangeRequest newChangeRequest = service.createChangeRequest("mySpace",
                                                                     "myRepository",
                                                                     "sourceBranch",
                                                                     "targetBranch",
                                                                     "summary",
                                                                     "description");

        assertThat(newChangeRequest.getId()).isEqualTo(11L);
        assertThat(newChangeRequest.getStatus()).isEqualTo(ChangeRequestStatus.OPEN);
        verify(spaceConfigStorageRegistry.get("mySpace")).saveChangeRequest("myRepository",
                                                                            newChangeRequest);
        verify(changeRequestListUpdatedEvent).fire(any(ChangeRequestListUpdatedEvent.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void createChangeRequestInvalidRepositoryTest() {
        service.createChangeRequest("mySpace",
                                    "myOtherRepository",
                                    "sourceBranch",
                                    "targetBranch",
                                    "summary",
                                    "description");
    }

    @Test
    public void getChangeRequestsTest() {
        List<ChangeRequest> crList = Collections.nCopies(5, createCommonChangeRequest());

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");
        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(5);
    }

    @Test
    public void getChangeRequestUserCannotAccessBranchesTest() {
        doReturn(false).when(branchAccessAuthorizer).authorize(anyString(),
                                                               anyString(),
                                                               anyString(),
                                                               anyString(),
                                                               anyString(),
                                                               any());

        List<ChangeRequest> crList = Collections.nCopies(5, mock(ChangeRequest.class));

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");
        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository");

        assertThat(actualList).isEmpty();
    }

    @Test
    public void getChangeRequestUserCanAccessSomeBranchesTest() {
        doReturn(false).when(branchAccessAuthorizer).authorize(anyString(),
                                                               anyString(),
                                                               anyString(),
                                                               anyString(),
                                                               eq("hiddenBranch"),
                                                               any());

        doReturn(true).when(branchAccessAuthorizer).authorize(anyString(),
                                                              anyString(),
                                                              anyString(),
                                                              anyString(),
                                                              eq("branch"),
                                                              any());

        ChangeRequest cr1 = createCommonChangeRequestWithTargetBranch("hiddenBranch");
        ChangeRequest cr2 = createCommonChangeRequestWithTargetBranch("hiddenBranch");
        ChangeRequest cr3 = createCommonChangeRequestWithTargetBranch("targetBranch");
        ChangeRequest cr4 = createCommonChangeRequestWithTargetBranch("targetBranch");

        List<ChangeRequest> crList = Arrays.asList(cr1, cr2, cr3, cr4);
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(2);
    }

    @Test
    public void getChangeRequestsWithFilterTest() {
        ChangeRequest cr1 = createCommonChangeRequestWithSummary("findme");
        ChangeRequest cr2 = createCommonChangeRequestWithSummary("findme");
        ChangeRequest cr3 = createCommonChangeRequestWithSummary("hidden");
        ChangeRequest cr4 = createCommonChangeRequestWithSummary("hidden");

        List<ChangeRequest> crList = Arrays.asList(cr1, cr2, cr3, cr4);
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository",
                                                                   "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(2);
    }

    @Test
    public void getChangeRequestsWithStatusTest() {
        ChangeRequest cr1 = createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN);
        ChangeRequest cr2 = createCommonChangeRequestWithStatus(ChangeRequestStatus.REJECTED);
        ChangeRequest cr3 = createCommonChangeRequestWithStatus(ChangeRequestStatus.ACCEPTED);
        ChangeRequest cr4 = createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN);

        List<ChangeRequest> crList = Arrays.asList(cr1, cr2, cr3, cr4);
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");
        List<ChangeRequestStatus> statusList = new ArrayList<ChangeRequestStatus>() {{
            add(ChangeRequestStatus.OPEN);
        }};

        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository",
                                                                   statusList);

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(2);
    }

    @Test
    public void getChangeRequestsWithStatusAndFilterTest() {
        ChangeRequest cr1 = createCommonChangeRequestWithStatusSummary(ChangeRequestStatus.OPEN,
                                                                       "findme");
        ChangeRequest cr2 = createCommonChangeRequestWithStatusSummary(ChangeRequestStatus.REJECTED,
                                                                       "findme");
        ChangeRequest cr3 = createCommonChangeRequestWithStatusSummary(ChangeRequestStatus.ACCEPTED,
                                                                       "findme");
        ChangeRequest cr4 = createCommonChangeRequestWithStatusSummary(ChangeRequestStatus.OPEN,
                                                                       "findme");

        List<ChangeRequest> crList = Arrays.asList(cr1, cr2, cr3, cr4);
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");
        List<ChangeRequestStatus> statusList = new ArrayList<ChangeRequestStatus>() {{
            add(ChangeRequestStatus.OPEN);
        }};

        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository",
                                                                   statusList,
                                                                   "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(2);
    }

    @Test
    public void getChangeRequestsPaginatedWithFilterTest() {
        ChangeRequest crsWithFilter = createCommonChangeRequestWithStatusSummary(ChangeRequestStatus.OPEN,
                                                                                 "findme");

        ChangeRequest crsHidden = createCommonChangeRequestWithStatusSummary(ChangeRequestStatus.OPEN,
                                                                             "hidden");

        List<ChangeRequest> crList = new ArrayList<ChangeRequest>() {{
            addAll(Collections.nCopies(26, crsWithFilter));
            addAll(Collections.nCopies(30, crsHidden));
        }};

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository",
                                                                   0,
                                                                   10,
                                                                   "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(10);

        actualList = service.getChangeRequests("mySpace",
                                               "myRepository",
                                               1,
                                               10,
                                               "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(10);

        actualList = service.getChangeRequests("mySpace",
                                               "myRepository",
                                               2,
                                               10,
                                               "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(6);
    }

    @Test
    public void getChangeRequestsPaginatedWithStatusAndFilterTest() {
        ChangeRequest crsWithStatusAndFilter = createCommonChangeRequestWithStatusSummary(ChangeRequestStatus.ACCEPTED,
                                                                                          "findme");

        ChangeRequest crsOnlyFilter = createCommonChangeRequestWithSummary("findme");

        ChangeRequest crsOnlyStatus = createCommonChangeRequestWithStatus(ChangeRequestStatus.ACCEPTED);

        ChangeRequest crsHidden = createCommonChangeRequestWithSummary("hidden");

        List<ChangeRequest> crList = new ArrayList<ChangeRequest>() {{
            addAll(Collections.nCopies(20, crsOnlyStatus));
            addAll(Collections.nCopies(26, crsWithStatusAndFilter));
            addAll(Collections.nCopies(20, crsOnlyFilter));
            addAll(Collections.nCopies(30, crsHidden));
        }};
        List<ChangeRequestStatus> statusList = new ArrayList<ChangeRequestStatus>() {{
            add(ChangeRequestStatus.ACCEPTED);
        }};

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository",
                                                                   0,
                                                                   10,
                                                                   statusList,
                                                                   "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(10);

        actualList = service.getChangeRequests("mySpace",
                                               "myRepository",
                                               1,
                                               10,
                                               statusList,
                                               "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(10);

        actualList = service.getChangeRequests("mySpace",
                                               "myRepository",
                                               2,
                                               10,
                                               statusList,
                                               "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(6);
    }

    @Test
    public void getChangeRequestTest() {
        ChangeRequest cr1 = createCommonChangeRequestWithId(1L);
        ChangeRequest cr2 = createCommonChangeRequestWithId(2L);
        ChangeRequest cr3 = createCommonChangeRequestWithId(3L);
        ChangeRequest cr4 = createCommonChangeRequestWithId(4L);

        List<ChangeRequest> crList = Arrays.asList(cr1, cr2, cr3, cr4);
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        ChangeRequest actual = service.getChangeRequest("mySpace",
                                                        "myRepository",
                                                        3L);

        assertThat(actual.getId()).isEqualTo(3L);
    }

    @Test(expected = NoSuchElementException.class)
    public void getChangeRequestNotFoundTest() {
        service.getChangeRequest("mySpace",
                                 "myRepository",
                                 10L);
    }

    @Test
    public void countChangeRequestsTest() {
        List<ChangeRequest> crList = Collections.nCopies(15, createCommonChangeRequest());
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        int count = service.countChangeRequests("mySpace",
                                                "myRepository");

        assertEquals(15, count);
    }

    @Test
    public void countChangeRequestsWithStatusTest() {
        ChangeRequest crsWithStatus = createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN);

        ChangeRequest crsHidden = mock(ChangeRequest.class);
        doReturn(ChangeRequestStatus.REJECTED).when(crsHidden).getStatus();

        List<ChangeRequest> crList = new ArrayList<ChangeRequest>() {{
            addAll(Collections.nCopies(26, crsWithStatus));
            addAll(Collections.nCopies(30, crsHidden));
        }};

        List<ChangeRequestStatus> statusList = new ArrayList<ChangeRequestStatus>() {{
            add(ChangeRequestStatus.OPEN);
        }};

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        int count = service.countChangeRequests("mySpace",
                                                "myRepository",
                                                statusList);

        assertEquals(26, count);
    }

    @Test
    public void countChangeRequestsWithFilterTest() {
        ChangeRequest crsWithFilter = createCommonChangeRequestWithSummary("findme");

        ChangeRequest crsHidden = createCommonChangeRequestWithSummary("hidden");

        List<ChangeRequest> crList = new ArrayList<ChangeRequest>() {{
            addAll(Collections.nCopies(26, crsWithFilter));
            addAll(Collections.nCopies(30, crsHidden));
        }};

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        int count = service.countChangeRequests("mySpace",
                                                "myRepository",
                                                "me");

        assertEquals(26, count);
    }

    @Test
    public void countChangeRequestsWithStatusAndFilterTest() {
        ChangeRequest crsWithFilter = createCommonChangeRequestWithSummary("findme");

        ChangeRequest crsWithStatus = createCommonChangeRequestWithStatus(ChangeRequestStatus.REJECTED);

        ChangeRequest crsWithStatusAndFilter = createCommonChangeRequestWithStatusSummary(ChangeRequestStatus.REJECTED,
                                                                                          "findme");

        ChangeRequest crsHidden = createCommonChangeRequestWithSummary("hidden");

        List<ChangeRequest> crList = new ArrayList<ChangeRequest>() {{
            addAll(Collections.nCopies(26, crsWithFilter));
            addAll(Collections.nCopies(5, crsWithStatus));
            addAll(Collections.nCopies(30, crsHidden));
            addAll(Collections.nCopies(18, crsWithStatusAndFilter));
        }};

        List<ChangeRequestStatus> statusList = new ArrayList<ChangeRequestStatus>() {{
            add(ChangeRequestStatus.REJECTED);
        }};

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        int count = service.countChangeRequests("mySpace",
                                                "myRepository",
                                                statusList,
                                                "me");

        assertEquals(18, count);
    }

    @Test
    public void countChangeRequestCommentsTest() {
        List<ChangeRequestComment> comments = Collections.nCopies(15, mock(ChangeRequestComment.class));
        doReturn(comments).when(spaceConfigStorage).getChangeRequestCommentIds("myRepository", 1L);

        int count = service.countChangeRequestComments("mySpace",
                                                       "myRepository",
                                                       1L);

        assertEquals(15, count);
    }

    @Test
    public void countChangeRequestCommentsEmptyListTest() {
        List<ChangeRequestComment> comments = Collections.nCopies(15, mock(ChangeRequestComment.class));
        doReturn(comments).when(spaceConfigStorage).getChangeRequestCommentIds("myRepository", 2L);

        int count = service.countChangeRequestComments("mySpace",
                                                       "myRepository",
                                                       1L);

        assertEquals(0, count);
    }

    @Test
    public void getDiffTestNoResultsTest() {
        doReturn(Collections.emptyList()).when(git).conflictBranchesChecker(anyString(),
                                                                            anyString());
        doReturn(Collections.emptyList()).when(git).textualDiffRefs(anyString(),
                                                                    anyString());

        List<ChangeRequestDiff> diffs = service.getDiff("mySpace",
                                                        "myRepository",
                                                        "sourceBranch",
                                                        "targetBranch");

        assertThat(diffs).isEmpty();
    }

    @Test
    public void getDiffTestNoResultsForChangeRequestTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequest());
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        doReturn(Collections.emptyList()).when(git).conflictBranchesChecker(anyString(),
                                                                            anyString());
        doReturn(Collections.emptyList()).when(git).textualDiffRefs(anyString(),
                                                                    anyString());

        List<ChangeRequestDiff> diffs = service.getDiff("mySpace",
                                                        "myRepository",
                                                        1L);

        assertThat(diffs).isEmpty();
    }

    @Test
    public void getDiffTestWithResultsTest() {
        doReturn(mock(Path.class)).when(sourceBranch).getPath();

        doReturn(mock(Path.class)).when(targetBranch).getPath();

        TextualDiff textualDiff = new TextualDiff("old/file/path",
                                                  "new/file/path",
                                                  "ADD",
                                                  10,
                                                  10,
                                                  "diff text");

        List<TextualDiff> diffList = Collections.nCopies(10, textualDiff);

        doReturn(Collections.emptyList()).when(git).conflictBranchesChecker(anyString(),
                                                                            anyString());
        doReturn(diffList).when(git).textualDiffRefs(anyString(),
                                                     anyString(),
                                                     anyString(),
                                                     anyString());
        List<ChangeRequestDiff> diffs = service.getDiff("mySpace",
                                                        "myRepository",
                                                        "sourceBranch",
                                                        "targetBranch");

        assertThat(diffs).isNotEmpty();
        assertThat(diffs).hasSize(10);
    }

    @Test
    public void getDiffTestWithResultsForChangeRequestTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequest());
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        doReturn(mock(Path.class)).when(sourceBranch).getPath();

        doReturn(mock(Path.class)).when(targetBranch).getPath();

        TextualDiff textualDiff = new TextualDiff("old/file/path",
                                                  "new/file/path",
                                                  "ADD",
                                                  10,
                                                  10,
                                                  "diff text");

        List<TextualDiff> diffList = Collections.nCopies(10, textualDiff);

        doReturn(Collections.emptyList()).when(git).conflictBranchesChecker(anyString(),
                                                                            anyString());
        doReturn(diffList).when(git).textualDiffRefs(anyString(),
                                                     anyString(),
                                                     anyString(),
                                                     anyString());
        List<ChangeRequestDiff> diffs = service.getDiff("mySpace",
                                                        "myRepository",
                                                        1L);

        assertThat(diffs).isNotEmpty();
        assertThat(diffs).hasSize(10);
    }

    @Test(expected = IllegalStateException.class)
    public void getDiffTestInvalidBranchTest() {
        doReturn(Optional.ofNullable(null)).when(repository).getBranch("branchA");

        service.getDiff("mySpace",
                        "myRepository",
                        "branchA",
                        "branchB");
    }

    @Test(expected = NoSuchElementException.class)
    public void getDiffTestInvalidChangeRequestTest() {
        service.getDiff("mySpace",
                        "myRepository",
                        10L);
    }

    @Test
    public void deleteChangeRequestsTest() {
        ChangeRequest crs = createCommonChangeRequestWithTargetBranch("hiddenBranch");
        List<ChangeRequest> crList = Collections.nCopies(10, crs);

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.deleteChangeRequests("mySpace",
                                     "myRepository",
                                     "sourceBranch");

        verify(spaceConfigStorage, times(10)).deleteChangeRequest(anyString(),
                                                                  anyLong());
    }

    @Test
    public void deleteChangeRequestsSomeTest() {
        ChangeRequest crsSourceBranch = createCommonChangeRequestWithSourceTargetBranch("branch",
                                                                                        "hiddenBranch");

        ChangeRequest crsTargetBranch = createCommonChangeRequestWithSourceTargetBranch("hiddenBranch",
                                                                                        "branch");

        ChangeRequest crsHidden = createCommonChangeRequestWithSourceTargetBranch("hiddenBranch",
                                                                                  "hiddenBranch");

        doReturn(Optional.of(mock(Branch.class))).when(repository).getBranch("branch");
        doReturn(git).when(service).getGitFromBranch(repository, "branch");

        List<ChangeRequest> crList = new ArrayList<ChangeRequest>() {{
            addAll(Collections.nCopies(10, crsSourceBranch));
            addAll(Collections.nCopies(20, crsTargetBranch));
            addAll(Collections.nCopies(15, crsHidden));
        }};

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.deleteChangeRequests("mySpace",
                                     "myRepository",
                                     "branch");

        verify(spaceConfigStorage, times(30)).deleteChangeRequest(anyString(),
                                                                  anyLong());
    }

    @Test
    public void deleteChangeRequestsNoneTest() {
        ChangeRequest crs = createCommonChangeRequestWithSourceTargetBranch("hiddenBranch",
                                                                            "hiddenBranch");

        List<ChangeRequest> crList = Collections.nCopies(10, crs);

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.deleteChangeRequests("mySpace",
                                     "myRepository",
                                     "branch");

        verify(spaceConfigStorage, never()).deleteChangeRequest(anyString(),
                                                                anyLong());
    }

    @Test
    public void rejectChangeRequestTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.rejectChangeRequest("mySpace",
                                    "myRepository",
                                    1L);
        verify(spaceConfigStorage).saveChangeRequest(eq("myRepository"),
                                                     any(ChangeRequest.class));
        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));
    }

    @Test(expected = IllegalStateException.class)
    public void rejectChangeRequestWhenChangeRequestNotOpenTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.ACCEPTED));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.rejectChangeRequest("mySpace",
                                    "myRepository",
                                    1L);
    }

    @Test
    public void acceptChangeRequestCannotMergeWithSquashTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        TextualDiff textualDiff = new TextualDiff("old/file/path",
                                                  "new/file/path",
                                                  "ADD",
                                                  10,
                                                  10,
                                                  "diff text");

        List<TextualDiff> diffList = Collections.nCopies(10, textualDiff);

        doReturn(Collections.emptyList()).when(git).conflictBranchesChecker(anyString(),
                                                                            anyString());
        doReturn(diffList).when(git).textualDiffRefs(anyString(),
                                                     anyString(),
                                                     anyString(),
                                                     anyString());

        List<RevCommit> commits = Collections.nCopies(1, mock(RevCommit.class));
        doReturn(commits).when(git).listCommits(anyString(),
                                                anyString());

        doThrow(GitException.class).when(git).merge(anyString(),
                                                    anyString());

        boolean result = service.acceptChangeRequest("mySpace",
                                                     "myRepository",
                                                     1L);

        verify(git).merge(anyString(),
                          anyString());

        verify(spaceConfigStorage, never()).saveChangeRequest(eq("myRepository"),
                                                              any(ChangeRequest.class));

        verify(changeRequestUpdatedEvent, never()).fire(any(ChangeRequestUpdatedEvent.class));

        assertFalse(result);
    }

    @Test
    public void acceptChangeRequestSuccessWithSquashManyCommitsTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        TextualDiff textualDiff = new TextualDiff("old/file/path",
                                                  "new/file/path",
                                                  "ADD",
                                                  10,
                                                  10,
                                                  "diff text");

        List<TextualDiff> diffList = Collections.nCopies(10, textualDiff);

        doReturn(Collections.emptyList()).when(git).conflictBranchesChecker(anyString(),
                                                                            anyString());
        doReturn(diffList).when(git).textualDiffRefs(anyString(),
                                                     anyString(),
                                                     anyString(),
                                                     anyString());

        List<RevCommit> commits = Collections.nCopies(3, mock(RevCommit.class));
        doReturn(commits).when(git).listCommits(anyString(),
                                                anyString());

        boolean result = service.acceptChangeRequest("mySpace",
                                                     "myRepository",
                                                     1L);

        verify(git).squash(anyString(),
                           anyString(),
                           anyString());

        verify(git).merge(anyString(),
                          anyString());

        verify(spaceConfigStorage).saveChangeRequest(eq("myRepository"),
                                                     any(ChangeRequest.class));

        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));

        assertTrue(result);
    }

    @Test
    public void acceptChangeRequestSuccessWithSquashOneCommitTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        TextualDiff textualDiff = new TextualDiff("old/file/path",
                                                  "new/file/path",
                                                  "ADD",
                                                  10,
                                                  10,
                                                  "diff text");

        List<TextualDiff> diffList = Collections.nCopies(10, textualDiff);

        doReturn(Collections.emptyList()).when(git).conflictBranchesChecker(anyString(),
                                                                            anyString());
        doReturn(diffList).when(git).textualDiffRefs(anyString(),
                                                     anyString(),
                                                     anyString(),
                                                     anyString());

        List<RevCommit> commits = Collections.nCopies(1, mock(RevCommit.class));
        doReturn(commits).when(git).listCommits(anyString(),
                                                anyString());

        boolean result = service.acceptChangeRequest("mySpace",
                                                     "myRepository",
                                                     1L);

        verify(git).squash(anyString(),
                           anyString(),
                           anyString());

        verify(git).merge(anyString(),
                          anyString());

        verify(spaceConfigStorage).saveChangeRequest(eq("myRepository"),
                                                     any(ChangeRequest.class));

        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));

        assertTrue(result);
    }

    @Test
    public void acceptChangeRequestWhenEmptyCommitsTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        TextualDiff textualDiff = new TextualDiff("old/file/path",
                                                  "new/file/path",
                                                  "ADD",
                                                  10,
                                                  10,
                                                  "diff text");

        List<TextualDiff> diffList = Collections.nCopies(10, textualDiff);

        doReturn(Collections.emptyList()).when(git).conflictBranchesChecker(anyString(),
                                                                            anyString());
        doReturn(diffList).when(git).textualDiffRefs(anyString(),
                                                     anyString(),
                                                     anyString(),
                                                     anyString());

        doReturn(Collections.emptyList()).when(git).listCommits(anyString(),
                                                                anyString());

        boolean result = service.acceptChangeRequest("mySpace",
                                                     "myRepository",
                                                     1L);
        verify(spaceConfigStorage, never()).saveChangeRequest(eq("myRepository"),
                                                              any(ChangeRequest.class));
        verify(changeRequestUpdatedEvent, never()).fire(any(ChangeRequestUpdatedEvent.class));

        assertFalse(result);
    }

    @Test
    public void acceptChangeRequestWhenEmptyDiffsTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        boolean result = service.acceptChangeRequest("mySpace",
                                                     "myRepository",
                                                     1L);
        verify(spaceConfigStorage, never()).saveChangeRequest(eq("myRepository"),
                                                              any(ChangeRequest.class));
        verify(changeRequestUpdatedEvent, never()).fire(any(ChangeRequestUpdatedEvent.class));

        assertFalse(result);
    }

    @Test(expected = IllegalStateException.class)
    public void acceptChangeRequestWhenChangeRequestNotOpenTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.ACCEPTED));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.acceptChangeRequest("mySpace",
                                    "myRepository",
                                    1L);
    }

    @Test(expected = IllegalStateException.class)
    public void revertChangeRequestWhenChangeRequestNotAcceptedTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.REJECTED));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.revertChangeRequest("mySpace",
                                    "myRepository",
                                    1L);
    }

    @Test
    public void revertChangeRequestFailWhenNotLastCommitTest() {
        final String lastCommitId = "abcde12";
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatusLastCommitId(ChangeRequestStatus.ACCEPTED,
                                                                                                            lastCommitId));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        doReturn(Collections.nCopies(5, mock(RevCommit.class))).when(git).listCommits(anyString(),
                                                                                      anyString());

        boolean result = service.revertChangeRequest("mySpace",
                                                     "myRepository",
                                                     1L);

        verify(git, never()).commit(anyString(),
                                    any(CommitInfo.class),
                                    anyBoolean(),
                                    any(RevCommit.class),
                                    any(RevertCommitContent.class));

        verify(spaceConfigStorage).saveChangeRequest(eq("myRepository"),
                                                     any(ChangeRequest.class));

        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));

        assertFalse(result);
    }

    @Test
    public void revertChangeRequestSuccessTest() {
        final String lastCommitId = "0000000000000000000000000000000000000000";
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatusLastCommitId(ChangeRequestStatus.ACCEPTED,
                                                                                                            lastCommitId));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        RevCommit commit = mock(RevCommit.class);
        doReturn(commit).when(git).getLastCommit("targetBranch");
        doReturn(commit).when(service).getFirstCommitParent(any(RevCommit.class));

        doReturn(true).when(git).commit(eq("targetBranch"),
                                        any(CommitInfo.class),
                                        eq(false),
                                        any(RevCommit.class),
                                        any(RevertCommitContent.class));

        boolean result = service.revertChangeRequest("mySpace",
                                                     "myRepository",
                                                     1L);

        verify(git, times(2)).commit(anyString(),
                                     any(CommitInfo.class),
                                     anyBoolean(),
                                     any(RevCommit.class),
                                     any(RevertCommitContent.class));

        verify(spaceConfigStorage).saveChangeRequest(eq("myRepository"),
                                                     any(ChangeRequest.class));

        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));

        assertTrue(result);
    }

    @Test
    public void revertChangeRequestSuccessFixSourceTest() {
        final String lastCommitId = "0000000000000000000000000000000000000000";
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatusLastCommitId(ChangeRequestStatus.ACCEPTED,
                                                                                                            lastCommitId));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        RevCommit commit = mock(RevCommit.class);
        doReturn(commit).when(git).getLastCommit("targetBranch");
        doReturn(commit).when(service).getFirstCommitParent(any(RevCommit.class));

        doReturn(true).when(git).commit(eq("targetBranch"),
                                        any(CommitInfo.class),
                                        eq(false),
                                        any(RevCommit.class),
                                        any(RevertCommitContent.class));

        boolean result = service.revertChangeRequest("mySpace",
                                                     "myRepository",
                                                     1L);

        verify(spaceConfigStorage).saveChangeRequest(eq("myRepository"),
                                                     any(ChangeRequest.class));

        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));

        verify(git).createRef(anyString(),
                              anyString());

        verify(git, times(2)).commit(anyString(),
                                     any(CommitInfo.class),
                                     anyBoolean(),
                                     any(RevCommit.class),
                                     any(RevertCommitContent.class));

        verify(git).merge(anyString(),
                          anyString());

        verify(git).deleteRef(any(Ref.class));

        assertTrue(result);
    }

    @Test
    public void revertChangeRequestFailedCommitTest() {
        final String lastCommitId = "0000000000000000000000000000000000000000";
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatusLastCommitId(ChangeRequestStatus.ACCEPTED,
                                                                                                            lastCommitId));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        doReturn(false).when(git).commit(eq("targetBranch"),
                                         any(CommitInfo.class),
                                         eq(false),
                                         any(RevCommit.class),
                                         any(RevertCommitContent.class));

        boolean result = service.revertChangeRequest("mySpace",
                                                     "myRepository",
                                                     1L);

        verify(spaceConfigStorage).saveChangeRequest(eq("myRepository"),
                                                     any(ChangeRequest.class));

        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));

        assertFalse(result);
    }

    @Test
    public void updateChangeRequestSummaryTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.updateChangeRequestSummary("mySpace",
                                           "myRepository",
                                           1L,
                                           "newSummary");

        verify(spaceConfigStorage).saveChangeRequest(eq("myRepository"),
                                                     any(ChangeRequest.class));
        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));
    }

    @Test
    public void updateChangeRequestDescriptionTest() {
        List<ChangeRequest> crList = Collections.nCopies(3, createCommonChangeRequestWithStatus(ChangeRequestStatus.OPEN));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.updateChangeRequestDescription("mySpace",
                                               "myRepository",
                                               1L,
                                               "newDescription");

        verify(spaceConfigStorage).saveChangeRequest(eq("myRepository"),
                                                     any(ChangeRequest.class));
        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));
    }

    @Test
    public void getCommentsAllTest() {
        ChangeRequestComment comment = new ChangeRequestComment(1L, "author", new Date(), "text");
        List<ChangeRequestComment> commentList = Collections.nCopies(3, comment);
        doReturn(commentList).when(spaceConfigStorage).loadChangeRequestComments("myRepository", 1L);

        List<ChangeRequestComment> comments = service.getComments("mySpace",
                                                                  "myRepository",
                                                                  1L,
                                                                  0,
                                                                  0);

        assertThat(comments).hasSize(3);
    }

    @Test
    public void getCommentsPaginatedTest() {
        ChangeRequestComment comment = new ChangeRequestComment(1L, "author", new Date(), "text");
        List<ChangeRequestComment> commentList = Collections.nCopies(25, comment);
        doReturn(commentList).when(spaceConfigStorage).loadChangeRequestComments("myRepository", 1L);

        int page0Size = service.getComments("mySpace",
                                            "myRepository",
                                            1L,
                                            0,
                                            10).size();

        int page1Size = service.getComments("mySpace",
                                            "myRepository",
                                            1L,
                                            1,
                                            10).size();

        int page2Size = service.getComments("mySpace",
                                            "myRepository",
                                            1L,
                                            2,
                                            10).size();

        int page3Size = service.getComments("mySpace",
                                            "myRepository",
                                            1L,
                                            3,
                                            10).size();

        assertThat(page0Size).isEqualTo(10);
        assertThat(page1Size).isEqualTo(10);
        assertThat(page2Size).isEqualTo(5);
        assertThat(page3Size).isEqualTo(0);
    }

    @Test
    public void addCommentTest() {
        doReturn(Collections.emptyList()).when(spaceConfigStorage).getChangeRequestCommentIds("myRepository", 1L);

        service.addComment("mySpace",
                           "myRepository",
                           1L,
                           "myComment");

        verify(spaceConfigStorageRegistry.get("mySpace")).saveChangeRequestComment(eq("myRepository"),
                                                                                   eq(1L),
                                                                                   any(ChangeRequestComment.class));
        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));
    }

    @Test
    public void deleteCommentTest() {
        service.deleteComment("mySpace",
                              "myRepository",
                              1L,
                              1L);

        verify(spaceConfigStorageRegistry.get("mySpace")).deleteChangeRequestComment(eq("myRepository"),
                                                                                     eq(1L),
                                                                                     eq(1L));
        verify(changeRequestUpdatedEvent).fire(any(ChangeRequestUpdatedEvent.class));
    }

    private ChangeRequest createCommonChangeRequestWithFields(final Long id,
                                                              final String sourceBranch,
                                                              final String targetBranch,
                                                              final ChangeRequestStatus status,
                                                              final String summary,
                                                              final String lastCommitId) {
        return new ChangeRequest(id,
                                 "mySpace",
                                 "myRepository",
                                 sourceBranch,
                                 targetBranch,
                                 status,
                                 "author",
                                 summary,
                                 "description",
                                 new Date(),
                                 "commonCommitId",
                                 lastCommitId);
    }

    private ChangeRequest createCommonChangeRequest() {
        return createCommonChangeRequestWithFields(1L,
                                                   "sourceBranch",
                                                   "targetBranch",
                                                   ChangeRequestStatus.OPEN,
                                                   "summary",
                                                   null);
    }

    private ChangeRequest createCommonChangeRequestWithId(final Long id) {
        return createCommonChangeRequestWithFields(id,
                                                   "sourceBranch",
                                                   "targetBranch",
                                                   ChangeRequestStatus.OPEN,
                                                   "summary",
                                                   null);
    }

    private ChangeRequest createCommonChangeRequestWithStatus(final ChangeRequestStatus status) {
        return createCommonChangeRequestWithFields(1L,
                                                   "sourceBranch",
                                                   "targetBranch",
                                                   status,
                                                   "summary",
                                                   null);
    }

    private ChangeRequest createCommonChangeRequestWithSummary(final String summary) {
        return createCommonChangeRequestWithFields(1L,
                                                   "sourceBranch",
                                                   "targetBranch",
                                                   ChangeRequestStatus.OPEN,
                                                   summary,
                                                   null);
    }

    private ChangeRequest createCommonChangeRequestWithSourceBranch(final String sourceBranch) {
        return createCommonChangeRequestWithFields(1L,
                                                   sourceBranch,
                                                   "targetBranch",
                                                   ChangeRequestStatus.OPEN,
                                                   "summary",
                                                   null);
    }

    private ChangeRequest createCommonChangeRequestWithTargetBranch(final String targetBranch) {
        return createCommonChangeRequestWithFields(1L,
                                                   "sourceBranch",
                                                   targetBranch,
                                                   ChangeRequestStatus.OPEN,
                                                   "summary",
                                                   null);
    }

    private ChangeRequest createCommonChangeRequestWithStatusLastCommitId(final ChangeRequestStatus status,
                                                                          final String lastCommitId) {
        return createCommonChangeRequestWithFields(1L,
                                                   "sourceBranch",
                                                   "targetBranch",
                                                   status,
                                                   "summary",
                                                   lastCommitId);
    }

    private ChangeRequest createCommonChangeRequestWithSourceTargetBranch(final String sourceBranch,
                                                                          final String targetBranch) {
        return createCommonChangeRequestWithFields(1L,
                                                   sourceBranch,
                                                   targetBranch,
                                                   ChangeRequestStatus.OPEN,
                                                   "summary",
                                                   null);
    }

    private ChangeRequest createCommonChangeRequestWithStatusSummary(final ChangeRequestStatus status,
                                                                     final String summary) {
        return createCommonChangeRequestWithFields(1L,
                                                   "sourceBranch",
                                                   "targetBranch",
                                                   status,
                                                   summary,
                                                   null);
    }
}