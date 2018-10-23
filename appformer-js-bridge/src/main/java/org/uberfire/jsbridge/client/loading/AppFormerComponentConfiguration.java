package org.uberfire.jsbridge.client.loading;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public class AppFormerComponentConfiguration {

    private final String id;
    private final JavaScriptObject self;

    public AppFormerComponentConfiguration(final String id, final JavaScriptObject self) {

        checkNotNull(id);
        checkNotNull(self);

        this.id = id;
        this.self = self;
    }

    public String getId() {
        return this.id;
    }

    public Type getType() {
        return Type.valueOf((String) get("type"));
    }

    public String getSource() {
        return (String) get("source");
    }

    public Map<String, String> getParams() {

        final JavaScriptObject jsParams = (JavaScriptObject) get("params");
        if (jsParams == null) {
            return new HashMap<>();
        }

        final JSONObject json = new JSONObject(jsParams);

        return json.keySet().stream()
                .filter(k -> json.get(k) != null)
                .map(k -> new SimpleImmutableEntry<>(k, json.get(k).isString().stringValue()))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private native Object get(final String key) /*-{
        return this.@org.uberfire.jsbridge.client.loading.AppFormerComponentConfiguration::self[key];
    }-*/;

    enum Type {
        PERSPECTIVE,
        SCREEN
    }

    public static class PerspectiveComponentParams {

        private final Map<String, String> params;

        public PerspectiveComponentParams(final Map<String, String> params) {
            this.params = params;
        }

        public Optional<Boolean> isDefault() {
            return ofNullable(this.params.get("is_default")).map(Boolean::valueOf);
        }
    }
}
