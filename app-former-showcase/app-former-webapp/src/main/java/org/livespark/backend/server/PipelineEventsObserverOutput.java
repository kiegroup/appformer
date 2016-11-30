/*
 * Copyright 2016 JBoss by Red Hat.
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
package org.livespark.backend.server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.guvnor.ala.pipeline.events.AfterPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.AfterStageExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforePipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforeStageExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorStageExecutionEvent;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.api.RpcContext;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.common.client.protocols.MessageParts;

/**
 */
@ApplicationScoped
public class PipelineEventsObserverOutput {

    @Inject
    private ServerMessageBus bus;

    public void beforePipelineEvent(@Observes BeforePipelineExecutionEvent bpee) {
        final String queueSessionId = RpcContext.getQueueSession().getSessionId();
        MessageBuilder.createMessage()
                .toSubject("MavenBuilderOutput")
                .signalling()
                .with(MessageParts.SessionID, queueSessionId)
                .with("output", "Before Starting  Pipeline: " + bpee.getPipeline().getName() + "\n")
                .noErrorHandling().sendNowWith(bus);
    }

    public void afterPipelineEvent(@Observes AfterPipelineExecutionEvent bpee) {
        final String queueSessionId = RpcContext.getQueueSession().getSessionId();
        MessageBuilder.createMessage()
                .toSubject("MavenBuilderOutput")
                .signalling()
                .with(MessageParts.SessionID, queueSessionId)
                .with("output", "After Completing  Pipeline: " + bpee.getPipeline().getName() + "\n")
                .noErrorHandling().sendNowWith(bus);
    }

    public void beforeStageEvent(@Observes BeforeStageExecutionEvent bpee) {
        final String queueSessionId = RpcContext.getQueueSession().getSessionId();
        MessageBuilder.createMessage()
                .toSubject("MavenBuilderOutput")
                .signalling()
                .with(MessageParts.SessionID, queueSessionId)
                .with("output", "Before Stage : " + bpee.getStage().getName() + "\n")
                .noErrorHandling().sendNowWith(bus);
    }

    public void afterStageEvent(@Observes AfterStageExecutionEvent bpee) {
        final String queueSessionId = RpcContext.getQueueSession().getSessionId();
        MessageBuilder.createMessage()
                .toSubject("MavenBuilderOutput")
                .signalling()
                .with(MessageParts.SessionID, queueSessionId)
                .with("output", "After Stage : " + bpee.getStage().getName() + "\n")
                .noErrorHandling().sendNowWith(bus);
    }
    
    public void onStageErrorEvent(@Observes OnErrorStageExecutionEvent bpee) {
        final String queueSessionId = RpcContext.getQueueSession().getSessionId();
        MessageBuilder.createMessage()
                .toSubject("MavenBuilderOutput")
                .signalling()
                .with(MessageParts.SessionID, queueSessionId)
                .with("output", "Error in Stage Stage : " + bpee.getStage().getName() + "\n" + bpee.getError().getMessage() + "\n")
                .noErrorHandling().sendNowWith(bus);
    }
    
    public void onStageErrorEvent(@Observes OnErrorPipelineExecutionEvent bpee) {
        final String queueSessionId = RpcContext.getQueueSession().getSessionId();
        MessageBuilder.createMessage()
                .toSubject("MavenBuilderOutput")
                .signalling()
                .with(MessageParts.SessionID, queueSessionId)
                .with("output", "Error in Pipeline  : " + bpee.getPipeline().getName() + "\n" + bpee.getError().getMessage() + "\n")
                .noErrorHandling().sendNowWith(bus);
    }

}
