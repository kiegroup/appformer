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


package org.livespark.flow.api;

import java.util.function.Consumer;

/**
 * <p>
 * A useful interface for UI components that are used in {@link Step steps} but can be displayed in
 * multiple ways (i.e. in a tab on a page or in a modal panel).
 *
 * <p>
 * A step displaying {@code UIComponents} should perform these steps:
 * <ol>
 * <li>Call {@link #asComponent()} and display the return value.
 * <li>Call {@link #start(Object, Consumer)} to initiate the user interaction.
 * <li>Handle the output of the component when the callback is invoked (likely by passing it on as
 * the output of the display step).
 * </ol>
 *
 * <p>
 * A step hiding {@code UIComponents} should perform these steps:
 * <ol>
 * <li>Hide the component returned by {@link #asComponent()}.
 * <li>Call {@link #destroy()}.
 * </ol>
 *
 * @param <INPUT>
 *            The type of the input required for the user interaction of this component.
 * @param <OUTPUT>
 *            The type of the output produced by the user interaction of thsi component.
 * @param <COMPONENT>
 *            The type of the component that is to be displayed to the user.
 */
public interface UIComponent<INPUT, OUTPUT, COMPONENT> {

    /**
     * Starts this component. A component need not accept any user interaction before this method is
     * invoked. A componenet may be started multiple input values. A component should only invoke
     * the output callback once for each time it is started.
     *
     * @param input
     *            The input required by this component.
     * @param callback
     *            A callback for returning output. Never null.
     */
    void start( INPUT input, Consumer<OUTPUT> callback );

    /**
     * A handle for UI components to perform clean-up after they have been hidden. Once this is
     * called a UI component is out of service.
     */
    void onHide();

    /**
     * @return The aspect of this component that is a displayable UI element (such as a GWT Widget or an HTML Element).
     */
    COMPONENT asComponent();

    /**
     * @return A name for this component, used for logging and error messages.
     */
    String getName();

}
