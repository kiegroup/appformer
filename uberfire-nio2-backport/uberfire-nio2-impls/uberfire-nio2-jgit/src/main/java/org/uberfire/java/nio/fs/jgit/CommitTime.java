/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

public class CommitTime {

    private Long firstCommitTime;
    private Long lastCommitTime;

    public Long getFirstCommitTime() {
        return firstCommitTime;
    }

    public void setFirstCommitTime(Long firstCommitTime) {
        this.firstCommitTime = firstCommitTime;
    }

    public Long getLastCommitTime() {
        return lastCommitTime;
    }

    public void setLastCommitTime(Long lastCommitTime) {
        this.lastCommitTime = lastCommitTime;
    }
}