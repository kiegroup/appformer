package org.uberfire.java.nio.fs.jgit;

import java.util.Collections;
import java.util.HashMap;

import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.junit.Test;
import org.uberfire.commons.config.ConfigProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

public class JGitSSHConfigSessionFactoryTest {

    @Test
    public void testNoProxy() {
        JGitFileSystemProviderConfiguration config = new JGitFileSystemProviderConfiguration() {

        };
        config.load(new ConfigProperties(Collections.emptyMap()));

        final JGitSSHConfigSessionFactory instance = new JGitSSHConfigSessionFactory(config) {
            @Override
            ProxyHTTP buildProxy(final JGitFileSystemProviderConfiguration config) {
                fail("no proxy should be set");
                return null;
            }
        };
        instance.configure(mock(OpenSshConfig.Host.class), mock(Session.class));
    }

    @Test
    public void testHttpProxy() {
        JGitFileSystemProviderConfiguration config = new JGitFileSystemProviderConfiguration() {

        };
        config.load(new ConfigProperties(new HashMap<String, String>() {{
            put("org.uberfire.nio.git.proxy.ssh.over.http", "true");
            put("http.proxyHost", "somehost");
            put("http.proxyPort", "10");
        }}));

        final JGitSSHConfigSessionFactory instance = new JGitSSHConfigSessionFactory(config) {
            @Override
            Proxy buildProxy(final JGitFileSystemProviderConfiguration config) {
                Proxy proxy = super.buildProxy(config);
                assertThat(proxy).hasFieldOrPropertyWithValue("proxy_host", "somehost");
                assertThat(proxy).hasFieldOrPropertyWithValue("proxy_port", 10);
                assertThat(proxy).hasFieldOrPropertyWithValue("user", null);
                assertThat(proxy).hasFieldOrPropertyWithValue("passwd", null);
                return proxy;
            }
        };
        instance.configure(mock(OpenSshConfig.Host.class), mock(Session.class));
    }

    @Test
    public void testHttpProxyWithAuthentication() {
        JGitFileSystemProviderConfiguration config = new JGitFileSystemProviderConfiguration() {

        };
        config.load(new ConfigProperties(new HashMap<String, String>() {{
            put("org.uberfire.nio.git.proxy.ssh.over.http", "true");
            put("http.proxyHost", "somehost");
            put("http.proxyPort", "10");
            put("http.proxyUser", "user");
            put("http.proxyPassword", "passwd");
        }}));

        final JGitSSHConfigSessionFactory instance = new JGitSSHConfigSessionFactory(config) {
            @Override
            Proxy buildProxy(final JGitFileSystemProviderConfiguration config) {
                Proxy proxy = super.buildProxy(config);
                assertThat(proxy).hasFieldOrPropertyWithValue("proxy_host", "somehost");
                assertThat(proxy).hasFieldOrPropertyWithValue("proxy_port", 10);
                assertThat(proxy).hasFieldOrPropertyWithValue("user", "user");
                assertThat(proxy).hasFieldOrPropertyWithValue("passwd", "passwd");
                return proxy;
            }
        };
        instance.configure(mock(OpenSshConfig.Host.class), mock(Session.class));
    }

    @Test
    public void testHttpsProxy() {
        JGitFileSystemProviderConfiguration config = new JGitFileSystemProviderConfiguration() {

        };
        config.load(new ConfigProperties(new HashMap<String, String>() {{
            put("org.uberfire.nio.git.proxy.ssh.over.https", "true");
            put("https.proxyHost", "somehost");
            put("https.proxyPort", "10");
        }}));

        final JGitSSHConfigSessionFactory instance = new JGitSSHConfigSessionFactory(config) {
            @Override
            Proxy buildProxy(final JGitFileSystemProviderConfiguration config) {
                Proxy proxy = super.buildProxy(config);
                assertThat(proxy).hasFieldOrPropertyWithValue("proxy_host", "somehost");
                assertThat(proxy).hasFieldOrPropertyWithValue("proxy_port", 10);
                assertThat(proxy).hasFieldOrPropertyWithValue("user", null);
                assertThat(proxy).hasFieldOrPropertyWithValue("passwd", null);
                return proxy;
            }
        };
        instance.configure(mock(OpenSshConfig.Host.class), mock(Session.class));
    }

    @Test
    public void testHttpsProxyWithAuthentication() {
        JGitFileSystemProviderConfiguration config = new JGitFileSystemProviderConfiguration() {

        };
        config.load(new ConfigProperties(new HashMap<String, String>() {{
            put("org.uberfire.nio.git.proxy.ssh.over.https", "true");
            put("https.proxyHost", "somehost");
            put("https.proxyPort", "10");
            put("https.proxyUser", "user");
            put("https.proxyPassword", "passwd");
        }}));

        final JGitSSHConfigSessionFactory instance = new JGitSSHConfigSessionFactory(config) {
            @Override
            Proxy buildProxy(final JGitFileSystemProviderConfiguration config) {
                Proxy proxy = super.buildProxy(config);
                assertThat(proxy).hasFieldOrPropertyWithValue("proxy_host", "somehost");
                assertThat(proxy).hasFieldOrPropertyWithValue("proxy_port", 10);
                assertThat(proxy).hasFieldOrPropertyWithValue("user", "user");
                assertThat(proxy).hasFieldOrPropertyWithValue("passwd", "passwd");
                return proxy;
            }
        };
        instance.configure(mock(OpenSshConfig.Host.class), mock(Session.class));
    }
}
