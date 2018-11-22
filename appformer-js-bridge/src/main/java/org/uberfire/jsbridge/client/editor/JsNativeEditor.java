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

package org.uberfire.jsbridge.client.editor;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

public class JsNativeEditor {

    private final Object self;
    private final String componentId;
    private final HTMLElement container;

    public JsNativeEditor(String componentId, final Object self) {
        this.componentId = componentId;
        this.self = self;
        this.container = (HTMLElement) DomGlobal.document.createElement("div");
    }

    public String getComponentId() {
        return componentId;
    }

    public native int getPriority() /*-{
        return this.@org.uberfire.jsbridge.client.editor.JsNativeEditor::self["af_priority"];
    }-*/;

    public native String getTitle() /*-{
        return this.@org.uberfire.jsbridge.client.editor.JsNativeEditor::self["af_componentTitle"];
    }-*/;

    public native void renderNative() /*-{
        $wnd.AppFormer.render(
                this.@org.uberfire.jsbridge.client.editor.JsNativeEditor::self["af_componentRoot"](),
                this.@org.uberfire.jsbridge.client.editor.JsNativeEditor::container);
    }-*/;

    public native void unmount() /*-{
        if (this.@org.uberfire.jsbridge.client.editor.JsNativeEditor::self["af_isReact"]) {
            $wnd.ReactDOM.unmountComponentAtNode(this.@org.uberfire.jsbridge.client.editor.JsNativeEditor::container);
        }
    }-*/;

    public HTMLElement getElement() {
        return container;
    }

    public native String[] getResourceTypes() /*-{
        return this.@org.uberfire.jsbridge.client.editor.JsNativeEditor::self["af_resourceTypes"];
    }-*/;
}
