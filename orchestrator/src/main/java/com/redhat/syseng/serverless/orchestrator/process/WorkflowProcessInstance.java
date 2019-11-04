package com.redhat.syseng.serverless.orchestrator.process;

import java.util.Map;

import com.redhat.syseng.serverless.orchestrator.model.Message;
import org.kie.api.runtime.process.ProcessRuntime;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.process.impl.AbstractProcessInstance;

public class WorkflowProcessInstance extends AbstractProcessInstance<Message> {

    public WorkflowProcessInstance(AbstractProcess<Message> process, Message variables, ProcessRuntime processRuntime) {
        super(process, variables, processRuntime);
    }

    @Override
    protected Map<String, Object> bind(Message variables) {
        return variables.toMap();
    }

    @Override
    protected void unbind(Message variables, Map<String, Object> vmap) {
        variables.fromMap(vmap);
    }
}
