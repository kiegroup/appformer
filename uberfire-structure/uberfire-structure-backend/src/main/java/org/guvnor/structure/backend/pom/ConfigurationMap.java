package org.guvnor.structure.backend.pom;

import java.util.List;
import java.util.Map;

import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;

public class ConfigurationMap {

    private Map<DependencyType, List<DynamicPomDependency>> mapping;
    private String kieVersion;

    public ConfigurationMap(Map<DependencyType, List<DynamicPomDependency>> mapping,
                            String kieVersion) {
        this.mapping = mapping;
        this.kieVersion = kieVersion;
    }

    public Map<DependencyType, List<DynamicPomDependency>> getMapping() {
        return mapping;
    }

    public String getKieVersion() {
        return kieVersion;
    }
}
