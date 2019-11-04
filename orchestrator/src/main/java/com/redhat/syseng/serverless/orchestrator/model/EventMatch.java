package com.redhat.syseng.serverless.orchestrator.model;

import javax.json.JsonObject;

import com.redhat.syseng.serverless.orchestrator.services.JsonPathUtils;
import org.serverless.workflow.api.events.TriggerEvent;

public class EventMatch {

    public final CorrelationToken token;
    public final TriggerEvent triggerDef;
    public final JsonObject data;

    public EventMatch(TriggerEvent t, JsonObject data) {
        this.token = JsonPathUtils.getCorrelationToken(t.getCorrelationToken(), data);
        this.data = data;
        this.triggerDef = t;
    }
}
