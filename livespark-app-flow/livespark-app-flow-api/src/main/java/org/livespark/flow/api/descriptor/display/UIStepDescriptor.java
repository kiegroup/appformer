/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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


package org.livespark.flow.api.descriptor.display;

import org.livespark.flow.api.Displayer;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.UIComponent;
import org.livespark.flow.api.descriptor.StepDescriptor;

/**
 * A descriptor for a {@link Step} that shows and/or hides a {@link UIComponent}.
 *
 * @see UIComponentDescriptor
 * @see DisplayerDescriptor
 */
public interface UIStepDescriptor extends StepDescriptor {

    /**
     * Represents the possible actions performed by a {@link Displayer} in a UI step.
     */
    enum Action {
        /**
         * Only shows the component.
         */
        SHOW,
        /**
         * Only hides the component.
         */
        HIDE,
        /**
         * Shows the component at the start and hides it at the end.
         */
        SHOW_AND_HIDE;
    }

    /**
     * @return A descriptor of the {@link UIComponent} shown in the described step.
     */
    UIComponentDescriptor getUIComponent();

    /**
     * @return A descriptor of the {@link Displayer} used to show and/or hide the component in this
     *         step.
     */
    DisplayerDescriptor getDisplayerDescriptor();

    /**
     * @return The action (showing, hiding, or both) performed by the {@link Displayer} in this
     *         step.
     */
    Action getAction();

}
