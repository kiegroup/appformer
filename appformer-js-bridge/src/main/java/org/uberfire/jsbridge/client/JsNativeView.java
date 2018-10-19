package org.uberfire.jsbridge.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsNativeView {

    private final JavaScriptObject self;
    private final String partsFieldName;
    private final String panelsFieldName;

    public JsNativeView(final JavaScriptObject self, final String partsFieldName, final String panelsFieldName) {
        this.self = self;
        this.partsFieldName = partsFieldName;
        this.panelsFieldName = panelsFieldName;
    }

    public List<JsNativePart> parts() {

        final List<JsNativePart> parts = new ArrayList<>();

        final JsArray<JavaScriptObject> jsParts = nativeParts(partsFieldName);
        for (int i = 0; i < jsParts.length(); i++) {
            parts.add(new JsNativePart(jsParts.get(i)));
        }

        return parts;
    }

    public List<JsNativePanel> panels() {

        final List<JsNativePanel> panels = new ArrayList<>();

        final JsArray<JavaScriptObject> jsPanels = nativePanels(panelsFieldName);
        for (int i = 0; i < jsPanels.length(); i++) {
            panels.add(new JsNativePanel(jsPanels.get(i)));
        }

        return panels;
    }

    private native JsArray<JavaScriptObject> nativeParts(final String partsField) /*-{
        return this.@org.uberfire.jsbridge.client.JsNativeView::self[partsField];
    }-*/;

    private native JsArray<JavaScriptObject> nativePanels(final String panelsField) /*-{
        return this.@org.uberfire.jsbridge.client.JsNativeView::self[panelsField];
    }-*/;
}
