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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

public class PomEditorDefaultTest {

    private final String POM = "pom.xml";
    private PomEditor editor;
    private DependencyTypesMapper mapper;
    private Path tmpRoot, tmp;
    private String JPA_HIBERNATE_VERSION;

    @Before
    public void setUp() throws Exception {
        mapper = new DependencyTypesMapper();
        editor = new PomEditorDefault(mapper);
        tmpRoot = Files.createTempDirectory("repo");
        tmp = TestUtil.createAndCopyToDirectory(tmpRoot,
                                                "dummy",
                                                "target/test-classes/dummy");
        JPA_HIBERNATE_VERSION = mapper.getMapping().get(DependencyType.JPA).get(0).getVersion();
    }

    @After
    public void tearDown() {
        if (tmpRoot != null) {
            TestUtil.rm(tmpRoot.toFile());
        }
    }

    private Dependency getDependency(List<Dependency> deps,
                                     String groupId,
                                     String artifactId) {
        Dependency dependency = new Dependency();
        for (Dependency dep : deps) {
            if (dep.getGroupId().equals(groupId) && dep.getArtifactId().equals(artifactId)) {
                dependency.setGroupId(dep.getGroupId());
                dependency.setArtifactId(dep.getArtifactId());
                dependency.setVersion(dep.getVersion());
                dependency.setScope(dep.getScope());
                break;
            }
        }
        return dependency;
    }

    @Test
    public void removeDepTest() throws Exception {
        DynamicPomDependency dep = new DynamicPomDependency("org.springframework",
                                                            "spring-aop",
                                                            "4.3.8.RELEASE",
                                                            "");
        boolean result = editor.removeDependency(dep,
                                                 PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                     tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isTrue();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new ByteArrayInputStream(Files.readAllBytes(Paths.get(tmp.toAbsolutePath().toString() + File.separator + POM))));
        assertThat(model.getDependencies()).hasSize(1);
    }

    @Test
    public void removeDepsTest() throws Exception {
        DynamicPomDependency dep = new DynamicPomDependency("org.springframework",
                                                            "spring-aop",
                                                            "4.3.8.RELEASE",
                                                            "");

        DynamicPomDependency depTwo = new DynamicPomDependency("junit",
                                                               "junit",
                                                               "4.12",
                                                               "");
        List<DynamicPomDependency> deps = Arrays.asList(dep,
                                                        depTwo);
        boolean result = editor.removeDependencies(deps,
                                                   PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                       tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isTrue();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new ByteArrayInputStream(Files.readAllBytes(Paths.get(tmp.toAbsolutePath().toString() + File.separator + POM))));
        assertThat(model.getDependencies()).hasSize(0);
    }

    @Test
    public void removeDependencyTypeTest() throws Exception {

        Set<DependencyType> deps = EnumSet.of(DependencyType.TEST);
        boolean result = editor.removeDependencyTypes(deps,
                                                      PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                          tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isTrue();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new ByteArrayInputStream(Files.readAllBytes(Paths.get(tmp.toAbsolutePath().toString() + File.separator + POM))));
        assertThat(model.getDependencies()).hasSize(1);
    }

