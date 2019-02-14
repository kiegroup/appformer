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
package org.guvnor.structure.backend.pom;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.Paths;

class TestUtil {

    private static Logger logger = LoggerFactory.getLogger(TestUtil.class);

    public static Path createAndCopyToDirectory(Path root,
                                                String dirName,
                                                String copyTree) throws IOException {
        Path dir = Files.createDirectories(Paths.get(root.toString(),
                                                     dirName));
        copyTree(Paths.get(copyTree),
                 dir);
        return dir;
    }

    public static void copyTree(Path source,
                                Path target) throws IOException {
        FileUtils.copyDirectory(source.toFile(),
                                target.toFile());
    }

    public static void rm(File f) {
        try {
            FileUtils.deleteDirectory(f);
        } catch (Exception e) {
            logger.error("Couldn't delete file {}",
                         f);
            logger.error(e.getMessage(),
                         e);
        }
    }
}
