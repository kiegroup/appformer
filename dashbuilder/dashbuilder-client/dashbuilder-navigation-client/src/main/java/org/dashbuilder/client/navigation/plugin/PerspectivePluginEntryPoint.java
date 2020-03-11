package org.dashbuilder.client.navigation.plugin;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;

@EntryPoint
@ApplicationScoped
public class PerspectivePluginEntryPoint {
    
    @Inject
    PerspectivePluginManager perspectivePluginManager;
    
    @PostConstruct
    private void init() {
        perspectivePluginManager.loadPlugins();
    }

}