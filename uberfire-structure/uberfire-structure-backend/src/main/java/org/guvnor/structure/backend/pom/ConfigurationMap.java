package org.guvnor.structure.backend.pom;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;

public class ConfigurationMap {

    private Map<DependencyType, List<DynamicPomDependency>> mapping;
    private Set<DynamicPomDependency> internalArtifacts;
    private String kieVersion;

    public ConfigurationMap(Map<DependencyType, List<DynamicPomDependency>> mapping,
                            Set<DynamicPomDependency> internalArtifacts,
                            String kieVersion) {
        this.mapping = mapping;
        this.kieVersion = kieVersion;
        this.internalArtifacts = internalArtifacts;
    }

    public Map<DependencyType, List<DynamicPomDependency>> getMapping() {
        return mapping;
    }

    public Set<DynamicPomDependency> getInternalArtifacts() {
        return internalArtifacts;
    }

    public String getKieVersion() {
        return kieVersion;
    }
}
