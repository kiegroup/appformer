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

import org.appformer.kogito.bridge.client.interop.WindowRef;
import org.appformer.kogito.bridge.client.stateControl.interop.StateControl;
import org.appformer.kogito.bridge.client.stateControl.interop.StateControlCommand;

import javax.enterprise.context.ApplicationScoped;
import java.util.function.Supplier;

@ApplicationScoped
public class KogitoStateControlInitializer {

    private final Supplier<Boolean> envelopeEnabledSupplier;
    private final Supplier<StateControl> stateControlSupplier;

    public KogitoStateControlInitializer() {
        this(WindowRef::isEnvelopeAvailable,
                () -> WindowRef.getEnvelope().getStateControl());
    }

    KogitoStateControlInitializer(final Supplier<Boolean> envelopeEnabledSupplier,
                                  final Supplier<StateControl> stateControlSupplier) {
        this.envelopeEnabledSupplier = envelopeEnabledSupplier;
        this.stateControlSupplier = stateControlSupplier;
    }

    public boolean isKogitoEnabled() {
        return envelopeEnabledSupplier.get();
    }

    StateControl getStateControl() {
        return stateControlSupplier.get();
    }

    public void setUndoCommand(StateControlCommand command) {
        if(isKogitoEnabled()) {
            getStateControl().setUndoCommand(command);
        }
    }

    public void setRedoCommand(StateControlCommand command) {
        if(isKogitoEnabled()) {
            getStateControl().setRedoCommand(command);
        }
    }
}
