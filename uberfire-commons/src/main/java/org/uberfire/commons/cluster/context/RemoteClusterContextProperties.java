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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.naming.Context;

import org.uberfire.commons.cluster.ClusterParameters;

public class RemoteClusterContextProperties implements ClusterContextProperties {

    @Override
    public Properties getClusterContextEnvironment(ClusterParameters clusterParameters) {
        String initialContextFactory = clusterParameters.getInitialContextFactory();
        String providerUrl = clusterParameters.getProviderUrl();
        String providerUserName = clusterParameters.getJmsUserName();
        String providerPassword = clusterParameters.getJmsPassword();

        validateClusterParameters(clusterParameters);

        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                initialContextFactory);
        env.put(Context.PROVIDER_URL,
                providerUrl);
        env.put(Context.SECURITY_PRINCIPAL, providerUserName);
        env.put(Context.SECURITY_CREDENTIALS, providerPassword);

        return env;
    }

    private void validateClusterParameters(ClusterParameters clusterParameters) {
        List<String> missingProperties = new ArrayList<>();

        if (clusterParameters.getInitialContextFactory() == null || clusterParameters.getInitialContextFactory().isEmpty()) {
            missingProperties.add(ClusterParameters.APPFORMER_INITIAL_CONTEXT_FACTORY);
        }
        if (clusterParameters.getProviderUrl() == null || clusterParameters.getProviderUrl().isEmpty()) {
            missingProperties.add(ClusterParameters.APPFORMER_PROVIDER_URL);
        }
        if (clusterParameters.getJmsUserName() == null || clusterParameters.getJmsUserName().isEmpty()) {
            missingProperties.add(ClusterParameters.APPFORMER_JMS_USERNAME);
        }
        if (clusterParameters.getJmsPassword() == null || clusterParameters.getJmsPassword().isEmpty()) {
            missingProperties.add(ClusterParameters.APPFORMER_JMS_PASSWORD);
        }

        if (!missingProperties.isEmpty()) {
            String missingPropertiesString = missingProperties.stream().collect(Collectors.joining(", "));
            throw new RuntimeException("Required parameters " + missingPropertiesString + " are not defined.");
        }
    }
}
