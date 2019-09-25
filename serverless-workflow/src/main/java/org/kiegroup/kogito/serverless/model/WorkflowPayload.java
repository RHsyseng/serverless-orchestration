package org.kiegroup.kogito.serverless.model;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

import org.kie.kogito.Model;

public class WorkflowPayload implements Model {

    public static final String ID_PARAM = "id";
    public static final String DATA_PARAM = "data";
    public static final String STATUS_PARAM = "status";

    private String id;
    private WorkflowData data;
    private String status;

    public WorkflowPayload setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public WorkflowPayload setData(WorkflowData data) {
        this.data = data;
        return this;
    }

    public WorkflowData getData() {
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
        this.id = null;
        this.data = (WorkflowData) params.get(DATA_PARAM);
        this.status = (String) params.get(STATUS_PARAM);
    }

    public void fromMap(String id, Map<String, Object> params) {
        this.id = id;
        this.data = (WorkflowData) params.get(DATA_PARAM);
        this.status = (String) params.get(STATUS_PARAM);
    }

    public static WorkflowPayload newInstance(WorkflowData data) {
        WorkflowPayload jsonModel = new WorkflowPayload();
        if (data == null) {
            data = new WorkflowData();
        }
        jsonModel.data = data;
        return jsonModel;
    }

    public static WorkflowPayload newInstance(JsonObject object) {
        WorkflowPayload jsonModel = new WorkflowPayload();
        if (object == null) {
            jsonModel.data = new WorkflowData();
        }
        jsonModel.data = new WorkflowData(object);
        return jsonModel;
    }
    public static WorkflowPayload newInstance(WorkflowPayload model) {
        if(model == null) {
            return null;
        }
        return WorkflowPayload.newInstance(model.data)
            .setId(model.id)
            .setStatus(model.status);
    }
}
