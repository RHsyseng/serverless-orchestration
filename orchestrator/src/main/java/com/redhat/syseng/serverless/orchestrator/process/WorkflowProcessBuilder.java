package com.redhat.syseng.serverless.orchestrator.process;

import java.util.Optional;

import com.redhat.syseng.serverless.orchestrator.model.Message;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.datatype.impl.type.StringDataType;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.kie.api.definition.process.Process;
import org.serverless.workflow.api.Workflow;

public class WorkflowProcessBuilder {

    public static final String BACKUP_DATA_VAR = "backup-data";

    private static final String PACKAGE_NAME = "org.kiegroup.kogito.workflow";
    private static final Boolean DYNAMIC = Boolean.TRUE;
    private static final String VISIBILITY = "Public";

    private static final ObjectDataType WORKFLOW_DATA_TYPE = new ObjectDataType();
    private static final StringDataType STRING_DATA_TYPE = new StringDataType();

    private final RuleFlowProcessFactory factory;

    private Long count = 0l;
    private final Workflow workflow;

    private WorkflowProcessBuilder(Workflow workflow) {
        this.workflow = workflow;
        this.factory = RuleFlowProcessFactory
            .createProcess(workflow.getName())
            .name(workflow.getName())
            .dynamic(DYNAMIC)
            .packageName(PACKAGE_NAME)
            .visibility(VISIBILITY)
            .version(workflow.getVersion())
            .variable(Message.DATA_PARAM, WORKFLOW_DATA_TYPE)
            .variable(Message.ERROR_PARAM, WORKFLOW_DATA_TYPE)
            .variable(Message.CORRELATION_TOKEN_PARAM, STRING_DATA_TYPE)
            .variable(Message.STATUS_PARAM, STRING_DATA_TYPE)
            .variable(BACKUP_DATA_VAR, WORKFLOW_DATA_TYPE);
    }

    private Process getProcess() {
        return this.factory.validate().getProcess();
    }

    public static Process build(Workflow workflow) {
        return new WorkflowProcessBuilder(workflow).getProcess();
    }
}
