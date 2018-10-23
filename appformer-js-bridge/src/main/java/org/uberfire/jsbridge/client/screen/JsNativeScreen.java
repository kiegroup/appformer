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

package org.uberfire.jsbridge.client.screen;

import java.util.function.Consumer;

import com.google.gwt.core.client.JavaScriptObject;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.uberfire.jsbridge.client.JsPlaceRequest;

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
        if (this.scriptLoaded()) {
            renderNative();
        } else {
            lazyLoadParentScript.accept(componentId);
        }
    }

    public boolean scriptLoaded() {
        return this.self != null;
    }

    public native void renderNative() /*-{
        $wnd.AppFormer.render(
                this.@org.uberfire.jsbridge.client.screen.JsNativeScreen::self.af_componentRoot(),
                this.@org.uberfire.jsbridge.client.screen.JsNativeScreen::container);
    }-*/;

    // ===== Properties

    public String componentId() {
        return (String) get("af_componentId");
    }

    public String componentTitle() {
        final String title = (String) get("af_componentTitle");
        return title != null ? title : getComponentId();
    }

    public String componentContextId() {
        return (String) get("af_componentContextId");
    }

    public JsObject subscriptions() {
        return (JsObject) get("af_subscriptions");
    }

    // ===== Lifecycle

    public void onStartup(final JsPlaceRequest placeRequest) {
        run("af_onStartup", placeRequest);
    }

    public void onOpen() {
        run("af_onOpen");
    }

    public void onClose() {
        run("af_onClose");
    }

    public boolean onMayClose() {
        return !defines("af_onMayClose") || (boolean) run("af_onMayClose");
    }

    public void onShutdown() {
        run("af_onShutdown");
    }

    public void onFocus() {
        run("af_onFocus");
    }

    public void onLostFocus() {
        run("af_onLostFocus");
    }

    private Object get(final String property) {
        if (!this.scriptLoaded()) {
            return null;
        }
        return getNative(property);
    }

    private native Object getNative(final String property)  /*-{
        return this.@org.uberfire.jsbridge.client.screen.JsNativeScreen::self[property];
    }-*/;

    private Object run(final String functionName) {
        if (!this.scriptLoaded()) {
            return null;
        }
        return runNative(functionName);
    }

    private native Object runNative(final String functionName) /*-{
        return this.@org.uberfire.jsbridge.client.screen.JsNativeScreen::self[functionName] && this.@org.uberfire.jsbridge.client.screen.JsNativeScreen::self[functionName]();
    }-*/;

    private Object run(final String functionName, final Object arg1) {
        if (!this.scriptLoaded()) {
            return null;
        }
        return runNative(functionName, arg1);
    }

    private native Object runNative(final String functionName, final Object arg1) /*-{
        return this.@org.uberfire.jsbridge.client.screen.JsNativeScreen::self[functionName] && this.@org.uberfire.jsbridge.client.screen.JsNativeScreen::self[functionName](arg1);
    }-*/;

    public boolean defines(final String property) {
        if (!this.scriptLoaded()) {
            return false;
        }
        return definesNative(property);
    }

    private native boolean definesNative(final String property) /*-{
        return this.@org.uberfire.jsbridge.client.screen.JsNativeScreen::self[property] !== undefined;
    }-*/;

    public String getComponentId() {
        return componentId;
    }
}
