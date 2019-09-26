package org.kiegroup.kogito.serverless.process;

import java.util.Map;

import org.kie.api.runtime.process.ProcessRuntime;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kiegroup.kogito.serverless.model.WorkflowPayload;

public class WorkflowProcessInstance extends AbstractProcessInstance<WorkflowPayload> {

    public WorkflowProcessInstance(AbstractProcess<WorkflowPayload> process, WorkflowPayload variables, ProcessRuntime processRuntime) {
        super(process, variables, processRuntime);
    }

    @Override
    protected Map<String, Object> bind(WorkflowPayload variables) {
        return variables.toMap();
    }

    @Override
    protected void unbind(WorkflowPayload variables, Map<String, Object> vmap) {
        variables.fromMap(vmap);
    }
}
