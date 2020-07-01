package org.dashbuilder.client.editor.external;

import java.util.Collections;
import java.util.Map;

import org.uberfire.ext.layout.editor.client.api.HasDragAndDropSettings;
import org.uberfire.ext.layout.editor.client.api.LayoutDragComponent;

import static org.dashbuilder.external.model.ExternalComponent.COMPONENT_ID_KEY;

public interface ExternalComponentDragDef extends LayoutDragComponent, HasDragAndDropSettings {

    @Override
    default String getDragComponentTitle() {
        final String componentName = getComponentName();
        return componentName == null ? "Unknow" : componentName;
    }

    @Override
    default String getDragComponentIconClass() {
        final String componentIcon = getComponentIcon();
        return componentIcon != null ? componentIcon : LayoutDragComponent.super.getDragComponentIconClass();
    }

    @Override
    public default String[] getSettingsKeys() {
        return new String[]{COMPONENT_ID_KEY};
    }

    @Override
    public default String getSettingValue(String key) {
        if (COMPONENT_ID_KEY.equals(key)) {
            return getComponentId();
        }
        return null;
    }

    @Override
    public default void setSettingValue(String key, String value) {
        if (COMPONENT_ID_KEY.equals(key)) {
            setComponentId(value);
        }
    }

    @Override
    public default Map<String, String> getMapSettings() {
        return Collections.singletonMap(COMPONENT_ID_KEY, getComponentId());
    }

    String getComponentName();

    String getComponentIcon();

    String getComponentId();

    void setComponentId(String componentId);

    void setDragInfo(String componentName, String componentIcon);

}