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
package org.uberfire.server.locale;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.LocaleUtils;
import org.jboss.errai.common.server.FilterCacheUtil.CharResponseWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Serializes and adds the GWT locale meta tag in the
 * application's host page. This is useful in case the
 * host page is a simple html file.
 */
public class GWTLocaleHeaderFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {

        final CharResponseWrapper wrappedResponse = getWrapper((HttpServletResponse) response);
        chain.doFilter(request,
                       wrappedResponse);

        final String output;
        final Locale locale = getLocale(request);
        final String injectedScript = "<meta name=\"gwt:property\" content=\"locale=" + locale.toString() + "\">";

        final Document document = Jsoup.parse(wrappedResponse.toString());
        document.head().append(injectedScript);
        output = document.html();

        final byte[] outputBytes = output.getBytes("UTF-8");
        response.setContentLength(outputBytes.length);
        response.getWriter().print(output);
    }

    protected CharResponseWrapper getWrapper(final HttpServletResponse response) {
        return new CharResponseWrapper(response);
    }

    private Locale getLocale(final ServletRequest request) {
        Locale locale = request.getLocale();
        final String paramLocale = request.getParameter("locale");
        if (paramLocale == null || paramLocale.isEmpty()) {
            return locale;
        }
        try {
            locale = LocaleUtils.toLocale(paramLocale);
        } catch (Exception e) {
            //Swallow. Locale is initially set to ServletRequest locale
        }
        return locale;
    }
}