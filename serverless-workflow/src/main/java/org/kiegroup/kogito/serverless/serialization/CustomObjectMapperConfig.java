package org.kiegroup.kogito.serverless.serialization;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import org.kiegroup.kogito.serverless.process.WorkflowProcessInstance;

@ApplicationScoped
public class CustomObjectMapperConfig {

    @Singleton
    @Produces
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(WorkflowProcessInstance.class, new WorkflowProcessInstanceSerializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.registerModule(new JSR353Module());

        return objectMapper;
    }
}
