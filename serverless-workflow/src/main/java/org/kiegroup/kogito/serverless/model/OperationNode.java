package org.kiegroup.kogito.serverless.model;

import java.util.Map;

import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.ruleflow.core.factory.HumanTaskNodeFactory;
import org.jbpm.ruleflow.core.factory.WorkItemNodeFactory;
import org.kiegroup.kogito.workitem.handler.BaseWorkItemHandler;
import org.kiegroup.kogito.workitem.handler.LogWorkItemHandler;
import org.kiegroup.kogito.workitem.handler.RestWorkItemHandler;
import org.serverless.workflow.api.actions.Action;
import org.serverless.workflow.api.functions.Function;
import org.serverless.workflow.api.interfaces.State;
import org.serverless.workflow.api.states.OperationState;

class OperationNode extends GraphNode {

    private static final String HUMAN_TASK_HANDLER_NAME = "HumanTask";

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
        String type = function.getType();
        if(HUMAN_TASK_HANDLER_NAME.equals(type)) {
            buildHumanTaskNode(factory.humanTaskNode(id), function);
        } else {
            WorkItemNodeFactory wi = factory.workItemNode(id)
                .name(function.getName())
                .inMapping(BaseWorkItemHandler.PARAM_CONTENT_DATA, WorkflowPayload.DATA_PARAM)
                .outMapping(BaseWorkItemHandler.PARAM_RESULT, WorkflowPayload.DATA_PARAM)
                .workName(function.getType());
            if (RestWorkItemHandler.HANDLER_NAME.equals(type)) {
                buildRestWorkItem(wi, function);
            } else if (LogWorkItemHandler.HANDLER_NAME.equals(type)) {
                buildLogWorkItem(wi, function);
            } else {
                throw new IllegalArgumentException("Unsupported function type: " + type);
            }
            wi.done();
        }
        connect(factory, prevId, id);
    }

    private void buildHumanTaskNode(HumanTaskNodeFactory factory, Function function) {
        factory.name(function.getName())
            .workParameter("Locale", "en-UK")
            .workParameter("TaskName", function.getName())
            .workParameter("Skippable", "true")
            .workParameter("Priority", "1")
            .inMapping(WorkflowPayload.DATA_PARAM, WorkflowPayload.DATA_PARAM)
            .outMapping(WorkflowPayload.DATA_PARAM, WorkflowPayload.DATA_PARAM)
            .done();
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
        wi.workParameter(RestWorkItemHandler.PARAM_TASK_NAME, RestWorkItemHandler.HANDLER_NAME);
        addWorkParameter(wi, RestWorkItemHandler.PARAM_METHOD, function.getParameters());
        addWorkParameter(wi, RestWorkItemHandler.PARAM_URL, function.getParameters());
        //TODO: Implement retry
        //TODO: Implement timeout
    }

    private void buildLogWorkItem(WorkItemNodeFactory wi, Function function) {
        addWorkParameter(wi, LogWorkItemHandler.PARAM_LEVEL, function.getParameters());
        addWorkParameter(wi, LogWorkItemHandler.PARAM_MESSAGE, function.getParameters());
        addWorkParameter(wi, LogWorkItemHandler.PARAM_FIELD, function.getParameters());
    }

    private void addWorkParameter(WorkItemNodeFactory wi, String param, Map<String, String> metadata) {
        if (metadata == null || !metadata.containsKey(param)) {
            return;
        }
        wi.workParameter(param, metadata.get(param));
    }
}
