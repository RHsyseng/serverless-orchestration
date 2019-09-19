package org.kiegroup.kogito.workitem.handler;

import org.kie.api.runtime.process.WorkItemHandler;

public interface BaseWorkItemHandler extends WorkItemHandler {

    String PARAM_TYPE = "type";
    String PARAM_CONTENT_DATA = "ContentData";
    String PARAM_RESULT = "Result";
}
