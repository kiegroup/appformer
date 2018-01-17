/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.guvnor.common.services.project.backend.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.enterprise.inject.Instance;

import org.guvnor.common.services.project.model.Module;
import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.common.services.project.service.ModuleService;
import org.guvnor.common.services.project.service.WorkspaceProjectService;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.organizationalunit.impl.OrganizationalUnitImpl;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.security.shared.api.identity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.security.authz.AuthorizationManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceProjectServiceImplTest {

    WorkspaceProjectService workspaceProjectService;

    @Mock
    OrganizationalUnitService organizationalUnitService;

    @Mock
    RepositoryService repositoryService;

    @Mock
    Instance<ModuleService<? extends Module>> moduleServices;

    @Mock
    Repository repository1;

    @Mock
    Repository repository2;

    @Mock
    Repository repository3;

    @Mock
    ModuleService moduleService;

    private OrganizationalUnit ou1;
    private OrganizationalUnit ou2;
    private List<Repository> allRepositories;

    @Before
    public void setUp() throws Exception {

        setUpRepositories();

        setUpOUs();

        doReturn(moduleService).when(moduleServices).get();

        workspaceProjectService = new WorkspaceProjectServiceImpl(organizationalUnitService,
                                                                  repositoryService,
                                                                  new EventSourceMock<>(),
                                                                  moduleServices);
    }

    private void setUpOUs() {
        ou1 = new OrganizationalUnitImpl("ou1",
                                         "owner",
                                         "defaultGroupID");
        ou2 = new OrganizationalUnitImpl("ou2",
                                         "owner",
                                         "defaultGroupID");

        doReturn(ou1).when(organizationalUnitService).getOrganizationalUnit("ou1");
        doReturn(ou2).when(organizationalUnitService).getOrganizationalUnit("ou2");

        final List<OrganizationalUnit> allOUs = new ArrayList<>();
        allOUs.add(ou1);
        allOUs.add(ou2);
        doReturn(allOUs).when(organizationalUnitService).getOrganizationalUnits();

        ou1.getRepositories().add(repository1);
        ou1.getRepositories().add(repository2);

        ou2.getRepositories().add(repository3);
    }

    private void setUpRepositories() {

        doReturn(Optional.of(mock(Branch.class))).when(repository1).getDefaultBranch();
        doReturn("repository1").when(repository1).getAlias();
        doReturn(Optional.of(mock(Branch.class))).when(repository2).getDefaultBranch();
        doReturn("repository2").when(repository2).getAlias();
        doReturn(Optional.of(mock(Branch.class))).when(repository3).getDefaultBranch();
        doReturn("repository3").when(repository3).getAlias();

        allRepositories = new ArrayList<>();
        allRepositories.add(repository1);
        allRepositories.add(repository2);
        allRepositories.add(repository3);
        doReturn(allRepositories).when(repositoryService).getRepositories();
    }

    @Test
    public void getAllProjects() throws Exception {

        final Collection<WorkspaceProject> allWorkspaceProjects = workspaceProjectService.getAllWorkspaceProjects();

        assertEquals(3,
                     allWorkspaceProjects.size());
    }

    @Test
    public void getAllProjectsForOU1() throws Exception {
        final Collection<WorkspaceProject> allWorkspaceProjects = workspaceProjectService.getAllWorkspaceProjects(ou1);

        assertContains(repository1,
                       allWorkspaceProjects);
        assertContains(repository2,
                       allWorkspaceProjects);

        assertEquals(2,
                     allWorkspaceProjects.size());
    }

    @Test
    public void getAllProjectsForOU2() throws Exception {
        final Collection<WorkspaceProject> allWorkspaceProjects = workspaceProjectService.getAllWorkspaceProjects(ou2);

        assertContains(repository3,
                       allWorkspaceProjects);

        assertEquals(1,
                     allWorkspaceProjects.size());
    }

    @Test
    /**
     * Here the list in the existing OU instance is old and does not have the latest repository that someone just created.
     */
    public void getAllProjectsForOU2WhenRepositoryListHasUpdated() throws Exception {

        final Repository repository4 = mock(Repository.class);
        doReturn(Optional.of(mock(Branch.class))).when(repository4).getDefaultBranch();
        doReturn("repository4").when(repository4).getAlias();
        ou2.getRepositories().add(repository4);
        allRepositories.add(repository4);

        final Collection<WorkspaceProject> allWorkspaceProjects = workspaceProjectService.getAllWorkspaceProjects(new OrganizationalUnitImpl("ou2",
                                                                                                                                             "",
                                                                                                                                             ""));

        assertContains(repository3,
                       allWorkspaceProjects);
        assertContains(repository4,
                       allWorkspaceProjects);

        assertEquals(2,
                     allWorkspaceProjects.size());
    }

    @Test
    public void noProjects() throws Exception {
        final OrganizationalUnit organizationalUnit = mock(OrganizationalUnit.class);
        doReturn("myOU").when(organizationalUnit).getName();

        doReturn(organizationalUnit).when(organizationalUnitService).getOrganizationalUnit("myOU");

        assertTrue(workspaceProjectService.getAllWorkspaceProjects(organizationalUnit).isEmpty());
    }

    private void assertContains(final Repository repository,
                                final Collection<WorkspaceProject> allWorkspaceProjects) {

        for (final WorkspaceProject workspaceProject : allWorkspaceProjects) {
            if (workspaceProject.getRepository().equals(repository)) {
                return;
            }
        }

        fail("Could not find " + repository);
    }
}