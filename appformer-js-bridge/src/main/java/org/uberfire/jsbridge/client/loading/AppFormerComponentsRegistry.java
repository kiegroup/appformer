package org.uberfire.jsbridge.client.loading;

import com.google.gwt.core.client.JavaScriptObject;

public final class AppFormerComponentsRegistry {

    private AppFormerComponentsRegistry() {
        // do nothing
    }

    public static native String[] keys() /*-{
        if (typeof $wnd.AppFormerComponentsRegistry === "undefined") {
            return [];
        }
        return Object.keys($wnd.AppFormerComponentsRegistry);
    }-*/;

    public static native JavaScriptObject get(String key) /*-{
        if (typeof $wnd.AppFormerComponentsRegistry[key] === "undefined") {
            return null;
        }
        return $wnd.AppFormerComponentsRegistry[key];
    }-*/;
}
