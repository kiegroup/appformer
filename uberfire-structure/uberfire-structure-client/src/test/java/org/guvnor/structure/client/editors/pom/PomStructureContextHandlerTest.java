/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.guvnor.structure.client.editors.pom;

import org.guvnor.common.services.project.client.context.WorkspaceProjectContext;
import org.guvnor.structure.pom.AddPomDependencyEvent;
import org.guvnor.structure.pom.DynamicPomDependency;
import org.guvnor.structure.pom.DynamicPomDependencyDefault;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PomStructureContextHandlerTest {

    @Mock
    private WorkspaceProjectContext projContext;

    private PomStructureContext context;

    @Before
    public void setUp(){
        context = new PomStructureContext(projContext);
    }

    @Test
    public void testHandler() {
        final PomStructureContextChangeHandler handler1 = mock(PomStructureContextChangeHandler.class);
        final PomStructureContextChangeHandler handler2 = mock(PomStructureContextChangeHandler.class);

        final PomStructureContextChangeHandler.HandlerRegistration handlerRegistration1 = context.addPomStructureContextChangeHandler(handler1);
        final PomStructureContextChangeHandler.HandlerRegistration handlerRegistration2 = context.addPomStructureContextChangeHandler(handler2);

        assertNotNull(handlerRegistration1);
        assertNotNull(handlerRegistration2);

        context.removeHandler(handlerRegistration2);

        DynamicPomDependency dep = new DynamicPomDependencyDefault("groupID", "artifactID", "1.0-SNAPSHOT");
        context.onNewDynamicDependency(new AddPomDependencyEvent(dep));

        verify(handler1).onNewDynamicDependencyAdded(dep);
        verify(handler2,
              never()).onNewDynamicDependencyAdded(dep);
    }

    @Test
    public void testNewDynamicDependency() throws Exception {
        final PomStructureContextChangeHandler handler = mock(PomStructureContextChangeHandler.class);

        context.addPomStructureContextChangeHandler(handler);
        DynamicPomDependency dep = new DynamicPomDependencyDefault("groupID", "artifactID", "1.0-SNAPSHOT");

        context.onNewDynamicDependency(new AddPomDependencyEvent(dep));

        verify(handler).onNewDynamicDependencyAdded(dep);
    }


}
