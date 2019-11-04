package com.redhat.syseng.serverless.orchestrator.services;

import java.util.Collection;
import java.util.List;

import com.redhat.syseng.serverless.orchestrator.model.EventMatch;
import com.redhat.syseng.serverless.orchestrator.model.Message;
import org.kie.kogito.process.ProcessInstance;

public interface ProcessService {

    ProcessInstance<Message> receive(EventMatch match);

    Collection<? extends ProcessInstance<Message>> instances(String version);
}
