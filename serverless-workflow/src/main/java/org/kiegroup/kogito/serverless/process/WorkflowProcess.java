package org.kiegroup.kogito.serverless.process;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.kie.api.definition.process.Process;
import org.kie.kogito.Config;
import org.kie.kogito.Model;
import org.kie.kogito.persistence.KogitoProcessInstancesFactory;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kiegroup.kogito.serverless.model.JsonModel;
import org.kiegroup.kogito.serverless.service.WorkflowService;

@Singleton
public class WorkflowProcess extends AbstractProcess<JsonModel> {

    final WorkflowService workflowService;

    @Inject
    public WorkflowProcess(Config config, RemoteCacheManager cacheManager, WorkflowService workflowService) {
        super(config.process());
        this.workflowService = workflowService;
        this.setProcessInstancesFactory(new CacheProcessInstancesFactory(cacheManager));
        this.configure();
    }

    @Override
    public Process legacyProcess() {
        return workflowService.getProcess();
    }

    @Override
    public ProcessInstance<JsonModel> createInstance(JsonModel value) {
        return new WorkflowProcessInstance(this, value, this.createLegacyProcessRuntime());
    }

    @Override
    public ProcessInstance<JsonModel> createInstance(Model value) {
        return this.createInstance((JsonModel) value);
    }

    @Override
    public org.kie.kogito.process.Process<JsonModel> configure() {
        super.configure();
        return this;
    }

    private class CacheProcessInstancesFactory extends KogitoProcessInstancesFactory {

        public CacheProcessInstancesFactory(RemoteCacheManager cacheManager) {
            super(cacheManager);
        }

        @Override
        public String proto() {
            return null;
        }

        @Override
        public List<?> marshallers() {
            return Collections.emptyList();
        }
    }
}
