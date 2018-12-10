package org.dashbuilder.renderer.service;

import java.util.Optional;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 *  Provide access to Renderer settings
 */
@Remote
public interface RendererSettings {
    
    /**
     * Access the user select default Renderer configured using the system property org.dashbuilder.renderer.default
     * 
     * @return the UUID of user's selected renderer or an empty String
     */
    public String userDefaultRenderer();

}
