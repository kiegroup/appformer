package org.dashbuilder.renderer.c3.client.jsbinding;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class C3Point {
    
    
    @JsOverlay
    public static C3Point create(RadiusCallback r) {
        C3Point instance = new C3Point();
        instance.setR(r);
        return instance;
    }

    @JsProperty
    public native void setR(RadiusCallback callback);

    @JsFunction
    @FunctionalInterface
    public interface RadiusCallback {
        
        double callback(C3D value);
    
    }
    
    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public static class C3D {
        
        @JsProperty
        public native String getId();
        
        @JsProperty
        public native int getIndex();
    }
        
}
