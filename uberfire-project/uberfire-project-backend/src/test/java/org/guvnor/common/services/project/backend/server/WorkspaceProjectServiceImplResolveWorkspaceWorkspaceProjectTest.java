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

import java.util.HashMap;
import java.util.Optional;
import javax.enterprise.inject.Instance;

import org.guvnor.common.services.project.model.Module;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.common.services.project.service.ModuleService;
import org.guvnor.common.services.project.service.WorkspaceProjectService;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.repositories.impl.git.GitRepository;
import org.jboss.errai.security.shared.api.identity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.security.authz.AuthorizationManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceProjectServiceImplResolveWorkspaceWorkspaceProjectTest {

    WorkspaceProjectService workspaceProjectService;

    @Mock
    RepositoryService repositoryService;

    @Mock
    Instance<ModuleService<? extends Module>> moduleServices;

    @Mock
    ModuleService moduleService;

    @Mock
    OrganizationalUnitService organizationalUnitService;

    @Mock
    OrganizationalUnit ou;

    @Mock
    Repository repository;

    @Mock
    Branch branch;

    @Mock
    Module module;

    private Path path;
    private Path branchRoot;
    private Path otherBranchRoot;
    private Branch masterBranch;
    private Branch otherBranch;

    @Before
    public void setUp() throws Exception {
        path = PathFactory.newPath("testFile",
                                   "file:///files/TestDataObject.java");
        branchRoot = PathFactory.newPath("testFile",
                                         "file:///branchRoot/");
        otherBranchRoot = PathFactory.newPath("testFile",
                                              "file:///otherBranchRoot/");

        doReturn(ou).when(organizationalUnitService).getParentOrganizationalUnit(repository);

        doReturn(Optional.of(branch)).when(repository).getDefaultBranch();
        doReturn(branchRoot).when(branch).getPath();

        doReturn(repository).when(repositoryService).getRepository(any(Path.class));

        doReturn(module).when(moduleService).resolveModule(any());

        masterBranch = new Branch("master",
                                  path);

        otherBranch = new Branch("other",
                                 otherBranchRoot);

        doReturn(moduleService).when(moduleServices).get();

        workspaceProjectService = new WorkspaceProjectServiceImpl(organizationalUnitService,
                                                                  repositoryService,
                                                                  new EventSourceMock<>(),
                                                                  moduleServices);
    }

    @Test
    public void resolveProjectPath() throws Exception {
        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(path);

        assertEquals(ou,
                     workspaceProject.getOrganizationalUnit());
        assertEquals(repository,
                     workspaceProject.getRepository());
        assertEquals(branch,
                     workspaceProject.getBranch());
        assertEquals(module,
                     workspaceProject.getMainModule());
    }

    @Test
    public void resolveProjectModule() throws Exception {

        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(new Module(path,
                                                                                                    mock(Path.class),
                                                                                                    mock(POM.class)));

        assertEquals(ou,
                     workspaceProject.getOrganizationalUnit());
        assertEquals(repository,
                     workspaceProject.getRepository());
        assertEquals(branch,
                     workspaceProject.getBranch());
        assertEquals(module,
                     workspaceProject.getMainModule());
    }

    @Test
    public void resolveProjectRepository() throws Exception {

        final GitRepository repository = new GitRepository();
        final HashMap<String, Branch> branches = new HashMap<>();
        branches.put("master", new Branch("master",
                                          path));

        repository.setBranches(branches);
        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(repository);

        assertEquals(ou,
                     workspaceProject.getOrganizationalUnit());
        assertEquals(this.repository,
                     workspaceProject.getRepository());
        assertEquals(branch,
                     workspaceProject.getBranch());
        assertEquals(module,
                     workspaceProject.getMainModule());
    }

    @Test
    public void resolveProjectBranch() throws Exception {

        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(masterBranch);

        assertEquals(ou,
                     workspaceProject.getOrganizationalUnit());
        assertEquals(this.repository,
                     workspaceProject.getRepository());
        assertEquals(branch,
                     workspaceProject.getBranch());
        assertEquals(module,
                     workspaceProject.getMainModule());
    }
}