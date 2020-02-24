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

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.appformer.kogito.api.stateControl.registry.CommandRegistry;
import org.appformer.kogito.api.stateControl.registry.CommandRegistryChangeListener;
import org.appformer.kogito.bridge.client.interop.WindowRef;
import org.appformer.kogito.bridge.client.stateControl.interop.StateControl;
import org.appformer.kogito.bridge.client.stateControl.registry.interop.KogitoJSCommandRegistry;

public class KogitoCommandRegistry<C> implements CommandRegistry<C> {

    private KogitoJSCommandRegistry<C> wrapped;
    private CommandRegistryChangeListener commandRegistryChangeListener;

    public KogitoCommandRegistry() {
        this(WindowRef::isEnvelopeAvailable, () -> StateControl.get().getCommandRegistry());
    }

    KogitoCommandRegistry(Supplier<Boolean> envelopeEnabledSupplier, Supplier<KogitoJSCommandRegistry<C>> kogitoJSCommandRegistrySupplier) {
        if (!envelopeEnabledSupplier.get()) {
            throw new RuntimeException("Envelope isn't present, we shouldn't be here!");
        }
        wrapped = kogitoJSCommandRegistrySupplier.get();
    }

    @Override
    public void register(C command) {
        wrapped.register(String.valueOf(command.hashCode()), command);
        notifyRegistryChange();
    }

    @Override
    public C peek() {
        return wrapped.peek();
    }

    @Override
    public C pop() {
        Optional<C> optional = Optional.ofNullable(wrapped.pop());
        if (optional.isPresent()) {
            notifyRegistryChange();
        }
        return optional.orElse(null);
    }

    @Override
    public List<C> getCommandHistory() {
        return Stream.of(wrapped.getCommands())
                .collect(Collectors.toList());
    }

    @Override
    public void setMaxSize(int size) {
        if(size < 0) {
            throw new IllegalArgumentException("The registry size should be a positive number");
        }
        wrapped.setMaxSize(size);
    }

    @Override
    public void clear() {
        wrapped.clear();
        notifyRegistryChange();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public void setCommandRegistryChangeListener(final CommandRegistryChangeListener commandRegistryChangeListener) {
        this.commandRegistryChangeListener = commandRegistryChangeListener;
    }

    private void notifyRegistryChange() {
        if (commandRegistryChangeListener != null) {
            commandRegistryChangeListener.notifyRegistryChange();
        }
    }
}
