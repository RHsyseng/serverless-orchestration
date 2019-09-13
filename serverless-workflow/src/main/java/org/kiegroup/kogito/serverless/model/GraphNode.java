package org.kiegroup.kogito.serverless.model;

import javax.json.JsonObject;

import org.jbpm.process.instance.impl.Action;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.kie.api.runtime.process.ProcessContext;
import org.kiegroup.kogito.workitem.handler.utils.JsonPath;
import org.serverless.workflow.api.filters.Filter;
import org.serverless.workflow.api.interfaces.State;

public abstract class GraphNode {

    final JsonPath jsonPath = new JsonPath();
    final Graph graph;
    private final Long headerId;
    private Long id = null;

    GraphNode(Graph graph) {
        this.graph = graph;
        this.headerId = graph.getNextId();
    }

    public Long getHeaderId() {
        return headerId;
    }


    void build(RuleFlowProcessFactory factory) {
        buildStart(factory);
        buildInput(factory);
        buildNode(factory);
        buildOutput(factory);
    }

    void connectNextState(RuleFlowProcessFactory factory) {
        if(getNextState() != null) {
            connect(factory, this.id, this.graph.getNodeId(getNextState()));
        }
    }

    void connect(RuleFlowProcessFactory factory, Long from, Long to) {
        if(from != null && to != null) {
            factory.connection(from, to, "Flow_" + from + "_" + to);
        }
    }

    abstract State getState();

    abstract String getNextState();

    Long getId() {
        return id;
    }

    Long getNextId() {
        if(this.id == null) {
            this.id = this.headerId;
        } else {
            this.id = this.graph.getNextId();
        }
        return this.id;
    }

    void buildNode(RuleFlowProcessFactory factory) {
    }

    void buildInput(RuleFlowProcessFactory factory) {
    }

    void buildOutput(RuleFlowProcessFactory factory) {

    }

    private void buildStart(RuleFlowProcessFactory factory) {
        if(getState().isStart()) {
            Long id = this.getNextId();
            factory.startNode(id)
                .done();
        }
    }

    void buildActionNode(RuleFlowProcessFactory factory, Action action) {
        Long prevId = this.getId();
        Long id = this.getNextId();
        factory.actionNode(id)
            .action(action)
            .done();
        connect(factory, prevId, id);
    }

    void buildInputAction(ProcessContext kcontext, Filter filter) {
        JsonObject data = (JsonObject) kcontext.getVariable(JsonModel.DATA_PARAM);
        kcontext.setVariable(Graph.BACKUP_DATA_VAR, data);
        Object result = jsonPath.filter(data, filter.getInputPath());
        kcontext.setVariable(JsonModel.DATA_PARAM, result);
    }

    void buildOutputAction(ProcessContext kcontext, Filter filter) {
        JsonObject data = (JsonObject) kcontext.getVariable(JsonModel.DATA_PARAM);
        Object result = jsonPath.filter(data, filter.getResultPath());
        JsonObject backup = (JsonObject) kcontext.getVariable(Graph.BACKUP_DATA_VAR);
        JsonObject newData = jsonPath.set(backup, filter.getOutputPath(), result);
        kcontext.setVariable(JsonModel.DATA_PARAM, newData);
    }
}
