/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;
import org.uberfire.java.nio.base.TextualDiff;
import org.uberfire.java.nio.fs.jgit.util.Git;
import org.uberfire.java.nio.fs.jgit.util.GitImpl;
import org.uberfire.java.nio.fs.jgit.util.commands.Commit;
import org.uberfire.java.nio.fs.jgit.util.commands.CreateBranch;
import org.uberfire.java.nio.fs.jgit.util.commands.CreateRepository;
import org.uberfire.java.nio.fs.jgit.util.exceptions.GitException;

import static org.assertj.core.api.Assertions.assertThat;

public class JGitTextualDiffBranchesTest extends AbstractTestInfra {

    private Git git;

    private static final String MAIN_BRANCH = "main";
    private static final String DEVELOP_BRANCH = "develop";

    private static final List<String> TXT_FILES =
            Stream.of("file0", "file1", "file2", "file3", "file4")
                    .collect(Collectors.toList());

    private static final String[] COMMON_TXT_LINES = {"Line1", "Line2", "Line3", "Line4"};

    @Before
    public void setup() throws IOException {
        final File parentFolder = createTempDirectory();

        final File gitSource = new File(parentFolder, "source/source.git");

        git = new CreateRepository(gitSource).execute().get();

        commit(git, MAIN_BRANCH, "Adding files into main",
               content(TXT_FILES.get(0), multiline(TXT_FILES.get(0), COMMON_TXT_LINES)),
               content(TXT_FILES.get(1), multiline(TXT_FILES.get(1), COMMON_TXT_LINES)),
               content(TXT_FILES.get(2), multiline(TXT_FILES.get(2), COMMON_TXT_LINES)));

        new CreateBranch((GitImpl) git, MAIN_BRANCH, DEVELOP_BRANCH).execute();
    }

    @Test
    public void testDiffWithAddedFiles() throws IOException {
        commit(git, DEVELOP_BRANCH, "Adding files",
               content(TXT_FILES.get(3), multiline(TXT_FILES.get(3), COMMON_TXT_LINES)),
               content(TXT_FILES.get(4), multiline(TXT_FILES.get(4), COMMON_TXT_LINES)));

        List<TextualDiff> diffs = git.textualDiffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(diffs).isNotEmpty();
        assertThat(diffs).hasSize(2);

        diffs.forEach(diff -> {
            assertThat(diff.getChangeType()).isEqualTo(DiffEntry.ChangeType.ADD.toString());
            assertThat(diff.getDiffText()).isNotEmpty();
        });

        assertThat(diffs.get(0).getOldFilePath()).isEqualTo(DiffEntry.DEV_NULL);
        assertThat(diffs.get(0).getNewFilePath()).isEqualTo(TXT_FILES.get(3));
        assertThat(diffs.get(0).getLinesAdded()).isEqualTo(4);
        assertThat(diffs.get(0).getLinesDeleted()).isEqualTo(0);
    }

    @Test
    public void testDiffWithAddedFilesSameBranch() throws IOException {
        RevCommit startCommit = git.getLastCommit(MAIN_BRANCH);

        commit(git, MAIN_BRANCH, "Adding files",
               content(TXT_FILES.get(3), multiline(TXT_FILES.get(3), COMMON_TXT_LINES)),
               content(TXT_FILES.get(4), multiline(TXT_FILES.get(4), COMMON_TXT_LINES)));

        RevCommit endCommit = git.getLastCommit(MAIN_BRANCH);

        List<TextualDiff> diffs = git.textualDiffRefs(MAIN_BRANCH,
                                                      MAIN_BRANCH,
                                                      startCommit.getName(),
                                                      endCommit.getName());

        assertThat(diffs).isNotEmpty();
        assertThat(diffs).hasSize(2);
    }

