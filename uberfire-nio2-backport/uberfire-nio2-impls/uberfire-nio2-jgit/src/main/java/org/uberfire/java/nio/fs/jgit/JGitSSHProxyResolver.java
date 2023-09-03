package org.uberfire.java.nio.fs.jgit;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.eclipse.jgit.transport.sshd.ProxyData;
import org.eclipse.jgit.transport.sshd.ProxyDataFactory;

public class JGitSSHProxyResolver implements ProxyDataFactory {

    private final JGitFileSystemProviderConfiguration config;

    private static final String SOCKPROXY = "sock";
    private static final String HTTPPROXY = "http";

    public JGitSSHProxyResolver(final JGitFileSystemProviderConfiguration config) {
        this.config = config;
    }

    @Override
    public ProxyData get(InetSocketAddress remoteAddress) {
        Proxy proxy = null;
        ProxyData proxyData = null;

        if (config.getHttpProxyHost() != null) {
            InetSocketAddress proxyAddress = new InetSocketAddress(config.getHttpProxyHost(), config.getHttpProxyPort());
            switch (config.getProxyType()) {
                case HTTPPROXY:
                    proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
                    break;
                case SOCKPROXY:
                    proxy = new Proxy(Proxy.Type.SOCKS, proxyAddress);
                    break;
                default:
                    proxy = new Proxy(Proxy.Type.DIRECT, proxyAddress);
                    break;
            }
            proxyData = new ProxyData(proxy, config.getHttpProxyUser(), config.getHttpProxyPassword().toCharArray());
        }
        return proxyData;
    }
}
