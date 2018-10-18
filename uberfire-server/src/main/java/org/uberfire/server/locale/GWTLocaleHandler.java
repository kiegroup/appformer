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
import java.util.List;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.LocaleUtils;
import org.jboss.errai.common.inject.script.filter.InjectScriptHandler;

@ApplicationScoped
public class GWTLocaleHandler implements InjectScriptHandler {

    @Override
    public List<String> injectScript(List<String> injectedScriptList, ServletRequest request, ServletResponse response) throws IOException {
        final Locale locale = getLocale(request);
        final String injectedScript = "<meta name=\"gwt:property\" content=\"locale=" + locale.toString() + "\">";
        injectedScriptList.add(injectedScript);
        return injectedScriptList;
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
