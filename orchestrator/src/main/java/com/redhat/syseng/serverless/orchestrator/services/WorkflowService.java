package com.redhat.syseng.serverless.orchestrator.services;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.json.JsonObject;

import com.redhat.syseng.serverless.orchestrator.model.EventMatch;
import com.redhat.syseng.serverless.orchestrator.process.WorkflowListener;
import org.serverless.workflow.api.Workflow;
import org.serverless.workflow.api.events.TriggerEvent;

public interface WorkflowService {

    String getName();

    Workflow get(String version);

    Workflow getLatest();

    String getLatestVersion();

    void registerListener(WorkflowListener listener);

    List<EventMatch> getEventMatches(URI source, String type, Optional<JsonObject> data);
}
