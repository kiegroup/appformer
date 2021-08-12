/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.ext.metadata.io;

import java.util.HashSet;
import java.util.Set;

import org.uberfire.java.nio.file.Path;

public final class IOServiceIndexedUtil {

    private static Set<String> denyList = new HashSet<String>() {{
        add(".gitignore");
        add(".gitkeep");
    }};

    public static boolean isInDenyList(final Path path) {
        final boolean isGitInternalFile = denyList.contains(path.getFileName().toString());
        return isGitInternalFile;
    }
}
