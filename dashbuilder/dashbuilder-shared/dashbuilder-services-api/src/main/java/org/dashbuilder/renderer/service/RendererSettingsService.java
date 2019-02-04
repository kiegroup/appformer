package org.dashbuilder.renderer.service;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 *  Provide access to Renderer settings. The settings can be configured using system properties:
 *   <ul><li><b>Offline</b>: A boolean property that can be set <i>org.dashbuilder.renderer.offline</i></li>
 *  <li><b>Default Renderer</b>: A string property that can be set using <i>org.dashbuilder.renderer.default</i></li>
 *  </ul>
 */
@Remote
public interface RendererSettingsService {
    
    final static String DEFAULT_RENDERER_PROPERTY = "org.dashbuilder.renderer.default";
    
    final static String OFFLINE_RENDERER_PROPERTY = "org.dashbuilder.renderer.offline";
    
    public RendererSettings getSettings();

}
