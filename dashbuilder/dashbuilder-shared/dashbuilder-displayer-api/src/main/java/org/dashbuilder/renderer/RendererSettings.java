package org.dashbuilder.renderer;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 *  Renderer Settings:
 *  <ul><li><b>Offline</b>: Only renderers that can work without an internet connection will be available</li>
 *  <li><b>Default Renderer</b>: Set the UUID of the renderer that should be used by default</li>
 *  </ul>
 */
@Portable
public class RendererSettings {

    private String defaultRenderer;
    private boolean offline;

    public void setDefaultRenderer(String defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
        
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public String getDefaultRenderer() {
        return defaultRenderer;
    }

    public boolean isOffline() {
        return offline;
    }
    
}