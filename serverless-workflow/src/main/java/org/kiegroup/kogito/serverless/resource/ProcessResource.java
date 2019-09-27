package org.kiegroup.kogito.serverless.resource;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbpm.process.instance.impl.humantask.HumanTaskTransition;
import org.kie.kogito.Config;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceExecutionException;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;
import org.kiegroup.kogito.serverless.model.WorkflowPayload;
import org.kiegroup.kogito.serverless.process.WorkflowProcess;

@Path("/process")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcessResource {

    @Inject
    Config config;

    @Inject
    WorkflowProcess process;

    @POST
    public CompletionStage<Response> createInstance(final String data) {
        return CompletableFuture.supplyAsync(() -> UnitOfWorkExecutor.executeInUnitOfWork(config.process().unitOfWorkManager(), () -> {
            ProcessInstance<WorkflowPayload> pi = process.createInstance(WorkflowPayload.newInstance(data));
            pi.start();
            WorkflowPayload payload = getModel(pi);
            return Response.ok(payload).header(HttpHeaders.LOCATION, "/process/" + pi.id()).build();
        }));
    }

    @GET
    @Path("/{id}")
    public CompletionStage<Response> getProcess(@PathParam("id") String id) {
        return CompletableFuture.supplyAsync(() -> Response.ok(process.instances()
                                                                   .findById(id)
                                                                   .orElseThrow(() -> new NotFoundException(id))).build()
        );
    }

    @GET
    public CompletionStage<Collection<String>> getAll() {
        return CompletableFuture.supplyAsync(() -> process.instances()
            .values().stream()
            .map(ProcessInstance::id)
            .collect(Collectors.toList()));
    }

    @POST
    @Path("/{id}")
    public CompletionStage<WorkflowPayload> update(@PathParam("id") String id, String data) {
        return CompletableFuture.supplyAsync(() -> UnitOfWorkExecutor.executeInUnitOfWork(config.process().unitOfWorkManager(), () -> {
            WorkflowPayload model = WorkflowPayload.newInstance(data);
            ProcessInstance<WorkflowPayload> pi = process.instances()
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Missing process instance with the given ID: " + id));
            pi.updateVariables(model);
            return getModel(pi);
        }));
    }

    @GET
    @Path("/{id}/tasks")
    public CompletionStage<Map<String, String>> getTasks(@PathParam("id") String id) {
        return CompletableFuture.supplyAsync(() -> process.instances()
            .findById(id)
            .map(var -> var.workItems())
            .map(var -> var.stream().collect(Collectors.toMap(WorkItem::getId, WorkItem::getName)))
            .orElseThrow(() -> new NotFoundException(id))
        );
    }

    @POST
    @Path("/{id}/tasks/{taskId}")
    public CompletionStage<WorkflowPayload> completeTask(@PathParam("id") String id, @PathParam("taskId") String taskId, String data) {
        return CompletableFuture.supplyAsync(() -> UnitOfWorkExecutor.executeInUnitOfWork(config.process().unitOfWorkManager(), () -> {
            ProcessInstance<WorkflowPayload> pi = process.instances()
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Missing process instance with the given ID: " + id));
            WorkflowPayload model = WorkflowPayload.newInstance(pi.variables()).setData(data);
            HumanTaskTransition transition = new HumanTaskTransition("complete", model.toMap());
            pi.transitionWorkItem(taskId, transition);
            return getModel(pi);
        }));
    }

    private WorkflowPayload getModel(ProcessInstance<WorkflowPayload> pi) {
        if (pi.status() == ProcessInstance.STATE_ERROR && pi.error().isPresent()) {
            throw new ProcessInstanceExecutionException(pi.id(), pi.error().get().failedNodeId(), pi.error().get().errorMessage());
        }
        return pi.variables();
    }
}
