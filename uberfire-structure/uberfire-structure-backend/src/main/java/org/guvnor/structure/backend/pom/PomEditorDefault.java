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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Paths;
import org.uberfire.java.nio.file.StandardOpenOption;

public class PomEditorDefault implements PomEditor {

    private static final String DELIMITER = ":";
    private final Logger logger = LoggerFactory.getLogger(PomEditorDefault.class);

    private MavenXpp3Reader reader;
    private MavenXpp3Writer writer;
    private DependencyTypesMapper mapper;
    private Set<String> internalGroupIds;

    public PomEditorDefault(DependencyTypesMapper mapper) {
        reader = new MavenXpp3Reader();
        writer = new MavenXpp3Writer();
        this.mapper = mapper;
        internalGroupIds = getInternalGroupIds(this.mapper);
    }

    public boolean addDependency(DynamicPomDependency dep,
                                 Path pomPath) {
        if (dep == null || !isGroupIDValid(dep) || !isArtifactIDValid(dep)) {
            return false;
        }

        boolean result = false;
        try {
            boolean internalArtifact = isInternalArtifact(dep);
            org.uberfire.java.nio.file.Path filePath = Paths.get(pomPath.toURI());
            Model model = getPOMModel(filePath);
            List<Dependency> depsFromPom = model.getDependencies();
            Map<String, String> keys = getKeysFromDependencies(depsFromPom);
            String keyDep = getKeyFromDynamicDependency(dep);
            if (!keys.containsKey(keyDep)) {
                Dependency pomDep = getMavenPomDep(dep,
                                                   internalArtifact,
                                                   getKieVersionFromPom(dep,
                                                                        depsFromPom));
                model.getDependencies().add(pomDep);
                result = true;
            } else {
                for (Dependency modelDep : depsFromPom) {
                    if (modelDep.getGroupId().equals(dep.getGroupID()) && modelDep.getArtifactId().equals(dep.getArtifactID())) {
                        if (internalArtifact) {
                            modelDep.setVersion(getKieVersionFromPom(dep,
                                                                     depsFromPom));
                        } else {
                            modelDep.setVersion(keys.get(keyDep));
                        }
                    }
                }
            }
            if (result) {
                writePOMModelOnFS(filePath,
                                  model);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(),
                         ex);
            result = false;
        }
        return result;
    }

    public boolean addDependencies(Set<DependencyType> dependencyTypes,
                                   Path pomPath) {
        List<DynamicPomDependency> deps = new ArrayList<>();
        for (DependencyType dep : dependencyTypes) {
            deps.addAll(mapper.getDependencies(EnumSet.of(dep)));
        }

        if (deps.isEmpty()) {
            return false;
        }
        boolean result = false;
        try {
            org.uberfire.java.nio.file.Path filePath = Paths.get(pomPath.toURI());
            Model model = getPOMModel(filePath);
            List<Dependency> depsFromPom = model.getDependencies();
            Map<String, String> keys = getKeysFromDependencies(model.getDependencies());

            for (DynamicPomDependency dep : deps) {
                boolean internalArtifact = isInternalArtifact(dep);

                if (!keys.containsKey(getKeyFromDynamicDependency(dep))) {
                    Dependency pomDep = getMavenPomDep(dep,
                                                       internalArtifact,
                                                       getKieVersionFromPom(dep,
                                                                            depsFromPom));
                    model.getDependencies().add(pomDep);

                    result = true;
                } else {
                    for (Dependency modelDep : depsFromPom) {
                        if (modelDep.getGroupId().equals(dep.getGroupID()) &&
                                modelDep.getArtifactId().equals(dep.getArtifactID()) &&
                                !modelDep.getVersion().equals(dep.getVersion())) {

                            if (internalArtifact) {
                                modelDep.setVersion(getKieVersionFromPom(dep,
                                                                         depsFromPom));
                            } else {
                                modelDep.setVersion(dep.getVersion());
                            }
                            result = true;
                        }
                    }
                }
            }
            if (result) {
                writePOMModelOnFS(filePath,
                                  model);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(),
                         ex);
            result = false;
        }
        return result;
    }

    @Override
    public boolean removeDependency(DynamicPomDependency dep,
                                    Path pomPath) {

        boolean result = false;
        try {
            org.uberfire.java.nio.file.Path filePath = Paths.get(pomPath.toURI());
            Model model = getPOMModel(filePath);
            List<Dependency> depsFromPom = model.getDependencies();
            if (removeDynamicDep(depsFromPom,
                                 dep)) {
                model.setDependencies(depsFromPom);
                writePOMModelOnFS(filePath,
                                  model);
                result = true;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(),
                         ex);
            result = false;
        }
        return result;
    }

    @Override
    public boolean removeDependencies(List<DynamicPomDependency> deps,
                                      Path pomPath) {

        if (deps.isEmpty()) {
            return false;
        }
        boolean result = false;
        try {
            org.uberfire.java.nio.file.Path filePath = Paths.get(pomPath.toURI());
            Model model = getPOMModel(filePath);
            List<Dependency> depsFromPom = model.getDependencies();
            Map<String, String> dynamicKeys = getKeysFromDynamicDependencies(deps);
            if (removeDynamicDeps(depsFromPom,
                                  dynamicKeys)) {
                model.setDependencies(depsFromPom);
                result = true;
                writePOMModelOnFS(filePath,
                                  model);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(),
                         ex);
            result = false;
        }
        return result;
    }

