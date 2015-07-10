/*
 * Copyright 2015 JBoss Inc
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

package org.livespark.backend.server.service.build;

import java.io.File;
import java.util.Set;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.mina.util.ConcurrentHashSet;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.livespark.client.shared.AppReady;

@ApplicationScoped
public class BuildCallableFactory {

    private static final String CODE_SERVER_CALLABLE_ATTR_KEY = BuildAndDeployWithCodeServerCallable.class.getCanonicalName();

    // TODO make configurable
    private static final int CODE_SERVER_LOWEST_PORT = 50000;
    private static final int CODE_SERVER_HIGHEST_PORT = 50100;

    @Inject
    private Event<AppReady> appReadyEvent;

    @Inject
    private ServerMessageBus bus;

    @Resource
    private ManagedExecutorService execService;

    private final Set<Integer> leasedCodeServerPorts = new ConcurrentHashSet<Integer>();

    public BuildCallable createProductionDeploymentCallable( final Project project,
                                                             final File pomXml,
                                                             final HttpSession session,
                                                             final String queueSessionId,
                                                             final ServletRequest sreq ) {
        return new BuildAndDeployCallable( project,
                                           pomXml,
                                           session,
                                           queueSessionId,
                                           sreq,
                                           bus,
                                           appReadyEvent );
    }

    public BuildCallable createDevModeDeploymentCallable( final Project project,
                                                          final File pomXml,
                                                          final HttpSession session,
                                                          final String queueSessionId,
                                                          final ServletRequest sreq ) {
        BuildCallable callable = (BuildCallable) session.getAttribute( CODE_SERVER_CALLABLE_ATTR_KEY );
        if ( callable == null ) {
            callable = new BuildAndDeployWithCodeServerCallable( project,
                                                                 pomXml,
                                                                 session,
                                                                 queueSessionId,
                                                                 sreq,
                                                                 bus,
                                                                 appReadyEvent,
                                                                 getAvailableCodeServerPort(),
                                                                 execService );
            session.setAttribute( CODE_SERVER_CALLABLE_ATTR_KEY, callable );
        }

        return callable;
    }

    private CodeServerPortHandle getAvailableCodeServerPort() {
        return new CodeServerPortHandle() {

            private Integer leasedPort = leaseAvailableCodeServerPort();

            @Override
            public void relinquishPort() {
                leasedCodeServerPorts.remove( leasedPort );
                leasedPort = null;
            }

            @Override
            public Integer getPortNumber() {
                if ( leasedPort != null )
                    return leasedPort;
                else
                    throw new RuntimeException( "Cannot get port number after relinquishing." );
            }
        };
    }

    private synchronized Integer leaseAvailableCodeServerPort() {
        Integer port = CODE_SERVER_LOWEST_PORT;

        while ( port <= CODE_SERVER_HIGHEST_PORT && leasedCodeServerPorts.contains( port ) ) {
            port++;
        }

        if ( port > CODE_SERVER_HIGHEST_PORT ) {
            throw new RuntimeException( "All available code server ports are in use." );
        }

        leasedCodeServerPorts.add( port );

        return port;
    }

}
