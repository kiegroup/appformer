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

import java.util.Properties;

import javax.naming.Context;

import org.uberfire.commons.cluster.ClusterParameters;

public class LocalClusterContextProperties implements ClusterContextProperties {

    @Override
    public Properties getClusterContextEnvironment(ClusterParameters clusterParameters) {
        String initialContextFactory = clusterParameters.getInitialContextFactory();

        validateInitialContextFactory(initialContextFactory);

        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                initialContextFactory);

        return env;
    }

    private void validateInitialContextFactory(String initialContextFactory) {
        if (initialContextFactory == null || initialContextFactory.isEmpty()) {
            throw new RuntimeException("Required parameter " + ClusterParameters.APPFORMER_INITIAL_CONTEXT_FACTORY + " is not defined.");
        }
    }
}
