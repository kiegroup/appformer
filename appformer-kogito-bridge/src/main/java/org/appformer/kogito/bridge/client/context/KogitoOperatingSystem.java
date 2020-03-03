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

package org.appformer.kogito.bridge.client.context;

import java.util.stream.Stream;

public enum KogitoOperatingSystem {

    DEFAULT("DEFAULT"),
    MACOS("MACOS"),
    LINUX("LINUX"),
    WINDOWS("WINDOWS");

    private final String name;

    KogitoOperatingSystem(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static KogitoOperatingSystem withName(final String name) {
        return Stream.of(KogitoOperatingSystem.values())
                     .filter(os -> os.getName().equalsIgnoreCase(name))
                     .findFirst().orElseThrow(() -> new IllegalArgumentException("Name not recognized: " + name));
    }

}
