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
package org.guvnor.structure.pom.types;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;

public class JPADependencyType implements DependencyType {

    public static final String type = "JPA";
    private List<DynamicPomDependency> dependencies;

    public JPADependencyType() {
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
        if (!(o instanceof JPADependencyType)) {
            return false;
        }
        JPADependencyType that = (JPADependencyType) o;
        return Objects.equals(getType(),
                              that.getType());
    }

    private List getDeps() {
        return Arrays.asList(
                new DynamicPomDependency("org.hibernate.javax.persistence",
                                         "hibernate-jpa-2.1-api",
                                         "${version.org.hibernate.javax.persistence.hibernate-jpa-2.1-api}",
                                         "compile"));
    }
}
