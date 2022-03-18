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

package org.uberfire.java.nio.fs.jgit;

import java.io.IOException;
import java.util.Map;
import org.apache.sshd.core.CoreModuleProperties;

import org.apache.sshd.server.SshServer;
import org.junit.Test;

import static org.junit.Assert.*;

public class JGitFileSystemImplProviderSSHBadConfigTest extends AbstractTestInfra {

    @Override
    public Map<String, String> getGitPreferences() {
        Map<String, String> gitPrefs = super.getGitPreferences();

        gitPrefs.put("org.uberfire.nio.git.ssh.enabled",
                     "true");
        gitPrefs.put("org.uberfire.nio.git.ssh.idle.timeout",
                     "bz");

        return gitPrefs;
    }

    @Test
    public void testCheckDefaultSSHIdleWithInvalidArg() throws IOException {
        assertEquals(JGitFileSystemProviderConfiguration.DEFAULT_SSH_IDLE_TIMEOUT,
                     provider.getGitSSHService().getProperties().get(CoreModuleProperties.IDLE_TIMEOUT.getName()));
    }
}
