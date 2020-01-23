/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.appformer.kogito.bridge.client.stateControl;

import org.appformer.kogito.bridge.client.stateControl.interop.StateControl;
import org.appformer.kogito.bridge.client.stateControl.interop.StateControlCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)

public class KogitoStateControlInitializerTest {

    @Mock
    private StateControl stateControl;

    @Mock
    private StateControlCommand stateControlCommand;

    private boolean envelopeEnabled;

    private KogitoStateControlInitializer initializer;

    @Before
    public void init() {
        initializer = new KogitoStateControlInitializer(() -> envelopeEnabled, () -> stateControl);
    }

    @Test
    public void testInitStateControlInKogito() {
        this.envelopeEnabled = true;

        assertTrue(initializer.isKogitoEnabled());

        initializer.setUndoCommand(stateControlCommand);
        initializer.setRedoCommand(stateControlCommand);

        verify(stateControl).setUndoCommand(same(stateControlCommand));
        verify(stateControl).setRedoCommand(same(stateControlCommand));
    }

    @Test
    public void testInitStateControlOutsideKogito() {
        this.envelopeEnabled = false;

        assertFalse(initializer.isKogitoEnabled());

        initializer.setUndoCommand(stateControlCommand);
        initializer.setRedoCommand(stateControlCommand);

        verify(stateControl, never()).setUndoCommand(same(stateControlCommand));
        verify(stateControl, never()).setRedoCommand(same(stateControlCommand));
    }
}
