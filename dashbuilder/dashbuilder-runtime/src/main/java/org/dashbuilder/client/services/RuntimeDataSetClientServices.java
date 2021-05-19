package org.dashbuilder.client.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.dom.XMLHttpRequest;
import elemental2.promise.IThenable;
import org.dashbuilder.client.error.DefaultRuntimeErrorCallback;
import org.dashbuilder.client.error.DefaultRuntimeErrorCallback.DefaultErrorType;
import org.dashbuilder.client.error.ErrorResponseVerifier;
import org.dashbuilder.client.marshalling.ClientDataSetMetadataJSONMarshaller;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.dataset.client.DataSetExportReadyCallback;
import org.dashbuilder.dataset.client.DataSetMetadataCallback;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.json.DataSetJSONMarshaller;
import org.dashbuilder.dataset.json.DataSetLookupJSONMarshaller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.backend.vfs.Path;

import static elemental2.dom.DomGlobal.fetch;

@Alternative
@ApplicationScoped
public class RuntimeDataSetClientServices implements DataSetClientServices {

    private static final String LOOKUP_ENDPOINT = "/rest/dataset/lookup";

    @Inject
    ClientDataSetMetadataJSONMarshaller dataSetMetadataJsonMarshaller;

    @Inject
    ErrorResponseVerifier verifier;

    @Inject
    DefaultRuntimeErrorCallback errorCallback;

    Map<String, DataSetMetadata> metadataCache = new HashMap<>();

    public RuntimeDataSetClientServices() {
        // empty 
    }

    @Override
    public void setPushRemoteDataSetEnabled(boolean pushRemoteDataSetEnabled) {
        // ignored
    }

    @Override
    public void fetchMetadata(String uuid, DataSetMetadataCallback listener) throws Exception {
        if (metadataCache.containsKey(uuid)) {
            listener.callback(metadataCache.get(uuid));
            return;
        }
        fetch(LOOKUP_ENDPOINT).then((Response response) -> {
            verifier.verify(response);
            response.text().then(responseText -> {
                if (response.status == 500) {
                    listener.onError(new ClientRuntimeError("Not able to retrieve dataset metadata", new Exception(responseText)));
                } else {
                    DataSetMetadata meta = parseMetadata(responseText);
                    listener.callback(meta);
                    metadataCache.put(uuid, meta);
                }
                return null;
            }, error -> {
                listener.onError(new ClientRuntimeError("Not able to read dataset metadata"));
                return null;
            });

            return null;
        }).catch_(this::handleError);
    }

    @Override
    public DataSetMetadata getMetadata(String uuid) {
        if (metadataCache.containsKey(uuid)) {
            return metadataCache.get(uuid);
        }

        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.open("GET", "/rest/dataset/" + uuid + "/metadata", false);
        xhr.send();
        verifier.verify(xhr);
        if (xhr.status == 500) {
            throw new RuntimeException("Not able to retrieve data set metadata: " + xhr.responseText);
        }
        DataSetMetadata metadata = parseMetadata(xhr.responseText);
        metadataCache.put(uuid, metadata);
        return metadata;
    }

    @Override
    public void lookupDataSet(DataSetDef def, DataSetLookup lookup, DataSetReadyCallback listener) throws Exception {
        RequestInit request = RequestInit.create();
        request.setMethod("POST");
        request.setBody(toJson(lookup));
        Headers headers = new Headers();
        headers.append("Content-type", "application/json");
        request.setHeaders(headers);
        fetch(LOOKUP_ENDPOINT, request).then((Response response) -> {
            verifier.verify(response);
            response.text().then(responseText -> {
                if (response.status == 500) {
                    listener.onError(new ClientRuntimeError("Not able to retrieve data set. ", new Exception(responseText)));
                } else {
                    DataSet dataSet = parseDataSet(responseText);
                    listener.callback(dataSet);
                }
                return null;
            }, error -> {
                listener.onError(new ClientRuntimeError("Error reading data set content."));
                return null;
            });
            return null;
        }).catch_(this::handleError);

    }

    @Override
    public void lookupDataSet(DataSetLookup request, DataSetReadyCallback listener) throws Exception {
        this.lookupDataSet(null, request, listener);
    }

    @Override
    public void exportDataSetCSV(DataSetLookup request, DataSetExportReadyCallback listener) throws Exception {
        throw new IllegalArgumentException("Export to CSV not supported");
    }

    @Override
    public void exportDataSetExcel(DataSetLookup request, DataSetExportReadyCallback listener) throws Exception {
        throw new IllegalArgumentException("Export to excel not supported");
    }

    @Override
    public void newDataSet(DataSetProviderType type, RemoteCallback<DataSetDef> callback) throws Exception {
        throw new IllegalArgumentException("New data sets are not supported");
    }

    @Override
    public void getPublicDataSetDefs(RemoteCallback<List<DataSetDef>> callback) {
        // ignored in runtime        
    }

    @Override
    public String getDownloadFileUrl(Path path) {
        throw new IllegalArgumentException("Download URL not supported");
    }

    @Override
    public String getUploadFileUrl(String path) {
        throw new IllegalArgumentException("Uploaded not supported");
    }

    private DataSetMetadata parseMetadata(String jsonContent) {
        return dataSetMetadataJsonMarshaller.fromJSON(jsonContent);
    }

    private String toJson(DataSetLookup lookup) {
        return DataSetLookupJSONMarshaller.get().toJson(lookup).toJson();
    }

    private DataSet parseDataSet(String dataSetJson) {
        return DataSetJSONMarshaller.get().fromJson(dataSetJson);
    }

    private IThenable<?> handleError(Object e) {
        errorCallback.error(DefaultErrorType.OTHER, "Unexpected error retrieving data set metadata:" + e);
        return null;
    }

}