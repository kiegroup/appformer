package org.guvnor.m2repo.backend.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.stream.Collectors;

import org.guvnor.common.services.project.model.GAV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.guvnor.m2repo.backend.server.M2ServletContextListener.ARTIFACT_ID;
import static org.guvnor.m2repo.backend.server.M2ServletContextListener.GROUP_ID;
import static org.guvnor.m2repo.backend.server.M2ServletContextListener.VERSION;

public class FixNotMavenizedArtifactInfo {

    private static final Logger logger = LoggerFactory.getLogger(FixNotMavenizedArtifactInfo.class);

    private final Properties notMavenizedArtifacts = new Properties();
    private final String pomTemplate;

    public FixNotMavenizedArtifactInfo() {
        String pomTemplate = null;
        try {
            pomTemplate = new BufferedReader(new InputStreamReader(M2ServletContextListener.class.getResourceAsStream("template.pom")))
                    .lines().collect(Collectors.joining("\n"));
            final InputStream isNotMavenizedArtifacts = M2ServletContextListener.class.getResourceAsStream("/not-mavenized-artifacts.txt");
            if (isNotMavenizedArtifacts != null) {
                notMavenizedArtifacts.load(isNotMavenizedArtifacts);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        this.pomTemplate = pomTemplate;
    }

    public Properties getProperties(final String filePath) {
        final Properties result = new Properties();
        final String fullFileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        final String fileNamePart = fullFileName.substring(0, fullFileName.indexOf("-"));
        final String fileVersion = fullFileName.substring(fullFileName.indexOf("-") + 1).replace(".jar", "");

        final String value = notMavenizedArtifacts.getProperty(fileNamePart);
        if (value != null) {
            final String[] GAV = value.split(":");
            result.put(GROUP_ID, GAV[0]);
            result.put(ARTIFACT_ID, GAV[1]);
            result.put(VERSION, fileVersion);
        }
        return result;
    }

    public String buildPom(GAV gav) {
        return pomTemplate.replace("{groupId}", gav.getGroupId())
                .replace("{artifactId}", gav.getArtifactId())
                .replace("{version}", gav.getVersion());
    }
}
