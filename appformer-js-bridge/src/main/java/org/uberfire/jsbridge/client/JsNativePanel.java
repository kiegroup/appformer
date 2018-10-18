package org.uberfire.jsbridge.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.ContextDisplayMode;
import org.uberfire.workbench.model.Position;

public class JsNativePanel {

    private final JavaScriptObject self;

    public JsNativePanel(final JavaScriptObject self) {
        this.self = self;
    }

    public String panelType() {
        return (String) get("panelType");
    }

    public Position position() {
        return CompassPosition.valueOf((String) get("position"));
    }

    public int width() {
        final Number width = (Number) get("width");
        if (width == null) {
            return -1;
        }

        return width.intValue();
    }

    public int minWidth() {
        final Number minWidth = (Number) get("minWidth");
        if (minWidth == null) {
            return -1;
        }

        return minWidth.intValue();
    }

    public int height() {
        final Number height = (Number) get("height");
        if (height == null) {
            return -1;
        }

        return height.intValue();
    }

    public int minHeight() {
        final Number minHeight = (Number) get("minHeight");
        if (minHeight == null) {
            return -1;
        }

        return minHeight.intValue();
    }

    public List<JsNativePart> parts() {

        final List<JsNativePart> parts = new ArrayList<>();

        final JsArray<JavaScriptObject> jsParts = (JsArray<JavaScriptObject>) get("parts");
        for (int i = 0; i < jsParts.length(); i++) {
            parts.add(new JsNativePart(jsParts.get(i)));
        }

        return parts;
    }

    public List<JsNativePanel> children() {

        final List<JsNativePanel> panels = new ArrayList<>();

        final JsArray<JavaScriptObject> jsPanels = (JsArray<JavaScriptObject>) get("children");
        for (int i = 0; i < jsPanels.length(); i++) {
            panels.add(new JsNativePanel(jsPanels.get(i)));
        }

        return panels;
    }

    public ContextDisplayMode contextDisplayMode() {
        return ContextDisplayMode.valueOf(contextDisplayModeString());
    }

    public native String contextId()   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePanel::self["displayInfo"]["contextId"];
    }-*/;

    private native String contextDisplayModeString()   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePanel::self["displayInfo"]["contextDisplayMode"];
    }-*/;

    private native Object get(final String fieldToInvoke)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePanel::self[fieldToInvoke];
    }-*/;

    public native Object run(final String method)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePanel::self[method]();
    }-*/;
}
