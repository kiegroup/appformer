package org.uberfire.jsbridge.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import org.uberfire.workbench.model.ContextDisplayMode;

public class JsNativePerspective {

    private final JavaScriptObject self;

    public JsNativePerspective(final JavaScriptObject self) {
        this.self = self;
    }

    public List<JsNativePart> parts() {

        final List<JsNativePart> parts = new ArrayList<>();

        final JsArray<JavaScriptObject> jsParts = (JsArray<JavaScriptObject>) get("parts");
        for (int i = 0; i < jsParts.length(); i++) {
            parts.add(new JsNativePart(jsParts.get(i)));
        }

        return parts;
    }

    public List<JsNativePanel> panels() {

        final List<JsNativePanel> panels = new ArrayList<>();

        final JsArray<JavaScriptObject> jsPanels = (JsArray<JavaScriptObject>) get("panels");
        for (int i = 0; i < jsPanels.length(); i++) {
            panels.add(new JsNativePanel(jsPanels.get(i)));
        }

        return panels;
    }

    public ContextDisplayMode contextDisplayMode() {
        return ContextDisplayMode.valueOf(contextDisplayModeString());
    }

    public native String contextId()   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self["displayInfo"]["contextId"];
    }-*/;

    private native String contextDisplayModeString()   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self["displayInfo"]["contextDisplayMode"];
    }-*/;

    public native Object get(final String fieldToInvoke)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self[fieldToInvoke];
    }-*/;

    public native Object run(final String method)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self[method]();
    }-*/;
}
