package org.dashbuilder.backend.resources.marshalling;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.dashbuilder.backend.services.dataset.provider.RuntimeDataSetProviderRegistry;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.json.DataSetJSONMarshaller;

@Provider
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class DataSetWriter implements MessageBodyWriter<DataSet> {

    @Inject
    RuntimeDataSetProviderRegistry runtimeDataSetProviderRegistry;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return DataSet.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(DataSet t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        DataSetJSONMarshaller dataSetJSONMarshaller = DataSetJSONMarshaller.get();
        String json = dataSetJSONMarshaller.toJson(t).toJson();
        entityStream.write(json.getBytes());
    }

}
