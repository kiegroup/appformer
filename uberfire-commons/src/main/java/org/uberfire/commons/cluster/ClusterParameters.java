/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.uberfire.commons.cluster;

public class ClusterParameters {

    public static final String APPFORMER_CLUSTER = "appformer-cluster";
    public static final String APPFORMER_DEFAULT_CLUSTER_CONFIGS = "appformer-default-cluster-configs";
    public static final String APPFORMER_PROVIDER_URL = "appformer-jms-url";
    public static final String APPFORMER_INITIAL_CONTEXT_FACTORY = "appformer-initial-context-factory";
    public static final String APPFORMER_JMS_CONNECTION_FACTORY = "appformer-jms-connection-factory";
    public static final String APPFORMER_JMS_USERNAME = "appformer-jms-username";
    public static final String APPFORMER_JMS_PASSWORD = "appformer-jms-password";
    private Boolean appFormerClustered;
    private String providerUrl;
    private String initialContextFactory;
    private String jmsConnectionFactoryJndiName;
    private String jmsUserName;
    private String jmsPassword;

    public ClusterParameters() {
        this.appFormerClustered = Boolean.valueOf(System.getProperty(APPFORMER_CLUSTER,
                                                                     "false"));
        this.initialContextFactory = System.getProperty(APPFORMER_INITIAL_CONTEXT_FACTORY,
                                                        "org.wildfly.naming.client.WildFlyInitialContextFactory");
        this.jmsConnectionFactoryJndiName = System.getProperty(APPFORMER_JMS_CONNECTION_FACTORY,
                                                               "java:/ConnectionFactory");
        if (appFormerClustered) {

            Boolean defaultConfigs = Boolean.valueOf(System.getProperty(APPFORMER_DEFAULT_CLUSTER_CONFIGS,
                                                                        "false"));
            if (defaultConfigs) {
                setupDefaultConfigs();
            } else {
                loadConfigs();
            }
        }
    }

    private void setupDefaultConfigs() {
        this.jmsUserName = "admin";
        this.jmsPassword = "admin";
    }

    private void loadConfigs() {
        this.providerUrl = System.getProperty(APPFORMER_PROVIDER_URL);
        this.jmsUserName = System.getProperty(APPFORMER_JMS_USERNAME);
        this.jmsPassword = System.getProperty(APPFORMER_JMS_PASSWORD);
        if (jmsUserName == null || jmsPassword == null) {
            throw new RuntimeException(buildErrorMessage().toString());
        }
    }

    private StringBuilder buildErrorMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("There is a error on appFormer cluster configurations: ");
        sb.append(APPFORMER_CLUSTER + ": " + appFormerClustered + ", ");
        sb.append(APPFORMER_PROVIDER_URL + ": " + providerUrl + ", ");
        sb.append(APPFORMER_JMS_USERNAME + ": " + jmsUserName + ", ");
        sb.append(APPFORMER_JMS_PASSWORD + ": " + jmsPassword);
        return sb;
    }

    public Boolean isAppFormerClustered() {
        return appFormerClustered;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public String getJmsUserName() {
        return jmsUserName;
    }

    public String getJmsPassword() {
        return jmsPassword;
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public String getJmsConnectionFactoryJndiName() {
        return jmsConnectionFactoryJndiName;
    }
}
