package org.uberfire.jsbridge.client;

import com.google.gwt.core.client.JavaScriptObject;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;

public class JsNativePanel {

    private final JavaScriptObject self;
    private final JsNativeContextDisplay contextDisplay;
    private final JsNativeView view;

    public JsNativePanel(final JavaScriptObject self) {
        this.self = self;
        this.contextDisplay = new JsNativeContextDisplay(self, "displayInfo");
        this.view = new JsNativeView(self, "parts", "children");
    }

    public String panelType() {
        return (String) get("panelType");
    }

    public Position position() {
        return CompassPosition.valueOf((String) get("position"));
    }

    public int width() {
        return ((Number) get("width")).intValue();
    }

    public int minWidth() {
        return ((Number) get("minWidth")).intValue();
    }

    public int height() {
        return ((Number) get("height")).intValue();
    }

    public int minHeight() {
        return ((Number) get("minHeight")).intValue();
    }

    public JsNativeView view() {
        return this.view;
    }

    public JsNativeContextDisplay contextDisplay() {
        return contextDisplay;
    }

    private native Object get(final String fieldToInvoke)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePanel::self[fieldToInvoke];
    }-*/;
}
