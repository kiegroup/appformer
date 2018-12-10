package org.guvnor.structure.pom;

public class DependencyTypeDefault implements DependencyType {

    private String type;

    public DependencyTypeDefault(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
}
