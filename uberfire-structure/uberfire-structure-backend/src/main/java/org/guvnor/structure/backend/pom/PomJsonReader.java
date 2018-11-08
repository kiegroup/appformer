package org.guvnor.structure.backend.pom;

/**
 * Behaviour to read configuration from a jsonfile
 */
public interface PomJsonReader {

    ConfigurationMap readConfiguration();
}
