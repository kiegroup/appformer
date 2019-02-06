/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.backend.server.util.gzip;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GzipFilter implements Filter {

    static final String GZIP = "gzip";

    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";

    public void init(final FilterConfig filterConfig) {
        // Empty on purpose
    }

    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {

        if (!(req instanceof HttpServletRequest)) {
            return;
        }

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        final String acceptEncodingHeader = request.getHeader(ACCEPT_ENCODING_HEADER);
        if (acceptEncodingHeader == null || !acceptEncodingHeader.contains(GZIP)) {
            chain.doFilter(req, res);
            return;
        }

        final GzipHttpServletResponseWrapper wResponse = new GzipHttpServletResponseWrapper(response);
        chain.doFilter(req, wResponse);
        wResponse.close();
    }

    public void destroy() {
        // Empty on purpose
    }
}

