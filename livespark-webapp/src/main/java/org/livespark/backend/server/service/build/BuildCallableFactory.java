package org.livespark.backend.server.service.build;

import java.io.File;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.livespark.client.AppReady;

@ApplicationScoped
public class BuildCallableFactory {

    private static final String CODE_SERVER_CALLABLE_ATTR_KEY = BuildAndDeployWithCodeServerCallable.class.getCanonicalName();

    @Inject
    private Event<AppReady> appReadyEvent;

    @Inject
    private ServerMessageBus bus;

    @Resource
    private ManagedExecutorService execService;

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
                                                                 execService );
            session.setAttribute( CODE_SERVER_CALLABLE_ATTR_KEY, callable );
        }

        return callable;
    }

}
