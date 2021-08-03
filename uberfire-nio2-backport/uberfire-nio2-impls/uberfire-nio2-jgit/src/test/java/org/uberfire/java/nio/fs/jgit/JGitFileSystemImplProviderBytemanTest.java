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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.java.nio.base.options.SquashOption;
import org.uberfire.java.nio.base.version.VersionRecord;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.fs.jgit.util.Git;
import org.uberfire.java.nio.fs.jgit.util.GitImpl;
import org.uberfire.java.nio.fs.jgit.util.commands.GetRef;

import static org.junit.Assert.*;
import static org.uberfire.java.nio.fs.jgit.JGitFileSystemProviderConfiguration.JGIT_CACHE_EVICT_THRESHOLD_DURATION;
import static org.uberfire.java.nio.fs.jgit.JGitFileSystemProviderConfiguration.JGIT_CACHE_EVICT_THRESHOLD_TIME_UNIT;

@RunWith(org.jboss.byteman.contrib.bmunit.BMUnitRunner.class)
@BMUnitConfig(loadDirectory = "target/test-classes", debug = true) // set "debug=true to see debug output
public class JGitFileSystemImplProviderBytemanTest extends AbstractTestInfra {

    private static Logger logger = LoggerFactory.getLogger(JGitFileSystemImplProviderBytemanTest.class);

    @Before
    public void createGitFsProvider() {
        provider = new JGitFileSystemProvider();
    }

    @Ignore("This test produces a strange behaviour that locks the other test. Is ignored until a solution is found.")
    @Test()
    @BMScript(value = "byteman/squash_lock.btm")
    public void testConcurrentLocking() throws IOException, GitAPIException {

        final URI newRepo = URI.create("git://byteman-lock-squash-repo");
        final JGitFileSystemImpl fs = (JGitFileSystemImpl) provider.newFileSystem(newRepo,
                                                                                  EMPTY_ENV);
        final CyclicBarrier threadsFinishedBarrier = new CyclicBarrier(3);

        final Thread t = new Thread(() -> {

            final Path main = provider.getPath(URI.create("git://main@byteman-lock-squash-repo"));
            final RevCommit commit = commitThreeTimesAndGetReference(fs,
                                                                     "byteman-lock-squash-repo",
                                                                     "main",
                                                                     "t1");

            Thread t1 = new Thread(() -> {
                logger.info("<<<<<<<<<<<<< " + commit.getName() + " --- " + commit.getFullMessage());
                printLog(fs.getGit());
                VersionRecord record = makeVersionRecord("aparedes",
                                                         "aparedes@redhat.com",
                                                         "squashing a!",
                                                         new Date(),
                                                         commit.getName());
                SquashOption squashOption = new SquashOption(record);

                logger.info("COMMITTER-1: Squashing");
                provider.setAttribute(main,
                                      SquashOption.SQUASH_ATTR,
                                      squashOption);
                printLog(fs.getGit());
                waitFor(threadsFinishedBarrier);
            });

            Thread t2 = new Thread(() -> {
                logger.info("<<<<<<<<<<<<< " + commit.getName() + " --- " + commit.getFullMessage());
                printLog(fs.getGit());
                VersionRecord record = makeVersionRecord("aparedes",
                                                         "aparedes@redhat.com",
                                                         "squashing a!",
                                                         new Date(),
                                                         commit.getName());
                SquashOption squashOption = new SquashOption(record);

                logger.info("COMMITTER-2: Squashing");
                provider.setAttribute(main,
                                      SquashOption.SQUASH_ATTR,
                                      squashOption);
                printLog(fs.getGit());
                waitFor(threadsFinishedBarrier);
            });

            t1.setName("LOCK-COMMITTER-1");
            t2.setName("LOCK-COMMITTER-2");
            t2.start();
            t1.start();

            waitFor(threadsFinishedBarrier);
        });
        try {
            t.start();
            t.join(10000);
            t.interrupt();
        } catch (InterruptedException e) {
        }

        assertEquals(3,
                     getCommitsFromBranch((GitImpl) fs.getGit(),
                                          "main").size());
    }

