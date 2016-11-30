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


package org.livespark.flow.impl.descriptor;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.livespark.flow.api.descriptor.display.DisplayerDescriptor;
import org.livespark.flow.api.descriptor.display.UIComponentDescriptor;
import org.livespark.flow.api.descriptor.display.UIStepDescriptor;
import org.livespark.flow.api.descriptor.type.Type;

@Portable
public class UIStepDescriptorImpl implements UIStepDescriptor {

    private final UIComponentDescriptor uiComponentInstance;
    private final DisplayerDescriptor displayerDescriptor;
    private final Action action;

    public UIStepDescriptorImpl( final @MapsTo("displayerDescriptor") DisplayerDescriptor displayerDescriptor,
                                 final @MapsTo( "uiComponentInstance" ) UIComponentDescriptor uiComponentInstance,
                                 final @MapsTo("action") Action action ) {
        this.displayerDescriptor = displayerDescriptor;
        this.uiComponentInstance = uiComponentInstance;
        this.action = action;
    }

    @Override
    public UIComponentDescriptor getUIComponent() {
        return uiComponentInstance;
    }

    @Override
    public Type getInputType() {
        return uiComponentInstance.getInputType();
    }

    @Override
    public Type getOutputType() {
        return uiComponentInstance.getOutputType();
    }

    @Override
    public DisplayerDescriptor getDisplayerDescriptor() {
        return displayerDescriptor;
    }

    @Override
    public Action getAction() {
        return action;
    }

}
