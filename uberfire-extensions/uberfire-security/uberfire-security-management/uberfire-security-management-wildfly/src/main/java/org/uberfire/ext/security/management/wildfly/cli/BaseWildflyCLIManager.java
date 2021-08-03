/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.ext.security.management.wildfly.cli;

import java.net.InetAddress;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.jboss.as.controller.client.ModelControllerClient;
import org.uberfire.commons.config.ConfigProperties;
import org.uberfire.ext.security.management.wildfly.filesystem.RealmProvider;

/**
 * <p>Base class for JBoss Wildfly security management that uses the administration Java API for managing the command line interface.</p>
 * <p>Based on JBoss Wildfly administration API & Util classes.</p>
 *
 * @since 0.8.0
 */
public abstract class BaseWildflyCLIManager {

    protected static final String DEFAULT_HOST = "localhost";
    protected static final int DEFAULT_PORT = 9990;
    protected static final String DEFAULT_ADMIN_USER = null;
    protected static final String DEFAULT_ADMIN_PASSWORD = null;
    protected String host;
    protected int port;
    protected String adminUser;
    protected String adminPassword;
    protected String folderPath;
    protected String levels;
    protected String encoded;

    protected void loadConfig(final ConfigProperties config) {
        final ConfigProperties.ConfigProperty host = config.get("org.uberfire.ext.security.management.wildfly.cli.host",
                                                                DEFAULT_HOST);
        final ConfigProperties.ConfigProperty port = config.get("org.uberfire.ext.security.management.wildfly.cli.port",
                                                                Integer.toString(DEFAULT_PORT));
        final ConfigProperties.ConfigProperty user = config.get("org.uberfire.ext.security.management.wildfly.cli.user",
                                                                DEFAULT_ADMIN_USER);
        final ConfigProperties.ConfigProperty password = config.get("org.uberfire.ext.security.management.wildfly.cli.password",
                                                                    DEFAULT_ADMIN_PASSWORD);
        final ConfigProperties.ConfigProperty realm = config.get("org.uberfire.ext.security.management.wildfly.cli.folderPath",
                                                                 RealmProvider.DEFAULT_FILE_SYSTEM_REALM_PATH);
        final ConfigProperties.ConfigProperty levels = config.get("org.uberfire.ext.security.management.wildfly.cli.levels",
                                                                  RealmProvider.DEFAULT_FILE_SYSTEM_LEVELS);
        final ConfigProperties.ConfigProperty encoded = config.get("org.uberfire.ext.security.management.wildfly.cli.encoded",
                                                                   RealmProvider.DEFAULT_FILE_SYSTEM_ENCODED);

        this.host = host.getValue();
        this.port = Integer.decode(port.getValue());
        this.adminUser = user.getValue();
        this.adminPassword = password.getValue();
        this.folderPath = realm.getValue();
        this.levels = levels.getValue();
        this.encoded = encoded.getValue();
    }

    public ModelControllerClient getClient() throws Exception {
        return ModelControllerClient.Factory.create(
                InetAddress.getByName(host),
                port,
                callbacks -> {
                    for (Callback current : callbacks) {
                        if (current instanceof NameCallback) {
                            NameCallback ncb = (NameCallback) current;
                            ncb.setName(adminUser);
                        } else if (current instanceof PasswordCallback) {
                            PasswordCallback pcb = (PasswordCallback) current;
                            pcb.setPassword(adminPassword.toCharArray());
                        } else if (current instanceof RealmCallback) {
                            RealmCallback rcb = (RealmCallback) current;
                            rcb.setText(rcb.getDefaultText());
                        } else {
                            throw new UnsupportedCallbackException(current);
                        }
                    }
                });
    }
}