    @Test
    @BMScript(value = "byteman/squash.btm")
    public void testConcurrentSquashWithThreeCommit() throws IOException, GitAPIException {
        final URI newRepo = URI.create("git://three-squash-repo");
        final JGitFileSystem fs = (JGitFileSystem) provider.newFileSystem(newRepo,
                                                                          EMPTY_ENV);

        final CyclicBarrier threadsFinishedBarrier = new CyclicBarrier(3);
        final Path main = provider.getPath(URI.create("git://three-squash-repo"));
        final RevCommit commit = commitThreeTimesAndGetReference(fs,
                                                                 "three-squash-repo",
                                                                 "master",
                                                                 "t1");

        Thread t1 = new Thread(() -> {
            logger.info("<<<<<<<<<<<<< COMMIT TO SQUASH " + commit.getName() + " --- " + commit.getFullMessage());
            printLog(fs.getGit());
            VersionRecord record = makeVersionRecord("aparedes",
                                                     "aparedes@redhat.com",
                                                     "squashing a!",
                                                     new Date(),
                                                     commit.getName());
            SquashOption squashOption = new SquashOption(record);
            logger.info("COMMITTER-1: Squashing");
            provider.setAttribute(main,
                                  SquashOption.SQUASH_ATTR,
                                  squashOption);
            printLog(fs.getGit());
            waitFor(threadsFinishedBarrier);
        });

        Thread t2 = new Thread(() -> {
            logger.info("<<<<<<<<<<<<< COMMIT TO SQUASH " + commit.getName() + " --- " + commit.getFullMessage());
            printLog(fs.getGit());
            VersionRecord record = makeVersionRecord("aparedes",
                                                     "aparedes@redhat.com",
                                                     "squashing b!",
                                                     new Date(),
                                                     commit.getName());
            SquashOption squashOption = new SquashOption(record);
            logger.info("COMMITTER-2: Squashing");
            provider.setAttribute(main,
                                  SquashOption.SQUASH_ATTR,
                                  squashOption);
            printLog(fs.getGit());
            waitFor(threadsFinishedBarrier);
        });

        t1.setName("COMMITTER-1");
        t2.setName("COMMITTER-2");
        t2.start();
        t1.start();

        waitFor(threadsFinishedBarrier);

        assertEquals(2,
                     getCommitsFromBranch((GitImpl) fs.getGit(),
                                          "master").size());
    }

    @Test
    @BMScript(value = "byteman/squash.btm")
    public void testConcurrentSquashWithSixCommit() throws IOException, GitAPIException {
        final URI newRepo = URI.create("git://byteman-six-squash-repo");
        final JGitFileSystem fs = (JGitFileSystem) provider.newFileSystem(newRepo,
                                                                          EMPTY_ENV);

        final CyclicBarrier threadsFinishedBarrier = new CyclicBarrier(3);
        final Path main = provider.getPath(URI.create("git://main@byteman-six-squash-repo"));
        final RevCommit commit = commitSixTimesAndGetReference(fs,
                                                               "byteman-six-squash-repo",
                                                               "main",
                                                               "t1");

        Thread t1 = new Thread(() -> {
            logger.info("<<<<<<<<<<<<< COMMIT TO SQUASH " + commit.getName() + " --- " + commit.getFullMessage());
            printLog(fs.getGit());
            VersionRecord record = makeVersionRecord("aparedes",
                                                     "aparedes@redhat.com",
                                                     "squashing a!",
                                                     new Date(),
                                                     commit.getName());
            SquashOption squashOption = new SquashOption(record);
            logger.info("COMMITTER-1: Squashing");
            provider.setAttribute(main,
                                  SquashOption.SQUASH_ATTR,
                                  squashOption);
            printLog(fs.getGit());
            waitFor(threadsFinishedBarrier);
        });

        Thread t2 = new Thread(() -> {
            logger.info("<<<<<<<<<<<<< COMMIT TO SQUASH " + commit.getName() + " --- " + commit.getFullMessage());
            printLog(fs.getGit());
            VersionRecord record = makeVersionRecord("aparedes",
                                                     "aparedes@redhat.com",
                                                     "squashing b!",
                                                     new Date(),
                                                     commit.getName());
            SquashOption squashOption = new SquashOption(record);
            logger.info("COMMITTER-2: Squashing");
            provider.setAttribute(main,
                                  SquashOption.SQUASH_ATTR,
                                  squashOption);
            printLog(fs.getGit());
            waitFor(threadsFinishedBarrier);
        });

        t1.setName("COMMITTER-1");
        t2.setName("COMMITTER-2");
        t2.start();
        t1.start();

        waitFor(threadsFinishedBarrier);

        assertEquals(2,
                     getCommitsFromBranch((GitImpl) fs.getGit(),
                                          "main").size());
    }

