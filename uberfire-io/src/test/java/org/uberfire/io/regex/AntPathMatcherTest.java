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

package org.uberfire.io.regex;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.uberfire.io.CommonIOServiceDotFileTest;
import org.uberfire.java.nio.file.FileSystems;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.Paths;
import org.uberfire.java.nio.file.api.FileSystemProviders;
import org.uberfire.java.nio.fs.jgit.JGitFileSystemProvider;

import static org.uberfire.io.regex.AntPathMatcher.excludes;
import static org.uberfire.io.regex.AntPathMatcher.filter;
import static org.uberfire.io.regex.AntPathMatcher.includes;

public class AntPathMatcherTest {

    private static File path;

    @BeforeClass
    public static void setup() throws IOException {
        path = CommonIOServiceDotFileTest.createTempDirectory();

        // XXX this is shaky at best: FileSystemProviders bootstraps the JGit FS in a static initializer.
        //     if anything has referenced it before now, setting this system property will have no effect.
        System.setProperty("org.uberfire.nio.git.dir",
                           path.getAbsolutePath());
        System.out.println(".niogit: " + path.getAbsolutePath());

        final URI newRepo = URI.create("git://antpathmatcher");

        FileSystems.newFileSystem(newRepo,
                                  new HashMap<String, Object>());
    }

    @AfterClass
    public static void cleanup() {
        FileUtils.deleteQuietly(path);
        JGitFileSystemProvider gitFsProvider = (JGitFileSystemProvider) FileSystemProviders.resolveProvider(URI.create("git://whatever"));
        gitFsProvider.shutdown();
        FileUtils.deleteQuietly(gitFsProvider.getGitRepoContainerDir());
    }

    @Test
    public void testIncludes() {
        final Collection<String> patterns = new ArrayList<String>() {{
            add("git://**");
            add("**/repo/**");
        }};

        {
            final Path path = Paths.get(URI.create("file:///Users/home"));
            Assert.assertFalse(includes(patterns,
                                        path));
        }

        {
            final Path path = Paths.get(URI.create("git://antpathmatcher"));
            Assert.assertTrue(includes(patterns,
                                       path));
        }

        {
            final Path path = Paths.get(URI.create("git://main@antpathmatcher"));
            Assert.assertTrue(includes(patterns,
                                       path));
        }
    }

    @Test
    public void testIncludesMid() {
        final Collection<String> patterns = new ArrayList<String>() {{
            add("default://**");
            add("**/repo/**");
        }};

        {
            final Path path = Paths.get(URI.create("file:///Users/home"));
            Assert.assertTrue(includes(patterns,
                                       path));
        }

        {
            final Path path = Paths.get(URI.create("git://antpathmatcher"));
            Assert.assertFalse(includes(patterns,
                                        path));
        }

        {
            final Path path = Paths.get(URI.create("git://main@antpathmatcher/repo/sss"));
            Assert.assertTrue(includes(patterns,
                                       path));
        }
    }

    @Test
    public void testExcludes() {
        final Collection<String> patterns = new ArrayList<String>() {{
            add("git://**");
            add("**/repo/**");
        }};

        {
            final Path path = Paths.get(URI.create("file:///Users/home"));
            Assert.assertFalse(excludes(patterns,
                                        path));
        }

        {
            final Path path = Paths.get(URI.create("git://antpathmatcher"));
            Assert.assertTrue(excludes(patterns,
                                       path));
        }

        {
            final Path path = Paths.get(URI.create("git://main@antpathmatcher"));
            Assert.assertTrue(excludes(patterns,
                                       path));
        }
    }

    @Test
    public void testExcludesMid() {
        final Collection<String> patterns = new ArrayList<String>() {{
            add("default://**");
            add("**/repo/**");
        }};

        {
            final Path path = Paths.get(URI.create("file:///Users/home"));
            Assert.assertTrue(excludes(patterns,
                                       path));
        }

        {
            final Path path = Paths.get(URI.create("git://antpathmatcher"));
            Assert.assertFalse(excludes(patterns,
                                        path));
        }

        {
            final Path path = Paths.get(URI.create("git://main@antpathmatcher/repo/sss"));
            Assert.assertTrue(excludes(patterns,
                                       path));
        }
    }

