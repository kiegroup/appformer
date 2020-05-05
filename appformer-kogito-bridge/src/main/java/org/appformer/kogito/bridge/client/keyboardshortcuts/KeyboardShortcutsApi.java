/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.appformer.kogito.bridge.client.keyboardshortcuts;

import elemental2.dom.EventTarget;
import jsinterop.annotations.JsFunction;
import org.appformer.client.keyboardShortcuts.KeyboardShortcutsApiOpts;

public interface KeyboardShortcutsApi {

    int registerKeyPress(String combination, String label, KeyboardShortcutsApi.Action onKeyDown, KeyboardShortcutsApiOpts opts);

    int registerKeyDownThenUp(String combination, String label, KeyboardShortcutsApi.Action onKeyDown, KeyboardShortcutsApi.Action onKeyUp, KeyboardShortcutsApiOpts opts);

    void deregister(int id);

    @JsFunction
    @FunctionalInterface
    interface Action {

        void execute(final EventTarget target);
    }
}
