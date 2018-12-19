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

public class ValidationDependencyType implements DependencyType {

    public static final String type = "VALIDATION";
    private List<DynamicPomDependency> dependencies;

    public ValidationDependencyType() {
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
        if (!(o instanceof ValidationDependencyType)) {
            return false;
        }
        ValidationDependencyType that = (ValidationDependencyType) o;
        return getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType());
    }

    private List getDeps() {
        return Arrays.asList(
                new DynamicPomDependency("javax.validation",
                                         "validation-api",
                                         "${version.javax.validation}",
                                         ""),
                
                new DynamicPomDependency("org.hibernate",
                                         "hibernate-validator",
                                         "${version.org.hibernate.hibernate-validator}",
                                         "")
        );
    }
}
