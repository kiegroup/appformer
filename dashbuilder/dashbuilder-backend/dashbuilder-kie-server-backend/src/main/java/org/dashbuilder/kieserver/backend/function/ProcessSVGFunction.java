/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.dashbuilder.kieserver.backend.function;

import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.dashbuilder.external.impl.BackendComponentFunction;
import org.dashbuilder.kieserver.KieServerConnectionInfo;
import org.dashbuilder.kieserver.KieServerConnectionInfoProvider;
import org.dashbuilder.kieserver.backend.rest.KieServerQueryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class ProcessSVGFunction implements BackendComponentFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessSVGFunction.class);

    final static String CONTAINERID_PARAM = "containerId";
    final static String PROCESSID_PARAM = "processId";
    final static String SERVER_TEMPLATE_PARAM = "serverTemplate";

    @Inject
    KieServerQueryClient queryClient;

    @Inject
    KieServerConnectionInfoProvider connectionInfoProvider;

    @Override
    public Object exec(Map<String, Object> params) {
        String containerId = getRequiredParam(CONTAINERID_PARAM, params);
        String processId = getRequiredParam(PROCESSID_PARAM, params);
        Object serverTemplate = params.get(SERVER_TEMPLATE_PARAM);
        KieServerConnectionInfo connectionInfo;
        if (serverTemplate != null) {
            connectionInfo = connectionInfoProvider.get(null, serverTemplate.toString())
                                                   .orElseThrow(() -> new RuntimeException("Configuration for server template not found " + serverTemplate));
        } else {
            connectionInfo = connectionInfoProvider.getDefault()
                                                   .orElseThrow(() -> new RuntimeException("Not able to find credentials to retrieve processes SVG"));
        }
        try {
            return queryClient.processSVG(connectionInfo, containerId, processId);
        } catch (Exception e) {

            if (e instanceof WebApplicationException) {
                WebApplicationException webException = (WebApplicationException) e;
                if (webException.getResponse().getStatus() == 404) {
                    LOGGER.warn("SVG not found for container " + containerId + ", process " + processId);
                    throw new RuntimeException("Process SVG not found for container " + containerId + "and process " + processId);
                }
            }
            LOGGER.warn("Error requesting SVG");
            LOGGER.debug("Error requesting SVG", e);
            throw new RuntimeException("Error requesting SVG from Kie Server.", e);
        }
    }

    private String getRequiredParam(String param, Map<String, Object> params) {
        Object value = params.get(param);
        if (value == null || value.toString().trim().isEmpty()) {
            throw new RuntimeException("Param '" + param + "' is required.");
        }
        return value.toString();
    }

}
