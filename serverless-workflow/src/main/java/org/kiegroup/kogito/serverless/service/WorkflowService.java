package org.kiegroup.kogito.serverless.service;

import org.kie.api.definition.process.Process;
import org.serverless.workflow.api.Workflow;

public interface WorkflowService {

    Workflow get();

    Process getProcess();

    String getProcessId();
}
