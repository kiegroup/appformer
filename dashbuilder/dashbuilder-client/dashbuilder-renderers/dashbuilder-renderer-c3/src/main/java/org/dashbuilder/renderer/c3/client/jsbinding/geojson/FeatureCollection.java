package org.dashbuilder.renderer.c3.client.jsbinding.geojson;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface FeatureCollection {
    @JsProperty
    public String getType();
    @JsProperty
    public Feature[] getFeatures();
    
}