package org.uberfire.ext.security.management.wildfly.filesystem;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.config.ConfigProperties;
import org.wildfly.security.auth.realm.FileSystemSecurityRealm;
import org.wildfly.security.auth.server.NameRewriter;

public class RealmProvider {

    private static final Logger LOG = LoggerFactory.getLogger(RealmProvider.class);

    public static final String DEFAULT_FILE_SYSTEM_REALM_PATH = System.getProperty("jboss.server.config.dir") + "/kie-fs-realm-users";
    public static final String DEFAULT_FILE_SYSTEM_LEVELS = "2";
    public static final String DEFAULT_FILE_SYSTEM_ENCODED = "true";

    private final String folderPath;
    private final int levels;
    private final boolean encoded;

    public RealmProvider(final ConfigProperties config) {
        LOG.debug("Configuring JBoss provider from properties.");
        // Configure properties.
        this.folderPath = config.get("org.uberfire.ext.security.management.wildfly.filesystem.folder-path",
                                     DEFAULT_FILE_SYSTEM_REALM_PATH).getValue();
        this.levels = config.get("org.uberfire.ext.security.management.wildfly.filesystem.levels",
                                 DEFAULT_FILE_SYSTEM_LEVELS).getIntValue();
        this.encoded = config.get("org.uberfire.ext.security.management.wildfly.filesystem.encoded",
                                  DEFAULT_FILE_SYSTEM_ENCODED).getBooleanValue();

        LOG.debug("Configuration of JBoss provider finished.");
    }

    public FileSystemSecurityRealm getRealm() {
        return new FileSystemSecurityRealm(Paths.get(folderPath), NameRewriter.IDENTITY_REWRITER, levels, encoded);
    }
}