    @Test
    public void testDiffWithAddedFileAndTargetUpdatedLater() throws IOException {
        commit(git, DEVELOP_BRANCH, "Adding file",
               content(TXT_FILES.get(3), multiline(TXT_FILES.get(3), COMMON_TXT_LINES)));

        commit(git, MAIN_BRANCH, "Adding file",
               content(TXT_FILES.get(4), multiline(TXT_FILES.get(4), COMMON_TXT_LINES)));

        List<TextualDiff> diffs = git.textualDiffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(diffs).isNotEmpty();
        assertThat(diffs).hasSize(1);

        assertThat(diffs.get(0).getChangeType()).isEqualTo(DiffEntry.ChangeType.ADD.toString());
        assertThat(diffs.get(0).getDiffText()).isNotEmpty();

        assertThat(diffs.get(0).getOldFilePath()).isEqualTo(DiffEntry.DEV_NULL);
        assertThat(diffs.get(0).getNewFilePath()).isEqualTo(TXT_FILES.get(3));
        assertThat(diffs.get(0).getLinesAdded()).isEqualTo(4);
        assertThat(diffs.get(0).getLinesDeleted()).isEqualTo(0);
    }

    @Test
    public void testDiffWithRemovedFile() {
        new Commit(git, DEVELOP_BRANCH, "name", "name@example.com", "Removing file",
                   null, null, false,
                   new HashMap<String, File>() {{
                       put(TXT_FILES.get(0), null);
                   }}).execute();

        List<TextualDiff> diffs = git.textualDiffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(diffs).isNotEmpty();
        assertThat(diffs).hasSize(1);

        assertThat(diffs.get(0).getChangeType()).isEqualTo(DiffEntry.ChangeType.DELETE.toString());
        assertThat(diffs.get(0).getOldFilePath()).isEqualTo(TXT_FILES.get(0));
        assertThat(diffs.get(0).getNewFilePath()).isEqualTo(DiffEntry.DEV_NULL);
        assertThat(diffs.get(0).getLinesAdded()).isEqualTo(0);
        assertThat(diffs.get(0).getLinesDeleted()).isEqualTo(4);
        assertThat(diffs.get(0).getDiffText()).isNotEmpty();
    }

    @Test
    public void testDiffWithUpdatedFiles() throws IOException {
        commit(git, DEVELOP_BRANCH, "Updating files",
               content(TXT_FILES.get(1), multiline(TXT_FILES.get(1), "Line1", "Line2Changed", "Line3", "Line4")),
               content(TXT_FILES.get(2), multiline(TXT_FILES.get(2), "Line1", "Line2Changed", "Line3", "Line4")));

        List<TextualDiff> diffs = git.textualDiffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(diffs).isNotEmpty();
        assertThat(diffs).hasSize(2);

        diffs.forEach(diff -> {
            assertThat(diff.getChangeType()).isEqualTo(DiffEntry.ChangeType.MODIFY.toString());
            assertThat(diff.getOldFilePath()).isEqualTo(diff.getNewFilePath());
            assertThat(diff.getLinesAdded()).isEqualTo(1);
            assertThat(diff.getLinesDeleted()).isEqualTo(1);
            assertThat(diff.getDiffText()).isNotEmpty();
        });

        assertThat(diffs.get(0).getOldFilePath()).isEqualTo(TXT_FILES.get(1));
        assertThat(diffs.get(1).getOldFilePath()).isEqualTo(TXT_FILES.get(2));
    }

    @Test
    public void testDiffWithUpdateFirstAndLastLines() throws IOException {
        commit(git, DEVELOP_BRANCH, "Updating file",
               content(TXT_FILES.get(1), multiline(TXT_FILES.get(1), "Line1Changed", "Line2", "Line3", "Line4Changed")));

        List<TextualDiff> fileDiffs = git.textualDiffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(fileDiffs).isNotEmpty();
        assertThat(fileDiffs).hasSize(1);
    }

    @Test
    public void testDiffWithEvenBranches() {
        List<TextualDiff> diffs = git.textualDiffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(diffs).isEmpty();
    }

    @Test(expected = GitException.class)
    public void testDiffWithNonExistentBranch() {
        List<TextualDiff> diffs = git.textualDiffRefs(MAIN_BRANCH, "nonExistentBranch");

        assertThat(diffs).isEmpty();
    }
}