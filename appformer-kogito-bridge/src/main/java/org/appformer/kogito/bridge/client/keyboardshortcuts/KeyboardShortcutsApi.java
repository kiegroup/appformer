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

public interface KeyboardShortcutsApi {

    int registerKeypress(String combination, String label, Runnable onKeyDown, Object opts);

    int registerKeyDownThenUp(String combination, String label, Runnable onKeyDown, Runnable onKeyUp, Object opts);

    void deregister(int id);

    class Opts {

        public enum Repeat {
            REPEAT,
            NO_REPEAT
        }

        public static final Opts DEFAULT = new Opts(Repeat.NO_REPEAT);

        private final Repeat repeat;

        public Opts(final Repeat repeat) {
            this.repeat = repeat;
        }

        public boolean getRepeat() {
            return Repeat.REPEAT.equals(repeat);
        }
    }
}
