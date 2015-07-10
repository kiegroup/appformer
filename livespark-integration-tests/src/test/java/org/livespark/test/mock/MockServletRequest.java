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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

public class MockServletRequest implements HttpServletRequest {

    private ServletContext context;
    private String serverName;
    private int serverPort;

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute( String name ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding( String env ) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    @Override
    public int getContentLength() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getContentLengthLong()
     */
    @Override
    public long getContentLengthLong() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getContentType()
     */
    @Override
    public String getContentType() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getInputStream()
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    @Override
    public String getParameter( String name ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    @Override
    public Enumeration<String> getParameterNames() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues( String name ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    @Override
    public String getProtocol() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getScheme()
     */
    @Override
    public String getScheme() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getServerName()
     */
    @Override
    public String getServerName() {
        return serverName;
    }

    public MockServletRequest setServerName( String serverName ) {
        this.serverName = serverName;
        return this;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    @Override
    public int getServerPort() {
        return serverPort;
    }

    public MockServletRequest setServerPort( int serverPort ) {
        this.serverPort = serverPort;
        return this;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getReader()
     */
    @Override
    public BufferedReader getReader() throws IOException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    @Override
    public String getRemoteAddr() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    @Override
    public String getRemoteHost() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute( String name,
                              Object o ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute( String name ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocale()
     */
    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocales()
     */
    @Override
    public Enumeration<Locale> getLocales() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#isSecure()
     */
    @Override
    public boolean isSecure() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    @Override
    public RequestDispatcher getRequestDispatcher( String path ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    @Override
    public String getRealPath( String path ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRemotePort()
     */
    @Override
    public int getRemotePort() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocalName()
     */
    @Override
    public String getLocalName() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocalAddr()
     */
    @Override
    public String getLocalAddr() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocalPort()
     */
    @Override
    public int getLocalPort() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getServletContext()
     */
    @Override
    public ServletContext getServletContext() {
        return context;
    }

    public MockServletRequest setServletContext( ServletContext context ) {
        this.context = context;
        return this;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#startAsync()
     */
    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#startAsync(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public AsyncContext startAsync( ServletRequest servletRequest,
                                    ServletResponse servletResponse ) throws IllegalStateException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#isAsyncStarted()
     */
    @Override
    public boolean isAsyncStarted() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#isAsyncSupported()
     */
    @Override
    public boolean isAsyncSupported() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAsyncContext()
     */
    @Override
    public AsyncContext getAsyncContext() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getDispatcherType()
     */
    @Override
    public DispatcherType getDispatcherType() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    @Override
    public String getAuthType() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    @Override
    public Cookie[] getCookies() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
     */
    @Override
    public long getDateHeader( String name ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    @Override
    public String getHeader( String name ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    @Override
    public Enumeration<String> getHeaders( String name ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    @Override
    public int getIntHeader( String name ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getMethod()
     */
    @Override
    public String getMethod() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    @Override
    public String getPathInfo() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    @Override
    public String getPathTranslated() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    @Override
    public String getContextPath() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    @Override
    public String getQueryString() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    @Override
    public String getRemoteUser() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
     */
    @Override
    public boolean isUserInRole( String role ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    @Override
    public Principal getUserPrincipal() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    @Override
    public String getRequestedSessionId() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    @Override
    public String getRequestURI() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    @Override
    public StringBuffer getRequestURL() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    @Override
    public String getServletPath() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     */
    @Override
    public HttpSession getSession( boolean create ) {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    @Override
    public HttpSession getSession() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#changeSessionId()
     */
    @Override
    public String changeSessionId() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#authenticate(javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean authenticate( HttpServletResponse response ) throws IOException,
                                                                ServletException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#login(java.lang.String, java.lang.String)
     */
    @Override
    public void login( String username,
                       String password ) throws ServletException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#logout()
     */
    @Override
    public void logout() throws ServletException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getParts()
     */
    @Override
    public Collection<Part> getParts() throws IOException,
                                       ServletException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getPart(java.lang.String)
     */
    @Override
    public Part getPart( String name ) throws IOException,
                                       ServletException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#upgrade(java.lang.Class)
     */
    @Override
    public <T extends HttpUpgradeHandler> T upgrade( Class<T> handlerClass ) throws IOException,
                                                                             ServletException {
        // TODO Auto-generated method stub
        throw new RuntimeException( "Not yet implemented." );
    }

}
