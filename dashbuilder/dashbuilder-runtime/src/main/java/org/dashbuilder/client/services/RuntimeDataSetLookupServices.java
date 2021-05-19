package org.dashbuilder.client.services;

import javax.inject.Inject;

import elemental2.dom.XMLHttpRequest;
import org.dashbuilder.client.error.ErrorResponseVerifier;
import org.dashbuilder.client.marshalling.ClientDataSetMetadataJSONMarshaller;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.json.DataSetJSONMarshaller;
import org.dashbuilder.dataset.json.DataSetLookupJSONMarshaller;
import org.dashbuilder.dataset.service.DataSetLookupServices;

// TODO: Removeme if everything goes wrong
public class RuntimeDataSetLookupServices implements DataSetLookupServices {

    @Inject
    ClientDataSetMetadataJSONMarshaller dataSetMetadataJsonMarshaller;
    
    @Inject
    ErrorResponseVerifier verifier;
    

    @Override
    public DataSet lookupDataSet(DataSetLookup lookup) throws Exception {
        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.open("POST", "/rest/dataset/lookup", false);
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send(toJson(lookup));
        verifier.verify(xhr);
        return parseDataSet(xhr.responseText);
    }

    @Override
    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        return lookupDataSet(lookup);
    }

    @Override
    public DataSetMetadata lookupDataSetMetadata(String uuid) throws Exception {
        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.open("GET", "/rest/dataset/" + uuid + "/metadata", false);
        xhr.send();
        verifier.verify(xhr);
        DataSetMetadata metadata = parseMetadata(xhr.responseText);
        return metadata;
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

}