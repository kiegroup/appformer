/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.m2repo.backend.server;

import org.guvnor.m2repo.backend.server.repositories.ArtifactRepositoryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class M2RepoServiceImplTest {

    @Mock
    private GuvnorM2Repository repository;

    private M2RepoServiceImpl m2RepoService;
    private String oldValue;

    @Before
    public void setUp() throws Exception {
        m2RepoService = new M2RepoServiceImpl(repository);
        doReturn("file://path-to-m2").when(repository).getRepositoryURL(ArtifactRepositoryService.GLOBAL_M2_REPO_NAME);
        oldValue = System.getProperty(ArtifactRepositoryService.GLOBAL_M2_REPO_URL);
    }

    @After
    public void tearDown() throws Exception {
        if (oldValue != null) {
            System.setProperty(ArtifactRepositoryService.GLOBAL_M2_REPO_URL, oldValue);
        }
    }

    @Test
    public void localRepoURL() throws Exception {
        assertEquals("file://path-to-m2", m2RepoService.getRepositoryURL());
    }

    @Test
    public void repoURLFromSystemProperty() throws Exception {

        System.setProperty(ArtifactRepositoryService.GLOBAL_M2_REPO_URL, "http://my-url");

        assertEquals("http://my-url", m2RepoService.getRepositoryURL());
    }
}