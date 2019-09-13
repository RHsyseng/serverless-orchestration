package org.kiegroup.kogito.serverless.model;

import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.ruleflow.core.factory.WorkItemNodeFactory;
import org.kiegroup.kogito.workitem.handler.LifecycleWorkItemHandler;
import org.kiegroup.kogito.workitem.handler.LogWorkItemHandler;
import org.kiegroup.kogito.workitem.handler.RestWorkItemHandler;
import org.serverless.workflow.api.actions.Action;
import org.serverless.workflow.api.functions.Function;
import org.serverless.workflow.api.interfaces.State;
import org.serverless.workflow.api.states.OperationState;

class OperationNode extends GraphNode {

    private static final String WORKITEM_TYPE = "Type";

    private final OperationState state;

    OperationNode(Graph graph, OperationState state) {
        super(graph);
        this.state = state;
    }

    @Override
    State getState() {
        return state;
    }

    @Override
    void buildNode(RuleFlowProcessFactory factory) {
        Long prevId = this.getId();
        if (OperationState.ActionMode.SEQUENTIAL.equals(this.state.getActionMode())) {
            this.state.getActions().forEach(a -> buildAction(factory, a));
        } else {
            throw new UnsupportedOperationException("Parallel executions are not yet supported");
        }
    }

    private void buildAction(RuleFlowProcessFactory factory, Action a) {
        if (a.getFilter() != null && a.getFilter().getInputPath() != null) {
            buildActionNode(factory, kcontext -> buildInputAction(kcontext, a.getFilter()));
        }
        buildWorkItemNode(factory, a.getFunction());
        if (a.getFilter() != null && a.getFilter().getOutputPath() != null && a.getFilter().getResultPath() != null) {
            buildActionNode(factory, kcontext -> buildOutputAction(kcontext, a.getFilter()));
        }
    }

    private void buildWorkItemNode(RuleFlowProcessFactory factory, Function function) {
        Long prevId = this.getId();
        Long id = this.getNextId();
        String type = null;
        if (function.getMetadata() != null) {
            type = function.getMetadata().get(WORKITEM_TYPE);
        }
        if (type == null) {
            throw new IllegalArgumentException("Type is mandatory in the function metadata for function: " + function.getName());
        }
        WorkItemNodeFactory wi = factory.workItemNode(id)
            .name(function.getName())
            .inMapping(LifecycleWorkItemHandler.PARAM_CONTENT_DATA, JsonModel.DATA_PARAM)
            .outMapping(LifecycleWorkItemHandler.PARAM_RESULT, JsonModel.DATA_PARAM)
            .workName(function.getMetadata().get(LifecycleWorkItemHandler.PARAM_TYPE));
        if (RestWorkItemHandler.HANDLER_NAME.equals(type)) {
            buildRestWorkItem(wi, function);
        } else if (LogWorkItemHandler.HANDLER_NAME.equals(type)) {
            buildLogWorkItem(wi, function);
        } else {
            throw new IllegalArgumentException("Unsupported function type: " + type);
        }
        wi.done();
        connect(factory, prevId, id);
    }

    @Override
    void buildInput(RuleFlowProcessFactory factory) {
        if (this.getState().getFilter() != null && this.getState().getFilter().getInputPath() != null) {
            buildActionNode(factory, kcontext -> buildInputAction(kcontext, this.getState().getFilter()));
        }
    }

    @Override
    void buildOutput(RuleFlowProcessFactory factory) {
        if (this.getState().getFilter() != null && this.getState().getFilter().getOutputPath() != null && this.getState().getFilter().getResultPath() != null) {
            buildActionNode(factory, kcontext -> buildOutputAction(kcontext, this.getState().getFilter()));
        }
    }

    @Override
    String getNextState() {
        return state.getNextState();
    }

    private void buildRestWorkItem(WorkItemNodeFactory wi, Function function) {
        wi.workParameter(RestWorkItemHandler.PARAM_TASK_NAME, RestWorkItemHandler.HANDLER_NAME)
            .workParameter(RestWorkItemHandler.PARAM_CONTENT_TYPE, MediaType.APPLICATION_JSON);
        addWorkParameterFromMetadata(wi, RestWorkItemHandler.PARAM_METHOD, function.getMetadata());
        addWorkParameterFromMetadata(wi, RestWorkItemHandler.PARAM_URL, function.getMetadata());
        addWorkParameterFromMetadata(wi, RestWorkItemHandler.PARAM_CONTENT_TYPE, function.getMetadata());
        //TODO: Implement retry
        //TODO: Implement timeout
    }

    private void buildLogWorkItem(WorkItemNodeFactory wi, Function function) {
        addWorkParameterFromMetadata(wi, LogWorkItemHandler.PARAM_LEVEL, function.getMetadata());
        addWorkParameterFromMetadata(wi, LogWorkItemHandler.PARAM_MESSAGE, function.getMetadata());
        addWorkParameterFromMetadata(wi, LogWorkItemHandler.PARAM_FIELD, function.getMetadata());
    }

    private void addWorkParameterFromMetadata(WorkItemNodeFactory wi, String param, Map<String, String> metadata) {
        if (metadata == null || !metadata.containsKey(param)) {
            return;
        }
        wi.workParameter(param, metadata.get(param));
    }
}
