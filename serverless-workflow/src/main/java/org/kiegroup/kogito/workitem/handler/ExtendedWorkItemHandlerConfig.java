package org.kiegroup.kogito.workitem.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig;

@Singleton
public class ExtendedWorkItemHandlerConfig extends DefaultWorkItemHandlerConfig {

    protected Map<String, LifecycleWorkItemHandler> handlers = new HashMap<>();

    public ExtendedWorkItemHandlerConfig() {
        handlers.put(RestWorkItemHandler.HANDLER_NAME, new RestWorkItemHandler());
        handlers.put(LogWorkItemHandler.HANDLER_NAME, new LogWorkItemHandler());
        handlers.forEach((k, v) -> this.register(k, v));
    }

    @PostConstruct
    public void onStartup() {
        handlers.values().forEach(v -> v.onStartup());
    }

    @PreDestroy
    public void onShutdown() {
        handlers.values().forEach(v -> v.onShutdown());
    }
}
