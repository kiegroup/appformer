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

import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.json.DataSetMetadataJSONMarshaller;

@Provider
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class DataSetMetadaWriter implements MessageBodyWriter<DataSetMetadata> {

    @Inject
    DataSetMetadataJSONMarshaller marshaller;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return DataSetMetadata.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(DataSetMetadata t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        String json = marshaller.toJson(t);
        entityStream.write(json.getBytes());
    }

}
