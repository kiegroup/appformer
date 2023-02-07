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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.sshd.common.cipher.BuiltinCiphers;
import org.apache.sshd.common.mac.BuiltinMacs;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.CachingPublicKeyAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.UnknownCommand;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.java.nio.fs.jgit.JGitFileSystemProvider;
import org.uberfire.java.nio.security.SSHAuthenticator;

import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.server.ServerBuilder;
import static org.apache.sshd.server.ServerBuilder.builder;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotEmpty;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;
import static org.uberfire.java.nio.fs.jgit.daemon.common.PortUtil.validateOrGetNew;

public class GitSSHService {

    private static final Logger LOG = LoggerFactory.getLogger(GitSSHService.class);

    private final List<BuiltinMacs> managedMACs =
            Collections.unmodifiableList(Arrays.asList(
                    //BuiltinMacs.hmacmd5,
                    BuiltinMacs.hmacsha1,
                    BuiltinMacs.hmacsha256,
                    BuiltinMacs.hmacsha512/*,
                    BuiltinMacs.hmacsha196,
                    BuiltinMacs.hmacmd596*/
            ));

    private SshServer sshd;
    private AuthenticationService authenticationService;
    private SSHAuthenticator sshAuthenticator;

    private SshServer buildSshServer(String ciphersConfigured,
                                     String macsConfigured) {

        return builder()
                .cipherFactories(ServerBuilder.setUpDefaultCiphers(true))
                .macFactories(ServerBuilder.setUpDefaultMacs(true))
                .build();
    }

    public void setup(final File certDir,
                      final InetSocketAddress inetSocketAddress,
                      final String sshIdleTimeout,
                      final String algorithm,
                      final ReceivePackFactory receivePackFactory,
                      final UploadPackFactory uploadPackFactory,
                      final JGitFileSystemProvider.RepositoryResolverImpl<BaseGitCommand> repositoryResolver,
                      final ExecutorService executorService) {
        setup(certDir, inetSocketAddress, sshIdleTimeout, algorithm, receivePackFactory, uploadPackFactory, repositoryResolver, executorService, null, null);
    }

    public void setup(final File certDir,
                      final InetSocketAddress inetSocketAddress,
                      final String sshIdleTimeout,
                      final String algorithm,
                      final ReceivePackFactory receivePackFactory,
                      final UploadPackFactory uploadPackFactory,
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
        checkNotNull("uploadPackFactory",
                     uploadPackFactory);
        checkNotNull("repositoryResolver",
                     repositoryResolver);

        buildSSHServer(gitSshCiphers,
                       gitSshMacs);

        sshd.getProperties().put(CoreModuleProperties.IDLE_TIMEOUT.getName(), sshIdleTimeout);

        if (inetSocketAddress != null) {
            sshd.setHost(inetSocketAddress.getHostName());
            sshd.setPort(validateOrGetNew(inetSocketAddress.getPort()));

            if (inetSocketAddress.getPort() != sshd.getPort()) {
                LOG.error("SSH for Git original port {} not available, new free port {} assigned.", inetSocketAddress.getPort(), sshd.getPort());
            }
        }

        if (!certDir.exists()) {
            certDir.mkdirs();
        }

        final AbstractGeneratorHostKeyProvider keyPairProvider = new SimpleGeneratorHostKeyProvider(certDir.toPath().resolve("hostkey.ser"));

        try {
            SecurityUtils.getKeyPairGenerator(algorithm);
            keyPairProvider.setAlgorithm(algorithm);
        } catch (final Exception ignore) {
            throw new RuntimeException(String.format("Can't use '%s' algorithm for ssh key pair generator.",
                                                     algorithm),
                                       ignore);
        }

        sshd.setKeyPairProvider(keyPairProvider);
        sshd.setCommandFactory((channel, command) -> {
            if (command.startsWith("git-upload-pack")) {
                return new GitUploadCommand(command,
                                            repositoryResolver,
                                            uploadPackFactory,
                                            executorService);
            } else if (command.startsWith("git-receive-pack")) {
                return new GitReceiveCommand(command,
                                             repositoryResolver,
                                             receivePackFactory,
                                             executorService);
            } else {
                return new UnknownCommand(command);
            }
        });
        sshd.setPublickeyAuthenticator(new CachingPublicKeyAuthenticator((username, key, session) -> {

            final User user = getSshAuthenticator().authenticate(username, key);

            if (user == null) {
                return false;
            }

            session.setAttribute(BaseGitCommand.SUBJECT_KEY, user);

            return true;
        }));
        sshd.setPasswordAuthenticator((username, password, session) -> {

            final User user = getUserPassAuthenticator().login(username, password);

            if (user == null) {
                return false;
            }

            session.setAttribute(BaseGitCommand.SUBJECT_KEY, user);
            return true;
        });
    }

    private void buildSSHServer(String gitSshCiphers,
                                String gitSshMacs) {
        sshd = buildSshServer(gitSshCiphers, gitSshMacs);
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

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(sshd.getProperties());
    }

    public AuthenticationService getUserPassAuthenticator() {
        return authenticationService;
    }

    public void setUserPassAuthenticator(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public SSHAuthenticator getSshAuthenticator() {
        return sshAuthenticator;
    }

    public void setSshAuthenticator(SSHAuthenticator sshAuthenticator) {
        this.sshAuthenticator = sshAuthenticator;
    }

    public List<BuiltinMacs> getManagedMACs() {
        return managedMACs;
    }
}
