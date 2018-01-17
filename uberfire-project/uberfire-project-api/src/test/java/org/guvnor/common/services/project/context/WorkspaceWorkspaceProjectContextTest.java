/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.common.services.project.context;

import org.guvnor.common.services.project.model.Module;
import org.guvnor.common.services.project.model.Package;
import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryRemovedEvent;
import org.guvnor.structure.repositories.impl.git.GitRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.uberfire.backend.vfs.Path;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceWorkspaceProjectContextTest {

    @Spy
    private EventSourceMock<WorkspaceProjectContextChangeEvent> changeEvent = new EventSourceMock<WorkspaceProjectContextChangeEvent>();

    private WorkspaceProjectContext context;

    @Before
    public void setUp() throws Exception {
        context = new WorkspaceProjectContext(changeEvent);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                context.onProjectContextChanged((WorkspaceProjectContextChangeEvent) invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(changeEvent).fire(any(WorkspaceProjectContextChangeEvent.class));
    }

    @Test
    public void testGetActiveRepositoryRoot() throws Exception {

        final Path devRoot = mock(Path.class);

        context.setActiveWorkspaceProject(new WorkspaceProject(mock(OrganizationalUnit.class),
                                                               mock(Repository.class),
                                                               new Branch("dev",
                                                        devRoot),
                                                               mock(Module.class)));

        assertEquals(devRoot,
                     context.getActiveRepositoryRoot());
    }

    @Test
    public void testRepositoryDeleted() throws Exception {
        final OrganizationalUnit organizationalUnit = mock(OrganizationalUnit.class);
        final Repository repository = mock(Repository.class);

        doReturn("myrepo").when(repository).getAlias();

        context.setActiveOrganizationalUnit(organizationalUnit);
        context.setActiveWorkspaceProject(new WorkspaceProject(organizationalUnit,
                                                               repository,
                                                               mock(Branch.class),
                                                               mock(Module.class)));

        assertNotNull(context.getActiveWorkspaceProject());

        final RepositoryRemovedEvent repositoryRemovedEvent = new RepositoryRemovedEvent(repository);
        context.onRepositoryRemoved(repositoryRemovedEvent);

        assertEquals(organizationalUnit,
                     context.getActiveOrganizationalUnit());
        assertNull(context.getActiveWorkspaceProject());
    }

    @Test
    public void testRepositoryDeletedNoActiveProject() throws Exception {
        final OrganizationalUnit organizationalUnit = mock(OrganizationalUnit.class);

        context.setActiveOrganizationalUnit(organizationalUnit);
        context.setActiveWorkspaceProject(null);

        context.onRepositoryRemoved(new RepositoryRemovedEvent(new GitRepository()));

        assertEquals(organizationalUnit,
                     context.getActiveOrganizationalUnit());
        assertNull(context.getActiveWorkspaceProject());
    }

    @Test
    public void testIgnoreRepositoryDeletedEventIfTheActiveRepositoryWasNotDeleted() throws Exception {

        GitRepository deletedRepository = new GitRepository("deleted repo");

        final WorkspaceProject activeWorkspaceProject = new WorkspaceProject(mock(OrganizationalUnit.class),
                                                                             new GitRepository("active repo"),
                                                                             mock(Branch.class),
                                                                             mock(Module.class));
        context.setActiveWorkspaceProject(activeWorkspaceProject);

        final RepositoryRemovedEvent repositoryRemovedEvent = new RepositoryRemovedEvent(deletedRepository);

        context.onRepositoryRemoved(repositoryRemovedEvent);

        assertEquals(activeWorkspaceProject,
                     context.getActiveWorkspaceProject());
    }

    @Test
    public void testContextChanged() throws Exception {
        final OrganizationalUnit oldOrganizationalUnit = mock(OrganizationalUnit.class);
        final Repository oldRepository = mock(Repository.class);
        final Package oldPackage = new Package();
        final Module oldModule = new Module();

        context.setActiveOrganizationalUnit(oldOrganizationalUnit);
        context.setActiveWorkspaceProject(new WorkspaceProject(oldOrganizationalUnit,
                                                               oldRepository,
                                                               mock(Branch.class),
                                                               mock(Module.class)));
        context.setActivePackage(oldPackage);
        context.setActiveModule(oldModule);

        final OrganizationalUnit newOrganizationalUnit = mock(OrganizationalUnit.class);
        final Branch newBranch = new Branch("master",
                                            mock(Path.class));
        final Package newPackage = new Package();
        final Module newModule = new Module();

        final WorkspaceProjectContextChangeHandler changeHandler = mock(WorkspaceProjectContextChangeHandler.class);
        context.addChangeHandler(changeHandler);

        final WorkspaceProject newWorkspaceProject = new WorkspaceProject(newOrganizationalUnit,
                                                                          mock(Repository.class),
                                                                          newBranch,
                                                                          mock(Module.class));
        context.onProjectContextChanged(new WorkspaceProjectContextChangeEvent(newWorkspaceProject,
                                                                               newModule,
                                                                               newPackage));

        assertEquals(newOrganizationalUnit,
                     context.getActiveOrganizationalUnit());
        assertEquals(newWorkspaceProject,
                     context.getActiveWorkspaceProject());
        assertEquals(newModule,
                     context.getActiveModule());
        assertEquals(newPackage,
                     context.getActivePackage());
        verify(changeHandler).onChange();
    }

    @Test
    public void testContextChangeHandlerGetsRemoved() throws Exception {
        WorkspaceProjectContextChangeHandler changeHandler = mock(WorkspaceProjectContextChangeHandler.class);
        ProjectContextChangeHandle handle = context.addChangeHandler(changeHandler);

        context.onProjectContextChanged(new WorkspaceProjectContextChangeEvent());

        verify(changeHandler).onChange();

        context.removeChangeHandler(handle);

        reset(changeHandler);

        context.onProjectContextChanged(new WorkspaceProjectContextChangeEvent());

        verify(changeHandler,
               never()).onChange();
    }
}