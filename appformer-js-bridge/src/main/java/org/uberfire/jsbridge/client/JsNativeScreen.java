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

package org.uberfire.jsbridge.client;

import com.google.gwt.core.client.JavaScriptObject;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

import java.util.function.Consumer;

public class JsNativeScreen {

    private final HTMLElement container;
    private JavaScriptObject self;
    private String componentId;
    private Consumer<String> lazyLoadParentScript;

    public static JsNativeScreen build(String identifier, JavaScriptObject jsObject, Consumer<String> lazyLoadParentScript) {
        if (jsObject == null) {
            return new JsNativeScreen(identifier, lazyLoadParentScript);
        } else {
            return new JsNativeScreen(jsObject);
        }
    }

    private JsNativeScreen(final JavaScriptObject obj) {
        this.self = obj;
        this.container = (HTMLElement) DomGlobal.document.createElement("div");
        final Element span = DomGlobal.document.createElement("span");
        this.container.classList.add("js-screen-container");
    }

    private JsNativeScreen(final String componentId, Consumer<String> lazyLoadParentScript) {
        this.componentId = componentId;
        this.lazyLoadParentScript = lazyLoadParentScript;
        this.container = (HTMLElement) DomGlobal.document.createElement("div");
        this.container.classList.add("js-screen-container");
    }

    public void updateRealContent(JavaScriptObject jsObject) {
        this.self = jsObject;
        render();
    }

    public HTMLElement getElement() {
        //This is just a placeholder. This empty div will passed to the JS component so it knows where to render at.
        return container;
    }

    public void render() {
        if (this.self != null) {
            renderNative();
        } else {
            lazyLoadParentScript.accept(componentId);
        }
    }

    public native void renderNative() /*-{
        $wnd.AppFormer.render(
            this.@org.uberfire.jsbridge.client.JsNativeScreen::self.af_componentRoot(),
            this.@org.uberfire.jsbridge.client.JsNativeScreen::container);
    }-*/;


    public Object get(final String property) {
        if (self == null) {
            return componentId;
        }
        return getNative(property);
    }

    public native Object getNative(final String property)  /*-{
        return this.@org.uberfire.jsbridge.client.JsNativeScreen::self[property];
    }-*/;

    public Object run(final String functionName) {
        if (self == null) {
            return null;
        }
        return runNative(functionName);
    }

    public native Object runNative(final String functionName) /*-{
        return this.@org.uberfire.jsbridge.client.JsNativeScreen::self[functionName] && this.@org.uberfire.jsbridge.client.JsNativeScreen::self[functionName]();
    }-*/;

    public Object run(final String functionName, final Object arg1) {
        if (self == null) {
            return null;
        }
        return runNative(functionName, arg1);
    }

    public native Object runNative(final String functionName, final Object arg1) /*-{
        return this.@org.uberfire.jsbridge.client.JsNativeScreen::self[functionName] && this.@org.uberfire.jsbridge.client.JsNativeScreen::self[functionName](arg1);
    }-*/;

    public Object run(final String functionName, final Object arg1, final Object arg2) {
        if (self == null) {
            return null;
        }
        return runNative(functionName, arg1, arg2);
    }

    public native Object runNative(final String functionName, final Object arg1, final Object arg2) /*-{
        return this.@org.uberfire.jsbridge.client.JsNativeScreen::self[functionName] && this.@org.uberfire.jsbridge.client.JsNativeScreen::self[functionName](arg1, arg2);
    }-*/;

    public boolean defines(final String property) {
        if (self == null) {
            return false;
        }
        return definesNative(property);
    }


    public native boolean definesNative(final String property) /*-{
        return this.@org.uberfire.jsbridge.client.JsNativeScreen::self[property] !== undefined;
    }-*/;

}
