package org.appformer.kogito.bridge.client.resource.interop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, name = "window", namespace = JsPackage.GLOBAL)
public class Envelope {

    @JsProperty(name = "envelope")
    private static native Envelope get();

    @JsOverlay
    public static boolean isAvailable() {
        return get() != null;
    }

}
