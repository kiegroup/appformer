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
import java.util.EnumSet;

import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class PomEditorDefaultTest {

    private final String POM = "pom.xml";
    private PomEditor editor;
    private DependencyTypesMapper mapper;
    private Path tmpRoot, tmp;

    @Before
    public void setUp() throws Exception {
        mapper = new DependencyTypesMapper();
        editor = new PomEditorDefault();
        tmpRoot = Files.createTempDirectory("repo");
        tmp = TestUtil.createAndCopyToDirectory(tmpRoot,
                                                "dummy",
                                                "target/test-classes/dummy");
    }

    @After
    public void tearDown() {
        if (tmpRoot != null) {
            TestUtil.rm(tmpRoot.toFile());
        }
    }

    @Test
    public void addDepTest() {
        DynamicPomDependency dep = new DynamicPomDependency("io.vertx",
                                                            "vertx-core",
                                                            "3.5.4",
                                                            "");
        boolean result = editor.addDependency(dep,
                                              PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                  tmp.toUri().toString() + File.separator + POM), mapper);
        assertThat(result).isTrue();
    }

    @Test
    public void addDepsTest() {
        boolean result = editor.addDependencies(EnumSet.of(DependencyType.JPA),
                                                PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                    tmp.toUri().toString() + File.separator + POM), mapper);
        assertThat(result).isTrue();
    }

    @Test
    public void addDuplicatedDepTest() {
        DynamicPomDependency dep = new DynamicPomDependency("junit",
                                                            "junit",
                                                            "4.12",
                                                            "test");
        boolean result = editor.addDependency(dep,
                                              PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                  tmp.toUri().toString() + File.separator + POM), mapper);
        assertThat(result).isFalse();
    }

    @Test
    public void addDuplicatedDepsTest() {
        boolean result = editor.addDependencies(EnumSet.of(DependencyType.TEST),
                                                PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                    tmp.toUri().toString() + File.separator + POM), mapper);
        assertThat(result).isFalse();
    }
}
