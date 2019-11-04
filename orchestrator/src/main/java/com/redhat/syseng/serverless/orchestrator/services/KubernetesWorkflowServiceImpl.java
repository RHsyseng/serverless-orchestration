package com.redhat.syseng.serverless.orchestrator.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;

import com.redhat.syseng.serverless.orchestrator.model.EventMatch;
import com.redhat.syseng.serverless.orchestrator.process.WorkflowListener;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.WorkflowManager;
import org.serverless.workflow.impl.manager.WorkflowManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class KubernetesWorkflowServiceImpl implements WorkflowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesWorkflowServiceImpl.class);

    public static final String CONFIGMAP_SELECTOR_KEY = "workflow-name";
    public static final String CONFIGMAP_WORKFLOW_FILE = "workflow.json";
    public static final String CONFIGMAP_WORKFLOW_VERSION_LABEL = "workflow-version";

    @ConfigProperty(name = "workflow.name")
    String workflowName;

    @ConfigProperty(name = "quarkus.kubernetes-client.namespace")
    String namespace;

    final Map<String, Workflow> workflows = new HashMap<>();
    final Collection<WorkflowListener> listeners = new ArrayList<>();

    KubernetesClient client;
    String latestVersion;

    @PostConstruct
    public void init() {
        Config config = new ConfigBuilder().withNamespace(namespace).build();
        client = new DefaultKubernetesClient(config);
        LOGGER.debug("Watching for ConfigMaps changes");
        client.configMaps()
            .inNamespace(client.getNamespace())
            .withLabel(CONFIGMAP_SELECTOR_KEY, workflowName)
            .watch(new ConfigMapWatcher());
    }

    @Override
    public String getName() {
        return workflowName;
    }

    @Override
    public Workflow get(String version) {
        LOGGER.debug("Get {}/{}", workflowName, version);
        return workflows.get(version);
    }

    @Override
    public Workflow getLatest() {
        return workflows.get(latestVersion);
    }

    @Override
    public String getLatestVersion() {
        return latestVersion;
    }

    @Override
    public List<EventMatch> getEventMatches(URI source, String type, Optional<JsonObject> data) {
        return getLatest().getTriggerDefs()
            .stream()
            .filter(t ->
                        matchesValue(t.getSource(), source.toString()) &&
                            matchesValue(t.getType(), type) &&
                            matchesCorrelationToken(t.getCorrelationToken(), data))
            .map(t -> new EventMatch(t, data.get()))
            .collect(Collectors.toList());
    }

    private boolean matchesValue(String expected, String received) {
        return expected == null || expected.equalsIgnoreCase(received);
    }

    private boolean matchesCorrelationToken(String path, Optional<JsonObject> data) {
        if (path == null) {
            return true;
        }
        if (!data.isPresent()) {
            return false;
        }
        return JsonPathUtils.getCorrelationToken(path, data.get()) != null;
    }

    @Override
    public void registerListener(WorkflowListener listener) {
        listeners.add(listener);
    }

    void load(ConfigMap configMap) {
        if (!configMap.getData().containsKey(CONFIGMAP_WORKFLOW_FILE)) {
            LOGGER.warn("Missing workflow spec. Expected file: {}", CONFIGMAP_WORKFLOW_FILE);
            return;
        }
        String workflowSpec = configMap.getData().get(CONFIGMAP_WORKFLOW_FILE);
        WorkflowManager workflowManager = new WorkflowManagerImpl().setMarkup(workflowSpec);
        String version = configMap.getMetadata().getLabels().get(CONFIGMAP_WORKFLOW_VERSION_LABEL);
        if (workflowManager.getWorkflowValidator().isValid()) {
            addWorkflow(version, workflowManager.getWorkflow());
            LOGGER.debug("Added workflow from configmap {} with name {} - version {}",
                         configMap.getMetadata().getName(),
                         workflowManager.getWorkflow().getName(),
                         version);
        } else {
            LOGGER.info("Ignored workflow in ConfigMap {}. Has validation errors: {}",
                        configMap.getMetadata().getName(), workflowManager.getWorkflowValidator().validate());
        }
    }

    private void addWorkflow(String version, Workflow workflow) {
        workflows.put(version, workflow);
        listeners.stream().forEach(l -> l.onCreateOrUpdate(version, workflow));
    }

    void delete(ConfigMap configMap) {
        String version = configMap.getMetadata().getLabels().get(CONFIGMAP_WORKFLOW_VERSION_LABEL);
        workflows.remove(version);
        listeners.stream().forEach(l -> l.onDelete(version));
    }

    private class ConfigMapWatcher implements Watcher<ConfigMap> {

        @Override
        public void eventReceived(Action action, ConfigMap configMap) {
            LOGGER.debug("Received action: {} on ConfigMap: {}", action, configMap.getMetadata().getName());
            switch (action) {
                case ADDED:
                case MODIFIED:
                    load(configMap);
                    break;
                case DELETED:
                    delete(configMap);
                    break;
                default:
                    LOGGER.info("Unsupported action received {}", action);
            }
            latestVersion = workflows.keySet().stream().max(String::compareTo).orElse(null);
        }

        @Override
        public void onClose(KubernetesClientException e) {
            LOGGER.warn("Closed configMap watch", e);
        }
    }
}
