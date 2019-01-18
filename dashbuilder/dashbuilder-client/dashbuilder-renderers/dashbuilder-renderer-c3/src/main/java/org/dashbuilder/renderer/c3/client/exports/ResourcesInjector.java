package org.dashbuilder.renderer.c3.client.exports;

import org.uberfire.client.views.pfly.sys.PatternFlyBootstrapper;

import com.google.gwt.core.client.ScriptInjector;

public class ResourcesInjector {

    static boolean c3Injected;
    static boolean d3geoprojectionInjected;


    public static void ensureC3Injected() {
        if (!c3Injected) {
            injectC3Resources();
            c3Injected = true;
        }
    }
    
    public static void ensureD3GeoProjectionInjected() {
        if (!d3geoprojectionInjected) {
            injectD3GeoProjectionResources();
            d3geoprojectionInjected = true;
        }
    }

    private static void injectC3Resources() {
        PatternFlyBootstrapper.ensureD3IsAvailable();
        ScriptInjector.fromString(NativeLibraryResources.INSTANCE.c3js().getText())
                        .setWindow(ScriptInjector.TOP_WINDOW)
                        .inject();
    }
    
    private static void injectD3GeoProjectionResources() {
        PatternFlyBootstrapper.ensureD3IsAvailable();
        ScriptInjector.fromString(NativeLibraryResources.INSTANCE.d3geoprojectionjs().getText())
                        .setWindow(ScriptInjector.TOP_WINDOW)
                        .inject();
    }

}
