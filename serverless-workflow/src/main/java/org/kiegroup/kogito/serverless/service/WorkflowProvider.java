package org.kiegroup.kogito.serverless.service;

import java.util.List;
import java.util.Map;

import org.serverless.workflow.api.Workflow;

public interface WorkflowProvider {

    List<Workflow> getAll();

    Workflow get(String name);
}
