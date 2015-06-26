package org.livespark.backend.server.service.build;

import java.io.File;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.enterprise.event.Event;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.livespark.client.AppReady;

public class BuildAndDeployWithCodeServerCallable extends BuildAndDeployCallable {

    private volatile boolean isCodeServerReady = false;
    private volatile Throwable error = null;
    private ExecutorService execService;
    private Future< ? > runningCodeServer;

    BuildAndDeployWithCodeServerCallable( Project project,
                                          File pomXml,
                                          HttpSession session,
                                          String queueSessionId,
                                          ServletRequest sreq,
                                          ServerMessageBus bus,
                                          Event<AppReady> appReadyEvent,
                                          ExecutorService execService ) {
        super( project, pomXml, session, queueSessionId, sreq, bus, appReadyEvent );
        this.execService = execService;
    }

    @Override
    protected InvocationResult executeRequest() throws Throwable {
        final InvocationRequest codeServerRequest = createCodeServerRequest( pomXml );
        final DefaultInvocationRequest packageRequest = createDevModePackageRequest( pomXml );

        setCodeServerOutputHandler( codeServerRequest );
        setPackageOutputHandler( packageRequest );

        maybeLaunchCodeServer( codeServerRequest );
        blockUntilCodeServerIsReadyOrError();
        if ( error != null ) {
            // TODO Handle this error. Currently gets swallowed.
            throw error;
        }

        return new DefaultInvoker().execute( packageRequest );
    }

    protected InvocationRequest createCodeServerRequest( final File pomXml ) {
        final DefaultInvocationRequest codeServerRequest = new DefaultInvocationRequest();
        final Properties codeServerProperties = new Properties();
        final File webappFolder = new File( pomXml.getParentFile(), "src/main/webapp" );

        codeServerProperties.setProperty( "gwt.codeServer.launcherDir", webappFolder.getAbsolutePath() );

        codeServerRequest.setPomFile( pomXml );
        codeServerRequest.setGoals( Collections.singletonList( "gwt:run-codeserver" ) );
        codeServerRequest.setProperties( codeServerProperties );

        return codeServerRequest;
    }

    protected DefaultInvocationRequest createDevModePackageRequest( final File pomXml ) {
        final DefaultInvocationRequest packageRequest = new DefaultInvocationRequest();
        final Properties props = new Properties();

        props.setProperty( "gwt.compiler.skip", "true" );

        packageRequest.setPomFile( pomXml );
        packageRequest.setGoals( Collections.singletonList( "package" ) );
        packageRequest.setProperties( props );

        return packageRequest;
    }

    private void maybeLaunchCodeServer(final InvocationRequest codeServerRequest ) {
        if ( !isCodeServerReady ) {
            runningCodeServer = execService.submit( new Runnable() {
                @Override
                public void run() {
                    try {
                        new DefaultInvoker().execute( codeServerRequest );
                    } catch ( MavenInvocationException e ) {
                        error = e;
                    }
                }
            } );
        }
    }

    private void maybeShutdownCodeServer() {
        if ( runningCodeServer != null )
            runningCodeServer.cancel( true );
    }

    private void blockUntilCodeServerIsReadyOrError() throws InterruptedException {
        while ( !( isCodeServerReady || error != null ) ) {
            Thread.sleep( 500 );
        }
    }

    private void setCodeServerOutputHandler( final InvocationRequest codeServerRequest ) {
        codeServerRequest.setOutputHandler( new InvocationOutputHandler() {

            @Override
            public void consumeLine( String line ) {
                if ( !isCodeServerReady && line.contains( "The code server is ready at" ) ) {
                    isCodeServerReady = true;
                }
                sendOutputToClient( line );
            }
        } );
    }

    private void setPackageOutputHandler( final InvocationRequest packageRequest ) {
        packageRequest.setOutputHandler( new InvocationOutputHandler() {
            @Override
            public void consumeLine( String line ) {
                sendOutputToClient( line );
            }
        } );
    }

    @Override
    public void valueBound( HttpSessionBindingEvent event ) {
    }

    @Override
    public void valueUnbound( HttpSessionBindingEvent event ) {
        maybeShutdownCodeServer();
        super.valueUnbound( event );
    }
}