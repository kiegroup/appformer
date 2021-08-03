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
import org.junit.Before;
import org.junit.Test;
import org.uberfire.java.nio.base.FileDiff;
import org.uberfire.java.nio.fs.jgit.util.Git;
import org.uberfire.java.nio.fs.jgit.util.GitImpl;
import org.uberfire.java.nio.fs.jgit.util.commands.Commit;
import org.uberfire.java.nio.fs.jgit.util.commands.CreateBranch;
import org.uberfire.java.nio.fs.jgit.util.commands.CreateRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class JGitDiffBranchesTest extends AbstractTestInfra {

    private Git git;

    private static final String MAIN_BRANCH = "main";
    private static final String DEVELOP_BRANCH = "develop";
    private static final String NON_EXISTENT_FILE = "/dev/null";

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

        List<FileDiff> fileDiffs = git.diffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(fileDiffs).isNotEmpty();
        assertThat(fileDiffs).hasSize(2);

        fileDiffs.forEach(diff -> assertThat(diff.getChangeType()).isEqualTo(DiffEntry.ChangeType.ADD.toString()));

        assertThat(fileDiffs.get(0).getNameA()).isEqualTo(NON_EXISTENT_FILE);
        assertThat(fileDiffs.get(0).getNameB()).isEqualTo(TXT_FILES.get(3));
        assertThat(fileDiffs.get(0).getStartA()).isEqualTo(0);
        assertThat(fileDiffs.get(0).getEndA()).isEqualTo(0);
        assertThat(fileDiffs.get(0).getStartB()).isEqualTo(0);
        assertThat(fileDiffs.get(0).getEndB()).isEqualTo(4);
    }

    @Test
    public void testDiffWithRemovedFile() {
        new Commit(git, DEVELOP_BRANCH, "name", "name@example.com", "Removing file",
                   null, null, false,
                   new HashMap<String, File>() {{
                       put(TXT_FILES.get(0), null);
                   }}).execute();

        List<FileDiff> fileDiffs = git.diffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(fileDiffs).isNotEmpty();
        assertThat(fileDiffs).hasSize(1);

        assertThat(fileDiffs.get(0).getChangeType()).isEqualTo(DiffEntry.ChangeType.DELETE.toString());
        assertThat(fileDiffs.get(0).getNameA()).isEqualTo(TXT_FILES.get(0));
        assertThat(fileDiffs.get(0).getNameB()).isEqualTo(NON_EXISTENT_FILE);
        assertThat(fileDiffs.get(0).getStartA()).isEqualTo(0);
        assertThat(fileDiffs.get(0).getEndA()).isEqualTo(4);
        assertThat(fileDiffs.get(0).getStartB()).isEqualTo(0);
        assertThat(fileDiffs.get(0).getEndB()).isEqualTo(0);
    }

    @Test
    public void testDiffWithUpdatedFiles() throws IOException {
        commit(git, DEVELOP_BRANCH, "Updating files",
               content(TXT_FILES.get(1), multiline(TXT_FILES.get(1), "Line1", "Line2Changed", "Line3", "Line4")),
               content(TXT_FILES.get(2), multiline(TXT_FILES.get(2), "Line1", "Line2Changed", "Line3", "Line4")));

        List<FileDiff> fileDiffs = git.diffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(fileDiffs).isNotEmpty();
        assertThat(fileDiffs).hasSize(2);

        fileDiffs.forEach(diff -> assertThat(diff.getChangeType()).isEqualTo(DiffEntry.ChangeType.MODIFY.toString()));

        assertThat(fileDiffs.get(0).getNameA()).isEqualTo(TXT_FILES.get(1));
        assertThat(fileDiffs.get(1).getNameB()).isEqualTo(TXT_FILES.get(2));
    }

    @Test
    public void testDiffWithUpdateFirstLine() throws IOException {
        commit(git, DEVELOP_BRANCH, "Updating file",
               content(TXT_FILES.get(1), multiline(TXT_FILES.get(1),"Line1Changed", "Line2", "Line3", "Line4")));

        List<FileDiff> fileDiffs = git.diffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(fileDiffs).isNotEmpty();
        assertThat(fileDiffs).hasSize(1);
        assertThat(fileDiffs.get(0).getChangeType()).isEqualTo(DiffEntry.ChangeType.MODIFY.toString());
        assertThat(fileDiffs.get(0).getNameA()).isEqualTo(TXT_FILES.get(1));
        assertThat(fileDiffs.get(0).getNameB()).isEqualTo(TXT_FILES.get(1));
        assertThat(fileDiffs.get(0).getStartA()).isEqualTo(0);
        assertThat(fileDiffs.get(0).getEndA()).isEqualTo(1);
        assertThat(fileDiffs.get(0).getStartB()).isEqualTo(0);
        assertThat(fileDiffs.get(0).getEndB()).isEqualTo(1);
    }

    @Test
    public void testDiffWithUpdateLastLine() throws IOException {
        commit(git, DEVELOP_BRANCH, "Updating file",
               content(TXT_FILES.get(1), multiline(TXT_FILES.get(1),"Line1", "Line2", "Line3", "Line4Changed")));

        List<FileDiff> fileDiffs = git.diffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(fileDiffs).isNotEmpty();
        assertThat(fileDiffs).hasSize(1);
        assertThat(fileDiffs.get(0).getChangeType()).isEqualTo(DiffEntry.ChangeType.MODIFY.toString());
        assertThat(fileDiffs.get(0).getNameA()).isEqualTo(TXT_FILES.get(1));
        assertThat(fileDiffs.get(0).getNameB()).isEqualTo(TXT_FILES.get(1));
        assertThat(fileDiffs.get(0).getStartA()).isEqualTo(3);
        assertThat(fileDiffs.get(0).getEndA()).isEqualTo(4);
        assertThat(fileDiffs.get(0).getStartB()).isEqualTo(3);
        assertThat(fileDiffs.get(0).getEndB()).isEqualTo(4);
    }

    @Test
    public void testDiffWithUpdateTwoConsecutiveLines() throws IOException {
        commit(git, DEVELOP_BRANCH, "Updating file",
               content(TXT_FILES.get(1), multiline(TXT_FILES.get(1),"Line1", "Line2Changed", "Line3Changed", "Line4")));

        List<FileDiff> fileDiffs = git.diffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(fileDiffs).isNotEmpty();
        assertThat(fileDiffs).hasSize(1);
        assertThat(fileDiffs.get(0).getChangeType()).isEqualTo(DiffEntry.ChangeType.MODIFY.toString());
        assertThat(fileDiffs.get(0).getNameA()).isEqualTo(TXT_FILES.get(1));
        assertThat(fileDiffs.get(0).getNameB()).isEqualTo(TXT_FILES.get(1));
        assertThat(fileDiffs.get(0).getStartA()).isEqualTo(1);
        assertThat(fileDiffs.get(0).getEndA()).isEqualTo(3);
        assertThat(fileDiffs.get(0).getStartB()).isEqualTo(1);
        assertThat(fileDiffs.get(0).getEndB()).isEqualTo(3);
    }

    @Test
    public void testDiffWithUpdateFirstAndLastLines() throws IOException {
        commit(git, DEVELOP_BRANCH, "Updating file",
               content(TXT_FILES.get(1), multiline(TXT_FILES.get(1),"Line1Changed", "Line2", "Line3", "Line4Changed")));

        List<FileDiff> fileDiffs = git.diffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(fileDiffs).isNotEmpty();
        assertThat(fileDiffs).hasSize(2);

        fileDiffs.forEach(diff -> {
            assertThat(diff.getChangeType()).isEqualTo(DiffEntry.ChangeType.MODIFY.toString());
            assertThat(diff.getNameA()).isEqualTo(TXT_FILES.get(1));
            assertThat(diff.getNameB()).isEqualTo(TXT_FILES.get(1));
        });

        assertThat(fileDiffs.get(0).getStartA()).isEqualTo(0);
        assertThat(fileDiffs.get(0).getEndA()).isEqualTo(1);
        assertThat(fileDiffs.get(0).getStartB()).isEqualTo(0);
        assertThat(fileDiffs.get(0).getEndB()).isEqualTo(1);

        assertThat(fileDiffs.get(1).getStartA()).isEqualTo(3);
        assertThat(fileDiffs.get(1).getEndA()).isEqualTo(4);
        assertThat(fileDiffs.get(1).getStartB()).isEqualTo(3);
        assertThat(fileDiffs.get(1).getEndB()).isEqualTo(4);
    }

    @Test
    public void testDiffWithAddedRemovedUpdatedFiles() throws IOException {
        new Commit(git, DEVELOP_BRANCH, "name", "name@example.com", "Removing file0",
                   null, null, false,
                   new HashMap<String, File>() {{
                       put(TXT_FILES.get(0), null);
                   }}).execute();

        commit(git, DEVELOP_BRANCH, "Updating file1",
               content(TXT_FILES.get(1), multiline(TXT_FILES.get(1), "Line1", "Line2Changed", "Line3", "Line4")));

        commit(git, DEVELOP_BRANCH, "Adding file3",
               content(TXT_FILES.get(3), multiline(TXT_FILES.get(3), COMMON_TXT_LINES)));

        List<FileDiff> fileDiffs = git.diffRefs(MAIN_BRANCH, DEVELOP_BRANCH);

        assertThat(fileDiffs).isNotEmpty();
        assertThat(fileDiffs).hasSize(3);

        assertThat(fileDiffs.get(0).getChangeType()).isEqualTo(DiffEntry.ChangeType.DELETE.toString());
        assertThat(fileDiffs.get(0).getNameA()).isEqualTo(TXT_FILES.get(0));
        assertThat(fileDiffs.get(0).getNameB()).isEqualTo(NON_EXISTENT_FILE);

        assertThat(fileDiffs.get(1).getChangeType()).isEqualTo(DiffEntry.ChangeType.MODIFY.toString());
        assertThat(fileDiffs.get(1).getNameA()).isEqualTo(TXT_FILES.get(1));
        assertThat(fileDiffs.get(1).getNameB()).isEqualTo(TXT_FILES.get(1));

        assertThat(fileDiffs.get(2).getChangeType()).isEqualTo(DiffEntry.ChangeType.ADD.toString());
        assertThat(fileDiffs.get(2).getNameA()).isEqualTo(NON_EXISTENT_FILE);
        assertThat(fileDiffs.get(2).getNameB()).isEqualTo(TXT_FILES.get(3));
    }

    @Test
    public void testDiffWithNonExistentBranch() {
        List<FileDiff> fileDiffs = git.diffRefs(MAIN_BRANCH, "nonExistentBranch");

        assertThat(fileDiffs).isEmpty();
    }
}