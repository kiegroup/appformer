/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.guvnor.m2repo.backend.server.helpers;

import java.io.File;
import java.io.IOException;

public class HttpHelper {

    public File getFile(String repo, String requestedFile) throws IOException {
        //File traversal check:
        final File mavenRootDir = new File(repo);
        final String canonicalDirPath = mavenRootDir.getCanonicalPath() + File.separator;
        final String canonicalEntryPath = new File(mavenRootDir,
                                                   requestedFile).getCanonicalPath();
        if (!canonicalEntryPath.startsWith(canonicalDirPath)) {
            return null;
        }

        requestedFile = canonicalEntryPath.substring(canonicalDirPath.length());
        return new File(mavenRootDir,
                                   requestedFile);
    }
}
