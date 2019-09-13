package org.kiegroup.kogito.serverless.resource;

import java.util.Optional;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kie.kogito.Config;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceExecutionException;
import org.kie.kogito.process.ProcessInstances;
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
    public Optional<? extends ProcessInstance<? extends Model>> getProcess(@PathParam("id") String id) {
        return process.instances().findById(id);
    }

    @GET
    public ProcessInstances<? extends Model> getAll() {
        return process.instances();
    }

    @POST
    @Path("/{id}")
    public JsonModel update(@PathParam("id") String id, JsonObject data) {
        return UnitOfWorkExecutor.executeInUnitOfWork(config.process().unitOfWorkManager(), () -> {
            JsonModel model = JsonModel.newInstance(data).setId(id);
            Optional<? extends ProcessInstance<JsonModel>> pi = process.instances().findById(id);
            if (pi.isPresent()) {
                pi.get().updateVariables(model);
                getModel(pi.get());
            }
            return null;
        });
    }

    protected JsonModel getModel(ProcessInstance<JsonModel> pi) {
        if(pi.status() == ProcessInstance.STATE_ERROR && pi.error().isPresent()) {
            throw new ProcessInstanceExecutionException(pi.id(), pi.error().get().failedNodeId(),pi.error().get().errorMessage());
        }
        return pi.variables();
    }
}
