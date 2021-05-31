/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.client.error;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import elemental2.dom.Response;
import elemental2.dom.XMLHttpRequest;
import org.dashbuilder.client.error.DefaultRuntimeErrorCallback.DefaultErrorType;

/**
 * Verify if HTTP responses contains a timeout 
 *
 */
@ApplicationScoped
public class ErrorResponseVerifier {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    
    @Inject
    DefaultRuntimeErrorCallback errorCallback;

    public void verify(XMLHttpRequest xhr) {
        verify(xhr.status, xhr.getResponseHeader(CONTENT_TYPE_HEADER), xhr.statusText);
    }

    public void verify(Response response) {
        verify(response.status, response.headers.get(CONTENT_TYPE_HEADER), response.statusText);
    }

    private void verify(int status, String contentType, String statusText) {
        if (status == 401 || "text/html".equals(contentType)) {
            errorCallback.error(DefaultErrorType.NOT_LOGGED);
        }

        if (status == 403) {
            errorCallback.error(DefaultErrorType.NOT_AUTHORIZED);
        }
    }

}