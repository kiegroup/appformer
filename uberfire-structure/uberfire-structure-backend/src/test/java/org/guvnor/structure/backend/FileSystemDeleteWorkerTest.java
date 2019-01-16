package org.guvnor.structure.backend;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.organizationalunit.config.SpaceConfigStorageRegistry;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.fs.jgit.JGitPathImpl;
import org.uberfire.spaces.Space;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FileSystemDeleteWorkerTest {

    private FileSystemDeleteWorker worker;

    @Mock
    private IOService ioService;

    @Mock
    private OrganizationalUnitService ouService;

    @Mock
    private RepositoryService repoService;

    @Mock
    private FileSystem systemFs;

    @Mock
    private SpaceConfigStorageRegistry registry;

    @Before
    public void setUp() throws IOException {

        this.worker = spy(new FileSystemDeleteWorker(this.ioService,
                                                     this.ouService,
                                                     this.repoService,
                                                     this.systemFs,
                                                     this.registry));

        doAnswer(invocation -> null).when(ioService).delete(any());
        doAnswer(invocation -> null).when(worker).removeRepository(any());
        doAnswer(invocation -> null).when(worker).delete(any());
    }

    @Test
    public void testRemoveSpaceDirectory() throws IOException {

        JGitPathImpl configPath = mock(JGitPathImpl.class,
                                       RETURNS_DEEP_STUBS);

        Path deletePath = mock(Path.class);

        Space space = mock(Space.class);

        doReturn(Collections.singletonList(mock(Repository.class)))
                .when(this.repoService)
                .getAllRepositories(eq(space),
                                    eq(true));

        when(configPath.getFileSystem().getPath(anyString())).thenReturn(deletePath);

        doReturn(configPath).when(ioService).get(any());

        File spacePathFile = mock(File.class);
        doReturn(spacePathFile).when(worker).getSpacePath(any());

        this.worker.removeSpaceDirectory(space);

        verify(this.worker,
               times(1)).removeRepository(any());

        verify(ioService).deleteIfExists(deletePath);
        verify(this.worker).delete(spacePathFile);
    }

    @Test
    public void testRemoveAllDeletedSpaces() {

        doAnswer(invocation -> null).when(worker).removeSpaceDirectory(any());

        OrganizationalUnit ou1 = mock(OrganizationalUnit.class);
        doReturn(mock(Space.class)).when(ou1).getSpace();
        OrganizationalUnit ou2 = mock(OrganizationalUnit.class);
        doReturn(mock(Space.class)).when(ou2).getSpace();

        List<OrganizationalUnit> orgUnits = Arrays.asList(ou1,
                                                          ou2);
        doReturn(orgUnits).when(this.ouService).getAllDeletedOrganizationalUnit();

        this.worker.removeAllDeletedSpaces();

        verify(this.worker,
               times(1)).removeSpaceDirectory(ou1.getSpace());
        verify(this.worker,
               times(1)).removeSpaceDirectory(ou2.getSpace());
    }

    @Test
    public void testRemoveAllDeletedRepository() {

        Repository repo1 = mock(Repository.class);
        Repository repo2 = mock(Repository.class);
        Repository repo3 = mock(Repository.class);
        Repository repo4 = mock(Repository.class);

        Space space1 = mock(Space.class);
        Space space2 = mock(Space.class);
        OrganizationalUnit ou1 = mock(OrganizationalUnit.class);
        OrganizationalUnit ou2 = mock(OrganizationalUnit.class);

        doReturn(space1).when(ou1).getSpace();
        doReturn(space2).when(ou2).getSpace();

        doReturn(Arrays.asList(ou1,
                               ou2)).when(this.ouService).getAllOrganizationalUnits();

        doReturn(Arrays.asList(repo1,
                               repo2)).when(this.repoService).getAllDeletedRepositories(eq(space1));

        doReturn(Arrays.asList(repo3,
                               repo4)).when(this.repoService).getAllDeletedRepositories(eq(space2));

        Branch branch = mock(Branch.class);
        Repository repo = mock(Repository.class);
        doReturn(Optional.of(branch)).when(repo).getDefaultBranch();

        this.worker.removeAllDeletedRepositories();

        verify(worker,
               times(1)).removeRepository(repo1);
        verify(worker,
               times(1)).removeRepository(repo2);
        verify(worker,
               times(1)).removeRepository(repo3);
        verify(worker,
               times(1)).removeRepository(repo4);
    }
}