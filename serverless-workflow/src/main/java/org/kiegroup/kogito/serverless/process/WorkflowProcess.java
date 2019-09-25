package org.kiegroup.kogito.serverless.process;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kie.api.definition.process.Process;
import org.kie.kogito.Config;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kiegroup.kogito.serverless.model.WorkflowPayload;
import org.kiegroup.kogito.serverless.service.WorkflowService;

@Singleton
public class WorkflowProcess extends AbstractProcess<WorkflowPayload> {

    private final WorkflowService workflowService;

    @Inject
    public WorkflowProcess(Config config, WorkflowService workflowService) {
        super(config.process());
        this.workflowService = workflowService;
        this.configure();
    }

    @Override
    public Process legacyProcess() {
        return workflowService.getProcess();
    }

    @Override
    public ProcessInstance<WorkflowPayload> createInstance(WorkflowPayload value) {
        return new WorkflowProcessInstance(this, value, this.createLegacyProcessRuntime());
    }

    @Override
    public ProcessInstance<WorkflowPayload> createInstance(Model value) {
        return this.createInstance((WorkflowPayload) value);
    }

    @Override
    public org.kie.kogito.process.Process<WorkflowPayload> configure() {
        super.configure();
        return this;
    }
}
