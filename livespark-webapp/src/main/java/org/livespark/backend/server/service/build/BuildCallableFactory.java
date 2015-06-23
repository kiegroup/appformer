package org.livespark.backend.server.service.build;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletRequest;

import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.livespark.client.AppReady;

@ApplicationScoped
public class BuildCallableFactory {

    @Inject
    private Event<AppReady> appReadyEvent;

    @Inject
    private ServerMessageBus bus;

    @Resource
    private ManagedExecutorService execService;

    private Map<String, BuildCallable> codeServerCallablesBySession = new ConcurrentHashMap<String, BuildCallable>();

    public BuildCallable createProductionDeploymentCallable( final Project project,
                                                             final File pomXml,
                                                             final String sessionId,
                                                             final ServletRequest sreq ) {
        return new BuildAndDeployCallable( project,
                                           pomXml,
                                           sessionId,
                                           sreq,
                                           bus,
                                           appReadyEvent );
    }

    public BuildCallable createDevModeDeploymentCallable( final Project project,
                                                          final File pomXml,
                                                          final String sessionId,
                                                          final ServletRequest sreq ) {
        // TODO Fix memory leak
        BuildCallable callable = codeServerCallablesBySession.get( sessionId );
        if ( callable == null ) {
            callable = new BuildAndDeployWithCodeServerCallable( project,
                                                                 pomXml,
                                                                 sessionId,
                                                                 sreq,
                                                                 bus,
                                                                 appReadyEvent,
                                                                 execService );
            codeServerCallablesBySession.put( sessionId, callable );
        }

        return callable;
    }

}
