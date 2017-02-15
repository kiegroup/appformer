/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.flowset.interpeter.util;

import java.util.ArrayList;
import java.util.List;

import org.kie.appformer.flow.api.Displayer;
import org.kie.appformer.flow.api.UIComponent;

public class TestDisplayer<C> implements Displayer<C> {

    public List<C> shown = new ArrayList<>();
    public List<C> hidden = new ArrayList<>();

    @Override
    public void show( final UIComponent<?, ?, C> uiComponent ) {
        shown.add( uiComponent.asComponent() );
    }

    @Override
    public void hide( final UIComponent<?, ?, C> uiComponent ) {
        hidden.add( uiComponent.asComponent() );
    }

}
