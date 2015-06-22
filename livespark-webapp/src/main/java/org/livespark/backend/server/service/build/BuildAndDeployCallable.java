package org.livespark.backend.server.service.build;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.event.Event;
import javax.servlet.ServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.maven.shared.invoker.InvocationResult;
import org.guvnor.common.services.project.builder.model.BuildMessage;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.livespark.client.AppReady;

public class BuildAndDeployCallable extends BaseBuildCallable {

    private final Event<AppReady> appReadyEvent;

    BuildAndDeployCallable( Project project,
                                   File pomXml,
                                   String sessionId,
                                   ServletRequest sreq,
                                   ServerMessageBus bus,
                                   Event<AppReady> appReadyEvent ) {
        super( project, pomXml, sessionId, sreq, bus );
        this.appReadyEvent = appReadyEvent;
    }

    @Override
    protected List<BuildMessage> postBuildTasks( InvocationResult res ) throws Exception {
        final List<BuildMessage> messages = new ArrayList<BuildMessage>();
        messages.addAll( processBuildResults( res ) );
        messages.addAll( deployIfSuccessful( res ) );

        return messages;
    }

    protected List<BuildMessage> deployIfSuccessful( InvocationResult res ) throws MalformedURLException, URISyntaxException, IOException, Exception {
        if ( res.getExitCode() == 0 )
            deploy();

        return Collections.emptyList();
    }

    protected void deploy() throws MalformedURLException, URISyntaxException, IOException, Exception {
        final File targetDir = new File( pomXml.getParent(), "/target" );
        final Collection<File> wars = FileUtils.listFiles( targetDir, new String[]{"war"}, false );
        for ( final File war : wars ) {
            sendOutputToClient("Deploying " + war.getName() + "...", sessionId);
            String home = getWildflyHome();
            File deployDir = new File( home, "/standalone/deployments" );
            FileUtils.deleteQuietly( new File( deployDir, war.getName() ) );
            FileUtils.copyFileToDirectory( war, deployDir );

            FileAlterationMonitor monitor = new FileAlterationMonitor( 500 );
            IOFileFilter filter = FileFilterUtils.nameFileFilter( war.getName() + ".deployed" );
            FileAlterationObserver observer = new FileAlterationObserver( deployDir, filter );
            observer.addListener( new FileAlterationListenerAdaptor() {

                @Override
                public void onFileCreate( final File file ) {
                   fireAppReadyEvent(war, sreq);
                }

                @Override
                public void onFileChange(final File file) {
                    fireAppReadyEvent(war, sreq);
                }
            } );
            monitor.addObserver( observer );
            monitor.start();
        }
    }

    private void fireAppReadyEvent(File war, ServletRequest sreq) {
        final String url = "http://" +
            sreq.getServerName() + ":" +
            sreq.getServerPort() + "/" +
            war.getName().replace( ".war", "" );

        appReadyEvent.fire( new AppReady( url ) );
    }
}