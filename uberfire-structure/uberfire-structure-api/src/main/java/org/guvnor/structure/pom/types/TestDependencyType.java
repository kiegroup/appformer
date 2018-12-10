package org.guvnor.structure.pom;

public class TestDependencyType implements DependencyType {

    public static String type = "TEST";

    @Override
    public String getType() {
        return type;
    }
}
