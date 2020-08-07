package org.appformer.kogito.bridge.client.marshaller.pmml;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "window", name = "envelope")
public class PMMLEditorMarshallerService {

    @JsMethod
    public native Object getPMMLModelData(String xmlContent);

    @JsProperty(name = "pmmlEditorMarshallerService")
    public static native PMMLEditorMarshallerService get();

}
