package org.kiegroup.kogito.serverless.model;

import java.util.HashMap;
import java.util.Map;

import org.kie.kogito.Model;

public class WorkflowPayload implements Model {

    public static final String DATA_PARAM = "data";
    public static final String STATUS_PARAM = "status";

    private String data;
    private String status;

    public WorkflowPayload setId(String id) {
        return this;
    }

    public WorkflowPayload setData(String data) {
        this.data = data;
        return this;
    }

    public String getData() {
        return data;
    }

    public WorkflowPayload setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> params = new HashMap<>();
        params.put(DATA_PARAM, data);
        params.put(STATUS_PARAM, status);
        return params;
    }

    @Override
    public void fromMap(Map<String, Object> params) {
        this.data = (String) params.get(DATA_PARAM);
        this.status = (String) params.get(STATUS_PARAM);
    }

    public static WorkflowPayload newInstance(String data) {
        WorkflowPayload jsonModel = new WorkflowPayload();
        jsonModel.data = data;
        return jsonModel;
    }

    public static WorkflowPayload newInstance(WorkflowPayload model) {
        if (model == null) {
            return null;
        }
        return WorkflowPayload.newInstance(model.data)
            .setStatus(model.status);
    }
}
