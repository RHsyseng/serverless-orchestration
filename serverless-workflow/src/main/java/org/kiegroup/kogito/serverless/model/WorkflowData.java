package org.kiegroup.kogito.serverless.model;

import javax.json.JsonObject;

public class WorkflowData {

    public final JsonObject object;

    public WorkflowData() {
        this.object = JsonObject.EMPTY_JSON_OBJECT;
    }

    public WorkflowData(JsonObject object) {
        this.object = object;
    }

}
