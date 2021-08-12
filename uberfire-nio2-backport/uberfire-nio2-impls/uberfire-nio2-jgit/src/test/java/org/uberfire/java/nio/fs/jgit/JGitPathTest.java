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

import org.junit.Before;
import org.junit.Test;
import org.uberfire.java.nio.EncodingUtil;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.spi.FileSystemProvider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JGitPathTest {

    private final FileSystemProvider fsp = mock(FileSystemProvider.class);
    private final JGitFileSystem fs = mock(JGitFileSystem.class);

    @Before
    public void setup() {
        when(fs.getSeparator()).thenReturn("/");
        when(fs.provider()).thenReturn(fsp);
        when(fsp.isDefault()).thenReturn(false);
        when(fsp.getScheme()).thenReturn("git");
    }

    @Test
    public void testSimpleBranchedGit() {
        final Path path = JGitPathImpl.create(fs,
                                              "",
                                              "main@my-host",
                                              false);

        assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isTrue();
        assertThat(path.toString()).isEqualTo("/");
        assertThat(path.toUri().toString()).isEqualTo("git://main@my-host/");
        assertThat(path.getRoot()).isEqualTo(path);

        assertThat(path.getNameCount()).isEqualTo(0);

        assertThat(path.getRoot()).isEqualTo(path);
    }

    @Test
    public void testSimpleBranchedGitRoot() {
        when(fs.getSeparator()).thenReturn("/");

        final Path path = JGitPathImpl.create(fs,
                                              "/",
                                              "main@my-host",
                                              false);

        assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isTrue();
        assertThat(path.toString()).isEqualTo("/");
        assertThat(path.toUri().toString()).isEqualTo("git://main@my-host/");
        assertThat(path.getRoot().toString()).isEqualTo("/");

        assertThat(path.getNameCount()).isEqualTo(0);
    }

    @Test
    public void testSimpleBranchedGitRelative() {
        when(fs.getSeparator()).thenReturn("/");

        final Path path = JGitPathImpl.create(fs,
                                              "home",
                                              "main@my-host",
                                              false);

        assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isFalse();
        assertThat(path.toString()).isEqualTo("home");
        assertThat(path.toUri().toString()).isEqualTo("git://main@my-host/:home");
        assertThat(path.getRoot().toString()).isEqualTo("");

        assertThat(path.getNameCount()).isEqualTo(1);
    }

    @Test
    public void testSimpleBranchedGitRoot2() {
        when(fs.getSeparator()).thenReturn("/");

        final Path path = JGitPathImpl.create(fs,
                                              "/path/to/some/place.txt",
                                              "main@my-host",
                                              false);

        assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isTrue();
        assertThat(path.toString()).isEqualTo("/path/to/some/place.txt");
        assertThat(path.toUri().toString()).isEqualTo("git://main@my-host/path/to/some/place.txt");

        assertThat(path.getNameCount()).isEqualTo(4);

        assertThat(path.getName(0).toString()).isEqualTo("path");
        assertThat(path.getRoot().toString()).isEqualTo("/");
    }

    @Test
    public void testSimpleBranchedGitRoot2Spaced() throws IllegalStateException {
        when(fs.getSeparator()).thenReturn("/");

        final Path path = JGitPathImpl.create(fs,
                                              EncodingUtil.decode("/path/to/some/some%20place.txt"),
                                              "main@my-host",
                                              false);

        assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isTrue();
        assertThat(path.toString()).isEqualTo("/path/to/some/some place.txt");
        assertThat(path.toUri().toString()).isEqualTo("git://main@my-host/path/to/some/some%20place.txt");

        assertThat(path.getNameCount()).isEqualTo(4);

        assertThat(path.getName(0).toString()).isEqualTo("path");
        assertThat(path.getRoot().toString()).isEqualTo("/");
    }

    @Test
    public void testSimpleImplicitBranchGit() {
        when(fs.getSeparator()).thenReturn("/");

        final Path path = JGitPathImpl.create(fs,
                                              "/path/to/some/place.txt",
                                              "my-host",
                                              false);

        assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isTrue();
        assertThat(path.toString()).isEqualTo("/path/to/some/place.txt");
        assertThat(path.toUri().toString()).isEqualTo("git://master@my-host/path/to/some/place.txt");

        assertThat(path.getNameCount()).isEqualTo(4);

        assertThat(path.getName(0).toString()).isEqualTo("path");
        assertThat(path.getRoot().toString()).isEqualTo("/");
    }

    @Test
    public void testSimpleImplicitBranchGitRoot() {
        when(fs.getSeparator()).thenReturn("/");

        final Path path = JGitPathImpl.create(fs,
                                              "/",
                                              "my-host",
                                              false);

        assertThat(path).isNotNull();
        assertThat(path.isAbsolute()).isTrue();
        assertThat(path.toString()).isEqualTo("/");
        assertThat(path.getRoot().toString()).isEqualTo("/");
        assertThat(path.toUri().toString()).isEqualTo("git://master@my-host/");

        assertThat(path.getNameCount()).isEqualTo(0);

        assertThat(path.getRoot().toString()).isEqualTo("/");
        assertThatThrownBy(() -> path.getName(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testRelativize() {
        final FileSystemProvider fsprovider = mock(FileSystemProvider.class);
        when(fsprovider.isDefault()).thenReturn(true);
        when(fsprovider.getScheme()).thenReturn("file");
        when(fs.provider()).thenReturn(fsprovider);

        when(fs.getSeparator()).thenReturn("/");

        final Path path = JGitPathImpl.create(fs,
                                              "/path/to",
                                              "main@my-host",
                                              false);
        final Path other = JGitPathImpl.create(fs,
                                               "/path/to/some/place",
                                               "main@my-host",
                                               false);

        final Path relative = path.relativize(other);
        assertThat(relative).isNotNull();
        assertThat(relative.toString()).isEqualTo("some/place");

        final Path path2 = JGitPathImpl.create(fs,
                                               "/path/to/some/place",
                                               "main@my-host",
                                               false);
        final Path other2 = JGitPathImpl.create(fs,
                                                "/path/to",
                                                "main@my-host",
                                                false);

        final Path relative2 = path2.relativize(other2);
        assertThat(relative2).isNotNull();
        assertThat(relative2.toString()).isEqualTo("../..");

        final Path path3 = JGitPathImpl.create(fs,
                                               "/path/to",
                                               "main@my-host",
                                               false);
        final Path other3 = JGitPathImpl.create(fs,
                                                "/path/to",
                                                "main@my-host",
                                                false);

        final Path relative3 = path3.relativize(other3);
        assertThat(relative3).isNotNull();
        assertThat(relative3.toString()).isEqualTo("");

        final Path path4 = JGitPathImpl.create(fs,
                                               "path/to",
                                               "main@my-host",
                                               false);
        final Path other4 = JGitPathImpl.create(fs,
                                                "path/to/some/place",
                                                "main@my-host",
                                                false);

        final Path relative4 = path4.relativize(other4);
        assertThat(relative4).isNotNull();
        assertThat(relative4.toString()).isEqualTo("some/place");

        final Path path5 = JGitPathImpl.create(fs,
                                               "path/to",
                                               "main@my-host",
                                               false);
        final Path other5 = JGitPathImpl.create(fs,
                                                "some/place",
                                                "main@my-host",
                                                false);

        final Path relative5 = path5.relativize(other5);
        assertThat(relative5).isNotNull();
        assertThat(relative5.toString()).isEqualTo("../../some/place");

        final Path path6 = JGitPathImpl.create(fs,
                                               "some/place",
                                               "main@my-host",
                                               false);
        final Path other6 = JGitPathImpl.create(fs,
                                                "path/to",
                                                "main@my-host",
                                                false);

        final Path relative6 = path6.relativize(other6);
        assertThat(relative6).isNotNull();
        assertThat(relative6.toString()).isEqualTo("../../path/to");

        final Path path7 = JGitPathImpl.create(fs,
                                               "path/to/some/thing/here",
                                               "main@my-host",
                                               false);
        final Path other7 = JGitPathImpl.create(fs,
                                                "some/place",
                                                "main@my-host",
                                                false);

        final Path relative7 = path7.relativize(other7);
        assertThat(relative7).isNotNull();
        assertThat(relative7.toString()).isEqualTo("../../../../../some/place");

        final Path path8 = JGitPathImpl.create(fs,
                                               "some/place",
                                               "main@my-host",
                                               false);
        final Path other8 = JGitPathImpl.create(fs,
                                                "path/to/some/thing/here",
                                                "main@my-host",
                                                false);

        final Path relative8 = path8.relativize(other8);
        assertThat(relative8).isNotNull();
        assertThat(relative8.toString()).isEqualTo("../../path/to/some/thing/here");

        final Path path9 = JGitPathImpl.create(fs,
                                               "/path/to",
                                               "main@my-host",
                                               false);
        final Path other9 = JGitPathImpl.create(fs,
                                                "/path/to",
                                                "main@my-host",
                                                false);

        final Path relative9 = path9.relativize(other9);
        assertThat(relative9).isNotNull();
        assertThat(relative9.toString()).isEqualTo("");

        final Path path10 = JGitPathImpl.create(fs,
                                                "path/to",
                                                "main@my-host",
                                                false);
        final Path other10 = JGitPathImpl.create(fs,
                                                 "path/to",
                                                 "main@my-host",
                                                 false);

        final Path relative10 = path10.relativize(other10);
        assertThat(relative10).isNotNull();
        assertThat(relative10.toString()).isEqualTo("");
    }
}
