package org.uberfire.jsbridge.client;

import com.google.gwt.core.client.JavaScriptObject;

public class JsNativePerspective {

    private final JavaScriptObject self;

    public JsNativePerspective(final JavaScriptObject self) {
        this.self = self;
    }

    public native Object get(final String fieldToInvoke)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self[fieldToInvoke];
    }-*/;

    public native Object run(final String method)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self[method]();
    }-*/;
}
