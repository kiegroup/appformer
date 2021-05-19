package org.dashbuilder.client.services.dummy;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.dashbuilder.renderer.RendererSettings;
import org.dashbuilder.renderer.c3.client.C3Renderer;
import org.dashbuilder.renderer.service.RendererSettingsService;
import org.jboss.errai.bus.server.annotations.ShadowService;


/**
 * Renderer settings for Runtime
 *
 */
@Alternative
@ShadowService
@ApplicationScoped
public class RuntimeRendererSettingsService implements RendererSettingsService {

    @Override
    public RendererSettings getSettings() {
        return new RendererSettings(C3Renderer.UUID, false);
    }

}