package org.kiegroup.kogito.serverless.model.marshaller;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
public class JsonObjectMarshallerProducer {

    @Produces
    JsonModelMarshaller jsonObjectMarshaller() {
        return new JsonModelMarshaller();
    }
}
