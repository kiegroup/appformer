package org.livespark.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchScreen;

import com.google.gwt.user.client.ui.Composite;

@ApplicationScoped
@WorkbenchScreen(identifier = "MavenBuildOutput")
public class MavenBuildOutput extends Composite {

    @Inject
    private MessageBus bus;

    @Inject
    private GeneralTextEditorScreenView textEditorScreen;

    @PostConstruct
    public void init() {
        initWidget( textEditorScreen );

        textEditorScreen.setWrapMode( true );
        textEditorScreen.setContent( null, "" );
        textEditorScreen.setReadOnly( true );

        bus.subscribe( "MavenBuilderOutput", new MessageCallback() {

            @Override
            public void callback( Message message ) {
                final Boolean clean = message.get( Boolean.class, "clean" );
                final String content = message.get( String.class, "output" );
                if ( clean != null && clean ) {
                    textEditorScreen.setContentAndScroll( content );
                } else {
                    textEditorScreen.appendContentAndScroll( content );
                }
            }
        } );
    }

    @WorkbenchPartTitle
    public String title() {
        return "Build Output";
    }

}