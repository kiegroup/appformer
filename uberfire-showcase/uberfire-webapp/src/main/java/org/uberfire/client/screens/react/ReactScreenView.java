/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.client.screens.react;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.ScriptInjector;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class ReactScreenView implements IsElement {

    @Inject
    @DataField("container")
    public HTMLDivElement container;

    @AfterInitialization
    public void init() {

        setContainer(container);

        if (!appformerJsIsAvailable()) {
            ScriptInjector.fromString(ReactScreen.ReactJs.INSTANCE.reactExample().getText())
                    .setWindow(ScriptInjector.TOP_WINDOW)
                    .inject();
        }
    }

    private native void setContainer(final Object container) /*-{
        $wnd.reactAppContainerElement = container;
    }-*/;

    private native boolean appformerJsIsAvailable() /*-{
        return $wnd.appformer !== undefined;
    }-*/;
}
