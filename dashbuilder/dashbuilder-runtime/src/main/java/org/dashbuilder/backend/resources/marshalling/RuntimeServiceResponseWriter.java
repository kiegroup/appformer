package org.dashbuilder.backend.resources.marshalling;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.dashbuilder.shared.marshalling.RuntimeServiceResponseJSONMarshaller;
import org.dashbuilder.shared.model.RuntimeServiceResponse;

@Provider
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class RuntimeServiceResponseWriter implements MessageBodyWriter<RuntimeServiceResponse> {
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RuntimeServiceResponse.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(RuntimeServiceResponse serviceResponse,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        String json = RuntimeServiceResponseJSONMarshaller.get().toJson(serviceResponse).toJson();
        entityStream.write(json.getBytes());
    }

}
