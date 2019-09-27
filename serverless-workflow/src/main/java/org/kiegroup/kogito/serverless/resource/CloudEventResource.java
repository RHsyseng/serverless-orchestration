package org.kiegroup.kogito.serverless.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.cloudevents.CloudEvent;
import org.kie.kogito.Config;
import org.kie.kogito.process.ProcessInstance;
import org.kiegroup.kogito.serverless.model.WorkflowPayload;
import org.kiegroup.kogito.serverless.process.WorkflowProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CloudEventResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudEventResource.class);

    @Inject
    Config config;

    @Inject
    WorkflowProcess process;

    @POST
    public CompletionStage<Response> onCloudEvent(CloudEvent<JsonObject> event) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Cloud event received {}", event);
            if (event.getData().isPresent()) {
                ProcessInstance<WorkflowPayload> pi = process.createInstance(WorkflowPayload.newInstance(event.getData().get().toString()));
                pi.start();
                LOGGER.info("Process Instance started with ID: {}", pi.id());
                return Response.accepted().build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        });
    }
}