    @Test
    public void addEmptyDepTest() {
        DynamicPomDependency dep = new DynamicPomDependency("",
                                                            "",
                                                            "",
                                                            "");
        boolean result = editor.addDependency(dep,
                                              PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                  tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isFalse();
    }

    @Test
    public void addNullDepTest() {
        boolean result = editor.addDependency(null,
                                              PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                  tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isFalse();
    }

    @Test
    public void addNullGroupIDTest() {
        DynamicPomDependency dep = new DynamicPomDependency(null,
                                                            "junit",
                                                            "4.12",
                                                            "");
        boolean result = editor.addDependency(dep,
                                              PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                  tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isFalse();
    }

    @Test
    public void addNullArtifactIDTest() {
        DynamicPomDependency dep = new DynamicPomDependency("junit",
                                                            null,
                                                            "4.12",
                                                            "");
        boolean result = editor.addDependency(dep,
                                              PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                  tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isFalse();
    }

    @Test
    public void addDepTest() {
        DynamicPomDependency dep = new DynamicPomDependency("io.vertx",
                                                            "vertx-core",
                                                            "3.5.4",
                                                            "");
        boolean result = editor.addDependency(dep,
                                              PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                  tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isTrue();
    }

    @Test
    public void addDepsTest() {
        boolean result = editor.addDependencies(EnumSet.of(DependencyType.JPA),
                                                PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                    tmp.toUri().toString() + File.separator + POM));
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
                                                                  tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isFalse();
    }

    @Test
    public void addDuplicatedDepsTest() {
        boolean result = editor.addDependencies(EnumSet.of(DependencyType.TEST),
                                                PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                    tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isFalse();
    }

    @Test
    public void addAndOverrideVersionDepTest() throws Exception {
        tmp = TestUtil.createAndCopyToDirectory(tmpRoot,
                                                "dummyOverride",
                                                "target/test-classes/dummyOverride");
        Set<DependencyType> deps = EnumSet.of(DependencyType.JPA);
        boolean result = editor.addDependencies(deps,
                                                PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                    tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isTrue();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new ByteArrayInputStream(Files.readAllBytes(Paths.get(tmp.toAbsolutePath().toString() + File.separator + POM))));
        Dependency changedDep = getDependency(model.getDependencies(),
                                              "org.hibernate.javax.persistence",
                                              "hibernate-jpa-2.1-api");
        assertThat(changedDep.getVersion()).isEqualTo(JPA_HIBERNATE_VERSION);
    }

    @Test
    public void addAndOverrideVersionDepsTest() throws Exception {
        tmp = TestUtil.createAndCopyToDirectory(tmpRoot,
                                                "dummyOverride",
                                                "target/test-classes/dummyOverride");
        Set<DependencyType> deps = EnumSet.of(DependencyType.JPA,
                                              DependencyType.TEST);
        boolean result = editor.addDependencies(deps,
                                                PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                    tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isTrue();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new ByteArrayInputStream(Files.readAllBytes(Paths.get(tmp.toAbsolutePath().toString() + File.separator + POM))));
        Dependency changedDep = getDependency(model.getDependencies(),
                                              "org.hibernate.javax.persistence",
                                              "hibernate-jpa-2.1-api");
        assertThat(changedDep.getVersion()).isEqualTo(JPA_HIBERNATE_VERSION);
        changedDep = getDependency(model.getDependencies(),
                                   "junit",
                                   "junit");
        assertThat(changedDep.getVersion()).isEqualTo("4.12");
    }

    @Test
    public void addAndOverrideKieVersionDepTest() throws Exception {
        tmp = TestUtil.createAndCopyToDirectory(tmpRoot,
                                                "dummyInternalDepsOld",
                                                "target/test-classes/dummyInternalDepsOld");
        Set<DependencyType> deps = EnumSet.of(DependencyType.JPA);
        boolean result = editor.addDependencies(deps,
                                                PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                    tmp.toUri().toString() + File.separator + POM));
        assertThat(result).isTrue();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new ByteArrayInputStream(Files.readAllBytes(Paths.get(tmp.toAbsolutePath().toString() + File.separator + POM))));
        Dependency changedDep = getDependency(model.getDependencies(),
                                              "org.kie",
                                              "kie-internal");
        assertThat(changedDep.getVersion()).isEqualTo("7.7.0-SNAPSHOT");
    }

    @Test
    public void addAndOverrideCurrentKieVersionDepTest() throws Exception {
        tmp = TestUtil.createAndCopyToDirectory(tmpRoot,
                                                "dummyInternalDepsCurrent",
                                                "target/classes");
        org.uberfire.backend.vfs.Path pomPath = PathFactory.newPath(POM,
                                                                    tmpRoot + File.separator + "dummyInternalDepsCurrent" + File.separator + POM);

        Set<DependencyType> deps = EnumSet.of(DependencyType.JPA);
        boolean result = editor.addDependencies(deps,
                                                pomPath);
        assertThat(result).isTrue();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new ByteArrayInputStream(Files.readAllBytes(Paths.get(pomPath.toURI()))));
        Dependency changedDep = getDependency(model.getDependencies(),
                                              "org.kie",
                                              "kie-internal");
        assertThat(changedDep.getVersion()).isEqualTo(mapper.getKieVersion());
    }

    @Test
    public void addUpdatedKieVersionDepTest() throws Exception {
        tmp = TestUtil.createAndCopyToDirectory(tmpRoot,
                                                "dummyInternalDepsOld",
                                                "target/test-classes/dummyInternalDepsOld");

        org.uberfire.backend.vfs.Path pomPath = PathFactory.newPath(tmp.toAbsolutePath().toString() + File.separator + POM,
                                                                    tmp.toUri().toString() + File.separator + POM);
        DynamicPomDependency dep = new DynamicPomDependency("org.kie",
                                                            "kie-internal",
                                                            "7.13.0-SNAPSHOT",
                                                            "provided");
        boolean result = editor.addDependency(dep,
                                              pomPath);
        assertThat(result).isFalse();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new ByteArrayInputStream(Files.readAllBytes(Paths.get(pomPath.toURI()))));
        Dependency changedDep = getDependency(model.getDependencies(),
                                              "org.kie",
                                              "kie-internal");
        assertThat(changedDep.getVersion()).isEqualTo("7.7.0-SNAPSHOT");
    }
}
