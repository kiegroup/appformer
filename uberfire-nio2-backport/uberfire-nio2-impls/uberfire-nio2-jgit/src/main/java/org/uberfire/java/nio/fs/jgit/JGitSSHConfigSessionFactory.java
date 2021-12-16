package org.uberfire.java.nio.fs.jgit;

import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProviderUserInfo;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.URIish;

public class JGitSSHConfigSessionFactory extends org.eclipse.jgit.transport.JschConfigSessionFactory {

    private final JGitFileSystemProviderConfiguration config;

    private static final String SOCK5PROXY = "sock5";
    private static final String SOCK4PROXY = "sock4";

    public JGitSSHConfigSessionFactory(final JGitFileSystemProviderConfiguration config) {
        this.config = config;
    }

    @Override
    protected void configure(final OpenSshConfig.Host hc,
                             final Session session) {
        final CredentialsProvider provider = new CredentialsProvider() {
            @Override
            public boolean isInteractive() {
                return false;
            }

            @Override
            public boolean supports(final CredentialItem... items) {
                return true;
            }

            @Override
            public boolean get(final URIish uri,
                               final CredentialItem... items) throws UnsupportedCredentialItem {
                for (CredentialItem item : items) {
                    if (item instanceof CredentialItem.YesNoType) {
                        ((CredentialItem.YesNoType) item).setValue(true);
                    } else if (item instanceof CredentialItem.StringType) {
                        ((CredentialItem.StringType) item).setValue(config.getSshPassphrase());
                    }
                }
                return true;
            }
        };
        final UserInfo userInfo = new CredentialsProviderUserInfo(session,
                                                                  provider);
        session.setUserInfo(userInfo);
        if (config.isSshOverHttpProxy() || config.isSshOverHttpsProxy()) {
            session.setProxy(buildProxy(config));
        }
    }

    Proxy buildProxy(final JGitFileSystemProviderConfiguration config) {
        Proxy proxy;
        final String host;
        final int port;
        String user = null;
        String passw = null;
        if (config.isSshOverHttpProxy()) {
            host = config.getHttpProxyHost();
            port = config.getHttpProxyPort();
            user = config.getHttpProxyUser();
            passw = config.getHttpProxyPassword();
        } else {
            host = config.getHttpsProxyHost();
            port = config.getHttpsProxyPort();
            user = config.getHttpsProxyUser();
            passw = config.getHttpsProxyPassword();
        }
        if (config.getProxyType().equalsIgnoreCase(SOCK5PROXY)) {
            ProxySOCKS5 proxySOCKS5 = new ProxySOCKS5(host, port);
            proxySOCKS5.setUserPasswd(user, passw);
            proxy = proxySOCKS5;
        } else if (config.getProxyType().equalsIgnoreCase(SOCK4PROXY)) {
            ProxySOCKS4 proxySOCKS4 = new ProxySOCKS4(host, port);
            proxySOCKS4.setUserPasswd(user, passw);
            proxy = proxySOCKS4;
        } else {
            ProxyHTTP proxyHTTP = new ProxyHTTP(host, port);
            proxyHTTP.setUserPasswd(user, passw);
            proxy = proxyHTTP;
        }
        return proxy;
    }
}
