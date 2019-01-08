/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.java.nio.fs.jgit.daemon.ssh;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.cipher.BuiltinCiphers;
import org.apache.sshd.common.mac.BuiltinMacs;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.CachingPublicKeyAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.UnknownCommand;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.java.nio.fs.jgit.JGitFileSystemProvider;
import org.uberfire.java.nio.security.FileSystemAuthenticator;
import org.uberfire.java.nio.security.FileSystemAuthorizer;
import org.uberfire.java.nio.security.FileSystemUser;
import org.uberfire.java.nio.security.SSHAuthenticator;

import static org.apache.sshd.common.NamedFactory.setUpBuiltinFactories;
import static org.apache.sshd.server.ServerBuilder.builder;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotEmpty;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;
import static org.uberfire.java.nio.fs.jgit.daemon.common.PortUtil.validateOrGetNew;

public class GitSSHService {

    private static final Logger LOG = LoggerFactory.getLogger(GitSSHService.class);

    private static List<BuiltinCiphers> managedCiphers =
            Collections.unmodifiableList(Arrays.asList(
                    BuiltinCiphers.aes128ctr,
                    BuiltinCiphers.aes192ctr,
                    BuiltinCiphers.aes256ctr,
                    BuiltinCiphers.arcfour256,
                    BuiltinCiphers.arcfour128,
                    BuiltinCiphers.aes192cbc,
                    BuiltinCiphers.aes256cbc
            ));

    private static List<BuiltinMacs> managedMACs =
            Collections.unmodifiableList(Arrays.asList(
                    BuiltinMacs.hmacmd5,
                    BuiltinMacs.hmacsha1,
                    BuiltinMacs.hmacsha256,
                    BuiltinMacs.hmacsha512,
                    BuiltinMacs.hmacsha196,
                    BuiltinMacs.hmacmd596
            ));

    private SshServer sshd;
    private FileSystemAuthenticator fileSystemAuthenticator;
    private FileSystemAuthorizer fileSystemAuthorizer;
    private SSHAuthenticator sshAuthenticator;

    private SshServer buildSshServer() {
        return builder().cipherFactories(
                setUpBuiltinFactories(false,
                                      managedCiphers)).
                macFactories(
                        setUpBuiltinFactories(false,
                                              managedMACs)).build();
    }

    private SshServer buildSshServer(List<BuiltinCiphers> ciphersConfigured,
                                     List<BuiltinMacs> macsConfigured) {
        ServerBuilder serverBuilder = builder();
        if(!ciphersConfigured.isEmpty()){
            serverBuilder.cipherFactories(
                    setUpBuiltinFactories(false,
                                          ciphersConfigured));
        }else {
            serverBuilder.cipherFactories(
                    setUpBuiltinFactories(false,
                                          managedCiphers));
        }
        if(!macsConfigured.isEmpty()){
            serverBuilder.macFactories(
                    setUpBuiltinFactories(false,
                                          macsConfigured));
        }else{
            serverBuilder.macFactories(
                    setUpBuiltinFactories(false,
                                          managedMACs));
        }

        return serverBuilder.build();
    }

    public void setup(final File certDir,
                      final InetSocketAddress inetSocketAddress,
                      final String sshIdleTimeout,
                      final String algorithm,
                      final ReceivePackFactory receivePackFactory,
                      final JGitFileSystemProvider.RepositoryResolverImpl<BaseGitCommand> repositoryResolver,
                      final ExecutorService executorService,
                      final String gitSshCiphers,
                      final String gitSshMacs) {
        checkNotNull("certDir",
                     certDir);
        checkNotEmpty("sshIdleTimeout",
                      sshIdleTimeout);
        checkNotEmpty("algorithm",
                      algorithm);
        checkNotNull("receivePackFactory",
                     receivePackFactory);
        checkNotNull("repositoryResolver",
                     repositoryResolver);

        buildSSHServer(gitSshCiphers,
                       gitSshMacs);

        sshd.getProperties().put(SshServer.IDLE_TIMEOUT,
                                 sshIdleTimeout);

        if (inetSocketAddress != null) {
            sshd.setHost(inetSocketAddress.getHostName());
            sshd.setPort(validateOrGetNew(inetSocketAddress.getPort()));

            if (inetSocketAddress.getPort() != sshd.getPort()) {
                LOG.error("SSH for Git original port {} not available, new free port {} assigned.",
                          inetSocketAddress.getPort(),
                          sshd.getPort());
            }
        }

        if (!certDir.exists()) {
            certDir.mkdirs();
        }

        final AbstractGeneratorHostKeyProvider keyPairProvider = new SimpleGeneratorHostKeyProvider(new File(certDir,
                                                                                                             "hostkey.ser"));

        try {
            SecurityUtils.getKeyPairGenerator(algorithm);
            keyPairProvider.setAlgorithm(algorithm);
        } catch (final Exception ignore) {
            throw new RuntimeException(String.format("Can't use '%s' algorithm for ssh key pair generator.",
                                                     algorithm),
                                       ignore);
        }

        sshd.setKeyPairProvider(keyPairProvider);
        sshd.setCommandFactory(command -> {
            if (command.startsWith("git-upload-pack")) {
                return new GitUploadCommand(command,
                                            repositoryResolver,
                                            getAuthorizationManager(),
                                            executorService);
            } else if (command.startsWith("git-receive-pack")) {
                return new GitReceiveCommand(command,
                                             repositoryResolver,
                                             getAuthorizationManager(),
                                             receivePackFactory,
                                             executorService);
            } else {
                return new UnknownCommand(command);
            }
        });
        sshd.setPublickeyAuthenticator(new CachingPublicKeyAuthenticator((username, key, session) -> {

            final FileSystemUser user = getSshAuthenticator().authenticate(username,
                                                                           key);

            if (user == null) {
                return false;
            }

            session.setAttribute(BaseGitCommand.SUBJECT_KEY,
                                 user);

            return true;
        }));
        sshd.setPasswordAuthenticator((username, password, session) -> {

            final FileSystemUser user = getUserPassAuthenticator().authenticate(username,
                                                                                password);

            if (user == null) {
                return false;
            }

            session.setAttribute(BaseGitCommand.SUBJECT_KEY,
                                 user);
            return true;
        });
    }

