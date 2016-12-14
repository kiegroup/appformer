/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.provisioning.client.build;

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
@WorkbenchScreen( identifier = "BuildConsoleScreen" )
public class BuildConsoleScreen extends Composite {

    @Inject
    private MessageBus bus;

    @Inject
    private BuildConsole outputScreen;

    @PostConstruct
    public void init() {
        initWidget( outputScreen );

        outputScreen.setWrapMode( true );
        outputScreen.setContent( null, "" );
        outputScreen.setReadOnly( true );

        bus.subscribe( "MavenBuilderOutput", new MessageCallback() {

            @Override
            public void callback( Message message ) {
                final Boolean clean = message.get( Boolean.class, "clean" );
                final String content = message.get( String.class, "output" );
                if ( clean != null && clean ) {
                    outputScreen.setContentAndScroll( content );
                } else {
                    outputScreen.appendContentAndScroll( content );
                }
            }
        } );
    }

    @WorkbenchPartTitle
    public String title() {
        return "Build Output";
    }

}
