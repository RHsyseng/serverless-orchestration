package com.redhat.syseng.serverless.orchestrator.process;

import com.redhat.syseng.serverless.orchestrator.model.Message;
import org.kie.api.definition.process.Process;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.events.TriggerEvent;

public class WorkflowProcess extends AbstractProcess<Message> {

    final Process process;

    public WorkflowProcess(Workflow workflow) {
        this.process = WorkflowProcessBuilder.build(workflow);
    }

    @Override
    public Process legacyProcess() {
        return process;
    }

    @Override
    public ProcessInstance<Message> createInstance(Message message) {
        return new WorkflowProcessInstance(this, message, this.createLegacyProcessRuntime());
    }

    @Override
    public ProcessInstance<Message> createInstance(Model m) {
        return this.createInstance((Message) m);
    }

    @Override
    public org.kie.kogito.process.Process<Message> configure() {
        super.configure();
        return this;
    }
}
