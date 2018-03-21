package org.uberfire.java.nio.fs.jgit.util.commands;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.lib.Ref;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.commons.data.Pair;
import org.uberfire.java.nio.fs.jgit.util.GitImpl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SyncRemoteTest {

    private static final String MASTER = "master";
    private static final String RELEASE = "release";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GitImpl git;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ListBranchCommand listBranchCommand;

    private SyncRemote syncRemote;

    @Mock
    private Ref branchMaster;

    @Mock
    private Ref branchRemoteMaster;

    @Mock
    private Ref branchDevelop;

    @Mock
    private Ref branchRemoteHead;

    @Mock
    private Ref branchRemoteRelease;

    @Before
    public void setUp() throws GitAPIException {

        when(branchMaster.getName()).thenReturn("refs/" + MASTER);
        when(branchDevelop.getName()).thenReturn("refs/develop");
        when(branchRemoteHead.getName()).thenReturn("refs/remotes/HEAD");
        when(branchRemoteMaster.getName()).thenReturn("refs/remotes/" + MASTER);
        when(branchRemoteRelease.getName()).thenReturn("refs/remotes/" + RELEASE);

        List<Ref> branches = Arrays.asList(branchMaster,
                                           branchDevelop,
                                           branchRemoteMaster,
                                           branchRemoteHead,
                                           branchRemoteRelease);

        when(listBranchCommand.setListMode(any())).thenReturn(listBranchCommand);
        when(listBranchCommand.call()).thenReturn(branches);

        when(this.git._branchList()).thenReturn(listBranchCommand);

        syncRemote = spy(new SyncRemote(git,
                                        new Pair<>("origin",
                                                   "")));
    }

    @Test
    public void testCreateBranches() throws GitAPIException {

        syncRemote.execute();

        verify(syncRemote,
               times(1)).createBranch(eq(MASTER));
        verify(syncRemote,
               never()).createBranch(eq("HEAD"));
        verify(syncRemote,
               never()).createBranch(eq("develop"));
        verify(syncRemote,
               times(1)).createBranch(eq(RELEASE));
    }

    @Test
    public void testIsRemote() {
        assertFalse(syncRemote.isRemote(branchMaster));

        assertTrue(syncRemote.isRemote(branchRemoteMaster));
    }

    @Test
    public void testGetBranchName() {
        assertEquals(MASTER,
                     this.syncRemote.getBranchName(branchMaster));
        assertEquals(MASTER,
                     this.syncRemote.getBranchName(branchRemoteMaster));
    }
}