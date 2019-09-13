package org.kiegroup.kogito.serverless.process;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.kie.api.definition.process.Process;
import org.kie.kogito.Config;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kiegroup.kogito.serverless.model.JsonModel;
import org.kiegroup.kogito.serverless.service.WorkflowService;

@Singleton
public class WorkflowProcess extends AbstractProcess<JsonModel> {

    @Inject
    WorkflowService workflowService;

    @Inject
    public WorkflowProcess(Config config) {
        super(config.process());
    }

    @Override
    public Process legacyProcess() {
        return workflowService.getProcess();
    }

    @Override
    public ProcessInstance<JsonModel> createInstance(JsonModel value) {
        return new WorkflowProcessInstance(this, value, this.createLegacyProcessRuntime());
    }

    @Override
    public ProcessInstance<JsonModel> createInstance(Model value) {
        return this.createInstance((JsonModel) value);
    }

    @Override
    public org.kie.kogito.process.Process<JsonModel> configure() {
        super.configure();
        return this;
    }
}
