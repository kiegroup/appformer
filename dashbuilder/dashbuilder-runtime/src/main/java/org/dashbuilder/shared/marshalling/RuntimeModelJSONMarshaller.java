package org.dashbuilder.shared.marshalling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.json.Json;
import org.dashbuilder.json.JsonArray;
import org.dashbuilder.json.JsonObject;
import org.dashbuilder.navigation.json.NavTreeJSONMarshaller;
import org.dashbuilder.shared.model.RuntimeModel;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;

@ApplicationScoped
public class RuntimeModelJSONMarshaller {

    private static String LAST_MODIFIED = "lastModified";
    private static String NAV_TREE = "navTree";
    private static String LAYOUT_TEMPLATES = "layoutTemplates";

    private static RuntimeModelJSONMarshaller instance;

    static {
        instance = new RuntimeModelJSONMarshaller();
    }

    public static RuntimeModelJSONMarshaller get() {
        return instance;
    }

    public JsonObject toJson(RuntimeModel model) {
        JsonObject jsonObject = Json.createObject();
        JsonObject navTreeJson = NavTreeJSONMarshaller.get().toJson(model.getNavTree());
        JsonArray ltArray = Json.createArray();

        jsonObject.set(LAST_MODIFIED, Json.create(model.getLastModified()));
        jsonObject.set(NAV_TREE, navTreeJson);

        AtomicInteger i = new AtomicInteger();
        model.getLayoutTemplates().forEach(lt -> {
            ltArray.set(i.getAndIncrement(), LayoutTemplateJSONMarshaller.get().toJson(lt));
        });
        jsonObject.set(LAYOUT_TEMPLATES, ltArray);
        return jsonObject;
    }
    
    public RuntimeModel fromJson(String json) {
        return fromJson(Json.parse(json));
    }

    public RuntimeModel fromJson(JsonObject jsonObject) {
        JsonObject navTreeJSONObject = jsonObject.getObject(NAV_TREE);
        JsonArray ltArray = jsonObject.getArray(LAYOUT_TEMPLATES);
        Number lastModified = jsonObject.getNumber(LAST_MODIFIED);
        List<LayoutTemplate> layoutTemplates = new ArrayList<>();
        for (int i = 0; i < ltArray.length(); i++) {
            JsonObject ltJson = ltArray.getObject(i);
            layoutTemplates.add(LayoutTemplateJSONMarshaller.get().fromJson(ltJson));
        }

        return new RuntimeModel(NavTreeJSONMarshaller.get().fromJson(navTreeJSONObject),
                                layoutTemplates,
                                lastModified.longValue());

    }

}
