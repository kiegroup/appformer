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

package org.uberfire.java.nio.fs.jgit;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.AssertionsForClassTypes;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.soup.commons.util.Maps;
import org.uberfire.java.nio.file.FileStore;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.fs.jgit.util.Git;
import org.uberfire.java.nio.fs.jgit.util.GitImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JGitFileSystemImplTest extends AbstractTestInfra {

    static {
        CredentialsProvider.setDefault(new UsernamePasswordCredentialsProvider("guest",
                                                                               ""));
    }

    @Test
    public void testOnlyLocalRoot() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final Git git = setupGit();
        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        assertThat(fileSystem.isReadOnly()).isFalse();
        assertThat(fileSystem.getSeparator()).isEqualTo("/");
        assertThat(fileSystem.getName()).isEqualTo("my-repo");

        assertThat(fileSystem.getRootDirectories()).hasSize(1);
        final Path root = fileSystem.getRootDirectories().iterator().next();
        assertThat(root.toString()).isEqualTo("/");

        assertThat(root.getRoot().toString()).isEqualTo("/");
    }

    @Test
    public void testRemoteRoot() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final File tempDir = createTempDirectory();
        final Git git = new GitImpl(GitImpl._cloneRepository().setNoCheckout(false).setBare(true).setCloneAllBranches(true).setURI(setupGit().getRepository().getDirectory().toString()).setDirectory(tempDir).call());

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        assertThat(fileSystem.isReadOnly()).isFalse();
        assertThat(fileSystem.getSeparator()).isEqualTo("/");
        assertThat(fileSystem.getName()).isEqualTo("my-repo");

        assertThat(fileSystem.getRootDirectories()).hasSize(1);
        final Path root = fileSystem.getRootDirectories().iterator().next();
        assertThat(root.toString()).isEqualTo("/");

        assertThat(root.getRoot().toString()).isEqualTo("/");
    }

    private JGitFileSystemLock createFSLock(Git git) {
        return new JGitFileSystemLock(git,
                                      TimeUnit.MILLISECONDS,
                                      30_000L);
    }

    @Test
    public void testProvider() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        assertThat(fileSystem.getName()).isEqualTo("my-repo");
        assertThat(fileSystem.isReadOnly()).isFalse();
        assertThat(fileSystem.getSeparator()).isEqualTo("/");

        assertThat(fileSystem.provider()).isEqualTo(fsProvider);
    }

    @Test(expected = IllegalStateException.class)
    public void testClose() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        assertThat(fileSystem.isReadOnly()).isFalse();
        assertThat(fileSystem.getSeparator()).isEqualTo("/");
        assertThat(fileSystem.getName()).isEqualTo("my-repo");

        assertThat(fileSystem.isOpen()).isTrue();
        assertThat(fileSystem.getFileStores()).isNotNull();
        fileSystem.close();
        assertThat(fileSystem.isOpen()).isFalse();
        assertThat(fileSystem.getFileStores()).isNotNull();
    }

    @Test
    public void testSupportedFileAttributeViews() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        assertThat(fileSystem.isReadOnly()).isFalse();
        assertThat(fileSystem.getSeparator()).isEqualTo("/");
        assertThat(fileSystem.getName()).isEqualTo("my-repo");

        assertThat(fileSystem.supportedFileAttributeViews()).isNotEmpty().hasSize(2).contains("basic",
                                                                                              "version");
    }

    @Test
    public void testPathNonBranchRooted() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);
        when(fsProvider.isDefault()).thenReturn(false);
        when(fsProvider.getScheme()).thenReturn("git");

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        final Path path = fileSystem.getPath("/path/to/some/place.txt");

        AssertionsForClassTypes.assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isTrue();
        assertThat(path.toString()).isEqualTo("/path/to/some/place.txt");
        assertThat(path.toUri().toString()).isEqualTo("git://master@my-repo/path/to/some/place.txt");

        assertThat(path.getNameCount()).isEqualTo(4);

        assertThat(path.getName(0).toString()).isNotNull().isEqualTo("path");
        assertThat(path.getRoot().toString()).isNotNull().isEqualTo("/");
    }

    @Test
    public void testPathNonBranchNonRooted() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);
        when(fsProvider.isDefault()).thenReturn(false);
        when(fsProvider.getScheme()).thenReturn("git");

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        final Path path = fileSystem.getPath("path/to/some/place.txt");

        AssertionsForClassTypes.assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isFalse();
        assertThat(path.toString()).isEqualTo("path/to/some/place.txt");
        assertThat(path.toUri().toString()).isEqualTo("git://master@my-repo/:path/to/some/place.txt");

        assertThat(path.getNameCount()).isEqualTo(4);

        assertThat(path.getName(0).toString()).isNotNull().isEqualTo("path");
        assertThat(path.getRoot().toString()).isNotNull().isEqualTo("");
    }

    @Test
    public void testPathBranchRooted() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);
        when(fsProvider.isDefault()).thenReturn(false);
        when(fsProvider.getScheme()).thenReturn("git");

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        final Path path = fileSystem.getPath("test-branch",
                                             "/path/to/some/place.txt");

        AssertionsForClassTypes.assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isTrue();
        assertThat(path.toString()).isEqualTo("/path/to/some/place.txt");
        assertThat(path.toUri().toString()).isEqualTo("git://test-branch@my-repo/path/to/some/place.txt");

        assertThat(path.getNameCount()).isEqualTo(4);

        assertThat(path.getName(0).toString()).isNotNull().isEqualTo("path");
        assertThat(path.getRoot().toString()).isNotNull().isEqualTo("/");
    }

    @Test
    public void testPathBranchNonRooted() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);
        when(fsProvider.isDefault()).thenReturn(false);
        when(fsProvider.getScheme()).thenReturn("git");

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        final Path path = fileSystem.getPath("test-branch",
                                             "path/to/some/place.txt");

        AssertionsForClassTypes.assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isFalse();
        assertThat(path.toString()).isEqualTo("path/to/some/place.txt");
        assertThat(path.toUri().toString()).isEqualTo("git://test-branch@my-repo/:path/to/some/place.txt");

        assertThat(path.getNameCount()).isEqualTo(4);

        assertThat(path.getName(0).toString()).isNotNull().isEqualTo("path");
        assertThat(path.getRoot().toString()).isNotNull().isEqualTo("");
    }

    @Test
    public void testPathBranchRooted2() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);
        when(fsProvider.isDefault()).thenReturn(false);
        when(fsProvider.getScheme()).thenReturn("git");

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        final Path path = fileSystem.getPath("test-branch",
                                             "/path/to",
                                             "some/place.txt");

        AssertionsForClassTypes.assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isTrue();
        assertThat(path.toString()).isEqualTo("/path/to/some/place.txt");
        assertThat(path.toUri().toString()).isEqualTo("git://test-branch@my-repo/path/to/some/place.txt");

        assertThat(path.getNameCount()).isEqualTo(4);

        assertThat(path.getName(0).toString()).isNotNull().isEqualTo("path");
        assertThat(path.getRoot().toString()).isNotNull().isEqualTo("/");
    }

    @Test
    public void testPathBranchNonRooted2() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);
        when(fsProvider.isDefault()).thenReturn(false);
        when(fsProvider.getScheme()).thenReturn("git");

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        final Path path = fileSystem.getPath("test-branch",
                                             "path/to",
                                             "some/place.txt");

        AssertionsForClassTypes.assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isFalse();
        assertThat(path.toString()).isEqualTo("path/to/some/place.txt");
        assertThat(path.toUri().toString()).isEqualTo("git://test-branch@my-repo/:path/to/some/place.txt");

        assertThat(path.getNameCount()).isEqualTo(4);

        assertThat(path.getName(0).toString()).isNotNull().isEqualTo("path");
        assertThat(path.getRoot().toString()).isNotNull().isEqualTo("");
    }

    @Test
    public void testFileStore() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final File tempDir = createTempDirectory();
        final Git git = setupGit(tempDir);

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        assertThat(fileSystem.getFileStores()).hasSize(1);
        final FileStore fileStore = fileSystem.getFileStores().iterator().next();
        assertThat(fileStore).isNotNull();

        assertThat(fileStore.getTotalSpace()).isEqualTo(tempDir.getTotalSpace());
        assertThat(fileStore.getUsableSpace()).isEqualTo(tempDir.getUsableSpace());
    }

    @Test
    public void testPathEqualsWithDifferentRepos() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final Git git1 = setupGit();

        final JGitFileSystemImpl fileSystem1 = new JGitFileSystemImpl(fsProvider,
                                                                      null,
                                                                      git1,
                                                                      createFSLock(git1),
                                                                      "my-repo1",
                                                                      CredentialsProvider.getDefault(),
                                                                      null,
                                                                      null);

        final Git git2 = setupGit();

        final JGitFileSystemImpl fileSystem2 = new JGitFileSystemImpl(fsProvider,
                                                                      null,
                                                                      git2,
                                                                      createFSLock(git2),
                                                                      "my-repo2",
                                                                      CredentialsProvider.getDefault(),
                                                                      null,
                                                                      null);

        final Path path1 = fileSystem1.getPath("master",
                                               "/path/to/some.txt");
        final Path path2 = fileSystem2.getPath("main",
                                               "/path/to/some.txt");

        AssertionsForClassTypes.assertThat(path1).isNotEqualTo(path2);

        AssertionsForClassTypes.assertThat(path1).isEqualTo(fileSystem1.getPath("/path/to/some.txt"));
    }

    @Test(expected = UnsupportedOperationException.class)
    @Ignore
    public void testNewWatchService() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);
        fileSystem.newWatchService();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetUserPrincipalLookupService() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);
        fileSystem.getUserPrincipalLookupService();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetPathMatcher() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);
        fileSystem.getPathMatcher("*");
    }

    @Test
    public void lockShouldSupportMultipleInnerLocksForTheSameThreadTest() throws IOException, GitAPIException {
        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     null,
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        fileSystem.lock();
        //inner locks
        fileSystem.lock();
        fileSystem.lock();
        fileSystem.unlock();
        fileSystem.unlock();
        fileSystem.unlock();
    }

    @Test
    public void lockTest() throws IOException, GitAPIException {

        final Git git = setupGit();
        JGitFileSystemLock lock = createFSLock(git);

        JGitFileSystemLock lockSpy = spy(lock);

        lockSpy.lock();
        lockSpy.lock();
        lockSpy.lock();
        verify(lockSpy,
               times(1)).physicalLockOnFS();

        lockSpy.unlock();
        lockSpy.unlock();
        lockSpy.unlock();
        verify(lockSpy,
               times(1)).physicalUnLockOnFS();
    }

    @Test
    public void testSetPublicURI() throws IOException, GitAPIException {

        final JGitFileSystemProvider fsProvider = mock(JGitFileSystemProvider.class);

        final Git git = setupGit();

        final JGitFileSystemImpl fileSystem = new JGitFileSystemImpl(fsProvider,
                                                                     new Maps.Builder<String, String>()
                                                                             .put("ssh", "localhost:8080/git")
                                                                             .build(),
                                                                     git,
                                                                     createFSLock(git),
                                                                     "my-repo",
                                                                     CredentialsProvider.getDefault(),
                                                                     null,
                                                                     null);

        assertTrue(checkProtocolPresent(fileSystem.toString(), "ssh"));
        assertFalse(checkProtocolPresent(fileSystem.toString(), "http"));

        fileSystem.setPublicURI(new Maps.Builder<String, String>()
                                        .put("http", "localhost:8080/git")
                                        .put("ssh", "localhost:8080/git")
                                        .build());
        assertTrue(checkProtocolPresent(fileSystem.toString(), "ssh"));
        assertTrue(checkProtocolPresent(fileSystem.toString(), "http"));
    }
}
