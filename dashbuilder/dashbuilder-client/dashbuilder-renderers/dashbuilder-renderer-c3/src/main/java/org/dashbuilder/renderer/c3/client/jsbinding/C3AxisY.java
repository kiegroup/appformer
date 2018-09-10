package org.dashbuilder.renderer.c3.client.jsbinding;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

// TODO: create common class for Axis and extend for Y and X
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class C3AxisY {
    
    @JsOverlay
    public static C3AxisY create(boolean show, C3Tick tick) {
        C3AxisY instance = new C3AxisY();
        instance.setShow(show);
        instance.setTick(tick);
        return instance;
    }

    @JsProperty
    public native void setShow(boolean show);
    
    @JsProperty
    public native void setTick(C3Tick tick);
    
        
}
