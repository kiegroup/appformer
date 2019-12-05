/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.uberfire.client.workbench;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.user.client.Window;
import org.uberfire.mvp.ParameterizedCommand;

/**
 * Generic WindowCloseHandler
 */
public class WorkbenchCloseHandlerImpl implements WorkbenchCloseHandler {

    @Override
    public void onWindowClosing(final ParameterizedCommand<Window.ClosingEvent> command,
                                final Window.ClosingEvent event) {
        if (command != null) {
            command.execute(event);
        }
    }

    @Override
    public void onWindowClose(final ParameterizedCommand<CloseEvent<Window>> command,
                              final CloseEvent<Window> event) {
        if (command != null) {
            command.execute(event);
        }
    }
}
