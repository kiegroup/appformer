package org.uberfire.jsbridge.client;

import com.google.gwt.core.client.JavaScriptObject;
import org.uberfire.workbench.model.ContextDisplayMode;

public class JsNativeContextDisplay {

    private final JavaScriptObject self;
    private final String displayInfoFieldName;

    public JsNativeContextDisplay(final JavaScriptObject self,
                                  final String displayInfoFieldName) {
        this.self = self;
        this.displayInfoFieldName = displayInfoFieldName;
    }

    public ContextDisplayMode mode() {
        return ContextDisplayMode.valueOf(contextDisplayModeString(displayInfoFieldName));
    }

    public String contextId() {
        return contextId(displayInfoFieldName);
    }

    private native String contextId(final String displayInfoField)   /*-{
        var contextDisplay = this.@org.uberfire.jsbridge.client.JsNativeContextDisplay::self[displayInfoField];
        return contextDisplay && contextDisplay["contextId"];
    }-*/;

    private native String contextDisplayModeString(final String displayInfoField)   /*-{
        var contextDisplay = this.@org.uberfire.jsbridge.client.JsNativeContextDisplay::self[displayInfoField];
        return contextDisplay && contextDisplay["contextDisplayMode"];
    }-*/;
}
