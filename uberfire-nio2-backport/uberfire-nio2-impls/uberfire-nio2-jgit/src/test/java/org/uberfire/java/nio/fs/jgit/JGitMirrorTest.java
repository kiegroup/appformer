/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Condition;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.java.nio.fs.jgit.util.commands.Clone;
import org.uberfire.java.nio.fs.jgit.util.commands.ListRefs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JGitMirrorTest extends AbstractTestInfra {

    public static final String TARGET_GIT = "test/target.git";
    public static final String ORIGIN = "https://github.com/uberfire/uberfire-website";
    private static Logger logger = LoggerFactory.getLogger(JGitMirrorTest.class);

    @Test
    public void testToHTTPMirrorSuccess() throws IOException, GitAPIException {
        final File parentFolder = createTempDirectory();
        final File directory = new File(parentFolder,
                                        TARGET_GIT);
        new Clone(directory,
                  ORIGIN,
                  true,
                  null,
                  CredentialsProvider.getDefault(),
                  null,
                  null).execute();

        final Git cloned = Git.open(directory);

        assertThat(cloned).isNotNull();

        assertThat(cloned.getRepository().getAllRefs()).is(new Condition<Map<String, Ref>>() {
            @Override
            public boolean matches(final Map<String, Ref> refs) {
                final boolean hasMainRef = refs.get("refs/heads/master") != null;
                final boolean hasNewWebsiteRef = refs.get("refs/heads/new-website") != null;
                final boolean hasRemoteOriginMain = refs.get("refs/remotes/origin/master") != null;
                final boolean hasRemoteOriginNewWebSite = refs.get("refs/remotes/origin/master") != null;

                return hasMainRef && hasNewWebsiteRef && hasRemoteOriginMain & hasRemoteOriginNewWebSite;
            }
        });


        URIish remoteUri = cloned.remoteList().call().get(0).getURIs().get(0);
        String remoteUrl = remoteUri.getScheme() + "://" + remoteUri.getHost() + remoteUri.getPath();
        assertThat(remoteUrl).isEqualTo(ORIGIN);
    }

    @Test
    public void testToHTTPUnmirrorSuccess() throws IOException, GitAPIException {
        final File parentFolder = createTempDirectory();
        final File directory = new File(parentFolder,
                                        TARGET_GIT);
        new Clone(directory,
                  ORIGIN,
                  false,
                  null,
                  CredentialsProvider.getDefault(),
                  null,
                  null).execute();

        final Git cloned = Git.open(directory);

        assertThat(cloned).isNotNull();

        assertThat(cloned.getRepository().getAllRefs()).is(new Condition<Map<String, Ref>>() {
            @Override
            public boolean matches(final Map<String, Ref> refs) {
                final boolean hasMainRef = refs.get("refs/heads/master") != null;
                final boolean hasNewWebsiteRef = refs.get("refs/heads/new-website") != null;
                final boolean hasRemoteOriginMain = refs.get("refs/remotes/origin/master") != null;
                final boolean hasRemoteOriginNewWebSite = refs.get("refs/remotes/origin/master") != null;

                return hasMainRef && hasNewWebsiteRef && hasRemoteOriginMain & hasRemoteOriginNewWebSite;
            }
        });

        final boolean isMirror = cloned.getRepository().getConfig().getBoolean("remote",
                                                                               "origin",
                                                                               "mirror",
                                                                               false);
        assertFalse(isMirror);

        assertThat(new ListRefs(cloned.getRepository()).execute().get(0).getName()).isEqualTo("refs/heads/master");

        URIish remoteUri = cloned.remoteList().call().get(0).getURIs().get(0);
        String remoteUrl = remoteUri.getScheme() + "://" + remoteUri.getHost() + remoteUri.getPath();
        assertThat(remoteUrl).isEqualTo(ORIGIN);
    }

    @Test
    public void testEmptyCredentials() throws IOException, GitAPIException {
        final File parentFolder = createTempDirectory();
        final File directory = new File(parentFolder,
                                        TARGET_GIT);
        new Clone(directory,
                  ORIGIN,
                  false,
                  null,
                  null,
                  null,
                  null).execute();

        final Git cloned = Git.open(directory);

        assertThat(cloned).isNotNull();

        assertThat(new ListRefs(cloned.getRepository()).execute()).is(new Condition<List<? extends Ref>>() {
            @Override
            public boolean matches(final List<? extends Ref> refs) {
                return refs.size() > 0;
            }
        });

        assertThat(new ListRefs(cloned.getRepository()).execute().get(0).getName()).isEqualTo("refs/heads/master");

        URIish remoteUri = cloned.remoteList().call().get(0).getURIs().get(0);
        String remoteUrl = remoteUri.getScheme() + "://" + remoteUri.getHost() + remoteUri.getPath();
        assertThat(remoteUrl).isEqualTo(ORIGIN);
    }

    @Test
    public void testBadUrl() throws IOException, GitAPIException {
        final File parentFolder = createTempDirectory();
        final File directory = new File(parentFolder,
                                        TARGET_GIT);
        try {
            new Clone(directory,
                      ORIGIN + "sssss",
                      false,
                      null,
                      CredentialsProvider.getDefault(),
                      null,
                      null).execute();
            fail("If got here the test is wrong because the ORIGIN does no exist");
        } catch (Clone.CloneException ex) {
            assertThat(ex).isNotNull();
            logger.info(ex.getMessage(),
                        ex);
        }
    }
}
