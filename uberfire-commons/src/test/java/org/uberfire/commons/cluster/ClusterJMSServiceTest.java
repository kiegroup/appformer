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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.commons.cluster.context.LocalClusterContextProperties;
import org.uberfire.commons.cluster.context.RemoteClusterContextProperties;

@RunWith(MockitoJUnitRunner.class)
public class ClusterJMSServiceTest {

    private static ConnectionFactory factory;
    private Connection connection;
    private Session session1;
    private Session session2;

    @Before
    public void setup() throws JMSException {
        System.setProperty(ClusterParameters.APPFORMER_INITIAL_CONTEXT_FACTORY,
                this.getClass().getCanonicalName() + "$MyContextFactory");

        factory = mock(ConnectionFactory.class);
        connection = mock(Connection.class);
        when(factory.createConnection(any(), any())).thenReturn(connection);
        session1 = mock(Session.class);
        session2 = mock(Session.class);
        when(connection.createSession(eq(false),
                                      eq(Session.AUTO_ACKNOWLEDGE)))
                .thenReturn(session1, session2);
    }

    @After
    public void tearDown() {
        System.clearProperty(ClusterParameters.APPFORMER_CLUSTER);
        System.clearProperty(ClusterParameters.APPFORMER_PROVIDER_URL);
        System.clearProperty(ClusterParameters.APPFORMER_INITIAL_CONTEXT_FACTORY);
        System.clearProperty(ClusterParameters.APPFORMER_JMS_USERNAME);
        System.clearProperty(ClusterParameters.APPFORMER_JMS_PASSWORD);
    }

    @Test
    public void connectTest() throws JMSException {
        ClusterJMSService clusterService = new ClusterJMSService();
        clusterService.connect();
        verify(connection).setExceptionListener(any());
        verify(connection).start();
    }

    @Test
    public void sessionConsumersCreatedShouldBeClosed() throws JMSException {
        ClusterJMSService clusterService = new ClusterJMSService();
        clusterService.connect();

        clusterService.createConsumer(ClusterJMSService.DestinationType.PubSub,
                                      "dora_destination",
                                      Object.class,
                                         l -> {
                                         });
        clusterService.createConsumer(ClusterJMSService.DestinationType.PubSub,
                                      "dora_destination",
                                      Object.class,
                                         l -> {
                                         });

        clusterService.close();
        verify(session1).close();
        verify(session2).close();
        verify(connection).close();
    }

    @Test
    public void testLocalClusterContextProperties() {
        System.setProperty(ClusterParameters.APPFORMER_CLUSTER,
                "true");
        System.setProperty(ClusterParameters.APPFORMER_JMS_USERNAME,
                "admin");
        System.setProperty(ClusterParameters.APPFORMER_JMS_PASSWORD,
                "admin");
        ClusterJMSService clusterService = new ClusterJMSService();

        clusterService.connect();
        assertThat(clusterService.getClusterContextProperties()).isInstanceOf(LocalClusterContextProperties.class);
    }

    @Test
    public void testRemoteClusterContextProperties() {
        System.setProperty(ClusterParameters.APPFORMER_CLUSTER,
                "true");
        System.setProperty(ClusterParameters.APPFORMER_PROVIDER_URL,
                "http-remote://localhost:8080");
        System.setProperty(ClusterParameters.APPFORMER_JMS_USERNAME,
                "admin");
        System.setProperty(ClusterParameters.APPFORMER_JMS_PASSWORD,
                "admin");
        ClusterJMSService clusterService = new ClusterJMSService();

        clusterService.connect();
        assertThat(clusterService.getClusterContextProperties()).isInstanceOf(RemoteClusterContextProperties.class);
    }

    public static class MyContextFactory implements InitialContextFactory {

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            InitialContext mockCtx = mock(InitialContext.class);
            when(mockCtx.lookup("java:/ConnectionFactory")).thenReturn(factory);
            return mockCtx;
        }
    }
}
