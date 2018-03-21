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

package org.uberfire.java.nio.fs.jgit.util.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.uberfire.commons.data.Pair;
import org.uberfire.java.nio.fs.jgit.util.GitImpl;

public class SyncRemote {

    private static final Character SEPARATOR = '/';
    private final GitImpl git;
    private final Pair<String, String> remote;

    public SyncRemote(final GitImpl git,
                      final Pair<String, String> remote) {
        this.git = git;
        this.remote = remote;
    }

    public void execute() {
        try {
            final List<Ref> branches = git._branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

            final Set<String> remoteBranches = new HashSet<>();
            final Set<String> localBranches = new HashSet<>();

            for (final Ref branch : branches) {
                final String branchName = getBranchName(branch);
                if (!branchName.equals(Constants.HEAD)) {
                    if (isRemote(branch)) {
                        remoteBranches.add(branchName);
                    } else {
                        localBranches.add(branchName);
                    }
                }
            }

            createLocalBranches(remoteBranches,
                                localBranches);
            remoteBranches.removeAll(localBranches);

            createRemoteBranches(remoteBranches);
        } catch (final Exception ex) {
            throw new SyncRemoteException(ex);
        }
    }

    private void createRemoteBranches(final Set<String> remoteBranches) throws GitAPIException {
        for (final String branch : remoteBranches) {
            createBranch(branch);
        }
    }

    private void createLocalBranches(final Set<String> remoteBranches,
                                     final Set<String> localBranches) throws GitAPIException {
        for (final String localBranch : localBranches) {
            if (remoteBranches.contains(localBranch)) {
                createBranch(localBranch);
            }
        }
    }

    protected String getBranchName(final Ref branch) {
        return branch.getName().substring(branch.getName().lastIndexOf(SEPARATOR) + 1);
    }

    protected boolean isRemote(final Ref branch) {
        return branch.getName().startsWith(Constants.R_REMOTES);
    }

    protected void createBranch(final String branchName) throws GitAPIException {
        git._branchCreate()
                .setName(branchName)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                .setStartPoint(remote.getK1() + SEPARATOR + branchName)
                .setForce(true)
                .call();
    }

    private class SyncRemoteException extends RuntimeException {

        public SyncRemoteException(Exception ex) {
            super(ex);
        }
    }
}
