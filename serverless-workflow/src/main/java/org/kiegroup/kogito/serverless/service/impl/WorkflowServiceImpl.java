package org.kiegroup.kogito.serverless.service.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.api.definition.process.Process;
import org.kiegroup.kogito.serverless.model.Graph;
import org.kiegroup.kogito.serverless.service.WorkflowProvider;
import org.kiegroup.kogito.serverless.service.WorkflowService;
import org.serverless.workflow.api.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class WorkflowServiceImpl implements WorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);

    private static final String ENV_WORKFLOW_SOURCE = "workflow-source";
    private static final String ENV_WORKFLOW_NAME = "workflow-name";

    private Workflow workflow;
    private Graph graph;
    private Process process;
    private String processId;

    @Inject
    @Named(KubernetesProviderImpl.SOURCE)
    WorkflowProvider k8sProvider;

    @Inject
    @Named(FileWorkflowProviderImpl.SOURCE)
    WorkflowProvider fileProvider;

    @ConfigProperty(
        name = ENV_WORKFLOW_SOURCE,
        defaultValue = FileWorkflowProviderImpl.SOURCE
    )
    String workflowSource;

    @ConfigProperty(name = ENV_WORKFLOW_NAME)
    Optional<String> workflowName;

    void onInit(@Observes StartupEvent event) {
        Workflow workflow = null;
        if (!workflowName.isPresent()) {
            throw new IllegalArgumentException("Missing required environment variable: " + ENV_WORKFLOW_NAME);
        }
        switch (workflowSource) {
            case FileWorkflowProviderImpl.SOURCE:
                workflow = fileProvider.get(workflowName.get());
                break;
            case KubernetesProviderImpl.SOURCE:
                workflow = k8sProvider.get(workflowName.get());
                break;
            default:
                throw new IllegalArgumentException("Unsupported process source configured: " + workflowSource);
        }
        if (workflow == null) {
            throw new IllegalStateException("Unable to load a valid process definition");
        }
        updateWorkflow(workflow);
    }

    @Override
    public Workflow get() {
        return workflow;
    }

    @Override
    public Process getProcess() {
        return process;
    }

    @Override
    public String getProcessId() {
        return processId;
    }

    private void updateWorkflow(Workflow workflow) {
        this.workflow = workflow;
        this.graph = new Graph(workflow);
        this.process = graph.getProcess();
        this.processId = graph.getProcessName();
    }

}
