package org.kiegroup.kogito.serverless.resource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jbpm.process.instance.impl.humantask.HumanTaskTransition;
import org.kie.kogito.Config;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceExecutionException;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;
import org.kiegroup.kogito.serverless.model.JsonModel;
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
    public JsonModel createInstance(JsonObject data) {
        return UnitOfWorkExecutor.executeInUnitOfWork(config.process().unitOfWorkManager(), () -> {
            ProcessInstance<JsonModel> pi = process.createInstance(JsonModel.newInstance(data));
            pi.start();
            return getModel(pi);
        });
    }

    @GET
    @Path("/{id}")
    public ProcessInstance<? extends Model> getProcess(@PathParam("id") String id) {
        return process.instances().findById(id).orElseThrow(() -> new NotFoundException(id));
    }

    @GET
    public List<JsonModel> getAll() {
        return process.instances().values().stream().map(ProcessInstance::variables).collect(Collectors.toList());
    }

    @POST
    @Path("/{id}")
    public JsonModel update(@PathParam("id") String id, JsonObject data) {
        return UnitOfWorkExecutor.executeInUnitOfWork(config.process().unitOfWorkManager(), () -> {
            JsonModel model = JsonModel.newInstance(data).setId(id);
            ProcessInstance<JsonModel> pi = process.instances()
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Missing process instance with the given ID: " + id));
            pi.updateVariables(model);
            return getModel(pi);
        });
    }

    @GET
    @Path("/{id}/tasks")
    public Map<String, String> getTasks(@PathParam("id") String id) {
        return process.instances()
            .findById(id)
            .map(var -> ((ProcessInstance) var).workItems())
            .map(var -> (Map<String, String>) var.stream().collect(Collectors.toMap(WorkItem::getId, WorkItem::getName)))
            .orElseThrow(() -> new NotFoundException(id));
    }

    @POST
    @Path("/{id}/tasks/{taskId}")
    public JsonModel completeTask(@PathParam("id") String id, @PathParam("taskId") String taskId, JsonObject data) {
        return UnitOfWorkExecutor.executeInUnitOfWork(config.process().unitOfWorkManager(), () -> {
            ProcessInstance<JsonModel> pi = process.instances()
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Missing process instance with the given ID: " + id));
            JsonModel model = JsonModel.newInstance(pi.variables()).setData(data);
            HumanTaskTransition transition = new HumanTaskTransition("complete", model.toMap());
            pi.transitionWorkItem(taskId, transition);
            return getModel(pi);
        });
    }

    protected JsonModel getModel(ProcessInstance<JsonModel> pi) {
        if (pi.status() == ProcessInstance.STATE_ERROR && pi.error().isPresent()) {
            throw new ProcessInstanceExecutionException(pi.id(), pi.error().get().failedNodeId(), pi.error().get().errorMessage());
        }
        return pi.variables();
    }
}
