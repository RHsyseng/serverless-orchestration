package org.kiegroup.kogito.serverless.process;

import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.kogito.Model;
import org.kiegroup.kogito.serverless.service.WorkflowService;

@ApplicationScoped
public class Processes implements org.kie.kogito.process.Processes {

    @Inject
    WorkflowProcess process;

    @Inject
    WorkflowService service;

    @Override
    public org.kie.kogito.process.Process<? extends Model> processById(String processId) {
        if (service.getProcessId().equals(processId)) {
            return process;
        }
        return null;
    }

    @Override
    public Collection<String> processIds() {
        return Arrays.asList(service.getProcessId());
    }
}
