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
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;
import org.guvnor.structure.pom.types.JPADependencyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

class TestUtil {

    public final static String GROUP_ID_TEST = "org.hibernate.javax.persistence";
    public final static String ARTIFACT_ID_TEST = "hibernate-jpa-2.1-api";
    public final static String SCOPE_TEST = "compile";
    private static DependencyTypesMapper mapper;
    private static String JPA_HIBERNATE_VERSION;
    private static Logger logger = LoggerFactory.getLogger(TestUtil.class);

    public static void testJPADep(Map<DependencyType, List<DynamicPomDependency>> mapping) {
        mapper = new DependencyTypesMapper();
        JPA_HIBERNATE_VERSION = mapper.getMapping().get(new JPADependencyType()).get(0).getVersion();
        List<DynamicPomDependency> deps = mapping.get(new JPADependencyType());
        assertThat(deps).hasSize(1);
        DynamicPomDependency dep = deps.get(0);
        assertThat(dep.getGroupID()).isEqualToIgnoringCase(GROUP_ID_TEST);
        assertThat(dep.getArtifactID()).isEqualToIgnoringCase(ARTIFACT_ID_TEST);
        assertThat(dep.getVersion()).isEqualToIgnoringCase(JPA_HIBERNATE_VERSION);
        assertThat(dep.getScope()).isEqualToIgnoringCase(SCOPE_TEST);
    }

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
