package com.redhat.syseng.serverless.orchestrator.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.redhat.syseng.serverless.orchestrator.model.EventMatch;
import com.redhat.syseng.serverless.orchestrator.model.Message;
import com.redhat.syseng.serverless.orchestrator.process.WorkflowListener;
import com.redhat.syseng.serverless.orchestrator.process.WorkflowProcess;
import io.quarkus.runtime.StartupEvent;
import org.drools.core.rule.Collect;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Signal;
import org.kie.kogito.process.impl.Sig;
import org.serverless.workflow.api.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ProcessServiceImpl implements ProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessServiceImpl.class);

    @Inject
    WorkflowService workflowService;

    private Map<String, WorkflowProcess> processes = new HashMap<>();

    public void registerListener(@Observes StartupEvent event) {
        workflowService.registerListener(new ProcessWorkflowListener());
    }

    @Override
    public ProcessInstance<Message> receive(EventMatch event) {
        Message msg = new Message();
        msg.setData(event.data);
        if(event.token == null) {
            return processes.get(workflowService.getLatestVersion()).createInstance(msg);
        }
        msg.setCorrelationToken(event.token.getId());
        Optional<? extends ProcessInstance<Message>> processInstance = processes.get(event.token.getVersion())
            .instances()
            .findById(event.token.getId());
        if(processInstance.isPresent()) {
            Signal<Message> signal = Sig.of(event.triggerDef.getName(), msg);
            processInstance.get().send(signal);
            return processInstance.get();
        }
        return null;
    }

    @Override
    public Collection<? extends ProcessInstance<Message>> instances(String version) {
        WorkflowProcess workflowProcess = processes.get(version);
        if(workflowProcess == null) {
            return Collections.emptyList();
        }
        return workflowProcess.instances().values();
    }

    private class ProcessWorkflowListener implements WorkflowListener {

        @Override
        public void onCreateOrUpdate(String version, Workflow workflow) {
            if (!processes.containsKey(version)) {
                LOGGER.debug("Adding new process {}/{}", workflowService.getName(), version);
                processes.put(version, new WorkflowProcess(workflow));
            } else {
                // Do not update an existing process
                LOGGER.info("Unsupported process update of {}/{}", workflowService.getName(), version);
            }
        }

        @Override
        public void onDelete(String version) {
            // Do not delete an existing process
            WorkflowProcess process = processes.get(version);
            if (process != null) {
                LOGGER.info("Unsupported process deletion of {}/{}", workflowService.getName(), version);
            } else {
                LOGGER.info("Ignoring onDelete for missing process {}", version);
            }
        }
    }
}
