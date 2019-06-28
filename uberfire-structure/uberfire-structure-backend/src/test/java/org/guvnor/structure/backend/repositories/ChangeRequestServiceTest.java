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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.enterprise.event.Event;

import org.guvnor.structure.organizationalunit.config.SpaceConfigStorage;
import org.guvnor.structure.organizationalunit.config.SpaceConfigStorageRegistry;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.repositories.changerequest.ChangeRequest;
import org.guvnor.structure.repositories.changerequest.ChangeRequestDiff;
import org.guvnor.structure.repositories.changerequest.ChangeRequestListUpdatedEvent;
import org.guvnor.structure.repositories.changerequest.ChangeRequestStatus;
import org.jboss.errai.security.shared.api.identity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.java.nio.base.TextualDiff;
import org.uberfire.java.nio.fs.jgit.util.Git;
import org.uberfire.spaces.Space;
import org.uberfire.spaces.SpacesAPI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
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
    private BranchAccessAuthorizer branchAccessAuthorizer;

    @Mock
    private User user;

    @Mock
    private SpaceConfigStorage spaceConfigStorage;

    @Mock
    private Repository repository;

    @Before
    public void setUp() {
        Space mySpace = mock(Space.class);

        doReturn(spaceConfigStorage).when(spaceConfigStorageRegistry).get("mySpace");
        doReturn(mySpace).when(spaces).getSpace("mySpace");
        doReturn(repository).when(repositoryService).getRepositoryFromSpace(mySpace,
                                                                            "myRepository");

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
                                                        branchAccessAuthorizer,
                                                        user));
    }

    @Test
    public void createFirstChangeRequestTest() {
        doReturn(Collections.emptyList()).when(spaceConfigStorage).getChangeRequestIds("myRepository");

        ChangeRequest newChangeRequest = service.createChangeRequest("mySpace",
                                                                     "myRepository",
                                                                     "sourceBranch",
                                                                     "targetBranch",
                                                                     "author",
                                                                     "summary",
                                                                     "description",
                                                                     10);

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
                                                                     "author",
                                                                     "summary",
                                                                     "description",
                                                                     10);

        assertThat(newChangeRequest.getId()).isEqualTo(11L);
        assertThat(newChangeRequest.getComments().size()).isEqualTo(0);
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
                                    "author",
                                    "summary",
                                    "description",
                                    1);
    }

    @Test
    public void getChangeRequestsTest() {
        List<ChangeRequest> crList = Collections.nCopies(5, mock(ChangeRequest.class));

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

        ChangeRequest cr1 = mock(ChangeRequest.class);
        ChangeRequest cr2 = mock(ChangeRequest.class);
        ChangeRequest cr3 = mock(ChangeRequest.class);
        ChangeRequest cr4 = mock(ChangeRequest.class);

        doReturn("hiddenBranch").when(cr1).getTargetBranch();
        doReturn("hiddenBranch").when(cr2).getTargetBranch();
        doReturn("branch").when(cr3).getTargetBranch();
        doReturn("branch").when(cr4).getTargetBranch();

        List<ChangeRequest> crList = Arrays.asList(cr1, cr2, cr3, cr4);
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(2);
    }

    @Test
    public void getChangeRequestsWithFilterTest() {
        ChangeRequest cr1 = mock(ChangeRequest.class);
        ChangeRequest cr2 = mock(ChangeRequest.class);
        ChangeRequest cr3 = mock(ChangeRequest.class);
        ChangeRequest cr4 = mock(ChangeRequest.class);

        doReturn("findme").when(cr1).getSummary();
        doReturn("findus").when(cr2).getSummary();
        doReturn("hidden").when(cr3).getSummary();
        doReturn("hidden").when(cr4).getSummary();

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
        ChangeRequest cr1 = mock(ChangeRequest.class);
        ChangeRequest cr2 = mock(ChangeRequest.class);
        ChangeRequest cr3 = mock(ChangeRequest.class);
        ChangeRequest cr4 = mock(ChangeRequest.class);

        doReturn(ChangeRequestStatus.OPEN).when(cr1).getStatus();
        doReturn(ChangeRequestStatus.CLOSED).when(cr2).getStatus();
        doReturn(ChangeRequestStatus.ACCEPTED).when(cr3).getStatus();
        doReturn(ChangeRequestStatus.OPEN).when(cr4).getStatus();

        List<ChangeRequest> crList = Arrays.asList(cr1, cr2, cr3, cr4);
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository",
                                                                   ChangeRequestStatus.OPEN);

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(2);
    }

    @Test
    public void getChangeRequestsWithStatusAndFilterTest() {
        ChangeRequest cr1 = mock(ChangeRequest.class);
        ChangeRequest cr2 = mock(ChangeRequest.class);
        ChangeRequest cr3 = mock(ChangeRequest.class);
        ChangeRequest cr4 = mock(ChangeRequest.class);

        doReturn(ChangeRequestStatus.OPEN).when(cr1).getStatus();
        doReturn(ChangeRequestStatus.CLOSED).when(cr2).getStatus();
        doReturn(ChangeRequestStatus.ACCEPTED).when(cr3).getStatus();
        doReturn(ChangeRequestStatus.OPEN).when(cr4).getStatus();

        doReturn("findme").when(cr1).getSummary();
        doReturn("findme").when(cr2).getSummary();
        doReturn("findme").when(cr3).getSummary();
        doReturn("findme").when(cr4).getSummary();

        List<ChangeRequest> crList = Arrays.asList(cr1, cr2, cr3, cr4);
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository",
                                                                   ChangeRequestStatus.OPEN,
                                                                   "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(2);
    }

    @Test
    public void getChangeRequestsPaginatedWithFilterTest() {
        ChangeRequest crsWithFilter = mock(ChangeRequest.class);
        doReturn("findme").when(crsWithFilter).getSummary();

        ChangeRequest crsHidden = mock(ChangeRequest.class);
        doReturn("hidden").when(crsHidden).getSummary();

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
        ChangeRequest crsWithStatusAndFilter = mock(ChangeRequest.class);
        doReturn("findme").when(crsWithStatusAndFilter).getSummary();
        doReturn(ChangeRequestStatus.ACCEPTED).when(crsWithStatusAndFilter).getStatus();

        ChangeRequest crsOnlyFilter = mock(ChangeRequest.class);
        doReturn("findme").when(crsOnlyFilter).getSummary();

        ChangeRequest crsOnlyStatus = mock(ChangeRequest.class);
        doReturn(ChangeRequestStatus.ACCEPTED).when(crsOnlyStatus).getStatus();

        ChangeRequest crsHidden = mock(ChangeRequest.class);
        doReturn("hidden").when(crsHidden).getSummary();

        List<ChangeRequest> crList = new ArrayList<ChangeRequest>() {{
            addAll(Collections.nCopies(20, crsOnlyStatus));
            addAll(Collections.nCopies(26, crsWithStatusAndFilter));
            addAll(Collections.nCopies(20, crsOnlyFilter));
            addAll(Collections.nCopies(30, crsHidden));
        }};

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        List<ChangeRequest> actualList = service.getChangeRequests("mySpace",
                                                                   "myRepository",
                                                                   0,
                                                                   10,
                                                                   ChangeRequestStatus.ACCEPTED,
                                                                   "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(10);

        actualList = service.getChangeRequests("mySpace",
                                               "myRepository",
                                               1,
                                               10,
                                               ChangeRequestStatus.ACCEPTED,
                                               "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(10);

        actualList = service.getChangeRequests("mySpace",
                                               "myRepository",
                                               2,
                                               10,
                                               ChangeRequestStatus.ACCEPTED,
                                               "find");

        assertThat(actualList).isNotEmpty();
        assertThat(actualList).hasSize(6);
    }

    @Test
    public void getChangeRequestTest() {
        ChangeRequest cr1 = mock(ChangeRequest.class);
        ChangeRequest cr2 = mock(ChangeRequest.class);
        ChangeRequest cr3 = mock(ChangeRequest.class);
        ChangeRequest cr4 = mock(ChangeRequest.class);

        doReturn(1L).when(cr1).getId();
        doReturn(2L).when(cr2).getId();
        doReturn(3L).when(cr3).getId();
        doReturn(4L).when(cr4).getId();

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
        List<ChangeRequest> crList = Collections.nCopies(15, mock(ChangeRequest.class));
        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        int count = service.countChangeRequests("mySpace",
                                                "myRepository");

        assertEquals(15, count);
    }

    @Test
    public void countChangeRequestsWithStatusTest() {
        ChangeRequest crsWithStatus = mock(ChangeRequest.class);
        doReturn(ChangeRequestStatus.OPEN).when(crsWithStatus).getStatus();

        ChangeRequest crsHidden = mock(ChangeRequest.class);
        doReturn(ChangeRequestStatus.CLOSED).when(crsHidden).getStatus();

        List<ChangeRequest> crList = new ArrayList<ChangeRequest>() {{
            addAll(Collections.nCopies(26, crsWithStatus));
            addAll(Collections.nCopies(30, crsHidden));
        }};

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        int count = service.countChangeRequests("mySpace",
                                                "myRepository",
                                                ChangeRequestStatus.OPEN);

        assertEquals(26, count);
    }

    @Test
    public void countChangeRequestsWithFilterTest() {
        ChangeRequest crsWithFilter = mock(ChangeRequest.class);
        doReturn("findme").when(crsWithFilter).getSummary();

        ChangeRequest crsHidden = mock(ChangeRequest.class);
        doReturn("hidden").when(crsHidden).getSummary();

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
        ChangeRequest crsWithFilter = mock(ChangeRequest.class);
        doReturn("findme").when(crsWithFilter).getSummary();

        ChangeRequest crsWithStatus = mock(ChangeRequest.class);
        doReturn(ChangeRequestStatus.OPEN).when(crsWithStatus).getStatus();

        ChangeRequest crsWithStatusAndFilter = mock(ChangeRequest.class);
        doReturn(ChangeRequestStatus.OPEN).when(crsWithStatusAndFilter).getStatus();
        doReturn("findme").when(crsWithStatusAndFilter).getSummary();

        ChangeRequest crsHidden = mock(ChangeRequest.class);
        doReturn("hidden").when(crsHidden).getSummary();

        List<ChangeRequest> crList = new ArrayList<ChangeRequest>() {{
            addAll(Collections.nCopies(26, crsWithFilter));
            addAll(Collections.nCopies(30, crsHidden));
            addAll(Collections.nCopies(18, crsWithStatusAndFilter));
        }};

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        int count = service.countChangeRequests("mySpace",
                                                "myRepository",
                                                ChangeRequestStatus.OPEN,
                                                "me");

        assertEquals(18, count);
    }

    @Test
    public void getDiffTestNoResults() {
        Branch sourceBranch = mock(Branch.class);
        Git git = mock(Git.class);

        doReturn(git).when(service).getGitFromBranch(sourceBranch);
        doReturn(Collections.emptyList()).when(git).conflictBranchesChecker(anyString(),
                                                                            anyString());
        doReturn(Collections.emptyList()).when(git).textualDiffRefs(anyString(),
                                                                    anyString());
        doReturn(Optional.of(sourceBranch)).when(repository).getBranch("branchA");
        doReturn(Optional.of(mock(Branch.class))).when(repository).getBranch("branchB");

        List<ChangeRequestDiff> diffs = service.getDiff("mySpace",
                                                        "myRepository",
                                                        "branchA",
                                                        "branchB");

        assertThat(diffs).isEmpty();
    }

    @Test
    public void getDiffTestWithResults() {
        Branch sourceBranch = mock(Branch.class);
        doReturn(mock(Path.class)).when(sourceBranch).getPath();

        Branch targetBranch = mock(Branch.class);
        doReturn(mock(Path.class)).when(targetBranch).getPath();

        Git git = mock(Git.class);

        TextualDiff textualDiff = new TextualDiff("old/file/path",
                                                  "new/file/path",
                                                  "ADD",
                                                  10,
                                                  10,
                                                  "diff text");

        List<TextualDiff> diffList = Collections.nCopies(10, textualDiff);

        doReturn(git).when(service).getGitFromBranch(sourceBranch);
        doReturn(Collections.emptyList()).when(git).conflictBranchesChecker(anyString(),
                                                                            anyString());
        doReturn(diffList).when(git).textualDiffRefs(anyString(),
                                                     anyString());
        doReturn(Optional.of(sourceBranch)).when(repository).getBranch("branchA");
        doReturn(Optional.of(targetBranch)).when(repository).getBranch("branchB");

        List<ChangeRequestDiff> diffs = service.getDiff("mySpace",
                                                        "myRepository",
                                                        "branchA",
                                                        "branchB");

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

    @Test
    public void deleteChangeRequestsTest() {
        ChangeRequest crs = mock(ChangeRequest.class);
        doReturn("branch").when(crs).getSourceBranch();
        doReturn("hiddenBranch").when(crs).getTargetBranch();

        List<ChangeRequest> crList = Collections.nCopies(10, crs);

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.deleteChangeRequests("mySpace",
                                     "myRepository",
                                     "branch");

        verify(spaceConfigStorage, times(10)).deleteChangeRequest(anyString(),
                                                                  anyLong());
    }

    @Test
    public void deleteChangeRequestsSomeTest() {
        ChangeRequest crsSourceBranch = mock(ChangeRequest.class);
        doReturn("branch").when(crsSourceBranch).getSourceBranch();
        doReturn("hiddenBranch").when(crsSourceBranch).getTargetBranch();

        ChangeRequest crsTargetBranch = mock(ChangeRequest.class);
        doReturn("hiddenBranch").when(crsTargetBranch).getSourceBranch();
        doReturn("branch").when(crsTargetBranch).getTargetBranch();

        ChangeRequest crsHidden = mock(ChangeRequest.class);
        doReturn("hiddenBranch").when(crsHidden).getSourceBranch();
        doReturn("hiddenBranch").when(crsHidden).getTargetBranch();

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
        ChangeRequest crs = mock(ChangeRequest.class);
        doReturn("hiddenBranch").when(crs).getSourceBranch();
        doReturn("hiddenBranch").when(crs).getTargetBranch();

        List<ChangeRequest> crList = Collections.nCopies(10, crs);

        doReturn(crList).when(spaceConfigStorage).loadChangeRequests("myRepository");

        service.deleteChangeRequests("mySpace",
                                     "myRepository",
                                     "branch");

        verify(spaceConfigStorage, never()).deleteChangeRequest(anyString(),
                                                                anyLong());
    }
}