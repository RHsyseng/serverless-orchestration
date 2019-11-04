package com.redhat.syseng.serverless.orchestrator.process;

import org.serverless.workflow.api.Workflow;

public interface WorkflowListener {

    void onCreateOrUpdate(String version, Workflow workflow);

    void onDelete(String version);
}
