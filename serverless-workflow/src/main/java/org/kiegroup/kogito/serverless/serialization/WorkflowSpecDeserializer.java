package org.kiegroup.kogito.serverless.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.kiegroup.kogito.serverless.k8s.model.WorkflowSpec;

@RegisterForReflection
public class WorkflowSpecDeserializer extends StdDeserializer<WorkflowSpec> {

    public WorkflowSpecDeserializer() {
        this(null);
    }

    public WorkflowSpecDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public WorkflowSpec deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        WorkflowSpec spec = new WorkflowSpec();
        TreeNode tn = p.readValueAsTree();
        String definition = tn.get("definition").toString();
        spec.setDefinition(definition);
        return spec;
    }
}
