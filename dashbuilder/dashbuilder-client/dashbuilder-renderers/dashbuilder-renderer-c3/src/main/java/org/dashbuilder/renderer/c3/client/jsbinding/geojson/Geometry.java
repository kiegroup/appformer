package org.dashbuilder.renderer.c3.client.jsbinding.geojson;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface Geometry {
    
    @JsProperty
    public GeometryType getType();
    @JsProperty
    public Double[] getCoordinates();

}
