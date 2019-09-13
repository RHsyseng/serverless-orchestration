package org.kiegroup.kogito.serverless;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.kie.kogito.Config;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.ProcessEventListenerConfig;
import org.kie.kogito.process.WorkItemHandlerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.kie.kogito.process.impl.StaticProcessConfig;
import org.kie.kogito.rules.RuleConfig;
import org.kie.kogito.services.uow.CollectingUnitOfWorkFactory;
import org.kie.kogito.services.uow.DefaultUnitOfWorkManager;
import org.kie.kogito.uow.UnitOfWorkManager;

@Singleton
public class ApplicationConfig implements Config {

    private final ProcessEventListenerConfig processEventListenerConfig = new DefaultProcessEventListenerConfig();
    private final UnitOfWorkManager unitOfWorkManager = new DefaultUnitOfWorkManager(new CollectingUnitOfWorkFactory());

    @Inject
    WorkItemHandlerConfig workItemHandlerConfig;

    protected ProcessConfig processConfig;
    protected RuleConfig ruleConfig = null;

    @PostConstruct
    public void init() {
        processConfig = new StaticProcessConfig(workItemHandlerConfig, processEventListenerConfig, unitOfWorkManager);
    }
    @Override
    public ProcessConfig process() {
        return processConfig;
    }

    @Override
    public RuleConfig rule() {
        return ruleConfig;
    }
}
