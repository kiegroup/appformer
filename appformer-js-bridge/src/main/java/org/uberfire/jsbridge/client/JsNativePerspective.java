package org.uberfire.jsbridge.client;

import com.google.gwt.core.client.JavaScriptObject;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

public class JsNativePerspective {

    private final JavaScriptObject self;
    private final JsNativeContextDisplay contextDisplay;
    private final JsNativeView view;

    public JsNativePerspective(final JavaScriptObject self) {
        this.self = self;
        this.contextDisplay = new JsNativeContextDisplay(self, "af_displayInfo");
        this.view = new JsNativeView(self, "af_parts", "af_panels");
    }

    public String componentId() {
        return (String) get("af_componentId");
    }

    public boolean isDefault() {
        return (boolean) get("af_isDefault");
    }

    public boolean isTransient() {
        return (boolean) get("af_isTransient");
    }

    public Menus menus() {
        return (Menus) get("af_menus");
    }

    public ToolBar toolbar() {
        return (ToolBar) get("af_toolbar");
    }

    public String defaultPanelType() {
        return (String) get("af_defaultPanelType");
    }

    public JsNativeView view() {
        return this.view;
    }

    public JsNativeContextDisplay contextDisplay() {
        return this.contextDisplay;
    }

    public void onStartup() {
        run("af_onStartup");
    }

    public void onOpen() {
        //this.render(); TODO ??
        run("af_onOpen");
    }

    public void onClose() {
        run("af_onClose");
    }

    public void onShutdown() {
        run("af_onShutdown");
    }

    private native Object get(final String fieldToInvoke)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self[fieldToInvoke];
    }-*/;

    private native Object run(final String method)   /*-{
        return this.@org.uberfire.jsbridge.client.JsNativePerspective::self[method]();
    }-*/;
}