    @Test
    public void testFilter() {
        final Collection<String> includes = new ArrayList<String>() {{
            add("git://**");
        }};
        final Collection<String> excludes = new ArrayList<String>() {{
            add("default://**");
        }};

        {
            final Path path = Paths.get(URI.create("file:///Users/home"));
            Assert.assertFalse(filter(includes,
                                      excludes,
                                      path));
        }

        {
            final Path path = Paths.get(URI.create("git://antpathmatcher"));
            Assert.assertTrue(filter(includes,
                                     excludes,
                                     path));
        }

        {
            final Path path = Paths.get(URI.create("git://main@antpathmatcher/repo/sss"));
            Assert.assertTrue(filter(includes,
                                     excludes,
                                     path));
        }

        Assert.assertTrue(filter(Collections.<String>emptyList(),
                                 Collections.<String>emptyList(),
                                 Paths.get(URI.create("git://main@antpathmatcher/repo/sss"))));
        Assert.assertTrue(filter(Collections.<String>emptyList(),
                                 Collections.<String>emptyList(),
                                 Paths.get(URI.create("git://antpathmatcher"))));
    }

    @Test
    public void testIncludesUri() {
        final Collection<String> patterns = new ArrayList<String>() {{
            add("git://**");
            add("**/repo/**");
        }};

        Assert.assertFalse(includes(patterns,
                                    URI.create("file:///Users/home")));

        Assert.assertTrue(includes(patterns,
                                   URI.create("git://antpathmatcher")));

        Assert.assertTrue(includes(patterns,
                                   URI.create("git://main@antpathmatcher")));
    }

    @Test
    public void testIncludesMidUri() {
        final Collection<String> patterns = new ArrayList<String>() {{
            add("file://**");
            add("**/repo/**");
        }};

        Assert.assertTrue(includes(patterns,
                                   URI.create("file:///Users/home")));

        Assert.assertFalse(includes(patterns,
                                    URI.create("git://antpathmatcher")));

        Assert.assertTrue(includes(patterns,
                                   URI.create("git://main@antpathmatcher/repo/sss")));
    }

    @Test
    public void testExcludesUri() {
        final Collection<String> patterns = new ArrayList<String>() {{
            add("git://**");
            add("**/repo/**");
        }};

        Assert.assertFalse(excludes(patterns,
                                    URI.create("file:///Users/home")));

        Assert.assertTrue(excludes(patterns,
                                   URI.create("git://antpathmatcher")));

        Assert.assertTrue(excludes(patterns,
                                   URI.create("git://main@antpathmatcher")));
    }

    @Test
    public void testExcludesMidUri() {
        final Collection<String> patterns = new ArrayList<String>() {{
            add("file://**");
            add("**/repo/**");
        }};

        Assert.assertTrue(excludes(patterns,
                                   URI.create("file:///Users/home")));

        Assert.assertFalse(excludes(patterns,
                                    URI.create("git://antpathmatcher")));

        Assert.assertTrue(excludes(patterns,
                                   URI.create("git://main@antpathmatcher/repo/sss")));
    }

    @Test
    public void testFilterUri() {
        final Collection<String> includes = new ArrayList<String>() {{
            add("git://**");
        }};
        final Collection<String> excludes = new ArrayList<String>() {{
            add("file://**");
        }};

        Assert.assertFalse(filter(includes,
                                  excludes,
                                  URI.create("file:///Users/home")));

        Assert.assertTrue(filter(includes,
                                 excludes,
                                 URI.create("git://antpathmatcher")));

        Assert.assertTrue(filter(includes,
                                 excludes,
                                 URI.create("git://main@antpathmatcher/repo/sss")));

        Assert.assertTrue(filter(Collections.<String>emptyList(),
                                 Collections.<String>emptyList(),
                                 URI.create("file:///Users/home")));

        Assert.assertTrue(filter(Collections.<String>emptyList(),
                                 Collections.<String>emptyList(),
                                 URI.create("git://main@antpathmatcher/repo/sss")));
    }
}
