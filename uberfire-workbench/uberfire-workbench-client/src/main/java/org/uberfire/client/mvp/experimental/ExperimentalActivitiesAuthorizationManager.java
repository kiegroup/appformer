/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.client.mvp.experimental;

import org.uberfire.client.mvp.WorkbenchActivity;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PartDefinition;

/**
 * Handles authorization management for any {@link WorkbenchActivity} marked as experimental
 */
public interface ExperimentalActivitiesAuthorizationManager {

    /**
     * Initializes the manager
     */
    void init();

    /**
     * Determines if the experimental framework enables rendering a given activity
     * @param activity
     * @return true or false depending on the settings.
     */
    boolean authorize(Object activity);

    /**
     *
     * @param panel
     * @param part
     */
    void securePart(PanelDefinition panel, PartDefinition part);
}
