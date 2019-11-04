package com.redhat.syseng.serverless.orchestrator.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesMockServerTestResource;
import io.quarkus.test.kubernetes.client.MockServer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.serverless.workflow.api.Workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTestResource(KubernetesMockServerTestResource.class)
@QuarkusTest
public class KubernetesWorkflowServiceTest {

    @MockServer
    KubernetesMockServer server;

    @Inject
    WorkflowService service;

    @ConfigProperty(name = "workflow.name")
    String workflowName;

    @BeforeEach
    public void init() throws InterruptedException {
        createWorkflowConfigMap("simple", "1");
        createWorkflowConfigMap("simple", "2");
    }

    @AfterEach
    public void stop() {
        server.createClient()
            .configMaps()
            .inNamespace(server.createClient().getNamespace())
            .delete();
    }

    @Test
    public void testNoConfigMapsDeployed() {
        assertNull(service.getLatest());
    }

    @Test
    public void testValidConfigMapIsFound() {
        Workflow workflow = service.get("1");

        assertNotNull(workflow);
        assertEquals("simple-workflow", workflow.getName());

        workflow = service.get("2");
        assertNotNull(workflow);
        assertEquals("simple-workflow", workflow.getName());
    }

    @Test
    @Disabled
    public void testConfigMapsAreWatched() throws InterruptedException {
        createWorkflowConfigMap("simple", "3");

        Workflow workflow = service.get("1");
        assertNotNull(workflow);
        assertEquals("simple-workflow", workflow.getName());

        workflow = service.get("2");
        assertNotNull(workflow);
        assertEquals("simple-workflow", workflow.getName());

        workflow = service.get("3");
        assertNotNull(workflow);
        assertEquals("simple-workflow", workflow.getName());
    }

    private ConfigMap createWorkflowConfigMap(String name, String version) {
        ConfigMap cm = new ConfigMapBuilder()
            .withNewMetadata()
            .withName(name)
            .addToLabels(KubernetesWorkflowServiceImpl.CONFIGMAP_SELECTOR_KEY, workflowName)
            .addToLabels(KubernetesWorkflowServiceImpl.CONFIGMAP_WORKFLOW_VERSION_LABEL, version)
            .endMetadata()
            .addToData(KubernetesWorkflowServiceImpl.CONFIGMAP_WORKFLOW_FILE, getWorkflowSpec(name))
            .build();
        return server.createClient().configMaps().inNamespace(server.createClient().getNamespace()).create(cm);
    }

    private String getWorkflowSpec(String name) {
        URL file = this.getClass().getClassLoader().getResource("workflows/" + name + ".json");
        try {
            return Files.lines(Paths.get(file.toURI())).collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException | URISyntaxException e) {
            fail("Unable to read workflow from file", e);
            return null;
        }
    }
}
