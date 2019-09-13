package org.kiegroup.kogito.serverless.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.api.definition.process.Process;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.mapper.WorkflowObjectMapper;
import org.serverless.workflow.api.states.DefaultState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTest {

    private final WorkflowObjectMapper mapper = new WorkflowObjectMapper();

    @Test
    void testEnd() {
        Graph graph = load("end.json");
        Map<String, GraphNode> nodes = graph.getNodes();
        assertEquals(1, nodes.size());
        EndNode endNode = (EndNode) nodes.get("End node");
        assertEquals(0, endNode.getHeaderId());
        assertEquals(DefaultState.Type.END, endNode.getState().getType());
        assertTrue(endNode.getState().isStart());
    }

    @Test
    void testOperation() {
        Graph graph = load("operation.json");
        Map<String, GraphNode> nodes = graph.getNodes();
        assertEquals(2, nodes.size());
        OperationNode opNode = (OperationNode) nodes.get("Operation node");
        assertEquals(0, opNode.getHeaderId());
        assertEquals(DefaultState.Type.OPERATION, opNode.getState().getType());
        EndNode endNode = (EndNode) nodes.get("End node");
        assertEquals(1, endNode.getHeaderId());
        assertEquals(DefaultState.Type.END, endNode.getState().getType());
    }

    @Test
    void testSwitch() {
        Graph graph = load("switch.json");
        Map<String, GraphNode> nodes = graph.getNodes();

        assertEquals(4, nodes.size());

        OperationNode opNode = (OperationNode) nodes.get("Operation node");
        assertEquals(0, opNode.getHeaderId());
        assertEquals(DefaultState.Type.OPERATION, opNode.getState().getType());

        SwitchNode switchNode = (SwitchNode) nodes.get("Switch node");
        assertEquals(1, switchNode.getHeaderId());
        assertEquals(DefaultState.Type.SWITCH, switchNode.getState().getType());

        EndNode endNode = (EndNode) nodes.get("Failure end");
        assertEquals(2, endNode.getHeaderId());
        assertEquals(DefaultState.Type.END, endNode.getState().getType());

        endNode = (EndNode) nodes.get("End node");
        assertEquals(3, endNode.getHeaderId());
        assertEquals(DefaultState.Type.END, endNode.getState().getType());
    }

    @Test
    void testEndProcess() {
        Graph graph = load("end.json");
        Process process = graph.getProcess();
    }

    @Test
    void testEndWithFilterProcess() {
        Graph graph = load("end-with-filter.json");
        assertNotNull(graph.getProcess());
    }

    @Test
    void testOperationProcess() {
        Graph graph = load("operation.json");
        assertNotNull(graph.getProcess());

    }

    @Test
    void testOperationFullOfFiltersProcess() {
        Graph graph = load("operation-full-of-filters.json");
        assertNotNull(graph.getProcess());
    }

    @Test
    void testOperationWithStateFiltersProcess() {
        Graph graph = load("operation-with-state-filters.json");
        assertNotNull(graph.getProcess());
    }

    @Test
    void testAgeEvaluationProcess() {
        Graph graph = load("age-evaluation.json");
        assertNotNull(graph.getProcess());
    }

    private Graph load(String name) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("workflows/" + name);
        Workflow workflow = null;
        try {
            workflow = mapper.readValue(is, Workflow.class);
        } catch (IOException e) {
            Assertions.fail("Unable to load workflow", e);
        }
        return new Graph(workflow);
    }
}
