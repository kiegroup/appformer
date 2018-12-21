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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;
import org.guvnor.structure.pom.types.DependencyTypeDefault;
import org.guvnor.structure.pom.types.TestDependencyType;
import org.guvnor.structure.pom.types.ValidationDependencyType;

/**
 * Configuration od DynamicDependency types loaded from classes
 */
@ApplicationScoped
public class DynamicDependencyTypeConfigurationMap {

    private Map<String, List<DynamicPomDependency>> mapping;
    private Set<DynamicPomDependency> internalArtifacts;
    private String kieVersion;

    public DynamicDependencyTypeConfigurationMap() {

        DependencyTypeDefault defaultType = new DependencyTypeDefault();

        this.kieVersion = defaultType.getKieVersion();
        this.internalArtifacts = defaultType.getInternalArtifacts();
        this.mapping = getDeps();
    }

    public Map<String, List<DynamicPomDependency>> getMapping() {
        return mapping;
    }

    public Set<DynamicPomDependency> getInternalArtifacts() {
        return internalArtifacts;
    }

    public String getKieVersion() {
        return kieVersion;
    }

    public void addDependencies(DependencyType k,
                                List<DynamicPomDependency> deps) {
        if (k != null && deps != null && !deps.isEmpty()) {
            mapping.putIfAbsent(k.getType(),
                                deps);
        }
    }

    public List<DynamicPomDependency> getDependencies(Set<DependencyType> dependencyTypes) {
        List<DynamicPomDependency> result = new ArrayList<>();
        for (DependencyType depType : dependencyTypes) {
            List<DynamicPomDependency> deps = mapping.get(depType.getType());
            if (deps != null && !deps.isEmpty()) {
                result.addAll(deps);
            }
        }
        return result;
    }

    private Map<String, List<DynamicPomDependency>> getDeps() {
        DependencyType test = new TestDependencyType();
        DependencyType validation = new ValidationDependencyType();

        Map<String, List<DynamicPomDependency>> hashMap = new ConcurrentHashMap() {{
            put(test.getType(),
                test.getDependencies());
            put(validation.getType(),
                validation.getDependencies());
        }};
        return hashMap;
    }
}