    private void buildSSHServer(String gitSshCiphers,
                                String gitSshMacs) {

        List<BuiltinCiphers> ciphers = checkAndSetGitCiphers(gitSshCiphers);
        List<BuiltinMacs> macs = checkAndSetGitMacs(gitSshMacs);
        if (ciphers.isEmpty() || macs.isEmpty()) {
            sshd = buildSshServer();
        } else {
            sshd = buildSshServer(ciphers,
                                  macs);
        }
    }

    private List<BuiltinCiphers> checkAndSetGitCiphers(String gitSshCiphers) {
        List<BuiltinCiphers> ciphersHandled = new ArrayList<>();
        if (gitSshCiphers == null) {
            return ciphersHandled;
        } else {
            List<String> ciphers = Arrays.asList(gitSshCiphers.split(","));
            for (String cipherCode : ciphers) {
                try {
                    BuiltinCiphers cipher = BuiltinCiphers.fromFactoryName(cipherCode.trim().toLowerCase());
                    if (cipher == null || !managedCiphers.contains(cipher)) {
                        LOG.warn("Cipher code {} not handled.", cipherCode);
                    } else {
                        ciphersHandled.add(cipher);
                        LOG.info("Added Cipher {} to the git ssh configuration. ", cipher);
                    }
                } catch (IllegalArgumentException ex) {
                    LOG.warn("No cipher found with identifier :{}", cipherCode);
                }
            }

            if (ciphersHandled.size() == 0) {
                LOG.warn("No valid ciphers for git ssh configuration provided :{}", gitSshCiphers);
            }
            return ciphersHandled;
        }
    }

    private List<BuiltinMacs> checkAndSetGitMacs(String gitSshMacs) {
        List<BuiltinMacs> macs = new ArrayList<>();
        if (gitSshMacs == null) {
            return macs;
        } else {
            List<String> macsInput = Arrays.asList(gitSshMacs.split(","));

            for (String macCode : macsInput) {
                try {
                    BuiltinMacs mac = BuiltinMacs.valueOf(macCode.trim().toLowerCase());
                    if (mac == null || !managedMACs.contains(mac)) {
                        LOG.warn("Mac code {} not handled.", macCode);
                    } else {
                        macs.add(mac);
                        LOG.info("Added MAC {} to the git ssh configuration. ", mac);
                    }
                } catch (IllegalArgumentException ex) {
                    LOG.warn("No cipher found with identifier :{}", macCode);
                }
            }

            if (macs.size() == 0) {
                LOG.warn("No valid macs for git ssh configuration provided :{}", gitSshMacs);
            }
            return macs;
        }
    }

    public void stop() {
        try {
            sshd.stop(true);
        } catch (IOException ignored) {
        }
    }

    public void start() {
        try {
            sshd.start();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't start SSH daemon at " + sshd.getHost() + ":" + sshd.getPort(),
                                       e);
        }
    }

    public boolean isRunning() {
        return !(sshd.isClosed() || sshd.isClosing());
    }

    SshServer getSshServer() {
        return sshd;
    }

    public List<String> getCipherFactoriesNames(){
        return sshd.getCipherFactoriesNames();
    }

    public List<String> getMacFactoriesNames(){
        return sshd.getMacFactoriesNames();
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(sshd.getProperties());
    }

    public FileSystemAuthenticator getUserPassAuthenticator() {
        return fileSystemAuthenticator;
    }

    public void setUserPassAuthenticator(FileSystemAuthenticator fileSystemAuthenticator) {
        this.fileSystemAuthenticator = fileSystemAuthenticator;
    }

    public FileSystemAuthorizer getAuthorizationManager() {
        return fileSystemAuthorizer;
    }

    public void setAuthorizationManager(FileSystemAuthorizer fileSystemAuthorizer) {
        this.fileSystemAuthorizer = fileSystemAuthorizer;
    }

    public SSHAuthenticator getSshAuthenticator() {
        return sshAuthenticator;
    }

    public void setSshAuthenticator(SSHAuthenticator sshAuthenticator) {
        this.sshAuthenticator = sshAuthenticator;
    }
}
