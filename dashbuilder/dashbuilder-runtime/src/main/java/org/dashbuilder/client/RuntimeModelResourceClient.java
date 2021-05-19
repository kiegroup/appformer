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
package org.dashbuilder.client;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import elemental2.dom.Response;
import org.dashbuilder.client.error.ErrorResponseVerifier;
import org.dashbuilder.shared.marshalling.RuntimeModelJSONMarshaller;
import org.dashbuilder.shared.marshalling.RuntimeServiceResponseJSONMarshaller;
import org.dashbuilder.shared.model.RuntimeModel;
import org.dashbuilder.shared.model.RuntimeServiceResponse;
import org.dashbuilder.shared.service.RuntimeModelService;
import org.jboss.errai.common.client.api.Caller;

import static elemental2.dom.DomGlobal.fetch;

@ApplicationScoped
public class RuntimeModelResourceClient {

    @Inject
    Caller<RuntimeModelService> runtimeModelServiceCaller;

    @Inject
    ErrorResponseVerifier verifier;

    public void getRuntimeModel(String runtimeModelId,
                                Consumer<Optional<RuntimeModel>> runtimeModelConsumer,
                                Consumer<String> onError) {

        fetch("/rest/runtime-model/" + runtimeModelId).then(response -> {
            if (response.status == 404) {
                runtimeModelConsumer.accept(Optional.empty());
            }
            if (response.status != 200) {
                handleErrorResponse(response, onError);
            }

            verifier.verify(response);

            response.text().then(runtimeModelJson -> {
                RuntimeModel model = RuntimeModelJSONMarshaller.get().fromJson(runtimeModelJson);
                runtimeModelConsumer.accept(Optional.of(model));
                return null;
            }, error -> {
                onError.accept("Error getting Runtime Model: " + error);
                return null;
            });

            return null;
        }).catch_(error -> {
            onError.accept("Error retrieving information from the server: " + error);
            return null;
        });
    }

    public void getRuntimeModelInfo(String runtimeModelId,
                                    Consumer<RuntimeServiceResponse> runtimeModelInfoConsumer,
                                    BiConsumer<Object, Throwable> onError) {

        fetch("/rest/runtime-model/" + runtimeModelId + "/info").then(response -> {
            if (response.status != 200) {
                handleErrorResponse(response, onError);
            }

            verifier.verify(response);
            
            response.text().then(runtimeResponseJson -> {
                RuntimeServiceResponse serviceResponse = RuntimeServiceResponseJSONMarshaller.get().fromJson(runtimeResponseJson);
                runtimeModelInfoConsumer.accept(serviceResponse);
                return null;
            }, error -> {
                String message = "Error getting runtime service response: " + error;
                onError.accept(message, new RuntimeException(message));
                return null;
            });

            return null;
        }).catch_(error -> {
            onError.accept(error, new RuntimeException("Error retrieving information from the server."));
            return null;
        });
    }

    private void handleErrorResponse(Response response, BiConsumer<Object, Throwable> onError) {
        handleErrorResponse(response, error -> onError.accept(error, new RuntimeException(error)));
    }

    private void handleErrorResponse(Response response, Consumer<String> onError) {
        onError.accept("Server responded with error " + response.status + ": " + response.statusText);
    }

}