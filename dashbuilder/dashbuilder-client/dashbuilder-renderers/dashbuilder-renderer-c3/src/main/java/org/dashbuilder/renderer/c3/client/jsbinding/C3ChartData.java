package org.dashbuilder.renderer.c3.client.jsbinding;

import elemental2.core.JsObject;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class C3ChartData {
    
    @JsOverlay
    public static C3ChartData create(String[][] columns, String type, String[][] groups, JsObject xs) {
        C3ChartData data = new C3ChartData();
        data.setColumns(columns);
        data.setType(type);
        data.setGroups(groups);
        data.setXs(xs);
        return data;
    }
    
    @JsProperty
    public native void setColumns(String columns[][]);

    @JsProperty
    public native void setType(String type);
    
    @JsProperty
    public native void setGroups(String groups[][]); 
    
    @JsProperty
    public native void setXs(JsObject xs);
    
    @JsProperty
    public native void setOrder(String order);
    

}