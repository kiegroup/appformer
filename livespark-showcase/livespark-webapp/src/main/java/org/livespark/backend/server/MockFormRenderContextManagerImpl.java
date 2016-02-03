/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import org.jbpm.formModeler.api.client.FormRenderContext;
import org.jbpm.formModeler.api.client.FormRenderContextManager;
import org.jbpm.formModeler.api.events.FormSubmitFailEvent;
import org.jbpm.formModeler.api.events.FormSubmittedEvent;
import org.jbpm.formModeler.api.events.ResizeFormcontainerEvent;
import org.jbpm.formModeler.api.model.Form;

@ApplicationScoped
public class MockFormRenderContextManagerImpl implements FormRenderContextManager {

    @Override 
    public FormRenderContext newContext(Form form, String deploymentId, Map<String, Object> ctx) {
        throw new UnsupportedOperationException("Form modeler is not available");
    }

    @Override 
    public FormRenderContext newContext(Form form, String deploymentId, Map<String, Object> inputData, Map<String, Object> outputData) {
        throw new UnsupportedOperationException("Form modeler is not available");
    }

    @Override
    public FormRenderContext getFormRenderContext( String UID ) {
        throw new UnsupportedOperationException( "Form modeler is not available" );
    }

    @Override
    public FormRenderContext getRootContext( String UID ) {
        throw new UnsupportedOperationException( "Form modeler is not available" );
    }

    @Override
    public void removeContext( String ctxUID ) {
        throw new UnsupportedOperationException( "Form modeler is not available" );
    }

    @Override
    public void removeContext( FormRenderContext context ) {
        throw new UnsupportedOperationException( "Form modeler is not available" );
    }

    @Override
    public void fireContextSubmitError( FormSubmitFailEvent event ) {
        throw new UnsupportedOperationException( "Form modeler is not available" );
    }

    @Override
    public void fireContextSubmit( FormSubmittedEvent event ) {
        throw new UnsupportedOperationException( "Form modeler is not available" );
    }

    @Override
    public void fireContextFormResize( ResizeFormcontainerEvent event ) {
        throw new UnsupportedOperationException( "Form modeler is not available" );
    }

    @Override
    public void persistContext( FormRenderContext ctx ) throws Exception {
        throw new UnsupportedOperationException( "Form modeler is not available" );
    }

    @Override
    public void persistContext( String ctxUID ) throws Exception {
        throw new UnsupportedOperationException( "Form modeler is not available" );
    }
}
