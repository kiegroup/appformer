package org.dashbuilder.renderer.c3.client.jsbinding.geojson;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true)
public interface Feature {

    @JsProperty
    public String getType();
    @JsProperty
    public Geometry getGeometry();
    @JsProperty
    public JsPropertyMap<Object> getProperties();
    @JsProperty
    public String getId();
}
