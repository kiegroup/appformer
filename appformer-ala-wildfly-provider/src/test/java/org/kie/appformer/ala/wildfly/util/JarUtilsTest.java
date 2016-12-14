/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.ala.wildfly.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JarUtilsTest {

    /**
     * The test jar has the following content:
     *
     *      root-entry.txt
     *      dir1/dir1_1/dir1_1-entry.txt
     *      dir2/dir2-entry.txt
     *
     *  and the files as the following expected contents below.
     */
    private static final String JAR_FILE = "JarUtilsTestFile-do-not-delete.file";

    private static final String ROOT_ENTRY = "root-entry.txt";

    private static final String ROOT_ENTRY_CONTENT = "root-entry content";

    private static final String DIR1_1_ENTRY = "dir1/dir1_1/dir1_1-entry.txt";

    private static final String DIR1_1_ENTRY_CONTENT = "dir1_1-entry content";

    private static final String DIR2_ENTRY = "dir2/dir2-entry.txt";

    private static final String DIR2_ENTRY_CONTENT = "dir2-entry content";

    private static final String NEW_ROOT_ENTRY = "new-root-entry.txt";

    private static final String NEW_ROOT_ENTRY_CONTENT = "new-root-entry content";

    private static final String NEW_DIR_ENTRY = "new-dir/new-dir-entry.txt";

    private static final String NEW_DIR_ENTRY_CONTENT = "new-dir-entry content";

    private Path path;

    @Before
    public void setup( ) throws Exception {
        path = Paths.get( "src/test/resources/" + JAR_FILE );
    }

    @Test
    public void testGetStrEntry( ) throws Exception {
        assertEquals( ROOT_ENTRY_CONTENT, JarUtils.getStrEntry( path, ROOT_ENTRY ) );
        assertEquals( DIR1_1_ENTRY_CONTENT, JarUtils.getStrEntry( path, DIR1_1_ENTRY ) );
        assertEquals( DIR2_ENTRY_CONTENT, JarUtils.getStrEntry( path, DIR2_ENTRY ) );
    }

    @Test
    public void testAddStrEntry( ) throws Exception {
        //create a temporary copy of the original file.
        Path tmpFile = null;
        try {
            tmpFile = Files.createTempFile( path.getFileName( ).toString( ), ".tmp" );
            Files.copy( path, tmpFile, StandardCopyOption.REPLACE_EXISTING );

            // add a two new entries to the copied file.
            JarUtils.addStrEntry( tmpFile, NEW_ROOT_ENTRY, NEW_ROOT_ENTRY_CONTENT );
            JarUtils.addStrEntry( tmpFile, NEW_DIR_ENTRY, NEW_DIR_ENTRY_CONTENT );

            // the new entries should be there
            assertEquals( NEW_ROOT_ENTRY_CONTENT, JarUtils.getStrEntry( tmpFile, NEW_ROOT_ENTRY ) );
            assertEquals( NEW_DIR_ENTRY_CONTENT, JarUtils.getStrEntry( tmpFile, NEW_DIR_ENTRY ) );

            // update an existing entry in the copied file.
            JarUtils.addStrEntry( tmpFile, DIR1_1_ENTRY, "content updated!" );
            assertEquals( "content updated!", JarUtils.getStrEntry( tmpFile, DIR1_1_ENTRY ) );
        } finally {
            if ( tmpFile != null ) {
                Files.delete( tmpFile );
            }
        }
    }
}