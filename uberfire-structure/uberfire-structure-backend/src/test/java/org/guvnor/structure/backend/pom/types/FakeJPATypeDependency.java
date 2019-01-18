package org.guvnor.structure.backend.pom.types;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;

public class FakeJPATypeDependency implements DependencyType {

    public static final String type = "JPA";
    private List<DynamicPomDependency> dependencies;

    public FakeJPATypeDependency() {
        this.dependencies = getDeps();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<DynamicPomDependency> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FakeJPATypeDependency)) {
            return false;
        }

        FakeJPATypeDependency that = (FakeJPATypeDependency) o;

        return getDependencies().equals(that.getDependencies());
    }

    @Override
    public int hashCode() {
        return getDependencies().hashCode();
    }

    private List getDeps() {
        return Arrays.asList(
                new DynamicPomDependency("org.fake.javax",
                                         "fake-jpa-api",
                                         "0.1.RELEASE",
                                         "compile"));
    }
}
