package org.dashbuilder.renderer.c3.client.jsbinding.d3;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface D3Projection  {
    
    public D3Projection geoNaturalEarth2();
    
    public D3Projection geoCylindricalEqualArea();
    
    public D3Projection geoMercator();
    
    public D3Projection scale(double s);
    
    public D3Projection translate(double xy[]);
    
    class Builder {

        @JsProperty(name = "d3", namespace = JsPackage.GLOBAL)
        public static native D3Projection get();
    }

}
