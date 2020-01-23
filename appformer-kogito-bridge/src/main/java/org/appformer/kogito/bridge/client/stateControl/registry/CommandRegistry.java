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

package org.appformer.kogito.bridge.client.stateControl.registry;

import java.util.List;

/**
 * Represents a registry of commands to be used on the StateControl engine.
 * @param <C> anything that can be considered a Command
 */
public interface CommandRegistry<C> {

    /**
     * Registers a command into the registry
     * @param command A command to register
     */
    void register(final C command);

    /**
     * Peeks the last added command. Doesn't remove it.
     * @return The last added Command
     */
    C peek();

    /**
     * Pops the last added command and removes it.
     * @return The last added Command
     */
    C pop();

    /**
     * Sets the max number of commands that can be stored on the registry.
     * @param size A positive integer
     */
    void setMaxSize(final int size);

    /**
     * Clears the registry
     */
    void clear();

    /**
     * Determines if the registry is empty or not
     * @return true if empty, false if not.
     */
    boolean isEmpty();

    /**
     * Returns a {@link List} containing all the commands in the registry
     * @return A {@link List} of commands
     */
    List<C> getCommandHistory();

    /**
     * Sets a {@link RegistryChangeListener} to be called when the registry changes.
     * @param registryChangeListener A {@link RegistryChangeListener}
     */
    void setRegistryChangeListener(RegistryChangeListener registryChangeListener);
}
