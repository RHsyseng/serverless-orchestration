package org.kiegroup.kogito.serverless;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.drools.core.config.StaticRuleConfig;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.kogito.Config;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.ProcessEventListenerConfig;
import org.kie.kogito.process.WorkItemHandlerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig;
import org.kie.kogito.process.impl.StaticProcessConfig;
import org.kie.kogito.rules.RuleConfig;
import org.kie.kogito.rules.RuleEventListenerConfig;
import org.kie.kogito.services.uow.CollectingUnitOfWorkFactory;
import org.kie.kogito.services.uow.DefaultUnitOfWorkManager;
import org.kie.kogito.uow.UnitOfWorkManager;

@Singleton
public class ApplicationConfig implements Config {

    private final UnitOfWorkManager defaultUnitOfWorkManager = new DefaultUnitOfWorkManager(new CollectingUnitOfWorkFactory());
    private final WorkItemHandlerConfig defaultWorkItemHandlerConfig = new DefaultWorkItemHandlerConfig();
    private final ProcessEventListenerConfig defaultProcessEventListenerConfig = new DefaultProcessEventListenerConfig(new ProcessEventListener[0]);
    private final RuleEventListenerConfig defaultRuleEventListenerConfig = new DefaultRuleEventListenerConfig();

    @Inject
    Instance<ProcessEventListenerConfig> processEventListenerConfig;
    @Inject
    Instance<WorkItemHandlerConfig> workItemHandlerConfig;
    @Inject
    Instance<UnitOfWorkManager> unitOfWorkManager;
    @Inject
    Instance<RuleEventListenerConfig> ruleEventListenerConfig;

    protected ProcessConfig processConfig;
    protected RuleConfig ruleConfig;

    @PostConstruct
    public void init() {
        processConfig = new StaticProcessConfig(getWorkItemHandlerConfig(), getProcessEventListenerConfig(), getUnitOfWorkManager());
        ruleConfig = new StaticRuleConfig(getRuleEventListenerConfig());
    }

    @Override
    public ProcessConfig process() {
        return processConfig;
    }

    @Override
    public RuleConfig rule() {
        return ruleConfig;
    }

    private UnitOfWorkManager getUnitOfWorkManager() {
        if (this.unitOfWorkManager.isUnsatisfied()) {
            return this.defaultUnitOfWorkManager;
        }
        return this.unitOfWorkManager.get();
    }

    private WorkItemHandlerConfig getWorkItemHandlerConfig() {
        if (this.workItemHandlerConfig.isUnsatisfied()) {
            return this.defaultWorkItemHandlerConfig;
        }
        return this.workItemHandlerConfig.get();
    }

    private ProcessEventListenerConfig getProcessEventListenerConfig() {
        if (this.processEventListenerConfig.isUnsatisfied()) {
            return this.defaultProcessEventListenerConfig;
        }
        return this.processEventListenerConfig.get();
    }

    private RuleEventListenerConfig getRuleEventListenerConfig() {
        if (this.ruleEventListenerConfig.isUnsatisfied()) {
            return this.defaultRuleEventListenerConfig;
        }
        return this.ruleEventListenerConfig.get();
    }
}
