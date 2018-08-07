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

package org.uberfire.client.screens.react;

import com.google.gwt.core.client.JavaScriptObject;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;

public class JsNativeScreen {

    private final JavaScriptObject self;
    private final HTMLElement container;

    public JsNativeScreen(final JavaScriptObject obj) {
        this.self = obj;
        this.container = (HTMLElement) DomGlobal.document.createElement("div");
        this.container.classList.add("js-screen-container");
    }

    public HTMLElement getElement() {
        //This is just a placeholder. This empty div will passed to the JS component so it knows where to render at.
        return container;
    }

    public native void render() /*-{
        $wnd.appformer.render(
                this.@org.uberfire.client.screens.react.JsNativeScreen::self.af_componentRoot(),
                this.@org.uberfire.client.screens.react.JsNativeScreen::container);
    }-*/;

    public native String get(final String property)  /*-{
        return this.@org.uberfire.client.screens.react.JsNativeScreen::self[property];
    }-*/;

    public native Object run(final String functionName) /*-{
        return this.@org.uberfire.client.screens.react.JsNativeScreen::self[functionName] && this.@org.uberfire.client.screens.react.JsNativeScreen::self[functionName]();
    }-*/;

    public native Object run(final String functionName, final Object arg1) /*-{
        return this.@org.uberfire.client.screens.react.JsNativeScreen::self[functionName] && this.@org.uberfire.client.screens.react.JsNativeScreen::self[functionName](arg1);
    }-*/;

    public native Object run(final String functionName, final Object arg1, final Object arg2) /*-{
        return this.@org.uberfire.client.screens.react.JsNativeScreen::self[functionName] && this.@org.uberfire.client.screens.react.JsNativeScreen::self[functionName](arg1, arg2);
    }-*/;

    public native boolean defines(final String property) /*-{
        return this.@org.uberfire.client.screens.react.JsNativeScreen::self[property] !== undefined;
    }-*/;
}
