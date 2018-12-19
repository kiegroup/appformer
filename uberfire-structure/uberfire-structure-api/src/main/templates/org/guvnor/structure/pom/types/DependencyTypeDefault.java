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
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import org.guvnor.structure.pom.DynamicPomDependency;

public class DependencyTypeDefault {

    private String kieVersion;
    private Set<DynamicPomDependency> internalArtifacts;

    public DependencyTypeDefault() {

        kieVersion = "${version.org.kie}";
        this.internalArtifacts = getDeps();
    }

    public String getKieVersion() {
        return kieVersion;
    }

    public Set<DynamicPomDependency> getInternalArtifacts() {
        return Collections.unmodifiableSet(internalArtifacts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DependencyTypeDefault)) {
            return false;
        }
        DependencyTypeDefault that = (DependencyTypeDefault) o;
        return getKieVersion().equals(that.getKieVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKieVersion());
    }

    private Set<DynamicPomDependency> getDeps() {
        return new HashSet<>(Arrays.asList(
                new DynamicPomDependency("org.kie",
                                         "kie-internal",
                                         "${version.org.kie}",
                                         "provided"),

                new DynamicPomDependency("org.optaplanner",
                                         "optaplanner-core",
                                         "${version.org.kie}",
                                         "provided"),

                new DynamicPomDependency("org.dashbuilder",
                                         "kie-internal",
                                         "${version.org.kie}",
                                         "provided")
        ));
    }
}
