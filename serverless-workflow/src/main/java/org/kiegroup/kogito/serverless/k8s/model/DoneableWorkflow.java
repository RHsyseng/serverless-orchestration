package org.kiegroup.kogito.serverless.k8s.model;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableWorkflow extends CustomResourceDoneable<Workflow> {

    public DoneableWorkflow(Workflow resource, Function<Workflow, Workflow> function) {
        super(resource, function);
    }
}
