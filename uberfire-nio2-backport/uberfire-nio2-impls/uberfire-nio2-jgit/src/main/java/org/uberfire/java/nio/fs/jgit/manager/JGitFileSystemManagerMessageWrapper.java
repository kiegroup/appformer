/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.uberfire.java.nio.fs.jgit.manager;

import java.io.Serializable;
import java.util.Objects;

public class JGitFileSystemManagerMessageWrapper implements Serializable {

    private final String nodeId;
    private final JGitFileSystemManagerMessage type;
    private final String message;

    public JGitFileSystemManagerMessageWrapper(String nodeId, JGitFileSystemManagerMessage type, String message) {
        this.nodeId = nodeId;
        this.type = type;
        this.message = message;
    }

    public String getNodeId() {
        return nodeId;
    }

    public JGitFileSystemManagerMessage getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public enum JGitFileSystemManagerMessage {
        FS_REMOVED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JGitFileSystemManagerMessageWrapper that = (JGitFileSystemManagerMessageWrapper) o;
        return Objects.equals(nodeId, that.nodeId) &&
                type == that.type &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {

        return Objects.hash(nodeId, type, message);
    }
}
