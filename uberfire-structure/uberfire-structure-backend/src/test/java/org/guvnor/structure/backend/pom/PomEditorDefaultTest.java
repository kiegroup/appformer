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
import java.net.URI;
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

import static org.assertj.core.api.Assertions.assertThat;

public class PomEditorDefaultTest {

    private final String POM = "pom.xml";
    private PomEditor editor;
    private DependencyTypesMapper mapper;
    private Path tmpRoot, tmp;

    @Before
    public void setUp() throws Exception {
        mapper = new DependencyTypesMapper();
        editor = new PomEditorDefault(mapper);
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
        //During the scan of the pom if a dep is founded present will be override the version with the version in the json file
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
        assertThat(changedDep.getVersion()).isEqualTo("1.0.2.Final");
    }

    @Test
    public void addAndOverrideVersionDepsTest() throws Exception {
        //During the scan of the pom if a dep is founded present will be override the version with the version in the json file
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
        assertThat(changedDep.getVersion()).isEqualTo("1.0.2.Final");
        changedDep = getDependency(model.getDependencies(),
                                   "junit",
                                   "junit");
        assertThat(changedDep.getVersion()).isEqualTo("4.12");
    }

    @Test
    public void addAndOverrideKieVersionDepTest() throws Exception {
        //During the scan of the pom if a dep is founded present will be override the version with the version in the json file
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
        //During the scan of the pom if a dep is founded present will be override the version with the version in the json file
         tmp = TestUtil.createAndCopyToDirectory(tmpRoot,
                                                "dummyInternalDepsCurrent",
                                                 "target/classes");
        org.uberfire.backend.vfs.Path pomPath = PathFactory.newPath(POM, tmpRoot + File.separator + "dummyInternalDepsCurrent" + File.separator + POM);

        Set<DependencyType> deps = EnumSet.of(DependencyType.JPA);
        boolean result = editor.addDependencies(deps, pomPath);
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
        boolean result = editor.addDependency(dep, pomPath);
        assertThat(result).isFalse();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new ByteArrayInputStream(Files.readAllBytes(Paths.get(pomPath.toURI()))));
        Dependency changedDep = getDependency(model.getDependencies(),
                                              "org.kie",
                                              "kie-internal");
        assertThat(changedDep.getVersion()).isEqualTo("7.7.0-SNAPSHOT");
    }
}
