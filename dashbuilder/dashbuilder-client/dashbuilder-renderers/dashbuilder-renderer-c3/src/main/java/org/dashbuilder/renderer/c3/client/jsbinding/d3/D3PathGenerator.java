package org.dashbuilder.renderer.c3.client.jsbinding.d3;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface D3PathGenerator {
    
    D3PathGenerator projection(D3Projection projection);
    
    D3PathGenerator geoPath();
    
    double[] centroid(Object path);
    
    class Builder {

        @JsProperty(name = "d3", namespace = JsPackage.GLOBAL)
        public static native D3PathGenerator get();
    }
    
}