    @Test
    @BMScript(value = "byteman/squash_exception.btm")
    public void testForceExceptionWhenTryingToSquash() throws IOException, GitAPIException {

        final URI newRepo = URI.create("git://byteman-exception-squash-repo");
        final JGitFileSystem fs = (JGitFileSystem) provider.newFileSystem(newRepo,
                                                                          EMPTY_ENV);

        final Path main = provider.getPath(URI.create("git://main@byteman-exception-squash-repo"));
        final RevCommit commit = commitThreeTimesAndGetReference(fs,
                                                                 "byteman-exception-squash-repo",
                                                                 "main",
                                                                 "t1");

        logger.info("<<<<<<<<<<<<< COMMIT TO SQUASH " + commit.getName() + " --- " + commit.getFullMessage());
        printLog(fs.getGit());
        VersionRecord record = makeVersionRecord("aparedes",
                                                 "aparedes@redhat.com",
                                                 "squashing a!",
                                                 new Date(),
                                                 commit.getName());
        SquashOption squashOption = new SquashOption(record);
        logger.info("COMMITTER-1: Squashing");

        try {
            provider.setAttribute(main,
                                  SquashOption.SQUASH_ATTR,
                                  squashOption);
        } catch (Exception e) {
            fs.lock();
            fs.unlock();
        }

        assertEquals(3,
                     getCommitsFromBranch((GitImpl) fs.getGit(),
                                          "main").size());
    }

