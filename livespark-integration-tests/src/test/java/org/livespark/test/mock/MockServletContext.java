/*
 * Copyright 2015 JBoss Inc
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
package org.livespark.test.mock;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

public class MockServletContext implements ServletContext {

    private Map<String, String> realPaths = new HashMap<String, String>();

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getContextPath()
     */
    @Override
    public String getContextPath() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getContext(java.lang.String)
     */
    @Override
    public ServletContext getContext( String uripath ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getMajorVersion()
     */
    @Override
    public int getMajorVersion() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getMinorVersion()
     */
    @Override
    public int getMinorVersion() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getEffectiveMajorVersion()
     */
    @Override
    public int getEffectiveMajorVersion() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getEffectiveMinorVersion()
     */
    @Override
    public int getEffectiveMinorVersion() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
     */
    @Override
    public String getMimeType( String file ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
     */
    @Override
    public Set<String> getResourcePaths( String path ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getResource(java.lang.String)
     */
    @Override
    public URL getResource( String path ) throws MalformedURLException {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream( String path ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
     */
    @Override
    public RequestDispatcher getRequestDispatcher( String path ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
     */
    @Override
    public RequestDispatcher getNamedDispatcher( String name ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getServlet(java.lang.String)
     */
    @Override
    public Servlet getServlet( String name ) throws ServletException {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getServlets()
     */
    @Override
    public Enumeration<Servlet> getServlets() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getServletNames()
     */
    @Override
    public Enumeration<String> getServletNames() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#log(java.lang.String)
     */
    @Override
    public void log( String msg ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#log(java.lang.Exception, java.lang.String)
     */
    @Override
    public void log( Exception exception,
                     String msg ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#log(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void log( String message,
                     Throwable throwable ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
     */
    @Override
    public String getRealPath( String path ) {
        final String realPath = realPaths.get( path );
        if ( realPath != null ) {
            return realPath;
        } else {
            throw new RuntimeException( "Given path, " + path + ", not supported by test." );
        }
    }

    public MockServletContext addRealPath( String path, String realPath ) {
        realPaths.put( path, realPath );
        return this;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getServerInfo()
     */
    @Override
    public String getServerInfo() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
     */
    @Override
    public String getInitParameter( String name ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getInitParameterNames()
     */
    @Override
    public Enumeration<String> getInitParameterNames() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#setInitParameter(java.lang.String, java.lang.String)
     */
    @Override
    public boolean setInitParameter( String name,
                                     String value ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute( String name ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getAttributeNames()
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute( String name,
                              Object object ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute( String name ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getServletContextName()
     */
    @Override
    public String getServletContextName() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#addServlet(java.lang.String, java.lang.String)
     */
    @Override
    public Dynamic addServlet( String servletName,
                               String className ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#addServlet(java.lang.String, javax.servlet.Servlet)
     */
    @Override
    public Dynamic addServlet( String servletName,
                               Servlet servlet ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#addServlet(java.lang.String, java.lang.Class)
     */
    @Override
    public Dynamic addServlet( String servletName,
                               Class< ? extends Servlet> servletClass ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#createServlet(java.lang.Class)
     */
    @Override
    public <T extends Servlet> T createServlet( Class<T> clazz ) throws ServletException {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getServletRegistration(java.lang.String)
     */
    @Override
    public ServletRegistration getServletRegistration( String servletName ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getServletRegistrations()
     */
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#addFilter(java.lang.String, java.lang.String)
     */
    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter( String filterName,
                                                               String className ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#addFilter(java.lang.String, javax.servlet.Filter)
     */
    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter( String filterName,
                                                               Filter filter ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#addFilter(java.lang.String, java.lang.Class)
     */
    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter( String filterName,
                                                               Class< ? extends Filter> filterClass ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#createFilter(java.lang.Class)
     */
    @Override
    public <T extends Filter> T createFilter( Class<T> clazz ) throws ServletException {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getFilterRegistration(java.lang.String)
     */
    @Override
    public FilterRegistration getFilterRegistration( String filterName ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getFilterRegistrations()
     */
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getSessionCookieConfig()
     */
    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#setSessionTrackingModes(java.util.Set)
     */
    @Override
    public void setSessionTrackingModes( Set<SessionTrackingMode> sessionTrackingModes ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getDefaultSessionTrackingModes()
     */
    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getEffectiveSessionTrackingModes()
     */
    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#addListener(java.lang.String)
     */
    @Override
    public void addListener( String className ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#addListener(java.util.EventListener)
     */
    @Override
    public <T extends EventListener> void addListener( T t ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#addListener(java.lang.Class)
     */
    @Override
    public void addListener( Class< ? extends EventListener> listenerClass ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#createListener(java.lang.Class)
     */
    @Override
    public <T extends EventListener> T createListener( Class<T> clazz ) throws ServletException {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getJspConfigDescriptor()
     */
    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getClassLoader()
     */
    @Override
    public ClassLoader getClassLoader() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#declareRoles(java.lang.String[])
     */
    @Override
    public void declareRoles( String... roleNames ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#getVirtualServerName()
     */
    @Override
    public String getVirtualServerName() {
        throw new RuntimeException( "Not yet implemented." );
    }

}
