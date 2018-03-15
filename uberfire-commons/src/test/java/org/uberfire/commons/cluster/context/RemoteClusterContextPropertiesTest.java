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
public class RemoteClusterContextPropertiesTest {

    @Test
    public void testGetClusterContextEnvironment() {
        ClusterParameters clusterParameters = mock(ClusterParameters.class);
        when(clusterParameters.getInitialContextFactory()).thenReturn("org.wildfly.naming.client.WildFlyInitialContextFactory");
        when(clusterParameters.getProviderUrl()).thenReturn("http-remote://localhost:8080");
        when(clusterParameters.getJmsUserName()).thenReturn("admin");
        when(clusterParameters.getJmsPassword()).thenReturn("adminPassword");

        RemoteClusterContextProperties contextProperties = new RemoteClusterContextProperties();
        Properties environment = contextProperties.getClusterContextEnvironment(clusterParameters);

        assertThat(environment).hasSize(4)
                               .containsKeys(Context.INITIAL_CONTEXT_FACTORY,
                                             Context.PROVIDER_URL,
                                             Context.SECURITY_PRINCIPAL,
                                             Context.SECURITY_CREDENTIALS)
                               .containsValues("org.wildfly.naming.client.WildFlyInitialContextFactory",
                                               "http-remote://localhost:8080",
                                               "admin",
                                               "adminPassword");
    }

    @Test
    public void testGetClusterContextEnvironmentNullClusterParametersValues() {
        ClusterParameters clusterParameters = mock(ClusterParameters.class);

        RemoteClusterContextProperties contextProperties = new RemoteClusterContextProperties();
        assertThatCode(() -> contextProperties.getClusterContextEnvironment(clusterParameters)).isExactlyInstanceOf(RuntimeException.class);
    }
}
