package com.redhat.syseng.serverless.orchestrator.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.syseng.serverless.orchestrator.services.ProcessService;
import com.redhat.syseng.serverless.orchestrator.services.WorkflowService;
import io.cloudevents.v03.CloudEventImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DefaultResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResource.class);

    @Inject
    ProcessService processService;

    @Inject
    WorkflowService workflowService;

    @POST
    public CompletionStage<Response> receiveEvent(CloudEventImpl<JsonObject> event) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("New event received: {} - {}", event.getAttributes().getId(), event.getData().get());
            workflowService.getEventMatches(event.getAttributes().getSource(), event.getAttributes().getType(), event.getData())
                .stream()
                .forEach(processService::receive);
            return Response.accepted().build();
        });
    }

    @GET
    @Path("/instances/{version}")
    public CompletionStage<Response> getInstances(@PathParam("version") String version) {
        return CompletableFuture.supplyAsync(() -> Response.ok(processService.instances(version)).build());
    }
}