    @Override
    public boolean removeDependencyTypes(Set<DependencyType> dependencyTypes,
                                         Path pomPath) {

        List<DynamicPomDependency> deps = new ArrayList<>();
        for (DependencyType dep : dependencyTypes) {
            deps.addAll(mapper.getDependencies(EnumSet.of(dep)));
        }
        if (deps.isEmpty()) {
            return false;
        }
        return removeDependencies(deps,
                                  pomPath);
    }

    private boolean removeDynamicDep(List<Dependency> depsFromPom,
                                     DynamicPomDependency dep) {
        boolean result = false;
        for (Dependency depFromPom : depsFromPom) {
            if (depFromPom.getGroupId().equals(dep.getGroupID()) && depFromPom.getArtifactId().equals(dep.getArtifactID()) && depFromPom.getVersion().equals(dep.getVersion())) {
                depsFromPom.remove(depFromPom);
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean removeDynamicDeps(List<Dependency> depsFromPom,
                                      Map<String, String> dymanicKeys) {
        boolean result = false;
        List<Dependency> depsToRemove = new ArrayList<>(depsFromPom.size());
        for (Dependency depFromPom : depsFromPom) {
            if (dymanicKeys.containsKey(getKeyFromDependency(depFromPom))) {
                depsToRemove.add(depFromPom);
                result = true;
            }
        }
        if (result) {
            depsFromPom.removeAll(depsToRemove);
        }
        return result;
    }

    private Dependency getMavenPomDep(DynamicPomDependency dep,
                                      boolean internalArtifact,
                                      String kieVersionFromPom) {
        Dependency pomDep = getMavenDependency(dep);
        if (internalArtifact) {
            pomDep.setVersion(kieVersionFromPom);
        }
        return pomDep;
    }

    private String getKieVersionFromPom(DynamicPomDependency dep,
                                        List<Dependency> depsFromPom) {
        Dependency depInternalFromPom = getDependency(depsFromPom,
                                                      dep.getGroupID(),
                                                      dep.getArtifactID());
        return depInternalFromPom.getVersion();
    }

    private String getKeyFromDynamicDependency(DynamicPomDependency dep) {
        StringBuilder sb = new StringBuilder();
        sb.append(dep.getGroupID()).append(DELIMITER).append(dep.getArtifactID()).toString();
        return sb.toString();
    }

    private String getKeyFromDependency(Dependency dep) {
        StringBuilder sb = new StringBuilder();
        sb.append(dep.getGroupId()).append(DELIMITER).append(dep.getArtifactId()).toString();
        return sb.toString();
    }

    private Map<String, String> getKeysFromDependencies(List<Dependency> deps) {
        Map<String, String> depsMap = new HashMap<>(deps.size());
        for (Dependency dep : deps) {
            StringBuilder sb = new StringBuilder();
            sb.append(dep.getGroupId()).append(DELIMITER).append(dep.getArtifactId());
            depsMap.put(sb.toString(),
                        dep.getVersion());
        }
        return depsMap;
    }

    private Map<String, String> getKeysFromDynamicDependencies(List<DynamicPomDependency> deps) {
        Map<String, String> depsMap = new HashMap<>(deps.size());
        for (DynamicPomDependency dep : deps) {
            StringBuilder sb = new StringBuilder();
            sb.append(dep.getGroupID()).append(DELIMITER).append(dep.getArtifactID());
            depsMap.put(sb.toString(),
                        dep.getVersion());
        }
        return depsMap;
    }

    private void writePOMModelOnFS(org.uberfire.java.nio.file.Path filePath,
                                   Model model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.write(baos,
                     model);
        Files.write(filePath,
                    baos.toByteArray(),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Model getPOMModel(org.uberfire.java.nio.file.Path filePath) throws IOException, XmlPullParserException {
        return reader.read(new ByteArrayInputStream(Files.readAllBytes(filePath)));
    }

    private Dependency getMavenDependency(DynamicPomDependency dep) {
        Dependency pomDep = new Dependency();
        pomDep.setGroupId(dep.getGroupID());
        pomDep.setArtifactId(dep.getArtifactID());
        if (!dep.getVersion().isEmpty()) {
            pomDep.setVersion(dep.getVersion());
        }
        pomDep.setType("jar");
        return pomDep;
    }

    private Set<String> getInternalGroupIds(DependencyTypesMapper mapper) {
        Set<DynamicPomDependency> deps = mapper.getInternalArtifacts();
        Set<String> groups = new HashSet<>(deps.size());
        for (DynamicPomDependency dep : deps) {
            groups.add(dep.getGroupID());
        }
        return groups;
    }

    private boolean isInternalArtifact(DynamicPomDependency dependency) {
        return internalGroupIds.contains(dependency.getGroupID());
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

    private boolean isGroupIDValid(DynamicPomDependency dep) {
        return dep.getGroupID() != null && !dep.getGroupID().isEmpty();
    }

    private boolean isArtifactIDValid(DynamicPomDependency dep) {
        return dep.getArtifactID() != null && !dep.getArtifactID().isEmpty();
    }
}
