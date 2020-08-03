package org.appformer.kogito.bridge.client.marshaller.pmml;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "window", name = "envelope")
public class PMMLMarshaller {

    @JsMethod
    public native Object marshall(String xmlContent);

    @JsMethod
    public native String unmarshall(Object pmml);

    @JsProperty(name = "pmmlMarshallerService")
    public static native PMMLMarshaller get();

}
