package org.kiegroup.kogito.serverless.model;

import java.util.List;

import javax.json.JsonObject;

import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.ruleflow.core.factory.SplitFactory;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.kie.api.runtime.process.ProcessContext;
import org.serverless.workflow.api.choices.AndChoice;
import org.serverless.workflow.api.choices.DefaultChoice;
import org.serverless.workflow.api.choices.NotChoice;
import org.serverless.workflow.api.choices.OrChoice;
import org.serverless.workflow.api.interfaces.Choice;
import org.serverless.workflow.api.interfaces.State;
import org.serverless.workflow.api.states.SwitchState;

class SwitchNode extends GraphNode {

    private final SwitchState state;

    SwitchNode(Graph graph, SwitchState state) {
        super(graph);
        this.state = state;
    }

    @Override
    State getState() {
        return state;
    }

    @Override
    void buildNode(RuleFlowProcessFactory factory) {
        List<Choice> choices = this.state.getChoices();
        if (choices.size() > 1) {
            Long prevId = this.getId();
            Long id = this.getNextId();
            Long joinId = this.getNextId();
            factory.joinNode(joinId)
                .name("join_" + state.getName())
                .type(Join.TYPE_OR)
                .done();
            SplitFactory split = factory.splitNode(id)
                .name(state.getName())
                .type(Split.TYPE_OR);
            choices.forEach(c -> addChoice(factory, c, id, joinId));
            split.done();
            connect(factory, prevId, id);
        } else if (choices.size() == 1) {
            addChoice(factory, choices.get(0), null, null);
        }
    }

    private void addChoice(RuleFlowProcessFactory factory, Choice choice, Long split, Long join) {
        if (choice instanceof DefaultChoice) {
            addDefaultChoice(factory, (DefaultChoice) choice, split, join);
        }
        if (choice instanceof NotChoice) {
            addNotChoice(factory, (NotChoice) choice, split, join);
        }
        if (choice instanceof AndChoice) {
            addAndChoice(factory, (AndChoice) choice, split, join);
        }
        if (choice instanceof OrChoice) {
            addOrChoice(factory, (OrChoice) choice, split, join);
        }
    }

    private void addDefaultChoice(RuleFlowProcessFactory factory, DefaultChoice choice, Long split, Long join) {
        Long id = getNextId();
        Long constraintId = this.graph.getNodeId(choice.getNextState());
        Long defaultId = this.graph.getNodeId(this.state.getDefault());
        factory.splitNode(id)
            .name("split_" + id)
            .type(Split.TYPE_XOR)
            .constraint(constraintId, "Flow_" + id + "_" + constraintId, Split.CONNECTION_DEFAULT_TYPE, "java", kcontext -> buildConstraint(kcontext, choice), 0)
            .metaData("Default", "Flow_" + id + "_" + defaultId)
            .done();
        connect(factory, id, constraintId);
        if (split != null) {
            connect(factory, split, id);
        }
        if (join != null) {
            connect(factory, id, join);
        }
    }

    private void addNotChoice(RuleFlowProcessFactory factory, NotChoice choice, Long split, Long join) {

    }

    private void addAndChoice(RuleFlowProcessFactory factory, AndChoice choice, Long split, Long join) {
    }

    private void addOrChoice(RuleFlowProcessFactory factory, OrChoice choice, Long split, Long join) {
    }

    private Boolean buildConstraint(ProcessContext kcontext, DefaultChoice choice) {
        JsonObject data = (JsonObject) kcontext.getVariable(JsonModel.DATA_PARAM);
        return jsonPath.eval(data, choice.getPath(), choice.getValue(), choice.getOperator());
    }

    @Override
    void buildInput(RuleFlowProcessFactory factory) {
        if (this.getState().getFilter() != null && this.getState().getFilter().getInputPath() != null) {
            buildActionNode(factory, kcontext -> buildInputAction(kcontext, state.getFilter()));
        }
    }

    @Override
    void buildOutput(RuleFlowProcessFactory factory) {
        if (this.getState().getFilter() != null && this.getState().getFilter().getOutputPath() != null && this.getState().getFilter().getResultPath() != null) {
            buildActionNode(factory, kcontext -> buildOutputAction(kcontext, state.getFilter()));
        }
    }

    @Override
    String getNextState() {
        return state.getDefault();
    }
}