    @Test
    @BMScript(value = "byteman/commit_exception.btm")
    public void testFileSystemLockOnException() throws IOException, GitAPIException {

        final URI newRepo = URI.create("git://byteman-exception-commit-repo");
        final JGitFileSystemProxy fsProxy = (JGitFileSystemProxy) provider.newFileSystem(newRepo,
                                                                                         EMPTY_ENV);
        JGitFileSystem fs = fsProxy.getRealJGitFileSystem();

        final Path path = provider.getPath(URI.create("git://main@byteman-exception-commit-repo/myfile.txt"));

        try {
            writeFile(fs,
                      path,
                      "main");
        } catch (RuntimeException e) {
        }

        // fs must be unlocked
        Object lock = null;
        try {
            Field field = JGitFileSystemImpl.class.getDeclaredField("lock");
            field.setAccessible(true);
            lock = field.get(fs);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        Object isLocked = null;
        try {
            Method method = lock.getClass().getMethod("hasBeenInUse");
            isLocked = method.invoke(lock);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue(((Boolean) isLocked));
    }

    private VersionRecord makeVersionRecord(final String author,
                                            final String email,
                                            final String comment,
                                            final Date date,
                                            final String commit) {
        return new VersionRecord() {
            @Override
            public String id() {
                return commit;
            }

            @Override
            public String author() {
                return author;
            }

            @Override
            public String email() {
                return email;
            }

            @Override
            public String comment() {
                return comment;
            }

            @Override
            public Date date() {
                return date;
            }

            @Override
            public String uri() {
                return null;
            }
        };
    }

    private static void printLog(final Git git) {
        try {
            for (final RevCommit revCommit : ((GitImpl) git)._log().call()) {
                logger.info("[LOG]: " + revCommit.getName() + " --- " + revCommit.getFullMessage());
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    protected static void waitFor(CyclicBarrier barrier) {
        String threadName = Thread.currentThread().getName();
        try {
            logger.info(threadName + " request for await");
            barrier.await();
            logger.info(threadName + " await finished");
        } catch (InterruptedException e) {
            fail("Thread '" + threadName + "' was interrupted while waiting for the other threads!");
        } catch (BrokenBarrierException e) {
            fail("Thread '" + threadName + "' barrier was broken while waiting for the other threads!");
        }
    }

    private RevCommit commitThreeTimesAndGetReference(JGitFileSystem fs,
                                                      String repo,
                                                      String branch,
                                                      String thread) {
        try {
            final Path path = provider.getPath(URI.create("git://" + branch + "@" + repo + "/" + thread + "-myfile1.txt"));
            final Path path2 = provider.getPath(URI.create("git://" + branch + "@" + repo + "/" + thread + "-myfile2.txt"));
            final Path path3 = provider.getPath(URI.create("git://" + branch + "@" + repo + "/" + thread + "-myfile3.txt"));

            final RevCommit commit = writeFile(fs,
                                               path,
                                               branch);
            writeFile(fs,
                      path2,
                      branch);
            writeFile(fs,
                      path3,
                      branch);

            return commit;
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private RevCommit commitSixTimesAndGetReference(JGitFileSystem fs,
                                                    String repo,
                                                    String branch,
                                                    String thread) {
        try {
            final Path path = provider.getPath(URI.create("git://" + branch + "@" + repo + "/" + thread + "-myfile1.txt"));
            final Path path2 = provider.getPath(URI.create("git://" + branch + "@" + repo + "/" + thread + "-myfile2.txt"));
            final Path path3 = provider.getPath(URI.create("git://" + branch + "@" + repo + "/" + thread + "-myfile3.txt"));
            final Path path4 = provider.getPath(URI.create("git://" + branch + "@" + repo + "/" + thread + "-myfile4.txt"));
            final Path path5 = provider.getPath(URI.create("git://" + branch + "@" + repo + "/" + thread + "-myfile5.txt"));
            final Path path6 = provider.getPath(URI.create("git://" + branch + "@" + repo + "/" + thread + "-myfile6.txt"));

            final RevCommit commit = writeFile(fs,
                                               path,
                                               branch);
            writeFile(fs,
                      path2,
                      branch);
            writeFile(fs,
                      path3,
                      branch);
            writeFile(fs,
                      path4,
                      branch);
            writeFile(fs,
                      path5,
                      branch);
            writeFile(fs,
                      path6,
                      branch);

            return commit;
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private RevCommit writeFile(final JGitFileSystem fs,
                                final Path path,
                                String branch) throws IOException, GitAPIException {
        final OutputStream stream = provider.newOutputStream(path);
        logger.info("Writing file: " + path.getFileName().toString());
        stream.write("my cool content".getBytes());
        stream.close();
        return this.getCommitsFromBranch((GitImpl) fs.getGit(),
                                         branch).get(0);
    }

    private List<RevCommit> getCommitsFromBranch(final GitImpl origin,
                                                 String branch) throws GitAPIException, MissingObjectException, IncorrectObjectTypeException {
        List<RevCommit> commits = new ArrayList<>();
        final ObjectId id = new GetRef(origin.getRepository(),
                                       branch).execute().getObjectId();
        for (RevCommit commit : origin._log().add(id).call()) {
            commits.add(commit);
        }
        return commits;
    }
}


