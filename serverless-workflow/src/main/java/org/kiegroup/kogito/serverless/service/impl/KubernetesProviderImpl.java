package org.kiegroup.kogito.serverless.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kiegroup.kogito.serverless.k8s.model.DoneableWorkflow;
import org.kiegroup.kogito.serverless.k8s.model.WorkflowList;
import org.kiegroup.kogito.serverless.service.WorkflowProvider;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.api.validation.ValidationError;
import org.serverless.workflow.spi.WorkflowManagerProvider;
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

    private final WorkflowManager manager = WorkflowManagerProvider.getInstance().get();
    private KubernetesClient client;

    @PostConstruct
    public void init() {
        Config config = new ConfigBuilder().withNamespace("default").build();
        client = new DefaultKubernetesClient(config);
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
        Workflow result = manager.setMarkup(workflow.getSpec().getDefinition()).getWorkflow();
        if (result.getName() == null) {
            result.setName(workflow.getMetadata().getName());
        }
        List<ValidationError> validationErrors = manager.getWorkflowValidator()
            .validate();
        if (validationErrors.isEmpty()) {
            return manager.getWorkflow();
        }
        logger.error("Invalid workflow provided: {}", validationErrors);
        throw new IllegalArgumentException("Invalid workflow provided");
    }
}
