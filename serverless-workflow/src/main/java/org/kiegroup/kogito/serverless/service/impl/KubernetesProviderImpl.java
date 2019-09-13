package org.kiegroup.kogito.serverless.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kiegroup.kogito.serverless.k8s.model.DoneableWorkflow;
import org.kiegroup.kogito.serverless.k8s.model.WorkflowList;
import org.kiegroup.kogito.serverless.service.WorkflowProvider;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.mapper.WorkflowObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Named("k8s")
public class KubernetesProviderImpl implements WorkflowProvider {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesProviderImpl.class);
    private static final String ENV_WORKFLOW_LABELS = "workflow-labels";

    static final String SOURCE = "k8s";

    @ConfigProperty(name = ENV_WORKFLOW_LABELS)
    Optional<Map<String, String>> workflowLabels;

    private final WorkflowObjectMapper mapper = new WorkflowObjectMapper();
    private KubernetesClient client;

    @PostConstruct
    public void init() {
        client = new DefaultKubernetesClient();
    }

    @Override
    public List<Workflow> getAll() {
        return client.customResources(org.kiegroup.kogito.serverless.k8s.model.Workflow.CUSTOM_RESOURCE_DEFINITION,
                                      org.kiegroup.kogito.serverless.k8s.model.Workflow.class,
                                      WorkflowList.class,
                                      DoneableWorkflow.class)
            .inNamespace(client.getNamespace())
            .withLabels(workflowLabels.orElse(null))
            .list()
            .getItems()
            .stream()
            .map(this::buildWorkflow)
            .collect(Collectors.toList());
    }

    @Override
    public Workflow get(String name) {
        logger.debug("Fetching definition from k8s resource: {}", name);
        return buildWorkflow(client.customResources(org.kiegroup.kogito.serverless.k8s.model.Workflow.CUSTOM_RESOURCE_DEFINITION,
                                                    org.kiegroup.kogito.serverless.k8s.model.Workflow.class,
                                                    WorkflowList.class,
                                                    DoneableWorkflow.class)
                                 .inNamespace(client.getNamespace())
                                 .withName(name)
                                 .get());
    }

    private Workflow buildWorkflow(org.kiegroup.kogito.serverless.k8s.model.Workflow workflow) {
        if (workflow == null) {
            logger.warn("Workflow resource not found");
            return null;
        }
        if (workflow.getSpec() == null || workflow.getSpec().getDefinition() == null) {
            logger.warn("Workflow definition not found in provided resource");
            return null;
        }
        try {
            return mapper.readValue(workflow.getSpec().getDefinition(), Workflow.class);
        } catch (IOException e) {
            logger.error("Unable to read workflow definition", e);
        }
        return null;
    }
}
