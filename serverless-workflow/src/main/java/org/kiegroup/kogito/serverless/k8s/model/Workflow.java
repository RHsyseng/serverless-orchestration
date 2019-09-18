package org.kiegroup.kogito.serverless.k8s.model;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.client.CustomResource;

public class Workflow extends CustomResource {

    public static final CustomResourceDefinition CUSTOM_RESOURCE_DEFINITION = new CustomResourceDefinitionBuilder()
        .withApiVersion("apiextensions.k8s.io/v1beta1")
        .withNewMetadata().withName("workflows.app.kiegroup.org").endMetadata()
        .withNewSpec().withGroup("app.kiegroup.org").withVersion("v1alpha1").withScope("Namespaced")
        .withNewNames().withKind("Workflow")
        .withShortNames("workflow").withPlural("workflows").endNames()
        .endSpec()
        .build();

    private WorkflowSpec spec;

    public WorkflowSpec getSpec() {
        return spec;
    }

    public void setSpec(WorkflowSpec spec) {
        this.spec = spec;
    }
}
