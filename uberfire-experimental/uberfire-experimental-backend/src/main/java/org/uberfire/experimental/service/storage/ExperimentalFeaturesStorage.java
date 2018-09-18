package org.uberfire.experimental.service.storage;

import java.util.Collection;

import org.uberfire.experimental.service.registry.impl.ExperimentalFeatureImpl;

public interface ExperimentalFeaturesStorage {

    Collection<ExperimentalFeatureImpl> getFeatures();

    void storeFeatures(Collection<ExperimentalFeatureImpl> features);

    void store(ExperimentalFeatureImpl experimentalFeature);
}
