package org.kiegroup.kogito.serverless.serialization;

import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.kie.kogito.process.WorkItem;
import org.kiegroup.kogito.serverless.process.WorkflowProcessInstance;

@RegisterForReflection
public class WorkflowProcessInstanceSerializer extends StdSerializer<WorkflowProcessInstance> {

    public WorkflowProcessInstanceSerializer() {
        this(null);
    }

    public WorkflowProcessInstanceSerializer(Class<WorkflowProcessInstance> t) {
        super(t);
    }

    @Override
    public void serialize(WorkflowProcessInstance pi, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("id", pi.id());
        gen.writeObjectField("status", pi.status());
        gen.writeObjectField("data", Json.createReader(new StringReader(pi.variables().getData())).readObject());
        gen.writeStringField("state", pi.variables().getStatus());
        gen.writeObjectField("workItems", pi.workItems().stream().collect(Collectors.toMap(WorkItem::getId, WorkItem::getName)));
        gen.writeEndObject();
    }
}
