package org.kiegroup.kogito.serverless.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class CloudEventMessageBodyReader implements MessageBodyReader<CloudEvent<JsonObject>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudEventMessageBodyReader.class);
    public static final String EVENT_SOURCE_HEADER = "ce-source";
    public static final String EVENT_ID_HEADER = "ce-id";
    public static final String EVENT_TYPE_HEADER = "ce-type";
    public static final String EVENT_SPECVERSION_HEADER = "ce-specversion";

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == CloudEvent.class;
    }

    @Override
    public CloudEvent<JsonObject> readFrom(Class<CloudEvent<JsonObject>> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        CloudEventBuilder<JsonObject> builder = new CloudEventBuilder<>();
        httpHeaders.forEach((header, values) -> setEventValue(header, values, builder));
        builder.data(Json.createReader(entityStream).readObject());
        CloudEvent<JsonObject> event = builder.build();
        LOGGER.debug("Parsed CloudEvent: {}", event);
        return event;
    }

    private CloudEventBuilder setEventValue(String header, List<String> values, CloudEventBuilder builder) {
        header = header.toLowerCase();
        if (values == null || values.isEmpty() || values.size() != 1) {
            return builder;
        }
        String value = values.get(0);
        switch (header) {
            case EVENT_ID_HEADER:
                return builder.id(value);
            case EVENT_SPECVERSION_HEADER:
                return builder.specVersion(value);
            case EVENT_SOURCE_HEADER:
                return builder.source(URI.create(value));
            case EVENT_TYPE_HEADER:
                return builder.type(value);
            default:
                return builder;
        }
    }
}
