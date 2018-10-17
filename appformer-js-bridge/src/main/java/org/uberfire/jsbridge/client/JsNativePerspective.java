package org.uberfire.jsbridge.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.uberfire.workbench.model.ContextDisplayMode;

public class JsNativePerspective {

    private final JavaScriptObject self;

    public JsNativePerspective(final JavaScriptObject self) {
        this.self = self;
    }

    public List<JsNativePart> parts() {

        final List<JsNativePart> parts = new ArrayList<>();

        final JsArray<JavaScriptObject> jsParts = (JsArray<JavaScriptObject>) get("af_parts");
        for (int i = 0; i < jsParts.length(); i++) {
            parts.add(new JsNativePart(jsParts.get(i)));
        }

        return parts;
    }

    public List<JsNativePanel> panels() {

        final List<JsNativePanel> panels = new ArrayList<>();

        final JsArray<JavaScriptObject> jsPanels = (JsArray<JavaScriptObject>) get("af_panels");
        for (int i = 0; i < jsPanels.length(); i++) {
            panels.add(new JsNativePanel(jsPanels.get(i)));
        }

        return panels;
    }

    public ContextDisplayMode contextDisplayMode() {
        final JSONObject displayInfo = new JSONObject(displayInfo());
        return ContextDisplayMode.valueOf(displayInfo.get("contextDisplayMode").isString().stringValue());
    }

    public String contextId() {

        final JSONValue contextId = new JSONObject(displayInfo()).get("contextId");
        if (contextId == null) {
            return null;
        }

        return contextId.isString().stringValue();
    }

    private native JavaScriptObject displayInfo()   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self["af_displayInfo"];
    }-*/;

    public native Object get(final String fieldToInvoke)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self[fieldToInvoke];
    }-*/;

    public native Object run(final String method)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self[method]();
    }-*/;
}
