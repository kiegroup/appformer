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

import java.util.Map;
import java.util.Properties;

import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MapperDependencyTypesTest {

    private MapperDependencyTypes mapper;

    @Before
    public void setUp(){
        mapper= new MapperDependencyTypes();
    }

    private void testJPADep(Map<DependencyType, DynamicPomDependency> mapping) {
        DynamicPomDependency dep = mapping.get(DependencyType.JPA);
        assertThat(dep.getGroupID()).isEqualToIgnoringCase("org.hibernate.javax.persistence");
        assertThat(dep.getArtifactID()).isEqualToIgnoringCase("hibernate-jpa-2.1-api");
        assertThat(dep.getVersion()).isEqualToIgnoringCase("1.0.2.Final");
        assertThat(dep.getScope()).isEmpty();
    }

    @Test
    public void mappingTest(){
        Properties props = mapper.getMapperProperties();
        assertThat(props).isNotEmpty();
        Map<DependencyType, DynamicPomDependency> mapping = mapper.getMapping();
        assertThat(mapping).isNotEmpty();
        testJPADep(mapping);
    }


    @Test
    public void loadMappingTest(){
        Properties props = mapper.getMapperProperties();
        assertThat(props).isNotEmpty();
        Map<DependencyType, DynamicPomDependency> mapping = mapper.loadMapping(props);
        assertThat(mapping).isNotEmpty();
        testJPADep(mapping);
    }

}
