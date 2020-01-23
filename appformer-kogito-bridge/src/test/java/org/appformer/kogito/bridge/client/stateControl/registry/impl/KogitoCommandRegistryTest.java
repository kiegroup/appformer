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

package org.appformer.kogito.bridge.client.stateControl.registry.impl;

import org.appformer.kogito.bridge.client.stateControl.registry.interop.KogitoJSCommandRegistry;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KogitoCommandRegistryTest {

    private boolean envelopeEnabled = true;

    @Mock
    private KogitoJSCommandRegistry<Object> kogitoJSCommandRegistry;

    private KogitoCommandRegistry<Object> commandRegistry;

    @Test
    public void testBuildOutsideEnvelope() {
        this.envelopeEnabled = false;

        Assertions.assertThatThrownBy(() -> new KogitoCommandRegistry<>(() -> envelopeEnabled, () -> kogitoJSCommandRegistry))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Envelope isn't present, we shouldn't be here!");
    }

    @Test
    public void testMethods() {

        when(kogitoJSCommandRegistry.getCommands()).thenReturn(new Object[]{});
        commandRegistry = new KogitoCommandRegistry<>(() -> envelopeEnabled, () -> kogitoJSCommandRegistry);

        commandRegistry.setRegistryChangeListener(() -> {});
        verify(kogitoJSCommandRegistry).setRegistryChangeListener(any());

        commandRegistry.register(new Object());
        verify(kogitoJSCommandRegistry).register(anyString(), anyObject());

        commandRegistry.peek();
        verify(kogitoJSCommandRegistry).peek();

        commandRegistry.pop();
        verify(kogitoJSCommandRegistry).pop();

        commandRegistry.clear();
        verify(kogitoJSCommandRegistry).clear();

        commandRegistry.isEmpty();
        verify(kogitoJSCommandRegistry).isEmpty();

        commandRegistry.getCommandHistory();
        verify(kogitoJSCommandRegistry).getCommands();

        commandRegistry.setMaxSize(1);
        verify(kogitoJSCommandRegistry).setMaxSize(eq(1));
    }
}
