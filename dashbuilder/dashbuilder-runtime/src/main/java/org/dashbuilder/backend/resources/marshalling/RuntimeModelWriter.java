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

import org.dashbuilder.shared.marshalling.RuntimeModelJSONMarshaller;
import org.dashbuilder.shared.model.RuntimeModel;

@Provider
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class RuntimeModelWriter implements MessageBodyWriter<RuntimeModel> {
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RuntimeModel.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(RuntimeModel runtimeModel,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        String json = RuntimeModelJSONMarshaller.get().toJson(runtimeModel).toJson();
        entityStream.write(json.getBytes());
    }

}
