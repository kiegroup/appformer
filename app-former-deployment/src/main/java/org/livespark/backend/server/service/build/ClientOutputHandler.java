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

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.common.client.protocols.MessageParts;

public class ClientOutputHandler implements OutputHandler {

    private final ServerMessageBus bus;
    private final String queueSessionId;

    public ClientOutputHandler( final ServerMessageBus bus, final String queueSessionId ) {
        this.bus = bus;
        this.queueSessionId = queueSessionId;
    }

    @Override
    public void handleOutput( String line ) {
        MessageBuilder.createMessage()
            .toSubject( "MavenBuilderOutput" )
            .signalling()
            .with( MessageParts.SessionID, queueSessionId )
            .with( "output", line + "\n" )
            .noErrorHandling().sendNowWith( bus );
    }

}
