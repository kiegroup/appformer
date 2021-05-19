package org.dashbuilder.client.services.dummy;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.uberfire.experimental.client.service.ClientExperimentalFeaturesRegistryService;
import org.uberfire.experimental.service.registry.ExperimentalFeature;
import org.uberfire.experimental.service.registry.ExperimentalFeaturesRegistry;

@Alternative
@ApplicationScoped
public class RuntimeClientExperimentalFeaturesRegistryService implements ClientExperimentalFeaturesRegistryService {

    ExperimentalFeaturesRegistry registry = new ExperimentalFeaturesRegistry() {

        @Override
        public boolean isFeatureEnabled(String featureId) {
            return true;
        }

        @Override
        public Optional<ExperimentalFeature> getFeature(String featureId) {
            return Optional.empty();
        }

        @Override
        public Collection<ExperimentalFeature> getAllFeatures() {
            return Collections.emptyList();
        }
    };

    @Override
    public ExperimentalFeaturesRegistry getFeaturesRegistry() {
        return registry;
    }

    @Override
    public boolean isFeatureEnabled(String featureId) {
        return true;
    }

    @Override
    public Boolean isExperimentalEnabled() {
        return false;
    }

    @Override
    public void loadRegistry() {
        // empty
    }

    @Override
    public void updateExperimentalFeature(String featureId, boolean enabled) {
        // empty
    }

}
