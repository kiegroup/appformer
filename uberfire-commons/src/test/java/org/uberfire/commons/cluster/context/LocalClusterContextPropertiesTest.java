/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.uberfire.commons.cluster.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.naming.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.commons.cluster.ClusterParameters;

@RunWith(MockitoJUnitRunner.class)
public class LocalClusterContextPropertiesTest {

    @Test
    public void testGetClusterContextEnvironment() {
        ClusterParameters clusterParameters = mock(ClusterParameters.class);
        when(clusterParameters.getInitialContextFactory()).thenReturn("org.wildfly.naming.client.WildFlyInitialContextFactory");

        LocalClusterContextProperties contextProperties = new LocalClusterContextProperties();
        Properties environment = contextProperties.getClusterContextEnvironment(clusterParameters);

        assertThat(environment).hasSize(1)
                               .containsKey(Context.INITIAL_CONTEXT_FACTORY)
                               .containsValue("org.wildfly.naming.client.WildFlyInitialContextFactory");
    }

    @Test
    public void testGetClusterContextEnvironmentNullInitialContextFactory() {
        ClusterParameters clusterParameters = mock(ClusterParameters.class);

        LocalClusterContextProperties contextProperties = new LocalClusterContextProperties();
        assertThatCode(() -> contextProperties.getClusterContextEnvironment(clusterParameters)).isExactlyInstanceOf(RuntimeException.class);
    }
}
