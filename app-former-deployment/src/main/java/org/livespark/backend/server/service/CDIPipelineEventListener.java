/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.backend.server.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.guvnor.ala.pipeline.events.AfterPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.AfterStageExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforePipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforeStageExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorStageExecutionEvent;
import org.guvnor.ala.pipeline.events.PipelineEvent;
import org.guvnor.ala.pipeline.events.PipelineEventListener;

/*
 * Pipeline Event Listener that forward the pipeline emitted events to CDI events 
*/
@ApplicationScoped
public class CDIPipelineEventListener implements PipelineEventListener {

    @Inject
    private Event<PipelineEvent> events;

    @Override
    public void beforePipelineExecution( BeforePipelineExecutionEvent bpee ) {
        events.fire( bpee );
    }

    @Override
    public void afterPipelineExecution( AfterPipelineExecutionEvent apee ) {
        events.fire( apee );
    }

    @Override
    public void beforeStageExecution( BeforeStageExecutionEvent bsee ) {
        events.fire( bsee );
    }

    @Override
    public void onStageError( OnErrorStageExecutionEvent oesee ) {
        events.fire( oesee );
    }

    @Override
    public void afterStageExecution( AfterStageExecutionEvent asee ) {
        events.fire( asee );
    }

    @Override
    public void onPipelineError( OnErrorPipelineExecutionEvent oepee ) {
        events.fire( oepee );
    }

}
