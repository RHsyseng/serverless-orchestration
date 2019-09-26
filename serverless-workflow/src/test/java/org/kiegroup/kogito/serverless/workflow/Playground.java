package org.kiegroup.kogito.serverless.workflow;

import javax.json.Json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.drools.core.config.StaticRuleConfig;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.workflow.core.node.SubProcessFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.api.definition.process.Process;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.runtime.process.ProcessContext;
import org.kie.kogito.Config;
import org.kie.kogito.StaticConfig;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.ProcessEventListenerConfig;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.WorkItemHandlerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig;
import org.kie.kogito.process.impl.StaticProcessConfig;
import org.kie.kogito.rules.RuleEventListenerConfig;
import org.kie.kogito.services.uow.CollectingUnitOfWorkFactory;
import org.kie.kogito.services.uow.DefaultUnitOfWorkManager;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.kiegroup.kogito.serverless.model.WorkflowPayload;
import org.kiegroup.kogito.serverless.process.WorkflowProcess;
import org.kiegroup.kogito.serverless.service.WorkflowService;
import org.kiegroup.kogito.serverless.serialization.CustomObjectMapperConfig;
import org.serverless.workflow.api.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Playground {

    private static final Logger logger = LoggerFactory.getLogger(Playground.class);

    private final UnitOfWorkManager defaultUnitOfWorkManager = new DefaultUnitOfWorkManager(new CollectingUnitOfWorkFactory());
    private final WorkItemHandlerConfig defaultWorkItemHandlerConfig = new DefaultWorkItemHandlerConfig();
    private final ProcessEventListenerConfig defaultProcessEventListenerConfig = new DefaultProcessEventListenerConfig(new ProcessEventListener[0]);
    private final RuleEventListenerConfig defaultRuleEventListenerConfig = new DefaultRuleEventListenerConfig();
    private final ProcessConfig processConfig = new StaticProcessConfig(defaultWorkItemHandlerConfig, defaultProcessEventListenerConfig, defaultUnitOfWorkManager);
    private final Config config = new StaticConfig(processConfig, new StaticRuleConfig(defaultRuleEventListenerConfig));

    @Test
    public void testMultiStart() throws JsonProcessingException {

        RuleFlowProcessFactory commonProcessFactory = RuleFlowProcessFactory
            .createProcess("subprocess")
            .name("suprocess test")
            .variable(WorkflowPayload.DATA_PARAM, new ObjectDataType(WorkflowPayload.class.getName()));
        commonProcessFactory
            .startNode(1).name("subprocess start").done()
            .actionNode(2)
            .action(context -> logger.debug("Test action"))
            .name("test action")
            .done()
            .endNode(3).name("End suprocess").done()
            .connection(1, 2)
            .connection(2, 3);
        commonProcessFactory.validate();
        WorkflowProcess subProcess = new WorkflowProcess(config, new MockWorkflowServiceImpl(commonProcessFactory.getProcess()));

        SubProcessFactory<WorkflowPayload> subProcessFactory = new SubProcessFactory<WorkflowPayload>() {
            @Override
            public WorkflowPayload bind(ProcessContext ctx) {
                return WorkflowPayload.newInstance((String) ctx.getVariable(WorkflowPayload.DATA_PARAM));
            }

            @Override
            public ProcessInstance createInstance(WorkflowPayload model) {
                WorkflowPayload payload = WorkflowPayload.newInstance(model);
                return subProcess.createInstance(payload);
            }

            @Override
            public void unbind(ProcessContext ctx, WorkflowPayload src) {
                WorkflowPayload model = WorkflowPayload.newInstance(src);
                ctx.setVariable(WorkflowPayload.DATA_PARAM, model.getData());
            }
        };

        RuleFlowProcessFactory httpFactory = RuleFlowProcessFactory.createProcess("test-multistart").name("test multistart");
        httpFactory
            .variable(WorkflowPayload.DATA_PARAM, new ObjectDataType(WorkflowPayload.class.getName()))
            .startNode(11).name("http-start").done()
            .subProcessNode(12).independent(Boolean.TRUE).name("common1").processId("subprocess").subProcessFactory(subProcessFactory).done()
            .endNode(13).name("End node 1").done()
            .connection(11, 12)
            .connection(12, 13);
        httpFactory.validate();

        RuleFlowProcessFactory kafkaFactory = RuleFlowProcessFactory.createProcess("test-multistart").name("test multistart");
        kafkaFactory
            .variable(WorkflowPayload.DATA_PARAM, new ObjectDataType(WorkflowPayload.class.getName()))
            .startNode(21).name("kafka-start").done()
            .subProcessNode(22).independent(Boolean.TRUE).name("common2").processId("subprocess").subProcessFactory(subProcessFactory).done()
            .endNode(23).name("End node 2").done()
            .connection(21, 22)
            .connection(22, 23);
        kafkaFactory.validate();

        WorkflowProcess kafkaProcess = new WorkflowProcess(config, new MockWorkflowServiceImpl(kafkaFactory.getProcess()));
        WorkflowProcess httpProcess = new WorkflowProcess(config, new MockWorkflowServiceImpl(httpFactory.getProcess()));

        ProcessInstance<WorkflowPayload> instance = UnitOfWorkExecutor.executeInUnitOfWork(config.process().unitOfWorkManager(), () -> {
            WorkflowPayload model = WorkflowPayload.newInstance(Json.createObjectBuilder().add("name", "test").build().toString());
            ProcessInstance<WorkflowPayload> pi = kafkaProcess.createInstance(model);
            pi.start();
            return pi;
        });

        Assertions.assertEquals(ProcessInstance.STATE_COMPLETED, instance.status());

        ObjectMapper objectMapper = new CustomObjectMapperConfig().objectMapper();
        Assertions.assertTrue(objectMapper.canSerialize(instance.getClass()));
        Assertions.assertEquals("{\"id\":\"" + instance.id() + "\"}", objectMapper.writeValueAsString(instance));
    }

    private class MockWorkflowServiceImpl implements WorkflowService {

        final Process process;

        MockWorkflowServiceImpl(Process process) {
            this.process = process;
        }

        @Override
        public Workflow get() {
            return null;
        }

        @Override
        public Process getProcess() {
            return process;
        }

        @Override
        public String getProcessId() {
            return null;
        }
    }
}
