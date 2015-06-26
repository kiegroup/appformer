package org.livespark.backend.server.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.kie.workbench.common.screens.datamodeller.backend.server.DataModelerServiceHelper;
import org.kie.workbench.common.services.shared.project.KieProject;

// FIXME This is a workaround for a performance issue with calculating the class loader.
@ApplicationScoped
@Alternative
@Priority(100)
public class DataModelerServiceHelperWorkAround extends DataModelerServiceHelper {
    
    private final Map<String, ClassLoader> classLoaderCache = new ConcurrentHashMap<String, ClassLoader>();
    
    @Override
    public ClassLoader getProjectClassLoader( KieProject project ) {
        ClassLoader classLoader = classLoaderCache.get( project.getSignatureId() );
        if ( classLoader == null ) {
            classLoader = super.getProjectClassLoader( project );
            classLoaderCache.put( project.getSignatureId(), classLoader );
        }
        
        return classLoader;
    }

}
