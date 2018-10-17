package org.uberfire.jsbridge.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.uberfire.commons.data.Pair;
import org.uberfire.workbench.model.ContextDisplayMode;

import static java.util.stream.Collectors.toMap;

public class JsNativePart {

    private final JavaScriptObject self;

    public JsNativePart(final JavaScriptObject self) {
        this.self = self;
    }

    public String placeName() {
        return (String) get("placeName");
    }

    public ContextDisplayMode contextDisplayMode() {
        final JSONObject displayInfo = new JSONObject(displayInfo());
        return ContextDisplayMode.valueOf(displayInfo.get("contextDisplayMode").isString().stringValue());
    }

    public String contextId() {

        final JSONObject displayInfo = new JSONObject(displayInfo());

        final JSONValue contextId = displayInfo.get("contextId");
        if (contextId == null) {
            return null;
        }

        return contextId.isString().stringValue();
    }

    public Map<String, String> parameters() {

        final JavaScriptObject jsParameters = (JavaScriptObject) get("parameters");
        if (jsParameters == null) {
            return new HashMap<>();
        }

        final JSONObject parametersJson = new JSONObject(jsParameters);
        return parametersJson.keySet().stream()
                .map(key -> new Pair<>(key, parametersJson.get(key).isString().stringValue()))
                .collect(toMap(Pair::getK1, Pair::getK2));
    }

    private native JavaScriptObject displayInfo()   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePart::self["displayInfo"];
    }-*/;

    private native Object get(final String fieldToInvoke)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePart::self[fieldToInvoke];
    }-*/;

    public native Object run(final String method)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePart::self[method]();
    }-*/;
}
