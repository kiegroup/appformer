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

package org.appformer.kogito.bridge.client.guided.tour.service;

import org.appformer.kogito.bridge.client.guided.tour.service.api.Tutorial;
import org.appformer.kogito.bridge.client.guided.tour.service.api.UserInteraction;

public interface GuidedTourService {

    void refresh(final UserInteraction userInteraction);

    void registerTutorial(final Tutorial tutorial);

    boolean isEnabled();

    GuidedTourService DEFAULT = new GuidedTourService() {

        @Override
        public void refresh(final UserInteraction userInteraction) {
            // empty
        }

        @Override
        public void registerTutorial(final Tutorial tutorial) {
            // empty
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };
}
